import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.mapper.SearchHistoryMapper;

import com.fudan.xiaozhong_dianping.shop.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
/**
 * 测试的是ShopServiceImpl类中的saveSearchHistory方法。
 * 主要验证该方法在不同数据库插入结果（成功和失败）情况下，能否正确返回对应的布尔值，
 * 以此判断方法的逻辑正确性和稳定性
 */
public class ShopServiceImplSaveSearchHistoryTest {

    // 模拟SearchHistoryMapper
    @Mock
    private SearchHistoryMapper searchHistoryMapper;

    // 注入被测试的ShopServiceImpl
    @InjectMocks
    private ShopServiceImpl shopService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 测试saveSearchHistory方法

    @Test
    public void testSaveSearchHistory() {
        // 正常情况测试
        SearchHistory searchHistory = new SearchHistory();
        when(searchHistoryMapper.insert(searchHistory)).thenReturn(1);
        assertTrue(shopService.saveSearchHistory(searchHistory));

        // 异常情况测试：插入失败
        when(searchHistoryMapper.insert(searchHistory)).thenReturn(0);
        assertFalse(shopService.saveSearchHistory(searchHistory));

        // 异常情况测试：插入方法抛出异常
        when(searchHistoryMapper.insert(searchHistory)).thenThrow(new RuntimeException("模拟插入异常"));
        assertThrows(RuntimeException.class, () -> shopService.saveSearchHistory(searchHistory));
    }
}
