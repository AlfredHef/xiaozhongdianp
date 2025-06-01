import com.fudan.xiaozhong_dianping.review.controller.ReviewController;
import com.fudan.xiaozhong_dianping.review.entity.Review;
import com.fudan.xiaozhong_dianping.review.repository.ReviewRepository;
import com.fudan.xiaozhong_dianping.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReviewControllerTest类用于对ReviewController中的评论功能进行单元测试。
 * 该类使用JUnit 5和Mockito框架，通过模拟ReviewService的行为来测试ReviewController的评论相关方法。
 * 确保在不同情况下，ReviewController的评论功能能正确响应并返回预期结果。
 *
 * @author Lab4 Test Team
 * @version 1.0
 */
public class ReviewControllerTest {

    // 模拟ReviewService，用于在测试中提供ReviewService的模拟行为
    @Mock
    private ReviewService reviewService;

    // 模拟ReviewRepository，用于在测试中提供数据访问的模拟行为
    @Mock
    private ReviewRepository reviewRepository;

    // 注入被测试的ReviewController，将模拟的服务注入到控制器中
    @InjectMocks
    private ReviewController reviewController;

    /**
     * 初始化方法，在每个测试方法执行前调用。
     * 用于初始化Mockito的模拟环境，确保模拟对象正确设置。
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试创建评论功能 - 正常情况
     * 验证在有效参数下能够成功创建评论
     */
    @Test
    public void testCreateReview_Success() {
        // 准备测试数据
        Long userId = 1L;
        Long merchantId = 100L;
        String content = "这是一个非常好的商户，服务很棒，推荐大家来这里消费！";
        Long parentId = null;

        // 创建预期的返回结果
        Review expectedReview = new Review();
        expectedReview.setId(1L);
        expectedReview.setUserId(userId);
        expectedReview.setMerchantId(merchantId);
        expectedReview.setContent(content);
        expectedReview.setParentId(parentId);
        expectedReview.setCreateTime(LocalDateTime.now());

        // 配置Mock行为
        when(reviewService.createReview(userId, merchantId, content, parentId))
                .thenReturn(expectedReview);

        // 执行测试
        ResponseEntity<Review> response = reviewController.createReview(userId, merchantId, content, parentId);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(expectedReview.getId(), response.getBody().getId());
        assertEquals(expectedReview.getContent(), response.getBody().getContent());
        assertEquals(expectedReview.getUserId(), response.getBody().getUserId());
        assertEquals(expectedReview.getMerchantId(), response.getBody().getMerchantId());

        // 验证服务方法被调用
        verify(reviewService, times(1)).createReview(userId, merchantId, content, parentId);
    }

    /**
     * 测试创建回复功能 - 正常情况
     * 验证在有效参数下能够成功创建回复
     */
    @Test
    public void testCreateReview_WithParent_Success() {
        // 准备测试数据
        Long userId = 2L;
        Long merchantId = 100L;
        String content = "同意楼上的观点，这家店确实很不错，我也推荐！";
        Long parentId = 1L;

        // 创建预期的返回结果
        Review expectedReply = new Review();
        expectedReply.setId(2L);
        expectedReply.setUserId(userId);
        expectedReply.setMerchantId(merchantId);
        expectedReply.setContent(content);
        expectedReply.setParentId(parentId);
        expectedReply.setCreateTime(LocalDateTime.now());

        // 配置Mock行为
        when(reviewService.createReview(userId, merchantId, content, parentId))
                .thenReturn(expectedReply);

        // 执行测试
        ResponseEntity<Review> response = reviewController.createReview(userId, merchantId, content, parentId);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(expectedReply.getId(), response.getBody().getId());
        assertEquals(expectedReply.getParentId(), response.getBody().getParentId());

        // 验证服务方法被调用
        verify(reviewService, times(1)).createReview(userId, merchantId, content, parentId);
    }

