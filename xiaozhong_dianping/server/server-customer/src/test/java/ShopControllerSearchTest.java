import com.fudan.result.Result;
import com.fudan.xiaozhong_dianping.shop.controller.ShopController;
import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.service.ShopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * ShopControllerSearchTest类用于对ShopController中的搜索功能进行单元测试。
 * 该类使用JUnit 5和Mockito框架，通过模拟ShopService的行为来测试ShopController的search方法。
 * 确保在不同情况下，ShopController的搜索功能能正确响应并返回预期结果。
 *
 * @author [你的姓名]
 * @version 1.0
 * @since [项目开始日期或版本起始日期]
 */
public class ShopControllerSearchTest {

    // 模拟ShopService，用于在测试中提供ShopService的模拟行为
    @Mock
    private ShopService shopService;

    // 注入被测试的ShopController，将模拟的ShopService注入到ShopController中
    @InjectMocks
    private ShopController shopController;

    /**
     * 初始化方法，在每个测试方法执行前调用。
     * 用于初始化Mockito的模拟环境，确保模拟对象正确设置。
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试ShopController的search方法在正常搜索情况下的行为。
     * 给定有效的搜索参数，验证方法能正确调用ShopService并返回成功结果。
     *
     * @see ShopController#search(ShopPageQueryDTO)
     */
    @Test
    public void testSearchNormal() {
        // 创建一个包含搜索条件的ShopPageQueryDTO对象
        ShopPageQueryDTO shopPageQueryDTO = new ShopPageQueryDTO();
        shopPageQueryDTO.setName("测试店铺");
        shopPageQueryDTO.setUserId(1L);

        // 创建一个包含一个Shop对象的列表，用于模拟ShopService返回的搜索结果
        List<Shop> shopList = new ArrayList<>();
        Shop shop = new Shop();
        shop.setId(1);
        shop.setName("测试店铺");
        shopList.add(shop);

        // 配置ShopService的searchShops方法在传入特定参数时返回上述模拟的店铺列表
        when(shopService.searchShops(shopPageQueryDTO)).thenReturn(shopList);

        // 调用ShopController的search方法进行搜索，并获取结果
        Result<List<Map<String, Object>>> result = shopController.search(shopPageQueryDTO);
        // 断言返回结果的状态码与成功状态码一致
        assertEquals(Result.success().getCode(), result.getCode());
    }

    /**
     * 测试ShopController的search方法在ShopService抛出异常时的行为。
     * 验证在服务层出现异常时，ShopController能正确处理并返回非成功结果。
     *
     * @see ShopController#search(ShopPageQueryDTO)
     */
    @Test
    public void testSearchException() {
        // 创建一个包含搜索条件的ShopPageQueryDTO对象
        ShopPageQueryDTO shopPageQueryDTO = new ShopPageQueryDTO();
        shopPageQueryDTO.setName("测试店铺");
        shopPageQueryDTO.setUserId(1L);

        // 配置ShopService的searchShops方法在传入特定参数时抛出一个运行时异常，模拟服务层错误
        when(shopService.searchShops(shopPageQueryDTO)).thenThrow(new RuntimeException("模拟搜索异常"));

        // 调用ShopController的search方法进行搜索，并获取结果
        Result<List<Map<String, Object>>> result = shopController.search(shopPageQueryDTO);
        // 断言返回结果的状态码与成功状态码不一致
        assertNotEquals(Result.success().getCode(), result.getCode());
    }

    /**
     * 测试ShopController的search方法在查询参数为空时的行为。
     * 验证在传入空参数时，ShopController能根据业务逻辑返回预期的结果（这里假设为空参数时应返回失败结果）。
     *
     * @see ShopController#search(ShopPageQueryDTO)
     */
    @Test
    public void testSearchWithEmptyQuery() {
        // 创建一个空的ShopPageQueryDTO对象
        ShopPageQueryDTO shopPageQueryDTO = new ShopPageQueryDTO();

        // 调用ShopController的search方法进行搜索，并获取结果
        Result<List<Map<String, Object>>> result = shopController.search(shopPageQueryDTO);
        // 根据实际业务逻辑判断返回结果是否符合预期，这里假设空参数应返回失败结果，断言返回结果的状态码与成功状态码不一致
        assertNotEquals(Result.success().getCode(), result.getCode());
    }
}