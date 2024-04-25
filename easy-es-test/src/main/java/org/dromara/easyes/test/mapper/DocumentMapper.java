package org.dromara.easyes.test.mapper;

import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.kernel.BaseEsMapper;
import org.dromara.easyes.core.kernel.EsWrappers;
import org.dromara.easyes.test.entity.Document;

import java.util.List;

/**
 * mapper 相当于Mybatis-plus的mapper
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface DocumentMapper extends BaseEsMapper<Document> {
    /**
     * 演示mapper中添加default方法
     *
     * @return document列表
     */
    default List<Document> testDefaultMethod() {
        LambdaEsQueryWrapper<Document> wrapper = EsWrappers.lambdaQuery(Document.class)
                .eq(Document::getTitle, "测试文档4").match(Document::getContent, "内容");
        return selectList(wrapper);
    }

}
