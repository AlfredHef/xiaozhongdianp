package com.fudan.xiaozhong_dianping.review.controller;
import com.fudan.xiaozhong_dianping.review.entity.Review;
import com.fudan.xiaozhong_dianping.review.repository.ReviewRepository;
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
    
    @Autowired
    private ReviewRepository reviewRepository;

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
        System.out.println("接收到商户ID: " + merchantId + ", 类型: " + merchantId.getClass().getName());
       
        // 直接在控制器查询一次，验证数据
        System.out.println("直接查询数据库测试:");
        List<Review> testReviews = reviewRepository.findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, null);
        System.out.println("直接查询结果数量: " + testReviews.size());
        
        if (testReviews.isEmpty()) {
            System.out.println("测试查询: 未找到数据，尝试查询所有评论");
            // 尝试进一步排查
            long count = reviewRepository.count();
            System.out.println("数据库中评论总数: " + count);
            
            // 尝试不用参数化查询
            List<Review> allReviews = reviewRepository.findAll();
            System.out.println("所有评论数: " + allReviews.size());
            
            if (!allReviews.isEmpty()) {
                Review first = allReviews.get(0);
                System.out.println("示例评论 - ID: " + first.getId() + ", 商户ID: " + first.getMerchantId() + ", 父ID: " + first.getParentId());
            }
        } else {
            System.out.println("测试查询: 找到数据! 第一条ID: " + testReviews.get(0).getId());
        }
        
        // 正常业务逻辑
        List<Review> reviews = reviewService.getReviewsByMerchant(merchantId);
        System.out.println("服务层返回结果数量: " + reviews.size());
        
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * 测试接口：获取所有评论
     * 用于调试评论数据问题
     */
    @GetMapping("/test/all")
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> allReviews = reviewRepository.findAll();
        System.out.println("测试接口返回评论数: " + allReviews.size());
        return ResponseEntity.ok(allReviews);
    }
    
    /**
     * 测试接口：获取指定商户的所有评论（不过滤parentId）
     */
    @GetMapping("/test/{merchantId}")
    public ResponseEntity<List<Review>> testGetAllReviewsByMerchant(@PathVariable Long merchantId) {
        List<Review> allMerchantReviews = reviewRepository.findByMerchantIdOrderByCreateTimeDesc(merchantId);
        System.out.println("商户" + merchantId + "的所有评论数: " + allMerchantReviews.size());
        return ResponseEntity.ok(allMerchantReviews);
    }
}
