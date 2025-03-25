package com.fudan.xiaozhong_dianping.shop.dto;

import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import lombok.Data;

import java.util.List;

@Data
public class ShopImportDTO {
    private List<Shop> shops;
    private List<String> imagePaths;

}
