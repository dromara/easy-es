package org.dromara.easyes.core.biz;


import lombok.Data;
import org.elasticsearch.common.settings.Settings;

import java.util.List;
import java.util.Map;

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
     * 分片数
     */
    private Integer shardsNum;
    /**
     * 副本数
     */
    private Integer replicasNum;
    /**
     * 最大返回数
     */
    private Integer maxResultWindow;
    /**
     * 索引字段及类型分词器等信息
     */
    private List<EsIndexParam> esIndexParamList;
    /**
     * 用户手动指定的mapping信息,优先级最高
     */
    private Map<String, Object> mapping;
    /**
     * 用户通过自定义注解指定的settings信息
     */
    private Map<String, Object> settingsMap;
    /**
     * 用户手动指定的settings信息,优先级最高
     */
    private Settings settings;
}
