package com.fudan.xiaozhong_dianping.shop.mapper;

import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 * 搜索历史记录的Mapper接口
 * 用于执行与搜索历史记录相关的数据库操作
 */
@Mapper
public interface SearchHistoryMapper {

    /**
     * 插入一条搜索历史记录
     *
     * @param searchHistory 搜索历史记录对象，包含用户ID、关键词和搜索时间等信息
     * @return 插入操作影响的行数
     */
    @Insert("INSERT INTO search_history (user_id, keyword, search_time) VALUES (#{userId}, #{keyword}, #{searchTime})")
    int insert(SearchHistory searchHistory);

    /**
     * 根据用户ID获取搜索历史记录
     *
     * @param userId 用户ID，用于查询搜索历史记录
     * @return 用户的搜索历史记录列表，按搜索时间降序排列
     */
    @Select("SELECT * FROM search_history WHERE user_id = #{userId} ORDER BY search_time DESC")
    List<SearchHistory> getSearchHistoryByUserId(Long userId);



    /**
     * 根据用户ID删除所有搜索历史记录
     * @param userId 用户ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM search_history WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
