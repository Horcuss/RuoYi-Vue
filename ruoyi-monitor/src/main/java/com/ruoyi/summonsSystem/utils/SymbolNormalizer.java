package com.ruoyi.monitor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 符号标准化工具类
 * 将各种语言的符号（中文、日文、全角等）统一转换为英文半角符号
 * 
 * @author ruoyi
 * @date 2025-01-15
 */
public class SymbolNormalizer 
{
    private static final Logger log = LoggerFactory.getLogger(SymbolNormalizer.class);
    
    /**
     * 符号映射表：各种语言的符号 → 英文符号
     */
    private static final Map<String, String> SYMBOL_MAP = new HashMap<>();
    
    static
    {
        // Brackets - Chinese
        SYMBOL_MAP.put("\uFF08", "(");  // （
        SYMBOL_MAP.put("\uFF09", ")");  // ）
        SYMBOL_MAP.put("\u3010", "[");  // 【
        SYMBOL_MAP.put("\u3011", "]");  // 】
        SYMBOL_MAP.put("\uFF5B", "{");  // ｛
        SYMBOL_MAP.put("\uFF5D", "}");  // ｝
        SYMBOL_MAP.put("\u3014", "(");  // 〔
        SYMBOL_MAP.put("\u3015", ")");  // 〕
        SYMBOL_MAP.put("\u3008", "<");  // 〈
        SYMBOL_MAP.put("\u3009", ">");  // 〉
        SYMBOL_MAP.put("\u300A", "<");  // 《
        SYMBOL_MAP.put("\u300B", ">");  // 》
        SYMBOL_MAP.put("\u300C", "[");  // 「
        SYMBOL_MAP.put("\u300D", "]");  // 」
        SYMBOL_MAP.put("\u300E", "[");  // 『
        SYMBOL_MAP.put("\u300F", "]");  // 』
        SYMBOL_MAP.put("\uFF3B", "[");  // ［
        SYMBOL_MAP.put("\uFF3D", "]");  // ］

        // Operators - Fullwidth
        SYMBOL_MAP.put("\uFF0B", "+");  // ＋
        SYMBOL_MAP.put("\uFF0D", "-");  // －
        SYMBOL_MAP.put("\u00D7", "*");  // ×
        SYMBOL_MAP.put("\u2715", "*");  // ✕
        SYMBOL_MAP.put("\u2716", "*");  // ✖
        SYMBOL_MAP.put("\u2A2F", "*");  // ⨯
        SYMBOL_MAP.put("\u00F7", "/");  // ÷
        SYMBOL_MAP.put("\uFF0F", "/");  // ／
        SYMBOL_MAP.put("\uFF0A", "*");  // ＊
        SYMBOL_MAP.put("\uFF1D", "=");  // ＝

        // Math symbols
        SYMBOL_MAP.put("\u00B7", "*");  // ·
        SYMBOL_MAP.put("\u2022", "*");  // •
        SYMBOL_MAP.put("\u2219", "*");  // ∙
        SYMBOL_MAP.put("\u22C5", "*");  // ⋅

        // Punctuation - Chinese/Fullwidth
        SYMBOL_MAP.put("\uFF0C", ",");  // ，
        SYMBOL_MAP.put("\u3002", ".");  // 。
        SYMBOL_MAP.put("\u3001", ",");  // 、
        SYMBOL_MAP.put("\uFF1B", ";");  // ；
        SYMBOL_MAP.put("\uFF1A", ":");  // ：
        SYMBOL_MAP.put("\uFF1F", "?");  // ？
        SYMBOL_MAP.put("\uFF01", "!");  // ！
        SYMBOL_MAP.put("\uFF02", "\""); // ＂
        SYMBOL_MAP.put("\uFF07", "'");  // ＇
        SYMBOL_MAP.put("\uFF40", "`");  // ｀
        SYMBOL_MAP.put("\uFF20", "@");  // ＠
        SYMBOL_MAP.put("\uFF03", "#");  // ＃
        SYMBOL_MAP.put("\uFF04", "$");  // ＄
        SYMBOL_MAP.put("\uFF05", "%");  // ％
        SYMBOL_MAP.put("\uFF3E", "^");  // ＾
        SYMBOL_MAP.put("\uFF06", "&");  // ＆
        SYMBOL_MAP.put("\uFF3F", "_");  // ＿
        SYMBOL_MAP.put("\uFF5C", "|");  // ｜
        SYMBOL_MAP.put("\uFF5E", "~");  // ～

        // Quotes
        SYMBOL_MAP.put("\u201C", "\""); // "
        SYMBOL_MAP.put("\u201D", "\""); // "
        SYMBOL_MAP.put("\u2018", "'");  // '
        SYMBOL_MAP.put("\u2019", "'");  // '

        // Spaces
        SYMBOL_MAP.put("\u3000", " ");  // 　(fullwidth space)

        // Dashes
        SYMBOL_MAP.put("\u2014", "-");  // —
        SYMBOL_MAP.put("\u2013", "-");  // –
        SYMBOL_MAP.put("\u2015", "-");  // ―

        // Ellipsis
        SYMBOL_MAP.put("\u2026", "..."); // …
        SYMBOL_MAP.put("\u2025", ".."); // ‥
    }
    
