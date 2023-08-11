package org.dromara.easyes.annotation;


import org.dromara.easyes.annotation.rely.Analyzer;
import org.dromara.easyes.annotation.rely.FieldType;

/**
 * 内部字段索引注解
 * <p>
 *
 * @author yinlei
 **/
public @interface InnerIndexField {
    /**
     * 内部字段的后缀,必须指定,否则无存在意义,且不可重名,重名则以首次出现的为准
     *
     * @return 内部字段的后缀
     */
    String suffix();

    /**
     * 内部字段在es索引中的类型,必须指定,否则无存在意义
     *
     * @return 类型
     */
    FieldType fieldType();

    /**
     * 内部字段索引文档时用的分词器
     *
     * @return 分词器
     */
    String analyzer() default Analyzer.NONE;

    /**
     * 内部字段查询分词器
     *
     * @return 分词器
     */
    String searchAnalyzer() default Analyzer.NONE;

    /**
     * 内部字段长度超过ignore_above设置的字符串将不会被索引或存储 keyword_text默认值为256
     *
     * @return 索引内部字段最大长度
     */
    int ignoreAbove() default -1;
}
