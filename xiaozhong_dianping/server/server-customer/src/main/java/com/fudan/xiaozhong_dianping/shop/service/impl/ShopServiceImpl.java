package com.fudan.xiaozhong_dianping.shop.service.impl;

import com.fudan.result.PageResult;
import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.entity.ShopImage;
import com.fudan.xiaozhong_dianping.shop.mapper.SearchHistoryMapper;
import com.fudan.xiaozhong_dianping.shop.mapper.ShopImageMapper;
import com.fudan.xiaozhong_dianping.shop.mapper.ShopMapper;
import com.fudan.xiaozhong_dianping.shop.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
/**
 * ShopServiceImpl类实现了ShopService接口，提供了一系列与商店相关的服务方法
 * 它使用了Spring的@Service注解，标志着它是一个服务层组件
 */
public class ShopServiceImpl implements ShopService {

    private static final Logger log = LoggerFactory.getLogger(ShopServiceImpl.class);

    @Autowired
    /**
     * 自动注入ShopMapper接口的实现类，用于访问商店相关的数据库操作
     */
    private ShopMapper shopMapper;

    @Autowired
    /**
     * 自动注入SearchHistoryMapper接口的实现类，用于访问搜索历史相关的数据库操作
     */
    private SearchHistoryMapper searchHistoryMapper;

    @Autowired
    private ShopImageMapper shopImageMapper; // 新增注入图片Mapper
    @Override
    /**
     * 保存用户的搜索历史记录
     *
     * @param searchHistory 搜索历史对象，包含用户ID和搜索关键词等信息
     * @return 如果插入操作成功，则返回true；否则返回false
     */
    public Boolean saveSearchHistory(SearchHistory searchHistory) {
        return searchHistoryMapper.insert(searchHistory) > 0;
    }

    @Override
    /**
     * 根据查询条件搜索商店信息
     *
     * @param shopPageQueryDTO 包含分页和查询条件的DTO对象
     * @return 返回查询到的商店列表
     */
    public List<Shop> searchShops(ShopPageQueryDTO shopPageQueryDTO) {
        // 记录搜索参数
        log.info("执行搜索，原始关键词：{}", shopPageQueryDTO.getName());
        if (shopPageQueryDTO.getExpandedKeywords() != null) {
            log.info("扩展后的关键词列表：{}", shopPageQueryDTO.getExpandedKeywords());
        } else {
            log.warn("未找到扩展的关键词列表");
        }
        
        List<Shop> result = shopMapper.searchShops(shopPageQueryDTO);
        log.info("搜索完成，找到 {} 条记录", result.size());
        
        // 打印每个商家的分类信息用于调试
        for (Shop shop : result) {
            log.info("搜索结果 - 商家ID: {}, 名称: {}, 分类ID: {}, 分类名称: {}", 
                shop.getId(), shop.getName(), shop.getCategoryId(), shop.getCategoryName());
        }
        
        return result;
    }

    @Override
    /**
     * 根据用户ID获取该用户的搜索历史记录
     *
     * @param userId 用户ID，用于查询搜索历史
     * @return 返回该用户的搜索历史记录列表
     */
    public List<SearchHistory> getSearchHistoryByUserId(Long userId) {
        return searchHistoryMapper.getSearchHistoryByUserId(userId);
    }

    /**
     * 分页查询店铺信息列表
     *
     * @param offset 分页偏移量
     * @param pageSize    每页显示的记录数量
     * @return            分页查询后的店铺信息集合，包含当前页的店铺数据
     */
    @Override
    public List<Shop> showShops(int offset, int pageSize) {
        // 调用数据访问层获取分页数据
        List<Shop> list = shopMapper.showShops(offset, pageSize);
        return list;
    }

    @Override
    public Map<String, Object> getShopDetails(Long shopId) {
        Map<String, Object> result = new HashMap<>();

        // 查询商家基本信息
        Shop shop = shopMapper.findShopById(shopId);
        result.put("shop", shop);

        // 查询商家图片（需确保ShopImageMapper已定义findImagesByShopId方法）
        List<ShopImage> images = shopImageMapper.findImagesByShopId(shopId);
        images.forEach(img -> {
            img.setImageUrl("/static/" + img.getImageUrl()); // 添加前缀
        });
        result.put("images", images);

        return result;
    }



    @Override
    public Boolean clearSearchHistory(Long userId) {
        return searchHistoryMapper.deleteByUserId(userId) > 0;
    }

    @Override
    public List<ShopImage> getShopImages(Integer shopId) {
        log.info("开始获取商家图片，商家ID: {}", shopId);
        
        // 将Integer类型的shopId转换为Long类型再传递
        Long shopIdLong = shopId != null ? shopId.longValue() : null;
        
        if (shopIdLong == null) {
            log.warn("商家ID为空，无法获取图片");
            return new ArrayList<>();
        }
        
        List<ShopImage> images = shopImageMapper.findImagesByShopId(shopIdLong);
        log.info("商家[{}]获取到{}张图片", shopId, images.size());
        
        // 处理图片URL
        processImageUrls(images);
        
        return images;
    }
    
