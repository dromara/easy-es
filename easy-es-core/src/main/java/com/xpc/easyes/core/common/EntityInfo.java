package com.xpc.easyes.core.common;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.xpc.easyes.core.enums.IdType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
     * 索引名称
     */
    private String indexName;
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
     * 表主键ID 字段名
     */
    private String keyColumn;
    /**
     * 表字段信息列表
     */
    private List<EntityFieldInfo> fieldList;
    /**
     * 标记该字段属于哪个类
     */
    private Class<?> clazz;
    /**
     * 是否有id注解
     */
    private Boolean hasIdAnnotation;

    /**
     * fastjson字段名称过滤器
     */
    private SerializeFilter serializeFilter;

    private PropertyNamingStrategy propertyNamingStrategy;

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
     * 获取id字段名
     *
     * @return id字段名
     */
    public String getId() {
        return keyColumn;
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
}
