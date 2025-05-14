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

import java.util.List;

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
        // 查询顶级点评（parentId为null）
        List<Review> topReviews = reviewRepository.findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, null);
        // 递归查询子回复（简化实现）
        topReviews.forEach(review -> {
            List<Review> replies = reviewRepository.findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, review.getId());
            review.setReplies(replies);
        });
        return topReviews;
    }
}
