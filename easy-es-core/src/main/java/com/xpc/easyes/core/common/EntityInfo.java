package com.xpc.easyes.core.common;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.xpc.easyes.core.constants.BaseEsConstants;
import com.xpc.easyes.core.enums.IdType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 实体类信息
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@Accessors(chain = true)
public class EntityInfo {
    /**
     * 表主键ID 类型
     */
    private IdType idType = IdType.NONE;
    /**
     * id数据类型 如Long.class String.class
     */
    private Class<?> idClass;
    /**
     * 索引名称(原索引名)
     */
    private String indexName;
    /**
     * 新索引名(由EE在更新索引时自动创建)
     */
    private String releaseIndexName;
    /***
     * 重试成功的索引名
     */
    private String retrySuccessIndexName;
    /**
     * 表映射结果集
     */
    private String resultMap;
    /**
     * 主键字段
     */
    private Field keyField;
    /**
     * 表主键ID 属性名
     */
    private String keyProperty;
    /**
     * 分片数 默认为1
     */
    private Integer shardsNum = BaseEsConstants.ONE;
    /**
     * 副本数 默认为1
     */
    private Integer replicasNum = BaseEsConstants.ONE;
    /**
     * 索引别名
     */
    private String aliasName;
    /**
     * 路由字段
     */
    private Field routingField;
    /**
     * 表字段信息列表
     */
    private List<EntityFieldInfo> fieldList;
    /**
     * 标记该字段属于哪个类
     */
    private Class<?> clazz;
    /**
     * fastjson 字段命名策略
     */
    private PropertyNamingStrategy propertyNamingStrategy;
    /**
     * fastjson 实体中不存在的字段处理器
     */
    private ExtraProcessor extraProcessor;
    /**
     * 实体字段->高亮返回结果 键值对
     */
    private final Map<String, String> highlightFieldMap = new HashMap<>();
    /**
     * 实体字段->es实际字段映射
     */
    private final Map<String, String> mappingColumnMap = new HashMap<>();
    /**
     * es实际字段映射->实体字段 (仅包含被重命名字段)
     */
    private final Map<String, String> columnMappingMap = new HashMap<>();
    /**
     * 不需要序列化JSON的字段 不存在字段,高亮字段等
     */
    private final Set<String> notSerializeField = new HashSet<>();
    /**
     * 嵌套类不需要序列化JSON的字段 不存在字段,高亮字段等
     */
    private final Map<Class<?>, Set<String>> nestedNotSerializeField = new HashMap<>();
    /**
     * 嵌套类型 path和class对应关系
     */
    private final Map<String, Class<?>> pathClassMap = new HashMap<>();
    /**
     * 嵌套类型 实体字段->es实际字段映射
     */
    private final Map<Class<?>, Map<String, String>> nestedClassMappingColumnMap = new HashMap<>();
    /**
     * 嵌套类型 es实际字段映射->实体字段 (仅包含被重命名字段)
     */
    private final Map<Class<?>, Map<String, String>> nestedClassColumnMappingMap = new HashMap<>();
    /**
     * fastjson 过滤器
     */
    private final Map<Class<?>, List<SerializeFilter>> classSimplePropertyPreFilterMap = new HashMap<>();

    /**
     * 获取需要进行查询的字段列表
     *
     * @param predicate 预言
     * @return 查询字段列表
     */
    public List<String> chooseSelect(Predicate<EntityFieldInfo> predicate) {
        return fieldList.stream()
                .filter(predicate)
                .map(EntityFieldInfo::getColumn)
                .collect(Collectors.toList());
    }

    /**
     * 获取实体字段映射es中的字段名
     *
     * @param column 字段名
     * @return es中的字段名
     */
    public String getMappingColumn(String column) {
        return Optional.ofNullable(mappingColumnMap.get(column))
                .orElse(column);
    }

    /**
     * 获取全部嵌套类
     *
     * @return 嵌套类集合
     */
    public Set<Class<?>> getAllNestedClass() {
        return nestedClassColumnMappingMap.keySet();
    }

    /**
     * 根据path获取嵌套类字段关系map
     *
     * @param path 路径
     * @return 字段关系map
     */
    public Map<String, String> getNestedMappingColumnMapByPath(String path) {
        return Optional.ofNullable(pathClassMap.get(path))
                .map(nestedClassMappingColumnMap::get)
                .orElse(new HashMap<>(0));
    }


}
