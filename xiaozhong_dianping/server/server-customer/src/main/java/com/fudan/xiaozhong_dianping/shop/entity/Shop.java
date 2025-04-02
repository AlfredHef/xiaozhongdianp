package com.fudan.xiaozhong_dianping.shop.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 该类表示商家信息，包含了商家的基本属性、分类信息以及创建和更新时间。
 * 使用 Lombok 注解简化代码，自动生成 getter、setter、构造函数等方法。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shop {

    /**
     * 商家的唯一标识符，用于在系统中唯一标识一个商家。
     */
    private Integer id;

    /**
     * 商家的名称，用于在搜索和展示时识别商家。
     */
    private String name;

    /**
     * 商家的详细地址，可能包含地理信息，可用于地理信息检索。
     */
    private String address;

    /**
     * 商家的营业时间，格式为“周一至周日 10:00-22:00”，方便用户了解商家的营业时间段。
     */
    private String businessHours;


    /**
     * 商家的联系电话，方便用户与商家取得联系。
     */
    private String phone;

    /**
     * 商家的描述信息
     */
    private String description;

    /**
     * 商家的人均消费金额，以元为单位，保留两位小数。
     */

    private BigDecimal averageCost;

    /**
     * 商家的综合评分，范围从 0.0 到 5.0，用于反映商家的整体服务质量和用户满意度。
     */
    private BigDecimal rating;

    /**
     * 商家的最低消费价格，以元为单位，保留两位小数。
     */
    private BigDecimal priceMin;

    /**
     * 商家的最高消费价格，以元为单位，保留两位小数。
     */
    private BigDecimal priceMax;

    /**
     * 商家所属的分类信息，关联到 Category 类，用于对商家进行分类管理和搜索。
     */
    private Integer categoryId;
    /**
     * 商家信息记录的创建时间，自动记录该商家信息在系统中创建的时刻。
     */
    private Date createdAt;
    /**
     * 商家信息记录的更新时间，当商家信息发生修改时，自动更新该时间。
     */
    private Date updatedAt;

private String categoryName;

}