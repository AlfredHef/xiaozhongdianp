package com.fudan.xiaozhong_dianping.review.repository;
import com.fudan.xiaozhong_dianping.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Review实体的仓库接口，用于执行Review相关的数据库操作
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 统计用户有效点评数（内容≥15字）
     *
     * @param userId 用户ID，用于指定查询的有效点评数所属的用户
     * @return 用户的有效点评数
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.userId = ?1 AND LENGTH(r.content) >= 15")
    int countValidReviewsByUser(Long userId);

    /**
     * 根据商户ID和父级ID查询点评/回复列表（按时间倒序）
     *
     * @param merchantId 商户ID，用于指定查询的点评/回复所属的商户
     * @param parentId 父级ID，用于指定查询的点评/回复的父级
     * @return 按时间倒序排列的点评/回复列表
     */
    @Query("SELECT r FROM Review r WHERE r.merchantId = ?1 AND r.parentId = ?2 ORDER BY r.createTime DESC")
    List<Review> findByMerchantIdAndParentIdOrderByCreateTimeDesc(Long merchantId, Long parentId);
    
    /**
     * 根据商户ID查询所有点评/回复列表（按时间倒序），不过滤parentId
     *
     * @param merchantId 商户ID，用于指定查询的点评/回复所属的商户
     * @return 按时间倒序排列的所有点评/回复列表
     */
    @Query("SELECT r FROM Review r WHERE r.merchantId = ?1 ORDER BY r.createTime DESC")
    List<Review> findByMerchantIdOrderByCreateTimeDesc(Long merchantId);
}

