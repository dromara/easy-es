package cn.easyes.core.toolkit;

import cn.easyes.annotation.*;
import cn.easyes.common.enums.FieldType;
import cn.easyes.common.enums.IdType;
import cn.easyes.common.params.DefaultNestedClass;
import cn.easyes.common.utils.ClassUtils;
import cn.easyes.common.utils.FastJsonUtils;
import cn.easyes.common.utils.ReflectionKit;
import cn.easyes.common.utils.StringUtils;
import cn.easyes.core.biz.EntityFieldInfo;
import cn.easyes.core.biz.EntityInfo;
import cn.easyes.core.biz.HighLightParam;
import cn.easyes.core.cache.BaseCache;
import cn.easyes.core.cache.GlobalConfigCache;
import cn.easyes.core.config.GlobalConfig;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.alibaba.fastjson.serializer.NameFilter;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cn.easyes.common.constants.BaseEsConstants.*;

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
        return initIndexInfo(globalConfig, clazz);
    }

    /**
     * 实体类反射获取表信息 初始化
     *
     * @param globalConfig 全局配置
     * @param clazz        类
     * @return 实体信息
     */
    public synchronized static EntityInfo initIndexInfo(GlobalConfig globalConfig, Class<?> clazz) {
        EntityInfo entityInfo = ENTITY_INFO_CACHE.get(clazz);
        if (entityInfo != null) {
            return entityInfo;
        }

        // 没有获取到缓存信息,则初始化
        entityInfo = new EntityInfo();
        // 初始化表名(索引名)相关
        initIndexName(clazz, globalConfig, entityInfo);
        // 初始化字段相关
        initIndexFields(clazz, globalConfig, entityInfo);

        // 放入缓存
        ENTITY_INFO_CACHE.put(clazz, entityInfo);
        return entityInfo;
    }


    /**
     * 初始化 索引主键,索引字段
     *
     * @param clazz        类
     * @param globalConfig 全局配置
     * @param entityInfo   实体信息
     */
    public static void initIndexFields(Class<?> clazz, GlobalConfig globalConfig, EntityInfo entityInfo) {
        // 数据库全局配置
        GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();
        List<Field> list = getAllFields(clazz);
        // 标记是否读取到主键
        boolean isReadPK = false;
        // 是否存在 @TableId 注解
        boolean existTableId = isExistIndexId(list);

        List<EntityFieldInfo> fieldList = new ArrayList<>();
        for (Field field : list) {
            // 主键ID 初始化
            if (!isReadPK) {
                if (existTableId) {
                    isReadPK = initIndexIdWithAnnotation(dbConfig, entityInfo, field);
                } else {
                    isReadPK = initIndexIdWithoutAnnotation(dbConfig, entityInfo, field);
                }
                if (isReadPK) {
                    continue;
                }
            }

            // 有 @IndexField 等已知自定义注解的字段初始化
            if (initIndexFieldWithAnnotation(dbConfig, fieldList, field, entityInfo)) {
                continue;
            }

            // 无 @IndexField 等已知自定义注解的字段初始化
            initIndexFieldWithoutAnnotation(dbConfig, fieldList, field, entityInfo);
        }

        // 字段列表
        entityInfo.setFieldList(fieldList);

        // 添加fastjson 前置过滤器
        addSimplePropertyPreFilter(entityInfo, clazz);

        // 添加fastjson ExtraProcessor
        addExtraProcessor(entityInfo);
    }

    /**
     * 添加fastjson前置过滤器
     *
     * @param entityInfo 实体信息
     * @param clazz      实体类
     */
    private static void addSimplePropertyPreFilter(EntityInfo entityInfo, Class<?> clazz) {
        // 字段是否序列化过滤 针对notExists字段及高亮字段等
        List<SerializeFilter> preFilters = new ArrayList<>();
        SimplePropertyPreFilter entityClassPreFilter =
                FastJsonUtils.getSimplePropertyPreFilter(clazz, entityInfo.getNotSerializeField());
        Optional.ofNullable(entityClassPreFilter).ifPresent(preFilters::add);

        // 嵌套类的字段序列化过滤器
        entityInfo.getNestedNotSerializeField()
                .forEach((k, v) -> Optional.ofNullable(FastJsonUtils.getSimplePropertyPreFilter(k, v))
                        .ifPresent(preFilters::add));

        // 父子类型的字段序列化过滤器
        if (!entityInfo.isChild()) {
            Set<String> notSerialField = new HashSet<>();
            notSerialField.add(PARENT);
            Optional.ofNullable(FastJsonUtils.getSimplePropertyPreFilter(entityInfo.getJoinFieldClass(), notSerialField))
                    .ifPresent(preFilters::add);
        }

        // 添加fastjson NameFilter 针对驼峰以及下划线转换
        addNameFilter(entityInfo, preFilters);
        entityInfo.getClassSimplePropertyPreFilterMap().putIfAbsent(clazz, preFilters);

        // 置空闲置容器,节省少量内存
        entityInfo.getNotSerializeField().clear();
        entityInfo.getNestedNotSerializeField().clear();
    }

    /**
     * 预添加fastjson解析object时对非实体类字段的处理(比如自定义字段名,下划线等)
     *
     * @param entityInfo 实体信息
     */
    private static void addExtraProcessor(EntityInfo entityInfo) {
        Map<String, String> columnMappingMap = entityInfo.getColumnMappingMap();
        Map<Class<?>, Map<String, String>> nestedClassColumnMappingMap = entityInfo.getNestedClassColumnMappingMap();
        ExtraProcessor extraProcessor = (object, key, value) -> {
            // 主类
            Map<String, String> nestedColumnMappingMap = nestedClassColumnMappingMap.get(object.getClass());
            if (nestedColumnMappingMap != null) {
                invokeExtraProcessor(nestedColumnMappingMap, object, key, value, object.getClass());
            } else {
                // 嵌套类
                invokeExtraProcessor(columnMappingMap, object, key, value, object.getClass());
            }
        };
        entityInfo.setExtraProcessor(extraProcessor);
    }

    private static void invokeExtraProcessor(Map<String, String> columnMappingMap, Object object, String key, Object value, Class<?> clazz) {
        Optional.ofNullable(columnMappingMap.get(key))
                .flatMap(realMethodName -> Optional.ofNullable(BaseCache.setterMethod(clazz, realMethodName)))
                .ifPresent(method -> {
                    try {
                        method.invoke(object, value);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * 添加fastjson字段过滤器
     *
     * @param entityInfo 实体信息
     */
    private static void addNameFilter(EntityInfo entityInfo, List<SerializeFilter> preFilters) {
        Map<String, String> mappingColumnMap = entityInfo.getMappingColumnMap();
        Map<Class<?>, Map<String, String>> nestedClassMappingColumnMap = entityInfo.getNestedClassMappingColumnMap();
        if (!mappingColumnMap.isEmpty()) {
            NameFilter nameFilter = (object, name, value) -> {
                Map<String, String> nestedMappingColumnMap = nestedClassMappingColumnMap.get(object.getClass());
                if (Objects.nonNull(nestedMappingColumnMap)) {
                    String nestedMappingColumn = nestedMappingColumnMap.get(name);
                    if (Objects.equals(nestedMappingColumn, name)) {
                        return name;
                    } else {
                        return nestedMappingColumn;
                    }
                }
                String mappingColumn = mappingColumnMap.get(name);
                if (Objects.equals(mappingColumn, name)) {
                    return name;
                }
                return mappingColumn;
            };
            preFilters.add(nameFilter);
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
    private static boolean initIndexFieldWithAnnotation(GlobalConfig.DbConfig dbConfig, List<EntityFieldInfo> fieldList,
                                                        Field field, EntityInfo entityInfo) {
        boolean hasAnnotation = false;

        // 初始化封装IndexField注解信息
        if (field.isAnnotationPresent(IndexField.class)) {
            initIndexFieldAnnotation(dbConfig, entityInfo, field, fieldList);
            hasAnnotation = true;
        }

        // 初始化封装HighLight注解信息
        if (field.isAnnotationPresent(HighLight.class)) {
            initHighLightAnnotation(dbConfig, entityInfo, field);
            // 此处无需返回true阻断流程,可防止用户未添加IndexField时,框架索引跳过读取此字段的信息
        }

        // 初始化封装Score注解信息
        if (field.isAnnotationPresent(Score.class)) {
            entityInfo.setScoreField(field.getName());
            entityInfo.getNotSerializeField().add(field.getName());
            entityInfo.setScoreDecimalPlaces(field.getAnnotation(Score.class).decimalPlaces());
            hasAnnotation = true;
        }

        // 初始化封装Distance注解信息
        if (field.isAnnotationPresent(Distance.class)) {
            Distance distance = field.getAnnotation(Distance.class);
            entityInfo.setDistanceField(field.getName());
            entityInfo.getNotSerializeField().add(field.getName());
            entityInfo.setDistanceDecimalPlaces(distance.decimalPlaces());
            entityInfo.setSortBuilderIndex(distance.sortBuilderIndex());
            hasAnnotation = true;
        }
        return hasAnnotation;
    }


    /**
     * IndexField注解信息初始化
     *
     * @param dbConfig   索引配置
     * @param fieldList  字段列表
     * @param field      字段
     * @param entityInfo 实体信息
     */
    private static void initIndexFieldAnnotation(GlobalConfig.DbConfig dbConfig, EntityInfo entityInfo,
                                                 Field field, List<EntityFieldInfo> fieldList) {
        IndexField tableField = field.getAnnotation(IndexField.class);
        if (tableField.exist()) {
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

            // 日期格式化
            if (StringUtils.isNotBlank(tableField.dateFormat())) {
                entityFieldInfo.setDateFormat(tableField.dateFormat());
            }

            // 其它
            entityFieldInfo.setMappingColumn(mappingColumn);
            entityFieldInfo.setAnalyzer(tableField.analyzer());
            entityFieldInfo.setSearchAnalyzer(tableField.searchAnalyzer());
            entityFieldInfo.setFieldType(tableField.fieldType());
            entityFieldInfo.setFieldData(tableField.fieldData());
            entityFieldInfo.setColumnType(field.getType().getSimpleName());

            // 父子类型
            if (FieldType.JOIN.equals(tableField.fieldType())) {
                entityFieldInfo.setParentName(tableField.parentName());
                entityFieldInfo.setChildName(tableField.childName());

                entityInfo.setJoinFieldName(mappingColumn);
                entityInfo.setJoinFieldClass(tableField.joinFieldClass());
                entityInfo.getPathClassMap().putIfAbsent(field.getName(), tableField.joinFieldClass());
                processNested(tableField.joinFieldClass(), dbConfig, entityInfo);
            }

            fieldList.add(entityFieldInfo);

            // 嵌套类处理
            if (DefaultNestedClass.class != tableField.nestedClass()) {
                // 嵌套类
                entityInfo.getPathClassMap().putIfAbsent(field.getName(), tableField.nestedClass());
                processNested(tableField.nestedClass(), dbConfig, entityInfo);
            }

        } else {
            entityInfo.getNotSerializeField().add(field.getName());
        }
    }

    /**
     * HighLight注解初始化
     *
     * @param dbConfig   索引配置
     * @param entityInfo 实体信息
     * @param field      字段
     */
    private static void initHighLightAnnotation(GlobalConfig.DbConfig dbConfig, EntityInfo entityInfo, Field field) {
        HighLight highLight = field.getAnnotation(HighLight.class);
        String mappingField = highLight.mappingField();
        if (StringUtils.isNotBlank(mappingField)) {
            entityInfo.getNotSerializeField().add(mappingField);
        } else {
            // 如果用户未指定高亮映射字段,则高亮映射字段用当前字段
            mappingField = field.getName();
        }

        String customField = entityInfo.getMappingColumnMap().get(field.getName());
        String realHighLightField = Objects.isNull(customField) ? field.getName() : customField;
        if (dbConfig.isMapUnderscoreToCamelCase()) {
            realHighLightField = StringUtils.camelToUnderline(realHighLightField);
        }
        entityInfo.getHighlightFieldMap().putIfAbsent(realHighLightField, mappingField);

        // 封装高亮参数
        HighLightParam highLightParam =
                new HighLightParam(highLight.fragmentSize(), highLight.preTag(), highLight.postTag(), realHighLightField);
        entityInfo.getHighLightParams().add(highLightParam);
    }

    /**
     * 处理嵌套类中的字段配置
     *
     * @param nestedClass 嵌套类
     * @param dbConfig    全局配置
     * @param entityInfo  实体信息
     */
    private static void processNested(Class<?> nestedClass, GlobalConfig.DbConfig dbConfig, EntityInfo entityInfo) {
        // 将字段映射置入map 对其子节点也执行同样的操作
        List<Field> allFields = getAllFields(nestedClass);
        Map<String, String> mappingColumnMap = new HashMap<>(allFields.size());
        Map<String, String> columnMappingMap = new HashMap<>(allFields.size());
        List<EntityFieldInfo> entityFieldInfoList = new ArrayList<>();
        Set<String> notSerializedFields = new HashSet<>();
        allFields.forEach(field -> {
            String mappingColumn;
            // 处理TableField注解
            IndexField tableField = field.getAnnotation(IndexField.class);
            if (Objects.isNull(tableField)) {
                mappingColumn = getMappingColumn(dbConfig, field);
                EntityFieldInfo entityFieldInfo = new EntityFieldInfo(dbConfig, field);
                entityFieldInfo.setMappingColumn(mappingColumn);
                entityFieldInfo.setFieldType(FieldType.TEXT);
                entityFieldInfo.setColumnType(FieldType.TEXT.getType());
                entityFieldInfoList.add(entityFieldInfo);
            } else {
                if (tableField.exist()) {
                    // 子嵌套,递归处理
                    if (DefaultNestedClass.class != tableField.nestedClass()) {
                        entityInfo.getPathClassMap().putIfAbsent(field.getName(), tableField.nestedClass());
                        processNested(tableField.nestedClass(), dbConfig, entityInfo);
                    }

                    // 字段名称
                    if (StringUtils.isNotBlank(tableField.value().trim())) {
                        mappingColumn = tableField.value();
                    } else {
                        mappingColumn = getMappingColumn(dbConfig, field);
                    }

                    // 设置实体字段信息
                    EntityFieldInfo entityFieldInfo = new EntityFieldInfo(dbConfig, field, tableField);
                    entityFieldInfo.setMappingColumn(mappingColumn);
                    FieldType fieldType = FieldType.NESTED.equals(tableField.fieldType()) ? FieldType.NESTED : FieldType.TEXT;
                    entityFieldInfo.setFieldType(fieldType);
                    entityFieldInfo.setFieldData(tableField.fieldData());
                    entityFieldInfo.setColumnType(fieldType.getType());
                    entityFieldInfo.setAnalyzer(tableField.analyzer());
                    entityFieldInfo.setSearchAnalyzer(tableField.searchAnalyzer());
                    entityFieldInfoList.add(entityFieldInfo);
                } else {
                    mappingColumn = getMappingColumn(dbConfig, field);
                    notSerializedFields.add(field.getName());
                }
            }
            columnMappingMap.putIfAbsent(mappingColumn, field.getName());
            mappingColumnMap.putIfAbsent(field.getName(), mappingColumn);

        });
        entityInfo.getNestedNotSerializeField().putIfAbsent(nestedClass, notSerializedFields);
        entityInfo.getNestedClassColumnMappingMap().putIfAbsent(nestedClass, columnMappingMap);
        entityInfo.getNestedClassMappingColumnMap().putIfAbsent(nestedClass, mappingColumnMap);
        entityInfo.getNestedFieldListMap().put(nestedClass, entityFieldInfoList);

    }


    /**
     * 字段属性初始化
     *
     * @param dbConfig   索引配置
     * @param fieldList  字段列表
     * @param field      字段
     * @param entityInfo 实体信息
     */
    private static void initIndexFieldWithoutAnnotation(GlobalConfig.DbConfig dbConfig, List<EntityFieldInfo> fieldList,
                                                        Field field, EntityInfo entityInfo) {
        boolean isNotSerializedField = entityInfo.getNotSerializeField().contains(field.getName());
        if (isNotSerializedField) {
            return;
        }

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
     * @return 布尔值
     */
    private static boolean initIndexIdWithAnnotation(GlobalConfig.DbConfig dbConfig, EntityInfo entityInfo,
                                                     Field field) {
        IndexId tableId = field.getAnnotation(IndexId.class);
        if (tableId != null) {
            // 主键策略（ 注解 > 全局 ）
            // 设置 Sequence 其他策略无效
            if (IdType.NONE == tableId.type()) {
                entityInfo.setIdType(dbConfig.getIdType());
            } else {
                entityInfo.setIdType(tableId.type());
            }
            // 字段
            field.setAccessible(Boolean.TRUE);
            entityInfo.setClazz(field.getDeclaringClass())
                    .setKeyField(field)
                    .setIdClass(field.getType())
                    .setKeyProperty(field.getName());

            entityInfo.getNotSerializeField().add(DEFAULT_ID_NAME);
            entityInfo.getNotSerializeField().add(field.getName());
            entityInfo.getMappingColumnMap().putIfAbsent(field.getName(), DEFAULT_ID_NAME);
            return true;
        }
        return false;
    }


    /**
     * 主键属性初始化
     *
     * @param dbConfig   索引配置
     * @param entityInfo 实体信息
     * @param field      字段
     * @return 布尔值
     */
    private static boolean initIndexIdWithoutAnnotation(GlobalConfig.DbConfig dbConfig, EntityInfo entityInfo,
                                                        Field field) {
        String column = field.getName();
        if (DEFAULT_ID_NAME.equalsIgnoreCase(column) || DEFAULT_ES_ID_NAME.equals(column)) {
            field.setAccessible(Boolean.TRUE);
            entityInfo.setIdType(dbConfig.getIdType())
                    .setKeyProperty(field.getName())
                    .setKeyField(field)
                    .setIdClass(field.getType())
                    .setClazz(field.getDeclaringClass());
            entityInfo.getNotSerializeField().add(DEFAULT_ID_NAME);
            entityInfo.getNotSerializeField().add(field.getName());
            entityInfo.getMappingColumnMap().putIfAbsent(field.getName(), DEFAULT_ID_NAME);
            return true;
        }
        return false;
    }

    /**
     * 判断主键注解是否存在
     *
     * @param list 字段列表
     * @return 布尔值
     */
    public static boolean isExistIndexId(List<Field> list) {
        for (Field field : list) {
            IndexId tableId = field.getAnnotation(IndexId.class);
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
        return ReflectionKit.getFieldList(ClassUtils.getUserClass(clazz));
    }

    /**
     * 初始化索引名等信息
     *
     * @param clazz        类
     * @param globalConfig 全局配置
     * @param entityInfo   实体信息
     */
    private static void initIndexName(Class<?> clazz, GlobalConfig globalConfig, EntityInfo entityInfo) {
        // 数据库全局配置
        GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();
        IndexName table = clazz.getAnnotation(IndexName.class);
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
            entityInfo.setShardsNum(table.shardsNum());
            entityInfo.setReplicasNum(table.replicasNum());
            entityInfo.setChild(table.child());
            entityInfo.setChildClass(table.childClass());
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
        String mappingColumn = getMappingColumn(dbConfig, field);
        entityInfo.getMappingColumnMap().putIfAbsent(field.getName(), mappingColumn);
        return mappingColumn;
    }

    /**
     * 获取实体类字段对应的es字段
     *
     * @param dbConfig 全局配置
     * @param field    字段
     * @return es字段名
     */
    private static String getMappingColumn(GlobalConfig.DbConfig dbConfig, Field field) {
        String mappingColumn = field.getName();
        if (dbConfig.isMapUnderscoreToCamelCase()) {
            // 下划线转驼峰
            mappingColumn = StringUtils.camelToUnderline(field.getName());
        }
        return mappingColumn;
    }
}