    /**
     * 标准化表达式中的符号
     * 将所有非英文符号转换为英文符号
     * 
     * @param expression 原始表达式
     * @return 标准化后的表达式
     */
    public static String normalize(String expression) 
    {
        if (expression == null || expression.isEmpty()) 
        {
            return expression;
        }
        
        String original = expression;
        String normalized = expression;
        
        // 遍历符号映射表，逐个替换
        for (Map.Entry<String, String> entry : SYMBOL_MAP.entrySet()) 
        {
            normalized = normalized.replace(entry.getKey(), entry.getValue());
        }
        
        // 如果发生了转换，记录日志
        if (!original.equals(normalized)) 
        {
            log.info("符号标准化: \"{}\" → \"{}\"", original, normalized);
        }
        
        return normalized;
    }
    
    /**
     * 标准化表达式中的符号（详细模式）
     * 返回标准化结果和转换详情
     * 
     * @param expression 原始表达式
     * @return 标准化结果对象
     */
    public static NormalizeResult normalizeWithDetails(String expression) 
    {
        if (expression == null || expression.isEmpty()) 
        {
            return new NormalizeResult(expression, false, new HashMap<>());
        }
        
        String original = expression;
        String normalized = expression;
        Map<String, Integer> replacements = new HashMap<>();
        
        // 遍历符号映射表，逐个替换并记录
        for (Map.Entry<String, String> entry : SYMBOL_MAP.entrySet()) 
        {
            String from = entry.getKey();
            String to = entry.getValue();
            
            // 统计出现次数
            int count = 0;
            int index = 0;
            while ((index = normalized.indexOf(from, index)) != -1) 
            {
                count++;
                index += from.length();
            }
            
            if (count > 0) 
            {
                normalized = normalized.replace(from, to);
                replacements.put(from + " → " + to, count);
            }
        }
        
        boolean changed = !original.equals(normalized);
        
        return new NormalizeResult(normalized, changed, replacements);
    }
    
    /**
     * 标准化结果类
     */
    public static class NormalizeResult 
    {
        private final String normalized;
        private final boolean changed;
        private final Map<String, Integer> replacements;
        
        public NormalizeResult(String normalized, boolean changed, Map<String, Integer> replacements) 
        {
            this.normalized = normalized;
            this.changed = changed;
            this.replacements = replacements;
        }
        
        public String getNormalized() 
        {
            return normalized;
        }
        
        public boolean isChanged() 
        {
            return changed;
        }
        
        public Map<String, Integer> getReplacements() 
        {
            return replacements;
        }
        
        @Override
        public String toString() 
        {
            if (!changed) 
            {
                return "无需转换";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("转换详情: ");
            for (Map.Entry<String, Integer> entry : replacements.entrySet()) 
            {
                sb.append(entry.getKey()).append("(").append(entry.getValue()).append("次) ");
            }
            return sb.toString();
        }
    }
    
    /**
     * 检查字符串是否包含非英文符号
     * 
     * @param text 待检查的文本
     * @return true表示包含非英文符号
     */
    public static boolean containsNonEnglishSymbols(String text) 
    {
        if (text == null || text.isEmpty()) 
        {
            return false;
        }
        
        for (String symbol : SYMBOL_MAP.keySet()) 
        {
            if (text.contains(symbol)) 
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取所有支持的符号映射
     * 
     * @return 符号映射表的副本
     */
    public static Map<String, String> getSupportedSymbols() 
    {
        return new HashMap<>(SYMBOL_MAP);
    }
}

