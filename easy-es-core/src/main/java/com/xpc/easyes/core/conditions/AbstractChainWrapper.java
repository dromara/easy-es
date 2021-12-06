package com.xpc.easyes.core.conditions;

import com.xpc.easyes.core.conditions.interfaces.Compare;
import com.xpc.easyes.core.conditions.interfaces.Func;
import com.xpc.easyes.core.conditions.interfaces.Join;
import com.xpc.easyes.core.conditions.interfaces.Nested;

import java.util.Collection;
import java.util.function.Function;

/**
 * 链式
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 处理链式调用
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings({"serial", "unchecked"})
public abstract class AbstractChainWrapper<T, R, Children extends AbstractChainWrapper<T, R, Children, Param>, Param>
        extends Wrapper<T> implements Compare<Children, R>, Join<Children>, Func<Children, R>, Nested<Param, Children> {

    protected final Children typedThis = (Children) this;
    /**
     * 子类所包装的具体 Wrapper 类型
     */
    protected Param wrapperChildren;

    /**
     * 必须的构造函数
     */
    public AbstractChainWrapper() {
    }

    public AbstractWrapper getWrapper() {
        return (AbstractWrapper) wrapperChildren;
    }

    @Override
    public Children eq(boolean condition, R column, Object val, Float boost) {
        getWrapper().eq(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children ne(boolean condition, R column, Object val, Float boost) {
        getWrapper().ne(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children gt(boolean condition, R column, Object val, Float boost) {
        getWrapper().gt(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children ge(boolean condition, R column, Object val, Float boost) {
        getWrapper().ge(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children lt(boolean condition, R column, Object val, Float boost) {
        getWrapper().lt(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children le(boolean condition, R column, Object val, Float boost) {
        getWrapper().le(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children between(boolean condition, R column, Object val1, Object val2, Float boost) {
        getWrapper().between(condition, column, val1, val2, boost);
        return typedThis;
    }

    @Override
    public Children notBetween(boolean condition, R column, Object val1, Object val2, Float boost) {
        getWrapper().notBetween(condition, column, val1, val2, boost);
        return typedThis;
    }

    @Override
    public Children match(boolean condition, R column, Object val, Float boost) {
        getWrapper().match(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children notMatch(boolean condition, R column, Object val, Float boost) {
        getWrapper().notMatch(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children like(boolean condition, R column, Object val, Float boost) {
        getWrapper().like(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children notLike(boolean condition, R column, Object val, Float boost) {
        getWrapper().notLike(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children likeLeft(boolean condition, R column, Object val, Float boost) {
        getWrapper().likeLeft(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children likeRight(boolean condition, R column, Object val, Float boost) {
        getWrapper().likeRight(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children isNull(boolean condition, R column, Float boost) {
        getWrapper().isNull(condition, column, boost);
        return typedThis;
    }

    @Override
    public Children isNotNull(boolean condition, R column, Float boost) {
        getWrapper().isNotNull(condition, column, boost);
        return typedThis;
    }

    @Override
    public Children in(boolean condition, R column, Collection<?> coll, Float boost) {
        getWrapper().in(condition, column, coll, boost);
        return typedThis;
    }

    @Override
    public Children highLight(boolean condition, String preTag, String postTag, R column) {
        getWrapper().highLight(condition, preTag, postTag, column);
        return typedThis;
    }

    @Override
    public Children highLight(String preTag, String postTag, R column) {
        getWrapper().highLight(postTag, postTag, column);
        return typedThis;
    }

    @Override
    public Children highLight(boolean condition, String preTag, String postTag, R... columns) {
        getWrapper().highLight(condition, preTag, postTag, columns);
        return typedThis;
    }

    @Override
    public Children orderBy(boolean condition, boolean isAsc, R... columns) {
        getWrapper().orderBy(condition, isAsc, columns);
        return typedThis;
    }

    @Override
    public Children groupBy(boolean condition, R... columns) {
        getWrapper().groupBy(condition, columns);
        return typedThis;
    }

    @Override
    public Children termsAggregation(boolean condition, String returnName, R column) {
        getWrapper().termsAggregation(condition, returnName, column);
        return typedThis;
    }

    @Override
    public Children avg(boolean condition, String returnName, R column) {
        getWrapper().avg(condition, returnName, column);
        return typedThis;
    }

    @Override
    public Children min(boolean condition, String returnName, R column) {
        getWrapper().min(condition, returnName, column);
        return typedThis;
    }

    @Override
    public Children max(boolean condition, String returnName, R column) {
        getWrapper().max(condition, returnName, column);
        return typedThis;
    }

    @Override
    public Children sum(boolean condition, String returnName, R column) {
        getWrapper().sum(condition, returnName, column);
        return typedThis;
    }

    @Override
    public Children or(boolean condition, Function<Param, Param> func) {
        return null;
    }

    @Override
    public Children notIn(boolean condition, R column, Collection<?> coll, Float boost) {
        getWrapper().notIn(condition, column, coll, boost);
        return typedThis;
    }

    @Override
    public Children and(boolean condition, Function<Param, Param> func) {
        getWrapper().and(condition, func);
        return typedThis;
    }

    @Override
    public Children or(boolean condition) {
        getWrapper().or(condition);
        return typedThis;
    }

    @Override
    public Children match(R column, Object val, Float boost) {
        getWrapper().match(column, val, boost);
        return typedThis;
    }

    @Override
    public Children notMatch(R column, Object val, Float boost) {
        getWrapper().notMatch(column, val, boost);
        return typedThis;
    }

    @Override
    public Children gt(R column, Object val, Float boost) {
        getWrapper().gt(column, val, boost);
        return typedThis;
    }

    @Override
    public Children ge(R column, Object val, Float boost) {
        getWrapper().ge(column, val, boost);
        return typedThis;
    }

    @Override
    public Children lt(R column, Object val, Float boost) {
        getWrapper().lt(column, val, boost);
        return typedThis;
    }

    @Override
    public Children le(R column, Object val, Float boost) {
        getWrapper().le(column, val, boost);
        return typedThis;
    }

    @Override
    public Children between(R column, Object val1, Object val2, Float boost) {
        getWrapper().between(column, val1, val2, boost);
        return typedThis;
    }

    @Override
    public Children notBetween(R column, Object val1, Object val2, Float boost) {
        getWrapper().between(column, val1, val2, boost);
        return typedThis;
    }

    @Override
    public Children like(R column, Object val, Float boost) {
        getWrapper().like(column, val, boost);
        return typedThis;
    }

    @Override
    public Children notLike(R column, Object val, Float boost) {
        getWrapper().notLike(column, val, boost);
        return typedThis;
    }

    @Override
    public Children likeLeft(R column, Object val, Float boost) {
        getWrapper().likeLeft(column, val, boost);
        return typedThis;
    }

    @Override
    public Children likeRight(R column, Object val, Float boost) {
        getWrapper().likeRight(column, val, boost);
        return typedThis;
    }
}
