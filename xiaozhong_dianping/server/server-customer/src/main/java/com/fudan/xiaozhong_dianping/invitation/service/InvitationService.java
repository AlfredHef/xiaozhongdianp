package com.fudan.xiaozhong_dianping.invitation.service;

import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyOrder;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationCode;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationRecord;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationReward;

import java.util.List;

public interface InvitationService {
    /**
     * 为用户生成唯一邀请码
     * @param userId 用户id
     * @return 邀请码
     */
     InvitationCode generateInvitationCode(Long userId);

    /**
     * 获取用户的邀请码
     * @param userId
     * @return 用户邀请码
     */
    InvitationCode getUserInvitationCode(Long userId);

    /**
     * 使用邀请码下单
     * @param inviteeId
     * @param invitationCode
     * @param order
     * @return 能否成功使用邀请码
     */
     boolean useInvitationCode(Long inviteeId, String invitationCode,
                               GroupBuyOrder order);

    /**
     * 获取用户邀请记录
     * @param userId
     * @return 邀请记录列表
     */
      List<InvitationRecord> getUserInvitationRecords(Long userId);

      /**
       * 获取用户邀请奖励记录
       * @param userId
       * @return 邀请奖励记录列表
       */
      List<InvitationReward> getUserInvitationRewards(Long userId);

    /**
     * 检查并发放邀请奖励
     * @param inviterId
     * @return 是否发放了奖励
     */
    boolean checkAndGrantInvitationReward(Long inviterId);
}
