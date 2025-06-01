import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.service.CouponService;
import com.fudan.xiaozhong_dianping.review.entity.Review;
import com.fudan.xiaozhong_dianping.review.repository.ReviewRepository;
import com.fudan.xiaozhong_dianping.review.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReviewServiceImplTest类用于对ReviewServiceImpl中的评论业务逻辑进行单元测试。
 * 该类使用JUnit 5和Mockito框架，通过模拟Repository的行为来测试服务层的业务逻辑。
 * 确保在不同情况下，评论服务能正确处理业务规则并返回预期结果。
 *
 * @author Lab4 Test Team
 * @version 1.0
 */
public class ReviewServiceImplTest {

    // 模拟ReviewRepository，用于在测试中提供数据访问的模拟行为
    @Mock
    private ReviewRepository reviewRepository;

    // 模拟CouponService，用于测试奖励机制
    @Mock
    private CouponService couponService;

    // 注入被测试的ReviewServiceImpl，将模拟的Repository注入到服务中
    @InjectMocks
    private ReviewServiceImpl reviewService;

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
     * 验证在有效参数下能够成功创建评论并保存到数据库
     */
    @Test
    public void testCreateReview_Success() {
        // 准备测试数据
        Long userId = 1L;
        Long merchantId = 100L;
        String content = "这是一个非常好的商户，服务很棒，推荐大家来这里消费！";
        Long parentId = null;

        // 创建模拟的保存结果
        Review savedReview = new Review();
        savedReview.setId(1L);
        savedReview.setUserId(userId);
        savedReview.setMerchantId(merchantId);
        savedReview.setContent(content);
        savedReview.setParentId(parentId);

        // 配置Mock行为
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewRepository.countValidReviewsByUser(userId)).thenReturn(1); // 第一次评论
        when(couponService.hasReceivedReviewReward(userId)).thenReturn(false);

        // 执行测试
        Review result = reviewService.createReview(userId, merchantId, content, parentId);

        // 验证结果
        assertNotNull(result);
        assertEquals(savedReview.getId(), result.getId());
        assertEquals(savedReview.getContent(), result.getContent());
        assertEquals(savedReview.getUserId(), result.getUserId());
        assertEquals(savedReview.getMerchantId(), result.getMerchantId());

        // 验证Repository方法被调用
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(reviewRepository, times(1)).countValidReviewsByUser(userId);
        
