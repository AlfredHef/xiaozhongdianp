package com.fudan.xiaozhong_dianping.review.controller;
import com.fudan.xiaozhong_dianping.review.entity.Review;
import com.fudan.xiaozhong_dianping.review.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ReviewController 类负责处理与点评相关的 HTTP 请求。
 * 它使用 ReviewService 来管理点评数据，并提供点评创建和获取商户点评列表的功能。
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    // 自动注入 ReviewService，用于执行具体的点评业务逻辑。
    @Autowired
    private ReviewService reviewService;

    /**
     * 提交点评/回复。
     *
     * @param userId 用户ID（从请求头获取），标识点评的用户。
     * @param merchantId 商户ID，标识被点评的商户。
     * @param content 点评内容（≥15字），用户对商户的评价或回复内容。
     * @param parentId 父级ID（可选，顶级点评无需填写），用于关联回复的上级点评。
     * @return 创建的点评对象，包含系统分配的点评ID等信息。
     */
    @PostMapping
    public ResponseEntity<Review> createReview(
            @RequestHeader("userId") Long userId,
            @RequestParam Long merchantId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId) {
        Review review = reviewService.createReview(userId, merchantId, content, parentId);
        return ResponseEntity.ok(review);
    }

    /**
     * 获取商户点评列表（含嵌套回复）。
     *
     * @param merchantId 商户ID，用于获取指定商户的点评列表。
     * @return 商户的点评列表，包括嵌套的回复。
     */
    @GetMapping("/{merchantId}")
    public ResponseEntity<List<Review>> getReviewsByMerchant(@PathVariable Long merchantId) {
        List<Review> reviews = reviewService.getReviewsByMerchant(merchantId);
        return ResponseEntity.ok(reviews);
    }
}