    /**
     * 处理图片URL列表
     * 提取方法以减少控制流嵌套
     * 
     * @param images 需要处理URL的图片列表
     */
    private void processImageUrls(List<ShopImage> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        
        for (ShopImage image : images) {
            processImageUrl(image);
        }
    }
    
    /**
     * 处理单个图片URL
     * 如果不是http开头的URL，添加前缀
     * 
     * @param image 需要处理的图片对象
     */
    private void processImageUrl(ShopImage image) {
        if (image == null) {
            return;
        }
        
        String imageUrl = image.getImageUrl();
        if (imageUrl == null || imageUrl.startsWith("http")) {
            return;
        }
        
        // 移除开头的斜杠
        imageUrl = removeLeadingSlashes(imageUrl);
        
        // 添加前缀
        imageUrl = "/static/" + imageUrl;
        image.setImageUrl(imageUrl);
        log.debug("处理后的图片URL: {}", imageUrl);
    }
    
    /**
     * 移除字符串开头的所有斜杠
     * 
     * @param input 输入字符串
     * @return 处理后的字符串
     */
    private String removeLeadingSlashes(String input) {
        if (input == null) {
            return "";
        }
        
        int startIndex = 0;
        while (startIndex < input.length() && input.charAt(startIndex) == '/') {
            startIndex++;
        }
        
        return startIndex > 0 ? input.substring(startIndex) : input;
    }

    @Override
    public int countShops() {
        return shopMapper.countAllShops();
    }

    /**
     * 添加测试数据
     * 用于测试模糊搜索功能
     * 
     * @return 是否添加成功
     */
    @Override
    public boolean addDemoShops() {
        try {
            log.info("准备添加测试数据");
            
            // 导入BigDecimal类
            java.math.BigDecimal bd;
            
            // 添加测试用的火锅店数据
            Shop hotPotShop = new Shop();
            hotPotShop.setName("老北京火锅店");
            hotPotShop.setAddress("北京市海淀区中关村大街1号");
            hotPotShop.setBusinessHours("10:00-22:00");
            hotPotShop.setPhone("010-12345678");
            hotPotShop.setDescription("正宗老北京火锅，各种口味任您选择");
            hotPotShop.setAverageCost(new java.math.BigDecimal("88.0"));
            hotPotShop.setRating(new java.math.BigDecimal("4.5"));
            hotPotShop.setPriceMin(new java.math.BigDecimal("50.0"));
            hotPotShop.setPriceMax(new java.math.BigDecimal("150.0"));
            hotPotShop.setCategoryId(1); // 假设1是火锅分类
            
            // 添加测试用的汤面店数据
            Shop noodleShop = new Shop();
            noodleShop.setName("香汤面馆");
            noodleShop.setAddress("上海市静安区南京西路100号");
            noodleShop.setBusinessHours("07:00-21:00");
            noodleShop.setPhone("021-87654321");
            noodleShop.setDescription("各种美味汤面，香气扑鼻");
            noodleShop.setAverageCost(new java.math.BigDecimal("35.0"));
            noodleShop.setRating(new java.math.BigDecimal("4.2"));
            noodleShop.setPriceMin(new java.math.BigDecimal("25.0"));
            noodleShop.setPriceMax(new java.math.BigDecimal("60.0"));
            noodleShop.setCategoryId(2); // 假设2是面食分类
            
            // 添加测试用的奶茶店数据
            Shop milkTeaShop = new Shop();
            milkTeaShop.setName("甜心奶茶店");
            milkTeaShop.setAddress("广州市天河区天河路5号");
            milkTeaShop.setBusinessHours("09:00-23:00");
            milkTeaShop.setPhone("020-56781234");
            milkTeaShop.setDescription("正宗台湾奶茶，香浓可口");
            milkTeaShop.setAverageCost(new java.math.BigDecimal("20.0"));
            milkTeaShop.setRating(new java.math.BigDecimal("4.8"));
            milkTeaShop.setPriceMin(new java.math.BigDecimal("15.0"));
            milkTeaShop.setPriceMax(new java.math.BigDecimal("30.0"));
            milkTeaShop.setCategoryId(3); // 假设3是饮品分类
            
            // 保存测试数据到数据库
            // 实际插入数据
            shopMapper.insertShop(hotPotShop);
            log.info("添加测试火锅店: {} 成功", hotPotShop.getName());
            
            shopMapper.insertShop(noodleShop);
            log.info("添加测试面馆: {} 成功", noodleShop.getName());
            
            shopMapper.insertShop(milkTeaShop);
            log.info("添加测试奶茶店: {} 成功", milkTeaShop.getName());
            
            return true;
        } catch (Exception e) {
            log.error("插入测试数据时出错: {}", e.getMessage(), e);
            return false;
        }
    }

}
