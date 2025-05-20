package com.fudan.xiaozhong_dianping.invitation.service.impl;

import com.fudan.xiaozhong_dianping.groupbuy.builder.CouponBuilder;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyOrder;
import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.service.CouponService;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationCode;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationRecord;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationReward;
import com.fudan.xiaozhong_dianping.invitation.repository.InvitationCodeRepository;
import com.fudan.xiaozhong_dianping.invitation.repository.InvitationRecordRepository;
import com.fudan.xiaozhong_dianping.invitation.repository.InvitationRewardRepository;
import com.fudan.xiaozhong_dianping.invitation.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private static final int INVITATION_REWARD_THRESHOLD = 2;  // 每2个有效邀请发放1次奖励
    private static final BigDecimal MIN_ORDER_AMOUNT = new BigDecimal("10");  // 最低订单金额10元

    @Autowired
    private InvitationCodeRepository invitationCodeRepository;

    @Autowired
    private InvitationRecordRepository invitationRecordRepository;

    @Autowired
    private InvitationRewardRepository invitationRewardRepository;

    @Autowired
    private CouponService couponService;

    @Override
    @Transactional
    public InvitationCode generateInvitationCode(Long userId) {
        // 检查用户是否已有邀请码
        InvitationCode existingCode = invitationCodeRepository.findByUserId(userId);
        if (existingCode != null) {
            return existingCode;
        }

        // 生成唯一邀请码
        String code;
        boolean isUnique = false;
        do {
            code = generateRandomCode();
            isUnique = (invitationCodeRepository.findByCode(code) == null);
        } while (!isUnique);

        // 保存邀请码
        InvitationCode invitationCode = new InvitationCode();
        invitationCode.setUserId(userId);
        invitationCode.setCode(code);
        return invitationCodeRepository.save(invitationCode);
    }

    @Override
    public InvitationCode getUserInvitationCode(Long userId) {
        InvitationCode invitationCode = invitationCodeRepository.findByUserId(userId);
        if (invitationCode == null) {
            // 如果用户还没有邀请码，则生成一个
            invitationCode = generateInvitationCode(userId);
        }
        return invitationCode;
    }

    /**
     * 根据邀请码查询邀请码实体
     * @param code 邀请码
     * @return 邀请码实体，不存在则返回null
     */
    public InvitationCode findByCode(String code) {
        return invitationCodeRepository.findByCode(code);
    }
    
    /**
     * 检查用户是否已被邀请过
     * @param userId 用户ID
     * @return 是否被邀请过
     */
    public boolean hasBeenInvited(Long userId) {
        return invitationRecordRepository.existsByInviteeId(userId);
    }

    @Override
    @Transactional
    public boolean useInvitationCode(Long inviteeId, String invitationCode, GroupBuyOrder order) {
        try {
            // 验证邀请码
            InvitationCode code = invitationCodeRepository.findByCode(invitationCode);
            if (code == null) {
                throw new BusinessException("邀请码不存在");
            }
    
            // 验证不能使用自己的邀请码
            if (code.getUserId().equals(inviteeId)) {
                throw new BusinessException("不能使用自己的邀请码");
            }
    
            // 检查被邀请人是否已经被邀请过
            if (invitationRecordRepository.existsByInviteeId(inviteeId)) {
                throw new BusinessException("您已经被邀请过，不能重复使用邀请码");
            }
    
            // 验证订单金额
            if (order.getOrderPrice().compareTo(MIN_ORDER_AMOUNT) < 0) {
                throw new BusinessException("订单金额需超过10元才能使用邀请码");
            }
            
            // 只有当订单已保存（有ID）时，才创建邀请记录
            if (order.getId() != null) {
                // 创建邀请记录
                InvitationRecord record = new InvitationRecord();
                record.setInviterId(code.getUserId());
                record.setInviteeId(inviteeId);
                record.setOrderId(order.getId());
                record.setOrderAmount(order.getOrderPrice());
                record.setOrderTime(order.getCreatedAt());
                record.setIsValid(true); // 显式设置为有效邀请
                
                // 保存邀请记录并确保实际保存到数据库
                InvitationRecord savedRecord = invitationRecordRepository.save(record);
                if (savedRecord.getId() == null) {
                    throw new BusinessException("保存邀请记录失败");
                }
                
                System.out.println("成功创建邀请记录 - ID: " + savedRecord.getId() 
                    + ", 邀请人: " + savedRecord.getInviterId() 
                    + ", 被邀请人: " + savedRecord.getInviteeId()
                    + ", 订单ID: " + savedRecord.getOrderId()
                    + ", 是否有效: " + savedRecord.getIsValid());
    
                // 检查是否需要发放奖励
                checkAndGrantInvitationReward(code.getUserId());
            } else {
                throw new BusinessException("订单ID为空，无法创建邀请记录");
            }
    
            return true;
        } catch (BusinessException e) {
            // 重新抛出业务异常，便于上层捕获
            throw e;
        } catch (Exception e) {
            System.err.println("处理邀请关系异常: " + e.getMessage());
            e.printStackTrace();
            // 将其他异常转换为业务异常
            throw new BusinessException("处理邀请关系异常: " + e.getMessage());
        }
    }

    @Override
    public List<InvitationRecord> getUserInvitationRecords(Long userId) {
        return invitationRecordRepository.findByInviterIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public List<InvitationReward> getUserInvitationRewards(Long userId) {
        return invitationRewardRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    /**
     * 修复邀请记录中可能存在的问题
     * 特别是处理isValid字段可能未正确设置的问题
     * @return 修复的记录数量
     */
    @Transactional
    public int fixInvitationRecords() {
        int fixedCount = 0;
        try {
            // 获取所有邀请记录
            List<InvitationRecord> allRecords = invitationRecordRepository.findAll();
            
            for (InvitationRecord record : allRecords) {
                // 检查isValid字段值
                if (record.getIsValid() == null) {
                    record.setIsValid(true);
                    invitationRecordRepository.save(record);
                    fixedCount++;
                    System.out.println("修复邀请记录 - ID: " + record.getId() + ", 设置isValid=true");
                }
            }
            
            System.out.println("邀请记录修复完成，共修复 " + fixedCount + " 条记录");
            return fixedCount;
        } catch (Exception e) {
            System.err.println("修复邀请记录失败: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("修复邀请记录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean checkAndGrantInvitationReward(Long inviterId) {
        // 获取有效邀请数量
        int validInvitations = invitationRecordRepository.countValidInvitationByInviterId(inviterId);

        // 获取用户最后一次奖励记录
        InvitationReward lastReward = invitationRewardRepository.findFirstByUserIdOrderByInvitationCountDesc(inviterId);
        int lastRewardCount = lastReward != null ? lastReward.getInvitationCount() : 0;

        // 计算新增奖励次数
        int newRewardCount = validInvitations / INVITATION_REWARD_THRESHOLD - lastRewardCount / INVITATION_REWARD_THRESHOLD;

        // 发放奖励
        boolean rewardGranted = false;
        for (int i = 0; i < newRewardCount; i++) {
            grantInvitationReward(inviterId, lastRewardCount + (i + 1) * INVITATION_REWARD_THRESHOLD);
            rewardGranted = true;
        }

        return rewardGranted;
    }

    // 生成随机邀请码
    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    // 发放邀请奖励
    private void grantInvitationReward(Long userId, int invitationCount) {
        // 创建奖励券（无门槛20元优惠券，7天有效）
        Coupon rewardCoupon = CouponBuilder.builder("邀请奖励无门槛20元券", "INVITATION_REWARD", new BigDecimal("20"))
                .description("任意品类通用，无门槛使用，7天有效")
                .validDays(7)
                .totalQuantity(1000)
                .maxPerUser(100)  // 用户可以多次获得邀请奖励
                .build();

        // 保存优惠券并发放
        Coupon savedCoupon = couponService.createCoupon(rewardCoupon);
        couponService.receiveCoupon(userId, savedCoupon.getId());

        // 记录奖励发放
        InvitationReward reward = new InvitationReward();
        reward.setUserId(userId);
        reward.setCouponId(savedCoupon.getId());
        reward.setInvitationCount(invitationCount);
        invitationRewardRepository.save(reward);
    }
}