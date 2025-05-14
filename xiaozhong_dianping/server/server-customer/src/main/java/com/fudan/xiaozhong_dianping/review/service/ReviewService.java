package com.fudan.xiaozhong_dianping.review.service;

import com.fudan.xiaozhong_dianping.review.entity.Review;

import java.util.List;

public interface ReviewService {
    Review createReview(Long userId, Long merchantId, String content, Long parentId);

    List<Review> getReviewsByMerchant(Long merchantId);
}