    /**
     * 测试创建评论功能 - 内容过短异常
     * 验证当评论内容少于15字时的异常处理
     */
    @Test
    public void testCreateReview_ContentTooShort() {
        // 准备测试数据
        Long userId = 1L;
        Long merchantId = 100L;
        String content = "太短了"; // 少于15字
        Long parentId = null;

        // 配置Mock行为 - 模拟服务层抛出异常
        when(reviewService.createReview(userId, merchantId, content, parentId))
                .thenThrow(new RuntimeException("点评内容需至少15字"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewController.createReview(userId, merchantId, content, parentId);
        });

        // 验证异常信息
        assertEquals("点评内容需至少15字", exception.getMessage());

        // 验证服务方法被调用
        verify(reviewService, times(1)).createReview(userId, merchantId, content, parentId);
    }

    /**
     * 测试创建评论功能 - 空内容异常
     * 验证当评论内容为空时的异常处理
     */
    @Test
    public void testCreateReview_EmptyContent() {
        // 准备测试数据
        Long userId = 1L;
        Long merchantId = 100L;
        String content = ""; // 空内容
        Long parentId = null;

        // 配置Mock行为 - 模拟服务层抛出异常
        when(reviewService.createReview(userId, merchantId, content, parentId))
                .thenThrow(new RuntimeException("点评内容需至少15字"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewController.createReview(userId, merchantId, content, parentId);
        });

        // 验证异常信息
        assertEquals("点评内容需至少15字", exception.getMessage());
    }

    /**
     * 测试创建评论功能 - null内容异常
     * 验证当评论内容为null时的异常处理
     */
    @Test
    public void testCreateReview_NullContent() {
        // 准备测试数据
        Long userId = 1L;
        Long merchantId = 100L;
        String content = null; // null内容
        Long parentId = null;

        // 配置Mock行为 - 模拟服务层抛出异常
        when(reviewService.createReview(userId, merchantId, content, parentId))
                .thenThrow(new RuntimeException("点评内容需至少15字"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewController.createReview(userId, merchantId, content, parentId);
        });

        // 验证异常信息
        assertEquals("点评内容需至少15字", exception.getMessage());
    }

    /**
     * 测试获取商户评论列表功能 - 正常情况
     * 验证能够成功获取商户的评论列表
     */
    @Test
    public void testGetReviewsByMerchant_Success() {
        // 准备测试数据
        Long merchantId = 100L;

        // 创建模拟的评论列表
        List<Review> expectedReviews = new ArrayList<>();
        
        // 创建第一条评论
        Review review1 = new Review();
        review1.setId(1L);
        review1.setUserId(1L);
        review1.setMerchantId(merchantId);
        review1.setContent("这是第一条评论，内容超过十五个字符，非常详细的评价");
        review1.setParentId(null);
        review1.setCreateTime(LocalDateTime.now().minusHours(2));
        expectedReviews.add(review1);

        // 创建第二条评论
        Review review2 = new Review();
        review2.setId(2L);
        review2.setUserId(2L);
        review2.setMerchantId(merchantId);
        review2.setContent("这是第二条评论，也是一个很详细的评价，内容丰富");
        review2.setParentId(null);
        review2.setCreateTime(LocalDateTime.now().minusHours(1));
        expectedReviews.add(review2);

        // 配置Mock行为
        when(reviewService.getReviewsByMerchant(merchantId)).thenReturn(expectedReviews);

        // 执行测试
        ResponseEntity<List<Review>> response = reviewController.getReviewsByMerchant(merchantId);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedReviews.get(0).getId(), response.getBody().get(0).getId());
        assertEquals(expectedReviews.get(1).getId(), response.getBody().get(1).getId());

