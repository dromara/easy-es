package org.dromara.easyes.core.toolkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.easyes.common.enums.Link;
import org.dromara.easyes.common.enums.Query;
import org.dromara.easyes.common.params.SFunction;

import java.util.Arrays;
import java.util.Optional;

/**
 * 查询参数工具类
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryUtils {
    /**
     * 左括号
     */
    private final static String OPEN = "(";
    /**
     * 右括号
     */
    private final static String CLOSE = ")";
    /**
     * 冒号
     */
    private final static String COLON = ":";

    /**
     * 封装queryString中的查询条件 比如 a and b or c ...等 方法重载,适用于最后一个条件,后面没有and或or需要拼接了 且查询类型默认为分词匹配
     *
     * @param column 列
     * @param value  值
     * @param <T>    字段所在类泛型
     * @return 查询参数
     */
    public static <T> String buildQueryString(SFunction<T, ?> column, Object value) {
        return buildQueryString(column, value, Query.MATCH, null);
    }

    /**
     * 封装queryString中的查询条件 比如 a and b or c ...等 方法重载,适用于最后一个条件,后面没有and或or需要拼接了
     *
     * @param column 列
     * @param value  值
     * @param query  精确查询还是分词匹配 默认分词匹配
     * @param <T>    字段所在类泛型
     * @return 查询参数
     */
    public static <T> String buildQueryString(SFunction<T, ?> column, Object value, Query query) {
        return buildQueryString(column, value, query, null);
    }

    /**
     * 封装queryString中的查询条件 比如 a and b or c ...等 方法重载,当查询类型为分词匹配时可少传一个参数
     *
     * @param column 列
     * @param value  值
     * @param link   与后面的条件是and 还是or 连接
     * @param <T>    字段所在类泛型
     * @return 查询参数
     */
    public static <T> String buildQueryString(SFunction<T, ?> column, Object value, Link link) {
        return buildQueryString(column, value, Query.MATCH, link);
    }

    /**
     * 封装queryString中的查询条件 比如 a and b or c ...等 适用于动态查询条件 详见官网说明
     *
     * @param column 列
     * @param value  值
     * @param query  精确查询还是分词匹配 默认分词匹配
     * @param link   与后面的条件是and 还是or 连接
     * @param <T>    字段所在类泛型
     * @return 查询参数
     */
    public static <T> String buildQueryString(SFunction<T, ?> column, Object value, Query query, Link link) {
        StringBuilder sb = new StringBuilder();
        sb.append(OPEN)
                .append(FieldUtils.val(column))
                .append(query.getText())
                .append(COLON)
                .append(value)
                .append(CLOSE);
        Optional.ofNullable(link).ifPresent(sb::append);
        return sb.toString();
    }


    /**
     * 合并多个已经构造好的queryString条件 比如我想把 (a="你好") OR (b="你坏") 合并成为((a="你好") OR (b="你坏"))
     * 重载,适用于合并完后面不再追加 and或or的场景
     *
     * @param queryString queryString查询条件
     * @return 合并完成的queryString条件
     */
    public static String combine(String... queryString) {
        return combine(null, queryString);
    }


    /**
     * 合并多个已经构造好的queryString条件 比如我想把 (a="你好") OR (b="你坏") 合并成为((a="你好") OR (b="你坏"))
     *
     * @param link        连接类型
     * @param queryString queryString查询条件
     * @return 合并完成的queryString条件
     */
    public static String combine(Link link, String... queryString) {
        StringBuilder sb = new StringBuilder();
        sb.append(OPEN);
        Arrays.stream(queryString)
                .forEach(sb::append);
        sb.append(CLOSE);
        Optional.ofNullable(link).ifPresent(sb::append);
        return sb.toString();
    }
}
