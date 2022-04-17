package com.xpc.easyes.core.toolkit;

import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.alibaba.fastjson.serializer.NameFilter;
import com.xpc.easyes.core.anno.HighLightMappingField;
import com.xpc.easyes.core.anno.TableField;
import com.xpc.easyes.core.anno.TableId;
import com.xpc.easyes.core.anno.TableName;
import com.xpc.easyes.core.cache.BaseCache;
import com.xpc.easyes.core.cache.GlobalConfigCache;
import com.xpc.easyes.core.common.EntityFieldInfo;
import com.xpc.easyes.core.common.EntityInfo;
import com.xpc.easyes.core.config.GlobalConfig;
import com.xpc.easyes.core.enums.IdType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

/**
 * 实体字段信息工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class EntityInfoHelper {
    /**
     * 储存反射类表信息
     */
    private static final Map<Class<?>, EntityInfo> ENTITY_INFO_CACHE = new ConcurrentHashMap<>();
    /**
     * 默认主键名称
     */
    private static final String DEFAULT_ID_NAME = "id";
    /**
     * Es 默认的主键名称
     */
    private static final String DEFAULT_ES_ID_NAME = "_id";

    /**
     * 获取实体映射表信息
     *
     * @param clazz 类
     * @return 实体字段信息
     */
    public static EntityInfo getEntityInfo(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        EntityInfo entityInfo = ENTITY_INFO_CACHE.get(ClassUtils.getUserClass(clazz));
        if (null != entityInfo) {
            return entityInfo;
        }
        // 尝试获取父类缓存
        Class currentClass = clazz;
        while (null == entityInfo && Object.class != currentClass) {
            currentClass = currentClass.getSuperclass();
            entityInfo = ENTITY_INFO_CACHE.get(ClassUtils.getUserClass(currentClass));
        }
        if (entityInfo != null) {
            ENTITY_INFO_CACHE.put(ClassUtils.getUserClass(clazz), entityInfo);
        }

        // 缓存中未获取到,则初始化
        GlobalConfig globalConfig = GlobalConfigCache.getGlobalConfig();
        return initTableInfo(globalConfig, clazz);
    }

    /**
     * 获取所有实体映射表信息
     *
     * @return 所有实体映射表信息
     */
    public static List<EntityInfo> getTableInfos() {
        return new ArrayList<>(ENTITY_INFO_CACHE.values());
    }

    /**
     * 实体类反射获取表信息 初始化
     *
     * @param globalConfig 全局配置
     * @param clazz        类
     * @return 实体信息
     */
    public synchronized static EntityInfo initTableInfo(GlobalConfig globalConfig, Class<?> clazz) {
        EntityInfo entityInfo = ENTITY_INFO_CACHE.get(clazz);
        if (entityInfo != null) {
            return entityInfo;
        }

        // 没有获取到缓存信息,则初始化
        entityInfo = new EntityInfo();
        // 初始化表名(索引名)相关
        initTableName(clazz, globalConfig, entityInfo);
        // 初始化字段相关
        initTableFields(clazz, globalConfig, entityInfo);

        // 放入缓存
        ENTITY_INFO_CACHE.put(clazz, entityInfo);
        return entityInfo;
    }


    /**
     * 初始化 表主键,表字段
     *
     * @param clazz        类
     * @param globalConfig 全局配置
     * @param entityInfo   实体信息
     */
    public static void initTableFields(Class<?> clazz, GlobalConfig globalConfig, EntityInfo entityInfo) {
        // 数据库全局配置
        GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();
        List<Field> list = getAllFields(clazz);
        // 标记是否读取到主键
        boolean isReadPK = false;
        // 是否存在 @TableId 注解
        boolean existTableId = isExistTableId(list);

        List<EntityFieldInfo> fieldList = new ArrayList<>();
        for (Field field : list) {
            // 主键ID 初始化
            if (!isReadPK) {
                if (existTableId) {
                    isReadPK = initTableIdWithAnnotation(dbConfig, entityInfo, field, clazz);
                } else {
                    isReadPK = initTableIdWithoutAnnotation(dbConfig, entityInfo, field, clazz);
                }
                if (isReadPK) {
                    continue;
                }
            }

            // 有 @TableField 等已知自定义注解的字段初始化
            if (initTableFieldWithAnnotation(dbConfig, fieldList, field, entityInfo)) {
                continue;
            }

            // 无 @TableField 等已知自定义注解的字段初始化
            initTableFieldWithoutAnnotation(dbConfig, fieldList, field, entityInfo);
        }

        // 字段列表
        entityInfo.setFieldList(fieldList);

        // 添加fastjson NameFilter
        addNameFilter(entityInfo);

        // 添加fastjson ExtraProcessor
        addExtraProcessor(entityInfo);
    }

    /**
     * 预添加fastjson解析object时对非实体类字段的处理(比如自定义字段名,下划线等)
     *
     * @param entityInfo 实体信息
     */
    private static void addExtraProcessor(EntityInfo entityInfo) {
        Map<String, String> columnMappingMap = entityInfo.getColumnMappingMap();
        ExtraProcessor extraProcessor = (object, key, value) ->
                Optional.ofNullable(columnMappingMap.get(key))
                        .flatMap(realMethodName -> Optional.ofNullable(BaseCache.setterMethod(entityInfo.getClazz(), realMethodName)))
                        .ifPresent(method -> {
                            try {
                                method.invoke(object, value);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        });
        entityInfo.setExtraProcessor(extraProcessor);
    }

    /**
     * 添加fastjson字段过滤器
     *
     * @param entityInfo 实体信息
     */
    private static void addNameFilter(EntityInfo entityInfo) {
        Map<String, String> mappingColumnMap = entityInfo.getMappingColumnMap();
        if (!mappingColumnMap.isEmpty()) {
            NameFilter nameFilter = (object, name, value) -> {
                String mappingColumn = mappingColumnMap.get(name);
                if (Objects.equals(mappingColumn, name)) {
                    return name;
                }
                return mappingColumn;
            };
            entityInfo.setSerializeFilter(nameFilter);
        }
    }

    /**
     * 字段属性初始化
     *
     * @param dbConfig   索引配置
     * @param fieldList  字段列表
     * @param field      字段
     * @param entityInfo 实体信息
     * @return
     */
    private static boolean initTableFieldWithAnnotation(GlobalConfig.DbConfig dbConfig,
                                                        List<EntityFieldInfo> fieldList, Field field, EntityInfo entityInfo) {
        boolean hasAnnotation = false;

        // 获取已知自定义注解
        HighLightMappingField highLightMappingField = field.getAnnotation(HighLightMappingField.class);
        TableField tableField = field.getAnnotation(TableField.class);

        if (Objects.nonNull(tableField) && tableField.exist()) {
            // 存在字段处理
            EntityFieldInfo entityFieldInfo = new EntityFieldInfo(dbConfig, field, tableField);

            // 自定义字段名及驼峰下划线转换
            String mappingColumn;
            if (!StringUtils.isBlank(tableField.value().trim())) {
                // 自定义注解指定的名称优先级最高
                entityInfo.getMappingColumnMap().putIfAbsent(field.getName(), tableField.value());
                entityInfo.getColumnMappingMap().putIfAbsent(tableField.value(), field.getName());
                mappingColumn = tableField.value();
            } else {
                // 下划线驼峰
                mappingColumn = initMappingColumnMapAndGet(dbConfig, entityInfo, field);
            }
            entityFieldInfo.setMappingColumn(mappingColumn);
            entityFieldInfo.setAnalyzer(tableField.analyzer());
            entityFieldInfo.setSearchAnalyzer(tableField.searchAnalyzer());
            entityFieldInfo.setFieldType(tableField.fieldType());
            entityFieldInfo.setColumnType(field.getType().getSimpleName());
            fieldList.add(entityFieldInfo);
            hasAnnotation = true;
        }

        if (Objects.nonNull(highLightMappingField) && StringUtils.isNotBlank(highLightMappingField.value())) {
            // 高亮映射字段处理
            String customField = entityInfo.getMappingColumnMap().get(highLightMappingField.value());
            String realHighLightField = Objects.isNull(customField) ? highLightMappingField.value() : customField;
            if (dbConfig.isMapUnderscoreToCamelCase()) {
                realHighLightField = StringUtils.camelToUnderline(realHighLightField);
            }
            entityInfo.getHighlightFieldMap().putIfAbsent(realHighLightField, field.getName());
            hasAnnotation = true;
        }

        return hasAnnotation;
    }

    /**
     * 字段属性初始化
     *
     * @param dbConfig   索引配置
     * @param fieldList  字段列表
     * @param field      字段
     * @param entityInfo 实体信息
     */
    private static void initTableFieldWithoutAnnotation(GlobalConfig.DbConfig dbConfig,
                                                        List<EntityFieldInfo> fieldList, Field field, EntityInfo entityInfo) {
        EntityFieldInfo entityFieldInfo = new EntityFieldInfo(dbConfig, field);

        // 初始化
        String mappingColumn = initMappingColumnMapAndGet(dbConfig, entityInfo, field);
        entityFieldInfo.setMappingColumn(mappingColumn);
        entityFieldInfo.setColumnType(field.getType().getSimpleName());
        fieldList.add(entityFieldInfo);
    }


    /**
     * 主键属性初始化
     *
     * @param dbConfig   索引配置
     * @param entityInfo 实体信息
     * @param field      字段
     * @param clazz      类
     * @return 布尔值
     */
    private static boolean initTableIdWithAnnotation(GlobalConfig.DbConfig dbConfig, EntityInfo entityInfo,
                                                     Field field, Class<?> clazz) {
        TableId tableId = field.getAnnotation(TableId.class);
        if (tableId != null) {
            if (StringUtils.isEmpty(entityInfo.getKeyColumn())) {
                // 主键策略（ 注解 > 全局 ）
                // 设置 Sequence 其他策略无效
                if (IdType.NONE == tableId.type()) {
                    entityInfo.setIdType(dbConfig.getIdType());
                } else {
                    entityInfo.setIdType(tableId.type());
                }
                // 字段
                String column = tableId.value();
                field.setAccessible(Boolean.TRUE);
                entityInfo.setClazz(field.getDeclaringClass())
                        .setKeyColumn(column)
                        .setKeyField(field)
                        .setIdClass(field.getType())
                        .setKeyProperty(field.getName());
                return true;
            } else {
                String msg = "There must be only one, Discover multiple @TableId annotation in %s";
                throw new RuntimeException(String.format(msg, clazz));
            }
        }
        entityInfo.setHasIdAnnotation(Boolean.TRUE);
        return false;
    }


    /**
     * 主键属性初始化
     *
     * @param dbConfig   索引配置
     * @param entityInfo 实体信息
     * @param field      字段
     * @param clazz      类
     * @return 布尔值
     */
    private static boolean initTableIdWithoutAnnotation(GlobalConfig.DbConfig dbConfig, EntityInfo entityInfo,
                                                        Field field, Class<?> clazz) {
        String column = field.getName();
        if (DEFAULT_ID_NAME.equalsIgnoreCase(column) || DEFAULT_ES_ID_NAME.equals(column)) {
            if (StringUtils.isEmpty(entityInfo.getKeyColumn())) {
                field.setAccessible(Boolean.TRUE);
                entityInfo.setIdType(dbConfig.getIdType())
                        .setKeyColumn(DEFAULT_ES_ID_NAME)
                        .setKeyProperty(field.getName())
                        .setKeyField(field)
                        .setIdClass(field.getType())
                        .setClazz(field.getDeclaringClass());
                return true;
            } else {
                String msg = "There must be only one, Discover multiple @TableId annotation in %s";
                throw new RuntimeException(String.format(msg, clazz));
            }
        }
        entityInfo.setHasIdAnnotation(Boolean.FALSE);
        return false;
    }

    /**
     * 判断主键注解是否存在
     *
     * @param list 字段列表
     * @return 布尔值
     */
    public static boolean isExistTableId(List<Field> list) {
        for (Field field : list) {
            TableId tableId = field.getAnnotation(TableId.class);
            if (tableId != null) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取该类的所有属性列表
     *
     * @param clazz 类
     * @return 字段列表
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fieldList = ReflectionKit.getFieldList(ClassUtils.getUserClass(clazz));
        if (CollectionUtils.isNotEmpty(fieldList)) {
            return fieldList.stream()
                    .filter(i -> {
                        // 过滤注解非表字段属性
                        TableField tableField = i.getAnnotation(TableField.class);
                        return (tableField == null || tableField.exist());
                    }).collect(toList());
        }
        return fieldList;
    }

    /**
     * 初始化表(索引)名称
     *
     * @param clazz        类
     * @param globalConfig 全局配置
     * @param entityInfo   实体信息
     */
    private static void initTableName(Class<?> clazz, GlobalConfig globalConfig, EntityInfo entityInfo) {
        // 数据库全局配置
        GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();
        TableName table = clazz.getAnnotation(TableName.class);
        String tableName = clazz.getSimpleName().toLowerCase(Locale.ROOT);
        String tablePrefix = dbConfig.getTablePrefix();

        boolean tablePrefixEffect = true;
        String indexName;
        if (Objects.isNull(table)) {
            // 无注解, 直接使用类名
            indexName = tableName;
        } else {
            // 有注解,看注解中是否有指定
            if (StringUtils.isNotBlank(table.value())) {
                indexName = table.value();
                if (StringUtils.isNotBlank(tablePrefix) && !table.keepGlobalPrefix()) {
                    tablePrefixEffect = false;
                }
            } else {
                indexName = tableName;
            }
            entityInfo.setAliasName(table.aliasName());
        }

        String targetIndexName = indexName;
        if (StringUtils.isNotBlank(tablePrefix) && tablePrefixEffect) {
            targetIndexName = tablePrefix + targetIndexName;
        }
        entityInfo.setIndexName(targetIndexName);
    }

    /**
     * 初始化实体类字段与es字段映射关系Map
     *
     * @param dbConfig   配置信息
     * @param entityInfo 实体信息
     * @param field      字段
     * @return mappingColumn es中的字段名
     */
    private static String initMappingColumnMapAndGet(GlobalConfig.DbConfig dbConfig, EntityInfo entityInfo, Field field) {
        // 自定义字段名及驼峰下划线转换
        String mappingColumn = field.getName();
        if (dbConfig.isMapUnderscoreToCamelCase()) {
            // 下划线转驼峰
            mappingColumn = StringUtils.camelToUnderline(field.getName());
        }
        entityInfo.getMappingColumnMap().putIfAbsent(field.getName(), mappingColumn);
        return mappingColumn;
    }
}
