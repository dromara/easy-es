package com.xpc.easyes.core.conditions.interfaces;

import com.xpc.easyes.core.constants.BaseEsConstants;

import java.io.Serializable;

/**
 * 比较相关
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 比较相关的查询参数封装都在此
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Compare<Children, R> extends Serializable {
    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children eq(R column, Object val) {
        return eq(true, column, val);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children eq(R column, Object val, Float boost) {
        return eq(true, column, val, boost);
    }

    /**
     * ignore
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    default Children eq(boolean condition, R column, Object val) {
        return eq(condition, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 等于 =
     *
     * @param condition
     * @param column
     * @param val
     * @param boost
     * @return
     */
    Children eq(boolean condition, R column, Object val, Float boost);


    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children ne(R column, Object val) {
        return ne(true, column, val);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children ne(R column, Object val, Float boost) {
        return ne(true, column, val, boost);
    }

    /**
     * ignore
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    default Children ne(boolean condition, R column, Object val) {
        return ne(condition, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 不等于 &lt;&gt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @param boost     权重
     * @return
     */
    Children ne(boolean condition, R column, Object val, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children match(R column, Object val) {
        return match(true, column, val);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children match(R column, Object val, Float boost) {
        return match(true, column, val, boost);
    }

    /**
     * ignore
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    default Children match(boolean condition, R column, Object val) {
        return match(condition, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * match 分词匹配
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @param boost     权重
     * @return children
     */
    Children match(boolean condition, R column, Object val, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children notMatch(R column, Object val) {
        return notMatch(true, column, val);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children notMatch(R column, Object val, Float boost) {
        return notMatch(true, column, val, boost);
    }

    /**
     * ignore
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    default Children notMatch(boolean condition, R column, Object val) {
        return notMatch(condition, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * NOT MATCH 分词不匹配
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @param boost     权重
     * @return children
     */
    Children notMatch(boolean condition, R column, Object val, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children gt(R column, Object val) {
        return gt(true, column, val);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children gt(R column, Object val, Float boost) {
        return gt(true, column, val, boost);
    }

    /**
     * ignore
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    default Children gt(boolean condition, R column, Object val) {
        return gt(condition, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 大于 &gt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @param boost     权重
     * @return children
     */
    Children gt(boolean condition, R column, Object val, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children ge(R column, Object val) {
        return ge(true, column, val);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children ge(R column, Object val, Float boost) {
        return ge(true, column, val, boost);
    }

    /**
     * 大于等于 &gt;=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    default Children ge(boolean condition, R column, Object val) {
        return ge(condition, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 大于等于 &gt;=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @param boost     权重
     * @return children
     */
    Children ge(boolean condition, R column, Object val, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children lt(R column, Object val) {
        return lt(true, column, val);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children lt(R column, Object val, Float boost) {
        return lt(true, column, val, boost);
    }

    /**
     * 小于 &lt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    default Children lt(boolean condition, R column, Object val) {
        return lt(condition, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 小于 &lt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @param boost     权重
     * @return children
     */
    Children lt(boolean condition, R column, Object val, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children le(R column, Object val) {
        return le(true, column, val);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children le(R column, Object val, Float boost) {
        return le(true, column, val, boost);
    }

    /**
     * 小于等于 &lt;=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    default Children le(boolean condition, R column, Object val) {
        return le(condition, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 小于等于 &lt;=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @param boost     权重
     * @return children
     */
    Children le(boolean condition, R column, Object val, Float boost);


    /**
     * ignore
     *
     * @param column
     * @param val1
     * @param val2
     * @return
     */
    default Children between(R column, Object val1, Object val2) {
        return between(true, column, val1, val2);
    }

    /**
     * ignore
     *
     * @param column
     * @param val1
     * @param val2
     * @param boost
     * @return
     */
    default Children between(R column, Object val1, Object val2, Float boost) {
        return between(true, column, val1, val2, boost);
    }

    /**
     * BETWEEN 值1 AND 值2
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @return children
     */
    default Children between(boolean condition, R column, Object val1, Object val2) {
        return between(condition, column, val1, val2, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * BETWEEN 值1 AND 值2
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @param boost     权重
     * @return children
     */
    Children between(boolean condition, R column, Object val1, Object val2, Float boost);


    /**
     * ignore
     *
     * @param column
     * @param val1
     * @param val2
     * @return
     */
    default Children notBetween(R column, Object val1, Object val2) {
        return notBetween(true, column, val1, val2);
    }

    /**
     * ignore
     *
     * @param column
     * @param val1
     * @param val2
     * @param boost
     * @return
     */
    default Children notBetween(R column, Object val1, Object val2, Float boost) {
        return notBetween(true, column, val1, val2, boost);
    }

    /**
     * NOT BETWEEN 值1 AND 值2
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @return children
     */
    default Children notBetween(boolean condition, R column, Object val1, Object val2) {
        return notBetween(condition, column, val1, val2, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * NOT BETWEEN 值1 AND 值2
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @param boost     权重
     * @return children
     */
    Children notBetween(boolean condition, R column, Object val1, Object val2, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children like(R column, Object val) {
        return like(true, column, val);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children like(R column, Object val, Float boost) {
        return like(true, column, val, boost);
    }

    /**
     * ignore
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    default Children like(boolean condition, R column, Object val) {
        return like(condition, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * like %xx%
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @param boost     权重
     * @return children
     */
    Children like(boolean condition, R column, Object val, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children notLike(R column, Object val) {
        return notLike(true, column, val);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children notLike(R column, Object val, Float boost) {
        return notLike(true, column, val, boost);
    }

    /**
     * ignore
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    default Children notLike(boolean condition, R column, Object val) {
        return notLike(condition, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * NOT LIKE
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @param boost     权重
     * @return children
     */
    Children notLike(boolean condition, R column, Object val, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children likeLeft(R column, Object val) {
        return likeLeft(true, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children likeLeft(R column, Object val, Float boost) {
        return likeLeft(true, column, val, boost);
    }

    /**
     * LIKE '%值'
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @param boost     权重
     * @return children
     */
    Children likeLeft(boolean condition, R column, Object val, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children likeRight(R column, Object val) {
        return likeRight(true, column, val, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * ignore
     *
     * @param column
     * @param val
     * @param boost
     * @return
     */
    default Children likeRight(R column, Object val, Float boost) {
        return likeRight(true, column, val, boost);
    }

    /**
     * LIKE '值%'
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @param boost     权重
     * @return children
     */
    Children likeRight(boolean condition, R column, Object val, Float boost);
}
