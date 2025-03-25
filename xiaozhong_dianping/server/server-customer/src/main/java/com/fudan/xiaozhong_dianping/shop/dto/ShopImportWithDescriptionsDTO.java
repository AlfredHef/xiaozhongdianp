package com.fudan.xiaozhong_dianping.shop.dto;

import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.entity.ShopImage;
import lombok.Data;

import java.util.List;

@Data
public class ShopImportWithDescriptionsDTO {
    private List<Shop> shops;
    private List<ShopImage> shopImages;

}