        // 验证奖励逻辑 - 第一次评论不应触发奖励
        verify(couponService, never()).grantReviewRewardCoupon(userId);
    }

    /**
     * 测试创建评论功能 - 触发奖励机制
     * 验证用户达到3条有效评论时触发奖励发放
     */
    @Test
    public void testCreateReview_TriggerReward() {
        // 准备测试数据
        Long userId = 1L;
        Long merchantId = 100L;
        String content = "这是第三条评论，应该触发奖励机制！";
        Long parentId = null;

        // 创建模拟的保存结果
        Review savedReview = new Review();
        savedReview.setId(3L);
        savedReview.setUserId(userId);
        savedReview.setMerchantId(merchantId);
        savedReview.setContent(content);
        savedReview.setParentId(parentId);

        // 配置Mock行为 - 模拟用户已有3条有效评论且未获得过奖励
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewRepository.countValidReviewsByUser(userId)).thenReturn(3); // 第3条评论
        when(couponService.hasReceivedReviewReward(userId)).thenReturn(false); // 未获得过奖励

        // 执行测试
        Review result = reviewService.createReview(userId, merchantId, content, parentId);

        // 验证结果
        assertNotNull(result);
        assertEquals(savedReview.getId(), result.getId());

        // 验证Repository方法被调用
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(reviewRepository, times(1)).countValidReviewsByUser(userId);
        
        // 验证奖励逻辑 - 第3条评论应触发奖励
        verify(couponService, times(1)).hasReceivedReviewReward(userId);
        verify(couponService, times(1)).grantReviewRewardCoupon(userId);
    }

    /**
     * 测试创建评论功能 - 不重复发放奖励
     * 验证用户已获得奖励后不会重复发放
     */
    @Test
    public void testCreateReview_NoRepeatReward() {
        // 准备测试数据
        Long userId = 1L;
        Long merchantId = 100L;
        String content = "这是第四条评论，但已经获得过奖励了！";
        Long parentId = null;

        // 创建模拟的保存结果
        Review savedReview = new Review();
        savedReview.setId(4L);
        savedReview.setUserId(userId);
        savedReview.setMerchantId(merchantId);
        savedReview.setContent(content);
        savedReview.setParentId(parentId);

        // 配置Mock行为 - 模拟用户已有4条有效评论且已获得过奖励
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewRepository.countValidReviewsByUser(userId)).thenReturn(4); // 第4条评论
        when(couponService.hasReceivedReviewReward(userId)).thenReturn(true); // 已获得过奖励

        // 执行测试
        Review result = reviewService.createReview(userId, merchantId, content, parentId);

        // 验证结果
        assertNotNull(result);
        assertEquals(savedReview.getId(), result.getId());

        // 验证Repository方法被调用
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(reviewRepository, times(1)).countValidReviewsByUser(userId);
        
        // 验证奖励逻辑 - 已获得奖励的用户不应重复发放
        verify(couponService, times(1)).hasReceivedReviewReward(userId);
        verify(couponService, never()).grantReviewRewardCoupon(userId);
    }

    /**
     * 测试创建评论功能 - 内容过短异常
     * 验证当评论内容少于15字时抛出异常
     */
    @Test
    public void testCreateReview_ContentTooShort() {
        // 准备测试数据
        Long userId = 1L;
        Long merchantId = 100L;
        String content = "太短"; // 少于15字
        Long parentId = null;

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reviewService.createReview(userId, merchantId, content, parentId);
        });

        // 验证异常信息
        assertEquals("点评内容需至少15字", exception.getMessage());

        // 验证Repository方法未被调用
        verify(reviewRepository, never()).save(any(Review.class));
        verify(couponService, never()).grantReviewRewardCoupon(userId);
    }

    /**
     * 测试创建评论功能 - 空内容异常
     * 验证当评论内容为空时抛出异常
     */
    @Test
    public void testCreateReview_EmptyContent() {
        // 准备测试数据
        Long userId = 1L;
        Long merchantId = 100L;
        String content = ""; // 空内容
        Long parentId = null;

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reviewService.createReview(userId, merchantId, content, parentId);
        });

        // 验证异常信息
        assertEquals("点评内容需至少15字", exception.getMessage());

        // 验证Repository方法未被调用
        verify(reviewRepository, never()).save(any(Review.class));
    }

    /**
     * 测试创建评论功能 - null内容异常
     * 验证当评论内容为null时抛出异常
     */
    @Test
    public void testCreateReview_NullContent() {
        // 准备测试数据
        Long userId = 1L;
        Long merchantId = 100L;
        String content = null; // null内容
        Long parentId = null;

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reviewService.createReview(userId, merchantId, content, parentId);
        });

        // 验证异常信息
        assertEquals("点评内容需至少15字", exception.getMessage());

        // 验证Repository方法未被调用
        verify(reviewRepository, never()).save(any(Review.class));
    }

    /**
     * 测试获取商户评论列表功能 - 正常情况
     * 验证能够成功获取商户的评论列表
     */
    @Test
    public void testGetReviewsByMerchant_Success() {
        // 准备测试数据
        Long merchantId = 100L;

        // 创建模拟的顶级评论列表
        List<Review> topReviews = new ArrayList<>();
        Review review1 = new Review();
        review1.setId(1L);
        review1.setMerchantId(merchantId);
        review1.setContent("这是第一条评论，内容详细");
        review1.setParentId(null);
        topReviews.add(review1);

        // 创建模拟的回复列表
        List<Review> replies = new ArrayList<>();
        Review reply1 = new Review();
        reply1.setId(2L);
        reply1.setMerchantId(merchantId);
        reply1.setContent("这是对第一条评论的回复");
        reply1.setParentId(1L);
        replies.add(reply1);

        // 配置Mock行为
        when(reviewRepository.findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, null))
                .thenReturn(topReviews);
        when(reviewRepository.findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, 1L))
                .thenReturn(replies);

        // 执行测试
        List<Review> result = reviewService.getReviewsByMerchant(merchantId);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(review1.getId(), result.get(0).getId());
        assertNotNull(result.get(0).getReplies());
        assertEquals(1, result.get(0).getReplies().size());
        assertEquals(reply1.getId(), result.get(0).getReplies().get(0).getId());

        // 验证Repository方法被调用
        verify(reviewRepository, times(1)).findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, null);
        verify(reviewRepository, times(1)).findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, 1L);
    }

    /**
     * 测试获取商户评论列表功能 - 空列表情况
     * 验证当商户没有评论时返回空列表
     */
    @Test
    public void testGetReviewsByMerchant_EmptyList() {
        // 准备测试数据
        Long merchantId = 999L; // 不存在评论的商户ID

        // 配置Mock行为 - 返回空列表
        when(reviewRepository.findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, null))
                .thenReturn(new ArrayList<>());
        when(reviewRepository.findByMerchantIdOrderByCreateTimeDesc(merchantId))
                .thenReturn(new ArrayList<>());

        // 执行测试
        List<Review> result = reviewService.getReviewsByMerchant(merchantId);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证Repository方法被调用
        verify(reviewRepository, times(1)).findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, null);
        verify(reviewRepository, times(1)).findByMerchantIdOrderByCreateTimeDesc(merchantId);
    }

    /**
     * 测试获取商户评论列表功能 - 数据库异常处理
     * 验证当数据库操作异常时能够正确处理并返回空列表
     */
    @Test
    public void testGetReviewsByMerchant_DatabaseException() {
        // 准备测试数据
        Long merchantId = 100L;

        // 配置Mock行为 - 模拟数据库异常
        when(reviewRepository.findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, null))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试 - 服务层应捕获异常并返回空列表
        List<Review> result = reviewService.getReviewsByMerchant(merchantId);

        // 验证结果 - 异常被捕获，返回空列表而不是抛出异常
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证Repository方法被调用
        verify(reviewRepository, times(1)).findByMerchantIdAndParentIdOrderByCreateTimeDesc(merchantId, null);
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
        String content = "这是一个包含十五个字符的评论内容"; // 刚好15个字符
        Long parentId = null;

        // 创建模拟的保存结果
        Review savedReview = new Review();
        savedReview.setId(1L);
        savedReview.setUserId(userId);
        savedReview.setMerchantId(merchantId);
        savedReview.setContent(content);
        savedReview.setParentId(parentId);

        // 配置Mock行为
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewRepository.countValidReviewsByUser(userId)).thenReturn(1);
        when(couponService.hasReceivedReviewReward(userId)).thenReturn(false);

        // 执行测试
        Review result = reviewService.createReview(userId, merchantId, content, parentId);

        // 验证结果
        assertNotNull(result);
        assertEquals(content, result.getContent());

        // 验证Repository方法被调用
        verify(reviewRepository, times(1)).save(any(Review.class));
    }
} 