package com.fudan.xiaozhong_dianping.shop.mapper;

import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;

import java.util.List;
/**
 * 搜索历史记录的Mapper接口
 * 用于执行与搜索历史记录相关的数据库操作
 */
@Mapper
public interface SearchHistoryMapper {

    /**
     * 插入一条搜索历史记录
     * 使用@Options注解配置自动生成主键
     *
     * @param searchHistory 搜索历史记录对象，包含用户ID、关键词和搜索时间等信息
     * @return 插入操作影响的行数，成功为1，失败为0
     */
    @Insert("INSERT INTO search_history (user_id, keyword, search_time) VALUES (#{userId}, #{keyword}, #{searchTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SearchHistory searchHistory);

    /**
     * 根据用户ID获取搜索历史记录
     * 结果按搜索时间降序排列，最新的记录排在最前面
     *
     * @param userId 用户ID，用于查询搜索历史记录
     * @return 用户的搜索历史记录列表，按搜索时间降序排列
     */
    @Select("SELECT id, user_id as userId, keyword, search_time as searchTime FROM search_history WHERE user_id = #{userId} ORDER BY search_time DESC")
    List<SearchHistory> getSearchHistoryByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID删除所有搜索历史记录
     * 
     * @param userId 用户ID
     * @return 影响的行数，返回值大于0表示删除成功
     */
    @Delete("DELETE FROM search_history WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 检查搜索历史表是否存在
     * 这个方法可以用来验证数据库结构
     * 
     * @return 表存在则返回1，否则返回0
     */
    @Select("SELECT 1 FROM information_schema.tables WHERE table_name = 'search_history' LIMIT 1")
    Integer checkTableExists();

    /**
     * 删除测试记录
     * 
     * @param id 记录ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM search_history WHERE id = #{id}")
    int deleteTestRecord(@Param("id") Long id);
}
