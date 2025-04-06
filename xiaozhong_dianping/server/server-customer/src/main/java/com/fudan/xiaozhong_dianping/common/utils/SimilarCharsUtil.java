package com.fudan.xiaozhong_dianping.common.utils;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 中文形近字工具类
 * 用于支持模糊搜索功能
 */
@Component
public class SimilarCharsUtil {

    /**
     * 形近字映射表：key为原字符，value为形近字集合
     */
    private static final Map<Character, Set<Character>> SIMILAR_CHARS_MAP = new HashMap<>();

    /**
     * 初始化形近字映射表
     */
    @PostConstruct
    public void init() {
        // 常见形近字映射
        addSimilarCharMapping('锅', '郭', '锢', '国');
        addSimilarCharMapping('火', '伙', '灬');
        addSimilarCharMapping('面', '麵', '棉', '绵');
        addSimilarCharMapping('汤', '湯', '烫');
        addSimilarCharMapping('奶', '乃', '钠');
        addSimilarCharMapping('茶', '查', '茬');
        addSimilarCharMapping('包', '胞', '苞', '饱');
        addSimilarCharMapping('饭', '飯', '饪');
        addSimilarCharMapping('馆', '館', '管');
        addSimilarCharMapping('酒', '洒', '酉');
        addSimilarCharMapping('鸡', '鸪', '鷄');
        addSimilarCharMapping('肉', '内', '肌');
        addSimilarCharMapping('鱼', '魚', '渔');
        addSimilarCharMapping('米', '米', '糸');
        addSimilarCharMapping('香', '鄉', '乡');
        addSimilarCharMapping('辣', '辢', '拉');
        addSimilarCharMapping('烧', '燒', '稍');
        addSimilarCharMapping('麻', '蔴', '摩');
        addSimilarCharMapping('川', '州', '訓');
        addSimilarCharMapping('菜', '采', '彩');
        
        // 可以根据需要继续添加更多形近字映射
    }
    
    /**
     * 添加形近字映射关系（双向映射）
     * @param mainChar 主字符
     * @param similarChars 形近字数组
     */
    private void addSimilarCharMapping(char mainChar, char... similarChars) {
        // 获取或创建主字符的形近字集合
        Set<Character> mainSet = SIMILAR_CHARS_MAP.computeIfAbsent(mainChar, k -> new HashSet<>());
        
        // 添加形近字到主字符的集合中
        for (char similarChar : similarChars) {
            mainSet.add(similarChar);
            
            // 双向映射：形近字 -> 主字符
            Set<Character> similarSet = SIMILAR_CHARS_MAP.computeIfAbsent(similarChar, k -> new HashSet<>());
            similarSet.add(mainChar);
        }
    }
    
    /**
     * 将给定的关键词扩展为包含形近字的SQL模糊搜索模式
     * @param keyword 原始关键词
     * @return 扩展后的SQL LIKE模式列表
     */
    public List<String> getExpandedKeywords(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return Collections.emptyList();
        }
        
        System.out.println("原始关键词: " + keyword);
        
        // 确保HashMap已经初始化，打印出当前字符映射表大小
        System.out.println("形近字映射表大小: " + SIMILAR_CHARS_MAP.size());
        if (SIMILAR_CHARS_MAP.isEmpty()) {
            System.out.println("警告: 形近字映射表为空，初始化可能失败");
            // 尝试手动初始化
            init();
        }
        
        List<String> expandedKeywords = new ArrayList<>();
        // 添加原始关键词
        expandedKeywords.add("%" + keyword + "%");
        System.out.println("添加原始关键词: %" + keyword + "%");
        
        // 生成形近字替换关键词
        char[] chars = keyword.toCharArray();
        
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            System.out.println("处理字符: " + c + " 位置: " + i);
            
            Set<Character> similarChars = SIMILAR_CHARS_MAP.get(c);
            
            if (similarChars != null && !similarChars.isEmpty()) {
                System.out.println("找到形近字: " + similarChars);
                
                for (char similarChar : similarChars) {
                    if (similarChar == c) {
                        System.out.println("跳过相同字符: " + similarChar);
                        continue; // 跳过相同字符
                    }
                    
                    char[] newChars = keyword.toCharArray();
                    newChars[i] = similarChar;
                    String newKeyword = new String(newChars);
                    String expandedPattern = "%" + newKeyword + "%";
                    expandedKeywords.add(expandedPattern);
                    System.out.println("添加扩展关键词: " + expandedPattern);
                }
            } else {
                System.out.println("未找到字符'" + c + "'的形近字");
            }
        }
        
        // 特殊处理"火锅"和"火郭"
        if (keyword.equals("火郭")) {
            expandedKeywords.add("%火锅%");
            System.out.println("特别添加: %火锅%");
        } else if (keyword.equals("火锅")) {
            expandedKeywords.add("%火郭%");
            System.out.println("特别添加: %火郭%");
        }
        
        System.out.println("最终扩展关键词: " + expandedKeywords);
        return expandedKeywords;
    }
    
    /**
     * 获取与给定关键词形近的关键词列表
     * @param keyword 原始关键词
     * @return 形近关键词列表
     */
    public List<String> getSimilarKeywords(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> similarKeywords = new ArrayList<>();
        
        // 生成形近字替换关键词
        char[] chars = keyword.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            Set<Character> similarChars = SIMILAR_CHARS_MAP.get(c);
            
            if (similarChars != null && !similarChars.isEmpty()) {
                for (char similarChar : similarChars) {
                    char[] newChars = keyword.toCharArray();
                    newChars[i] = similarChar;
                    similarKeywords.add(new String(newChars));
                }
            }
        }
        
        return similarKeywords;
    }
} 