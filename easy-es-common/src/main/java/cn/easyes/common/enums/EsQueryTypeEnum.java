package cn.easyes.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.elasticsearch.index.query.RangeQueryBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * 查询类型枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@AllArgsConstructor
public enum EsQueryTypeEnum {
    /**
     * 精确值匹配 相当于MySQL 等于
     */
    TERM_QUERY(1),
    /**
     * 精确值列表匹配 相当于mysql in
     */
    TERMS_QUERY(2),
    /**
     * 模糊匹配 分词 相当于mysql like
     */
    MATCH_QUERY(3),
    /**
     * 范围查询
     * <p>
     * 范围查询内部使用{@link RangeQueryBuilder}
     * <h1>如果是对于日期类型的比较，进行说明:</h1>
     * <ul>
     *     <li>1、value支持: 字符串{@link String}、日期{@link Date}、日期{@link LocalDate}、日期时间{@link LocalDateTime}、带有时区的日期时间{@link ZonedDateTime}</li>
     *     <li>2、对于字符串类型，可以使用{@link RangeQueryBuilder#format(String)}方法进行格式化， 字符串和format格式必须匹配。</li>
     *     <li>2-1、如果，format为空：会使用，创建索引的mapper指定格式化方式，如：<code>{"gmt_create": {"type": "date","format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"；}}</code></li>
     *     <li>2-2、如果，创建索引的mapper中也未指定format,会使用：<code>"strict_date_optional_time||epoch_millis"</code> </li>
     *     <li>2-2-1、strict_date_optional_time：中文含义，严格日期可选时间，即：日期必须有，时间可选；ISO datetime parser 可以正常解析的都支持，种类非常丰富;</li>
     *     <li>2-2-2、epoch_millis： epoch 以来的毫秒数，即：1970.1.1 零点到现在的毫秒数</li>
     *     <li>2-2-1-1、ISO dateOptionalTimeParser: https://www.joda.org/joda-time/apidocs/org/joda/time/format/ISODateTimeFormat.html#dateOptionalTimeParser-- </li>
     *     <li>2-2-1-2、如：yyyy-MM-dd、yyyy-MM-dd HH:mm:ss、yyyy-MM-dd HH:mm:ss.SSS、yyyy-MM-dd'T'HH:mm:ssZ、yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
     *     <li>2-3、如果value是：日期{@link Date}、java8日期/日期时间{@link java.time}，format可以全指定为:yyyy-MM-dd'T'HH:mm:ss.SSSz</li>
     *     <li>3、value: 中字符串未包含时区，或者是：未包含时区的日期对象，需要手工指定日期的时区，不指定就是UTC(0时区的日期)，通过{@link RangeQueryBuilder#timeZone(String)}指定</li>
     *     <li>3-1: ZoneId.of("UTC").toString() 0时区、ZoneId.of("Asia/Shanghai").toString() 东八区、ZoneId.of("Europe/Paris").toString()东一区</li>
     *     <li>3-2: 我们一般应该使用，东八区，Asia/Shanghai</li>
     * </ul>
     */
    RANGE_QUERY(4),
    /**
     * 区间查询,特殊的RANGE_QUERY 相当于mysql between
     */
    INTERVAL_QUERY(5),
    /**
     * 存在查询 相当于Mysql中的 is null,not null这种查询类型
     */
    EXISTS_QUERY(6),
    /**
     * 聚合查询 相当于mysql中的 group by 当然 不仅限于group by 还新增了 sum,avg,max,min等功能
     */
    AGGREGATION_QUERY(7),
    /**
     * 通配,相当于mysql中的like
     */
    WILDCARD_QUERY(8),
    /**
     * 分词匹配 需要结果中也包含所有的分词，且顺序一样
     */
    MATCH_PHRASE(9),
    /**
     * 查询全部文档 相当于select all
     */
    MATCH_ALL_QUERY(10),
    /**
     * 前缀匹配
     */
    MATCH_PHRASE_PREFIX(11),
    /**
     * 多字段匹配
     */
    MULTI_MATCH_QUERY(12),
    /**
     * 所有字段中搜索
     */
    QUERY_STRING_QUERY(13),
    /**
     * 前缀匹配搜索
     */
    PREFIX_QUERY(14);

    /**
     * 类型
     */
    @Getter
    private Integer type;
}
