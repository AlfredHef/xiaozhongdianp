package com.fudan.xiaozhong_dianping.review.service.impl;

import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.service.CouponService;
import com.fudan.xiaozhong_dianping.review.entity.Review;
import com.fudan.xiaozhong_dianping.review.repository.ReviewRepository;
import com.fudan.xiaozhong_dianping.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CouponService couponService;

    @Override
    @Transactional
    public Review createReview(Long userId, Long merchantId, String content, Long parentId) {
        // 校验内容长度
        if (content == null || content.length() < 15) {
            throw new BusinessException("点评内容需至少15字");
        }

        // 保存点评
        Review review = new Review();
        review.setUserId(userId);
        review.setMerchantId(merchantId);
        review.setContent(content);
        review.setParentId(parentId);
        Review savedReview = reviewRepository.save(review);

        // 触发奖励：用户首次达到3条有效点评时发放优惠券
        int validCount = reviewRepository.countValidReviewsByUser(userId);
        if (validCount >= 3 && !couponService.hasReceivedReviewReward(userId)) {
            couponService.grantReviewRewardCoupon(userId);
        }

        return savedReview;
    }

    @Override
    public List<Review> getReviewsByMerchant(Long merchantId) {
        System.out.println("服务层开始查询商户ID: " + merchantId + " 的评论");
        
        try {
            // 查询顶级点评（parentId为null）
            System.out.println("查询顶级评论（parentId为null）");
            List<Review> topReviews = reviewRepository.findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, null);
            System.out.println("顶级评论查询结果数量: " + topReviews.size());
            
            // 如果没有顶级评论，尝试获取该商户的所有评论并构建层级关系
            if (topReviews.isEmpty()) {
                System.out.println("未找到顶级评论，尝试获取所有评论并构建层级");
                return buildReviewHierarchy(merchantId);
            }
            
            // 正常处理：为每个顶级评论查询子回复
            System.out.println("为顶级评论查询子回复");
            for (Review review : topReviews) {
                List<Review> replies = reviewRepository.findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, review.getId());
                System.out.println("评论ID " + review.getId() + " 的回复数量: " + replies.size());
                review.setReplies(replies);
            }
            
            System.out.println("评论查询及构建完成，返回 " + topReviews.size() + " 条顶级评论");
            return topReviews;
        } catch (Exception e) {
            System.err.println("查询评论时发生错误: " + e.getMessage());
            e.printStackTrace();
            // 返回空列表而不是抛出异常，避免前端崩溃
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取所有评论并构建层级关系
     * 用于处理没有顶级评论的情况
     */
    private List<Review> buildReviewHierarchy(Long merchantId) {
        try {
            System.out.println("开始构建评论层级关系");
            
            // 获取该商户的所有评论
            List<Review> allReviews = reviewRepository.findByMerchantIdOrderByCreateTimeDesc(merchantId);
            System.out.println("获取到商户所有评论: " + allReviews.size() + " 条");
            
            if (allReviews.isEmpty()) {
                System.out.println("商户没有任何评论，返回空列表");
                return new ArrayList<>();
            }
            
            // 使用Map来存储每个评论，便于构建层级关系
            Map<Long, Review> reviewMap = new HashMap<>();
            List<Review> topLevelReviews = new ArrayList<>();
            
            // 第一遍遍历：将所有评论放入Map
            for (Review review : allReviews) {
                reviewMap.put(review.getId(), review);
                review.setReplies(new ArrayList<>());
            }
            
            // 第二遍遍历：构建层级关系
            for (Review review : allReviews) {
                if (review.getParentId() == null) {
                    // 如果是顶级评论，加入结果列表
                    topLevelReviews.add(review);
                } else {
                    // 如果是回复，添加到父评论的replies列表中
                    Review parent = reviewMap.get(review.getParentId());
                    if (parent != null) {
                        parent.getReplies().add(review);
                    } else {
                        // 如果找不到父评论，作为顶级评论处理
                        System.out.println("警告: 评论ID " + review.getId() + " 的父评论ID " + review.getParentId() + " 不存在，作为顶级评论处理");
                        topLevelReviews.add(review);
                    }
                }
            }
            
            System.out.println("层级构建完成，顶级评论数: " + topLevelReviews.size());
            return topLevelReviews;
            
        } catch (Exception e) {
            System.err.println("构建评论层级时发生错误: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
