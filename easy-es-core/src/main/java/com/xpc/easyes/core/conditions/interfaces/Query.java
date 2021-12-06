package com.xpc.easyes.core.conditions.interfaces;

import com.xpc.easyes.core.common.EntityFieldInfo;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * 查询相关
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 查询的一些字段, 起止都在此完成封装
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Query<Children, T, R> extends Serializable {

    /**
     * 设置查询字段
     *
     * @param columns 字段数组
     * @return children
     */
    Children select(R... columns);

    /**
     * ignore
     * <p>注意只有内部有 entity 才能使用该方法</p>
     *
     * @param predicate
     * @return
     */
    Children select(Predicate<EntityFieldInfo> predicate);

    /**
     * 过滤查询的字段信息(主键除外!)
     * <p>例1: 只要 java 字段名以 "test" 开头的             -> select(i -> i.getProperty().startsWith("test"))</p>
     * <p>例2: 只要 java 字段属性是 CharSequence 类型的     -> select(TableFieldInfo::isCharSequence)</p>
     * <p>例3: 只要 java 字段没有填充策略的                 -> select(i -> i.getFieldFill() == FieldFill.DEFAULT)</p>
     * <p>例4: 要全部字段                                   -> select(i -> true)</p>
     * <p>例5: 只要主键字段                                 -> select(i -> false)</p>
     *
     * @param entityClass 要查询的类的类型
     * @param predicate   过滤方式
     * @return children
     */
    Children select(Class<T> entityClass, Predicate<EntityFieldInfo> predicate);

    /**
     * 设置不查询字段
     *
     * @param columns
     * @return
     */
    Children notSelect(R... columns);

    /**
     * 从第几条数据开始查询
     *
     * @param from
     * @return
     */
    Children from(Integer from);

    /**
     * 总共查询多少条数据
     *
     * @param size
     * @return
     */
    Children size(Integer size);


}
