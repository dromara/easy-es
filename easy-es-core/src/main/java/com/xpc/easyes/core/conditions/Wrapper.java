package com.xpc.easyes.core.conditions;

import org.elasticsearch.action.search.SearchRequest;

/**
 * Lambda表达式的祖宗类
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: Lambda表达式的祖宗类
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public abstract class Wrapper<T> {
    /**
     * 获取查询条件 // TODO 此处后续考虑将核心实现逻辑抽离到此方法中
     *
     * @return
     */
    protected abstract SearchRequest getSearchRequest();
}
