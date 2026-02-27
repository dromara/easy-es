package org.dromara.easyes.core.biz;


import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import lombok.Data;

import java.util.List;

/**
 * 创建索引参数
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
public class CreateIndexParam {
    /**
     * 实体类
     */
    private Class<?> entityClass;
    /**
     * 索引名
     */
    private String indexName;
    /**
     * 别名
     */
    private String aliasName;
    /**
     * 索引字段及类型分词器等信息
     */
    private List<EsIndexParam> esIndexParamList;
    /**
     * 用户手动指定的mapping信息,优先级最高
     */
    private TypeMapping.Builder mapping;
    /**
     * 用户通过自定义注解指定的settings信息
     */
    private IndexSettings indexSettings;
    /**
     * 用户手动指定的settings信息,优先级最高
     */
    private IndexSettings.Builder settings;
}