        // 验证服务方法被调用
        verify(reviewService, times(1)).getReviewsByMerchant(merchantId);
    }

    /**
     * 测试获取商户评论列表功能 - 空列表情况
     * 验证当商户没有评论时返回空列表
     */
    @Test
    public void testGetReviewsByMerchant_EmptyList() {
        // 准备测试数据
        Long merchantId = 999L; // 不存在评论的商户ID

        // 创建空的评论列表
        List<Review> emptyReviews = new ArrayList<>();

        // 配置Mock行为
        when(reviewService.getReviewsByMerchant(merchantId)).thenReturn(emptyReviews);

        // 执行测试
        ResponseEntity<List<Review>> response = reviewController.getReviewsByMerchant(merchantId);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        // 验证服务方法被调用
        verify(reviewService, times(1)).getReviewsByMerchant(merchantId);
    }

    /**
     * 测试获取商户评论列表功能 - 服务层异常
     * 验证当服务层抛出异常时的处理
     */
    @Test
    public void testGetReviewsByMerchant_ServiceException() {
        // 准备测试数据
        Long merchantId = 100L;

        // 配置Mock行为 - 模拟服务层抛出异常
        when(reviewService.getReviewsByMerchant(merchantId))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewController.getReviewsByMerchant(merchantId);
        });

        // 验证异常信息
        assertEquals("数据库连接失败", exception.getMessage());

        // 验证服务方法被调用
        verify(reviewService, times(1)).getReviewsByMerchant(merchantId);
    }

    /**
     * 测试获取商户评论列表功能 - 包含嵌套回复
     * 验证能够正确处理包含回复的评论结构
     */
    @Test
    public void testGetReviewsByMerchant_WithReplies() {
        // 准备测试数据
        Long merchantId = 100L;

        // 创建主评论
        Review mainReview = new Review();
        mainReview.setId(1L);
        mainReview.setUserId(1L);
        mainReview.setMerchantId(merchantId);
        mainReview.setContent("这是主评论，内容详细描述了商户的服务质量");
        mainReview.setParentId(null);
        mainReview.setCreateTime(LocalDateTime.now().minusHours(2));

        // 创建回复列表
        List<Review> replies = new ArrayList<>();
        Review reply1 = new Review();
        reply1.setId(2L);
        reply1.setUserId(2L);
        reply1.setMerchantId(merchantId);
        reply1.setContent("我同意这个观点，这家店确实很不错！");
        reply1.setParentId(1L);
        reply1.setCreateTime(LocalDateTime.now().minusHours(1));
        replies.add(reply1);

        mainReview.setReplies(replies);

        List<Review> reviewsWithReplies = new ArrayList<>();
        reviewsWithReplies.add(mainReview);

        // 配置Mock行为
        when(reviewService.getReviewsByMerchant(merchantId)).thenReturn(reviewsWithReplies);

        // 执行测试
        ResponseEntity<List<Review>> response = reviewController.getReviewsByMerchant(merchantId);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
        Review returnedReview = response.getBody().get(0);
        assertEquals(mainReview.getId(), returnedReview.getId());
        assertNotNull(returnedReview.getReplies());
        assertEquals(1, returnedReview.getReplies().size());
        assertEquals(reply1.getId(), returnedReview.getReplies().get(0).getId());

        // 验证服务方法被调用
        verify(reviewService, times(1)).getReviewsByMerchant(merchantId);
    }

    /**
     * 测试边界条件 - 最小长度内容
     * 验证刚好15字符的评论内容能够正常处理
     */
    @Test
    public void testCreateReview_MinimumValidContent() {
        // 准备测试数据 - 刚好15个字符
        Long userId = 1L;
        Long merchantId = 100L;
        String content = "这是十五个字符的评论内容测试"; // 刚好15个字符
        Long parentId = null;

        // 创建预期的返回结果
        Review expectedReview = new Review();
        expectedReview.setId(1L);
        expectedReview.setUserId(userId);
        expectedReview.setMerchantId(merchantId);
        expectedReview.setContent(content);
        expectedReview.setParentId(parentId);
        expectedReview.setCreateTime(LocalDateTime.now());

        // 配置Mock行为
        when(reviewService.createReview(userId, merchantId, content, parentId))
                .thenReturn(expectedReview);

        // 执行测试
        ResponseEntity<Review> response = reviewController.createReview(userId, merchantId, content, parentId);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(content, response.getBody().getContent());

        // 验证服务方法被调用
        verify(reviewService, times(1)).createReview(userId, merchantId, content, parentId);
    }
} 