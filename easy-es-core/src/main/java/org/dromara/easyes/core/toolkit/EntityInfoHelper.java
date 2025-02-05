package org.dromara.easyes.core.toolkit;

import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.alibaba.fastjson.serializer.NameFilter;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.alibaba.fastjson.serializer.ValueFilter;
import lombok.SneakyThrows;
import org.dromara.easyes.annotation.*;
import org.dromara.easyes.annotation.rely.*;
import org.dromara.easyes.common.property.GlobalConfig;
import org.dromara.easyes.common.utils.*;
import org.dromara.easyes.core.biz.EntityFieldInfo;
import org.dromara.easyes.core.biz.EntityInfo;
import org.dromara.easyes.core.biz.HighLightParam;
import org.dromara.easyes.core.cache.BaseCache;
import org.dromara.easyes.core.cache.GlobalConfigCache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;

/**
 * 实体字段信息工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class EntityInfoHelper {
    /**
     * 获取索引settings方法名
     */
    private final static String GET_SETTINGS_METHOD = "getSettings";
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
        Class<?> currentClass = clazz;
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
        // 初始化索引settings相关
        initSettings(clazz, entityInfo);
        // 初始化字段相关
        initIndexFields(clazz, globalConfig, entityInfo);
        // 初始化封装@Join父子类型注解信息
        initJoin(clazz, globalConfig, entityInfo);

        // 放入缓存
        ENTITY_INFO_CACHE.put(clazz, entityInfo);
        return entityInfo;
    }

    /**
     * 初始化父子类型相关信息
     *
     * @param clazz        实体类
     * @param globalConfig 全局配置
     * @param entityInfo   实体信息
     */
    private static void initJoin(Class<?> clazz, GlobalConfig globalConfig, EntityInfo entityInfo) {
        Join join = clazz.getAnnotation(Join.class);
        if (join == null || ArrayUtils.isEmpty(join.nodes())) {
            return;
        }
        boolean camelCase = globalConfig.getDbConfig().isMapUnderscoreToCamelCase();
        String joinFieldName = camelToUnderline(JoinField.class.getSimpleName(), camelCase);
        entityInfo.setJoinFieldName(joinFieldName);
        String joinAlias = StringUtils.isBlank(join.rootAlias()) ? clazz.getSimpleName() : join.rootAlias();
        String underlineJoinAlias = camelToUnderline(joinAlias, camelCase);
        entityInfo.setJoinAlias(underlineJoinAlias);
        entityInfo.setEagerGlobalOrdinals(join.eagerGlobalOrdinals());

        Map<String, Object> relationMap = entityInfo.getRelationMap();
        Arrays.stream(join.nodes())
                .forEach(child -> {
                    String parentAlias = StringUtils.isBlank(child.parentAlias()) ? child.parentClass().getSimpleName().toLowerCase() : child.parentAlias();
                    String underlineParentAlias = camelToUnderline(parentAlias, camelCase);
                    List<String> childAliases = ArrayUtils.isEmpty(child.childAliases()) ?
                            Arrays.stream(child.childClasses()).map(Class::getSimpleName).map(i -> camelToUnderline(i.toLowerCase(), camelCase)).distinct().collect(Collectors.toList()) :
                            Arrays.stream(child.childAliases()).map(i -> camelToUnderline(i, camelCase)).collect(Collectors.toList());
                    // 大于1以数组形式置入,只有1个元素,则以object置入 否则会导致平滑模式下,从es中查询到的索引mapping与根据注解构造出的mapping结构有差异,被误判索引发生变动
                    Object relation = childAliases.size() > ONE ? childAliases : childAliases.get(ZERO);
                    relationMap.put(underlineParentAlias, relation);

                    // 在join-父加载时预加载join-子信息
                    AtomicInteger index = new AtomicInteger(ZERO);
                    Arrays.stream(child.childClasses()).forEach(childClass -> {
                        EntityInfo childEntityInfo = EntityInfoHelper.getEntityInfo(childClass);
                        childEntityInfo.setChild(true);
                        childEntityInfo.setIndexName(entityInfo.getIndexName());
                        childEntityInfo.setJoinFieldName(joinFieldName);
                        childEntityInfo.setParentJoinAlias(parentAlias);

                        // 将join-子信息加入到join-父中以备创建索引之需
                        if (CollectionUtils.isNotEmpty(childEntityInfo.getFieldList())) {
                            entityInfo.getChildFieldList().addAll(childEntityInfo.getFieldList());
                        }

                        // 将join-父信息加入到join-子中 已备CRUD数据之需
                        childEntityInfo.setJoinAlias(childAliases.get(index.getAndIncrement()));
                    });
                });
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
        // 是否存在 @IndexId 注解
        boolean existsIndexId = isExistIndexId(list);

        List<EntityFieldInfo> fieldList = new ArrayList<>();
        for (Field field : list) {
            // 主键ID 初始化
            if (!isReadPK) {
                if (existsIndexId) {
                    isReadPK = initIndexIdWithAnnotation(dbConfig, entityInfo, field);
                } else {
                    isReadPK = initIndexIdWithoutAnnotation(dbConfig, entityInfo, field);
                }
                if (isReadPK) {
                    continue;
                }
            }

            // 有 @IndexField 等已知自定义注解的字段初始化
            if (initIndexFieldWithAnnotation(dbConfig, fieldList, field, entityInfo, clazz)) {
                continue;
            }

            // 无 @IndexField 等已知自定义注解的字段初始化
            initIndexFieldWithoutAnnotation(dbConfig, fieldList, field, entityInfo, clazz);
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

        // 日期等特殊字段过滤器
        List<SerializeFilter> valueFilters = getValueFilter(entityInfo, clazz);
        preFilters.addAll(valueFilters);

        // 嵌套类的字段序列化过滤器
        entityInfo.getNestedNotSerializeField()
                .forEach((k, v) -> Optional.ofNullable(FastJsonUtils.getSimplePropertyPreFilter(k, v))
                        .ifPresent(preFilters::add));

        // 添加fastjson NameFilter 针对驼峰以及下划线转换
        addNameFilter(entityInfo, preFilters);
        entityInfo.getClassSimplePropertyPreFilterMap().putIfAbsent(clazz, preFilters);

        // 置空闲置容器,节省少量内存
        entityInfo.getNotSerializeField().clear();
        entityInfo.getNestedNotSerializeField().clear();
    }

    /**
     * 获取fastjson 值过滤器 针对日期等需要格式化及转换字段处理
     *
     * @param entityInfo 实体信息缓存
     * @param clazz      对应类
     * @return 过滤器
     */
    private static List<SerializeFilter> getValueFilter(EntityInfo entityInfo, Class<?> clazz) {
        // 日期字段序列化过滤器
        List<SerializeFilter> serializeFilters = new ArrayList<>();
        Map<String, String> dateFormatMap = entityInfo.getClassDateFormatMap().get(clazz);
        if (CollectionUtils.isEmpty(dateFormatMap)) {
            return serializeFilters;
        }

        Map<Class<?>, Map<String, String>> nestedClassColumnMappingMap = entityInfo.getNestedClassColumnMappingMap();
        SerializeFilter serializeFilter = (ValueFilter) (object, name, value) -> {
            Map<String, String> nestedColumnMappingMap = nestedClassColumnMappingMap.get(object.getClass());
            if (nestedColumnMappingMap != null) {
                Map<String, String> nestedDateFormatMap = entityInfo.getClassDateFormatMap().get(object.getClass());
                if (CollectionUtils.isEmpty(nestedDateFormatMap)) {
                    return value;
                }
                return formatDate(name, value, nestedColumnMappingMap, nestedDateFormatMap);
            } else {
                return formatDate(name, value, entityInfo.getColumnMappingMap(), dateFormatMap);
            }
        };
        serializeFilters.add(serializeFilter);
        return serializeFilters;
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
            Map<String, String> nestedColumnMappingMap = nestedClassColumnMappingMap.get(object.getClass());
            if (nestedColumnMappingMap != null) {
                // 嵌套类
                invokeExtraProcessor(nestedColumnMappingMap, object, key, value, object.getClass());
            } else {
                // 主类
                invokeExtraProcessor(columnMappingMap, object, key, value, object.getClass());
            }
        };
        entityInfo.setExtraProcessor(extraProcessor);
    }

    /**
     * fastjson字段转换 (由es中查询结果映射至对象)
     *
     * @param columnMappingMap 字段映射
     * @param object           对象
     * @param key              字段名
     * @param value            字段值
     * @param clazz            实体类
     */
    private static void invokeExtraProcessor(Map<String, String> columnMappingMap, Object object, String key, Object value, Class<?> clazz) {
        Optional.ofNullable(columnMappingMap.get(key))
                .flatMap(realMethodName -> Optional.ofNullable(BaseCache.setterMethod(clazz, realMethodName)))
                .ifPresent(method -> {
                    try {
                        method.invoke(object, value);
                    } catch (IllegalArgumentException illegalArgumentException) {
                        illegalArgumentException.printStackTrace();
                        // 日期类型失败按默认format格式重试 几乎不会走到这里,除非用户针对日期字段设置了别名
                        reInvokeDate(object, value, method);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * 日期类型重新反射
     *
     * @param object 对象
     * @param value  值
     * @param method 方法名
     */
    private static void reInvokeDate(Object object, Object value, Method method) {
        if (value instanceof String) {
            String paramTypeName = method.getParameterTypes()[0].getSimpleName();
            Object parsed = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
            if (LocalDateTime.class.getSimpleName().equals(paramTypeName)) {
                parsed = LocalDateTime.parse(value.toString(), formatter);
            } else if (LocalDate.class.getSimpleName().equals(paramTypeName)) {
                parsed = LocalDate.parse(value.toString(), formatter);
            } else if (Date.class.getSimpleName().equals(paramTypeName)) {
                parsed = Date.from(LocalDateTime.parse(value.toString(), formatter).atZone(ZoneId.systemDefault()).toInstant());
            }
            try {
                method.invoke(object, parsed);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
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
                                                        Field field, EntityInfo entityInfo, Class<?> clazz) {
        boolean hasAnnotation = false;

        // 初始化封装IndexField及MultiIndexField注解信息
        if (field.isAnnotationPresent(IndexField.class) || field.isAnnotationPresent(MultiIndexField.class)) {
            initIndexFieldAnnotation(dbConfig, entityInfo, clazz, field, fieldList);
            hasAnnotation = true;
        }

        // 初始化封装HighLight注解信息
        if (field.isAnnotationPresent(HighLight.class)) {
            initHighLightAnnotation(dbConfig, entityInfo, field, entityInfo.getMappingColumnMap(), null);
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
            entityInfo.getDistanceFields().add(field.getName());
            entityInfo.getNotSerializeField().add(field.getName());
            entityInfo.getDistanceDecimalPlaces().add(distance.decimalPlaces());
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
    private static void initIndexFieldAnnotation(GlobalConfig.DbConfig dbConfig, EntityInfo entityInfo, Class<?> clazz,
                                                 Field field, List<EntityFieldInfo> fieldList) {
        MultiIndexField multiIndexField = field.getAnnotation(MultiIndexField.class);
        IndexField indexField = Optional.ofNullable(multiIndexField).map(MultiIndexField::mainIndexField)
                .orElse(field.getAnnotation(IndexField.class));
        if (indexField.exist()) {
            // 存在字段处理
            EntityFieldInfo entityFieldInfo = new EntityFieldInfo(dbConfig, field, indexField);

            // 自定义字段名及驼峰下划线转换
            String mappingColumn;
            if (!StringUtils.isBlank(indexField.value().trim())) {
                // 自定义注解指定的名称优先级最高
                entityInfo.getMappingColumnMap().putIfAbsent(field.getName(), indexField.value());
                entityInfo.getColumnMappingMap().putIfAbsent(indexField.value(), field.getName());
                mappingColumn = indexField.value();
            } else {
                // 下划线驼峰
                mappingColumn = initMappingColumnMapAndGet(dbConfig, entityInfo, field);
            }

            // 缩放因子
            if (MINUS_ONE != indexField.scalingFactor()) {
                entityFieldInfo.setScalingFactor(indexField.scalingFactor());
            }

            // 日期格式化信息初始化
            String format = StringUtils.isBlank(indexField.dateFormat()) ? DEFAULT_DATE_TIME_FORMAT : indexField.dateFormat();
            initClassDateFormatMap(indexField.fieldType(), field.getName(), entityInfo, clazz, format);

            // 是否忽略大小写
            FieldType fieldType = FieldType.getByType(IndexUtils.getEsFieldType(indexField.fieldType(), field.getType().getSimpleName()));
            if (FieldType.KEYWORD.equals(fieldType)) {
                // 仅对keyword类型设置,其它类型es不支持
                entityFieldInfo.setIgnoreCase(indexField.ignoreCase());
            }

            // 最大索引长度
            if (indexField.ignoreAbove() > ZERO) {
                entityFieldInfo.setIgnoreAbove(indexField.ignoreAbove());
            }

            // 向量的维度大小
            if (indexField.dims() > ZERO) {
                entityFieldInfo.setDims(indexField.dims());
            }

            // 复制字段
            if (ArrayUtils.isNotEmpty(indexField.copyTo())){
                List<String> collect;
                if (dbConfig.isMapUnderscoreToCamelCase()){
                    collect = Arrays.stream(indexField.copyTo())
                            .map(StringUtils::camelToUnderline)
                            .collect(Collectors.toList());
                }else {
                    collect = Arrays.stream(indexField.copyTo())
                            .collect(Collectors.toList());
                }
                entityFieldInfo.setCopyToList(collect);
            }

            // 其它
            entityFieldInfo.setMappingColumn(mappingColumn);
            entityFieldInfo.setAnalyzer(indexField.analyzer());
            entityFieldInfo.setSearchAnalyzer(indexField.searchAnalyzer());
            entityFieldInfo.setFieldType(fieldType);
            entityFieldInfo.setFieldData(indexField.fieldData());
            entityFieldInfo.setColumnType(field.getType().getSimpleName());
            entityInfo.getFieldTypeMap().putIfAbsent(field.getName(), fieldType.getType());

            // 处理内部字段
            InnerIndexField[] innerIndexFields = Optional.ofNullable(multiIndexField).map(MultiIndexField::otherIndexFields).orElse(null);
            processInnerField(innerIndexFields, entityFieldInfo);

            fieldList.add(entityFieldInfo);

            // 嵌套类处理
            if (DefaultNestedClass.class != indexField.nestedClass()) {
                // 嵌套类
                entityInfo.getPathClassMap().putIfAbsent(field.getName(), indexField.nestedClass());
                processNested(indexField.nestedClass(), dbConfig, entityInfo);
            }

        } else {
            entityInfo.getNotSerializeField().add(field.getName());
        }
    }

    /**
     * HighLight注解初始化
     *
     * @param dbConfig         索引配置
     * @param entityInfo       实体信息
     * @param field            字段
     * @param mappingColumnMap 实体字段与es字段映射关系
     * @param nestedClass      嵌套类
     */
    private static void initHighLightAnnotation(GlobalConfig.DbConfig dbConfig, EntityInfo entityInfo, Field field,
                                                Map<String, String> mappingColumnMap, Class<?> nestedClass) {
        HighLight highLight = field.getAnnotation(HighLight.class);
        String mappingField = highLight.mappingField();

        // 置入字段json序列化忽略缓存
        boolean skip = false;
        if (StringUtils.isBlank(mappingField)) {
            // 如果用户未指定高亮映射字段,则高亮映射字段用当前字段
            mappingField = field.getName();
            // 当使用当前字段作为高亮字段时,当前字段参与索引创建
            skip = true;
        }
        if (!skip) {
            // 添加无需序列化字段至缓存
            if (nestedClass == null) {
                entityInfo.getNotSerializeField().add(mappingField);
            } else {
                // 嵌套类型
                Set<String> nestedNotSerializeFieldSet = Optional.ofNullable(entityInfo.getNestedNotSerializeField().get(nestedClass))
                        .orElse(new HashSet<>());
                nestedNotSerializeFieldSet.add(mappingField);
                entityInfo.getNestedNotSerializeField().put(nestedClass, nestedNotSerializeFieldSet);
            }
        }

        // 置入高亮字段与实体类中字段名对应关系缓存
        String customField = mappingColumnMap.get(field.getName());
        String realHighLightField = Objects.isNull(customField) ? field.getName() : customField;
        if (dbConfig.isMapUnderscoreToCamelCase()) {
            realHighLightField = StringUtils.camelToUnderline(realHighLightField);
        }
        addHighlightParam(entityInfo, nestedClass, highLight, realHighLightField, mappingField);

        MultiIndexField multiIndexField = field.getAnnotation(MultiIndexField.class);
        if (multiIndexField != null) {
            for (InnerIndexField innerIndexField : multiIndexField.otherIndexFields()) {
                addHighlightParam(entityInfo, nestedClass, highLight,
                        realHighLightField + STR_SIGN + innerIndexField.suffix(), mappingField);
            }
        }
    }

    /**
     * 添加高亮参数
     *
     * @param entityInfo         实体信息
     * @param nestedClass        嵌套类
     * @param highLight          高亮注解
     * @param realHighLightField 实际高亮字段
     * @param mappingField       映射字段
     */
    private static void addHighlightParam(EntityInfo entityInfo, Class<?> nestedClass, HighLight highLight,
                                          String realHighLightField, String mappingField) {
        if (nestedClass == null) {
            entityInfo.getHighlightFieldMap().putIfAbsent(realHighLightField, mappingField);
        } else {
            Map<String, String> nestedHighlightFieldMap = Optional.ofNullable(entityInfo.getNestedHighlightFieldMap().get(nestedClass))
                    .orElse(new HashMap<>());
            nestedHighlightFieldMap.putIfAbsent(realHighLightField, mappingField);
            entityInfo.getNestedHighlightFieldMap().put(nestedClass, nestedHighlightFieldMap);
        }

        // 置入高亮查询参数缓存
        HighLightParam highlightParam = new HighLightParam();
        highlightParam.setFragmentSize(highLight.fragmentSize())
                .setPreTag(highLight.preTag())
                .setPostTag(highLight.postTag())
                .setHighLightField(realHighLightField)
                .setHighLightType(highLight.highLightType())
                .setRequireFieldMatch(highLight.requireFieldMatch());
        if (MINUS_ONE != highLight.numberOfFragments() && highLight.numberOfFragments() > ZERO) {
            highlightParam.setNumberOfFragments(highLight.numberOfFragments());
        }
        if (nestedClass == null) {
            entityInfo.getHighlightParams().add(highlightParam);
        } else {
            List<HighLightParam> nestedHighlightParams = Optional.ofNullable(entityInfo.getNestedHighLightParamsMap().get(nestedClass))
                    .orElse(new ArrayList<>());
            nestedHighlightParams.add(highlightParam);
            entityInfo.getNestedHighLightParamsMap().put(nestedClass, nestedHighlightParams);
        }
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
        Map<String, String> fieldTypeMap = new HashMap<>();

        List<EntityFieldInfo> entityFieldInfoList = new ArrayList<>();
        Set<String> notSerializedFields = new HashSet<>();
        allFields.forEach(field -> {
            String mappingColumn;
            FieldType fieldType;

            // 初始化封装嵌套类中的HighLight注解信息
            if (field.isAnnotationPresent(HighLight.class)) {
                initHighLightAnnotation(dbConfig, entityInfo, field, mappingColumnMap, nestedClass);
            }

            // 处理TableField注解
            MultiIndexField multiIndexField = field.getAnnotation(MultiIndexField.class);
            IndexField indexField = Optional.ofNullable(multiIndexField).map(MultiIndexField::mainIndexField)
                    .orElse(field.getAnnotation(IndexField.class));
            if (Objects.isNull(indexField)) {
                // 跳过无关字段
                Set<String> notSerializeFields = Optional.ofNullable(entityInfo.getNestedNotSerializeField().get(nestedClass)).orElse(Collections.emptySet());
                if (notSerializeFields.contains(field.getName())) {
                    return;
                }

                mappingColumn = getMappingColumn(dbConfig, field);
                EntityFieldInfo entityFieldInfo = new EntityFieldInfo(dbConfig, field);
                entityFieldInfo.setMappingColumn(mappingColumn);
                fieldType = FieldType.getByType(IndexUtils.getEsFieldType(FieldType.NONE, field.getType().getSimpleName()));
                entityFieldInfo.setFieldType(fieldType);
                entityFieldInfo.setColumnType(field.getType().getSimpleName());

                // 日期类型,如果没加注解, 设置默认的日期format
                initClassDateFormatMap(fieldType, field.getName(), entityInfo, nestedClass, DEFAULT_DATE_TIME_FORMAT);

                entityFieldInfoList.add(entityFieldInfo);
            } else {
                if (indexField.exist()) {
                    // 子嵌套,递归处理
                    if (DefaultNestedClass.class != indexField.nestedClass()) {
                        entityInfo.getPathClassMap().putIfAbsent(field.getName(), indexField.nestedClass());
                        processNested(indexField.nestedClass(), dbConfig, entityInfo);
                    }

                    // 字段名称
                    if (StringUtils.isNotBlank(indexField.value().trim())) {
                        mappingColumn = indexField.value();
                    } else {
                        mappingColumn = getMappingColumn(dbConfig, field);
                    }

                    // 设置实体字段信息
                    EntityFieldInfo entityFieldInfo = new EntityFieldInfo(dbConfig, field, indexField);
                    fieldType = FieldType.NONE.equals(indexField.fieldType()) ? FieldType.KEYWORD_TEXT : indexField.fieldType();
                    entityFieldInfo.setMappingColumn(mappingColumn);
                    entityFieldInfo.setFieldType(fieldType);
                    entityFieldInfo.setFieldData(indexField.fieldData());
                    entityFieldInfo.setColumnType(fieldType.getType());
                    entityFieldInfo.setAnalyzer(indexField.analyzer());
                    entityFieldInfo.setSearchAnalyzer(indexField.searchAnalyzer());
                    if (FieldType.KEYWORD.equals(fieldType)) {
                        // 仅对keyword类型设置,其它类型es不支持
                        entityFieldInfo.setIgnoreCase(indexField.ignoreCase());
                    }

                    // 日期格式化信息初始化
                    String format = StringUtils.isBlank(indexField.dateFormat()) ? DEFAULT_DATE_TIME_FORMAT : indexField.dateFormat();
                    initClassDateFormatMap(indexField.fieldType(), field.getName(), entityInfo, nestedClass, format);

                    // 缩放因子
                    if (MINUS_ONE != indexField.scalingFactor()) {
                        entityFieldInfo.setScalingFactor(indexField.scalingFactor());
                    }

                    // 向量的维度大小
                    if (indexField.dims() > ZERO) {
                        entityFieldInfo.setDims(indexField.dims());
                    }

                    // 处理内部字段
                    InnerIndexField[] innerIndexFields = Optional.ofNullable(multiIndexField).map(MultiIndexField::otherIndexFields).orElse(null);
                    processInnerField(innerIndexFields, entityFieldInfo);

                    entityFieldInfoList.add(entityFieldInfo);
                } else {
                    mappingColumn = getMappingColumn(dbConfig, field);
                    fieldType = FieldType.KEYWORD_TEXT;
                    notSerializedFields.add(field.getName());
                }
            }
            columnMappingMap.putIfAbsent(mappingColumn, field.getName());
            mappingColumnMap.putIfAbsent(field.getName(), mappingColumn);
            fieldTypeMap.putIfAbsent(field.getName(), fieldType.getType());


        });
        entityInfo.getNestedNotSerializeField().putIfAbsent(nestedClass, notSerializedFields);
        entityInfo.getNestedClassColumnMappingMap().putIfAbsent(nestedClass, columnMappingMap);
        entityInfo.getNestedClassMappingColumnMap().putIfAbsent(nestedClass, mappingColumnMap);
        entityInfo.getNestedClassFieldTypeMap().putIfAbsent(nestedClass, fieldTypeMap);
        entityInfo.getNestedFieldListMap().put(nestedClass, entityFieldInfoList);
    }

    /**
     * 处理内部字段
     *
     * @param innerIndexFields 内部字段注解数组
     * @param entityFieldInfo  内部字段信息
     */
    private static void processInnerField(InnerIndexField[] innerIndexFields, EntityFieldInfo entityFieldInfo) {
        if (ArrayUtils.isNotEmpty(innerIndexFields)) {
            List<EntityFieldInfo.InnerFieldInfo> innerFieldInfoList = new ArrayList<>();
            Arrays.stream(innerIndexFields).forEach(innerField -> {
                Assert.notBlank(innerField.suffix(), "The Annotation MultiIndexField.InnerIndexField.value must has text");
                EntityFieldInfo.InnerFieldInfo innerFieldInfo = new EntityFieldInfo.InnerFieldInfo();
                innerFieldInfo.setColumn(innerField.suffix());
                innerFieldInfo.setFieldType(innerField.fieldType());
                innerFieldInfo.setAnalyzer(innerField.analyzer());
                innerFieldInfo.setSearchAnalyzer(innerField.searchAnalyzer());
                if (innerField.ignoreAbove() > ZERO) {
                    innerFieldInfo.setIgnoreAbove(innerField.ignoreAbove());
                }
                innerFieldInfoList.add(innerFieldInfo);
            });
            entityFieldInfo.setInnerFieldInfoList(innerFieldInfoList);
        }
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
                                                        Field field, EntityInfo entityInfo, Class<?> clazz) {
        boolean isNotSerializedField = entityInfo.getNotSerializeField().contains(field.getName());
        if (isNotSerializedField) {
            return;
        }

        EntityFieldInfo entityFieldInfo = new EntityFieldInfo(dbConfig, field);
        // 初始化
        String mappingColumn = initMappingColumnMapAndGet(dbConfig, entityInfo, field);
        entityFieldInfo.setMappingColumn(mappingColumn);
        FieldType fieldType = FieldType.getByType(IndexUtils.getEsFieldType(FieldType.NONE, field.getType().getSimpleName()));

        // 日期类型,如果没加注解, 设置默认的日期format
        if (FieldType.DATE.equals(fieldType)) {
            Map<Class<?>, Map<String, String>> classDateFormatMap = entityInfo.getClassDateFormatMap();
            Map<String, String> dateFormatMap = Optional.ofNullable(classDateFormatMap.get(clazz)).orElse(new HashMap<>());
            dateFormatMap.putIfAbsent(field.getName(), DEFAULT_DATE_TIME_FORMAT);
            classDateFormatMap.putIfAbsent(clazz, dateFormatMap);
        }

        entityInfo.getFieldTypeMap().putIfAbsent(field.getName(), fieldType.getType());
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
        String tablePrefix = dbConfig.getIndexPrefix();

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
            RefreshPolicy refreshPolicy = table.refreshPolicy();
            if (RefreshPolicy.GLOBAL.equals(refreshPolicy)) {
                refreshPolicy = dbConfig.getRefreshPolicy();
            }
            entityInfo.setRefreshPolicy(refreshPolicy);
        }

        String targetIndexName = indexName;
        if (StringUtils.isNotBlank(tablePrefix) && tablePrefixEffect) {
            targetIndexName = tablePrefix + targetIndexName;
        }
        entityInfo.setIndexName(targetIndexName);
    }

    /**
     * 初始化索引settings
     *
     * @param clazz      实体类
     * @param entityInfo 实体信息
     */

    @SneakyThrows
    private static void initSettings(Class<?> clazz, EntityInfo entityInfo) {
        Settings settings = clazz.getAnnotation(Settings.class);
        Optional.ofNullable(settings).ifPresent(i -> {
            entityInfo.getSettingsMap().put(REPLICAS_FIELD, i.replicasNum());
            entityInfo.getSettingsMap().put(SHARDS_FIELD, i.shardsNum());
            entityInfo.getSettingsMap().put(MAX_RESULT_WINDOW_FIELD, i.maxResultWindow());
            if (StringUtils.isNotBlank(i.refreshInterval())) {
                entityInfo.getSettingsMap().put(REFRESH_INTERVAL_FIELD, i.refreshInterval());
            }
        });
        Class<? extends DefaultSettingsProvider> provider = settings == null ? DefaultSettingsProvider.class : settings.settingsProvider();
        Object instance = provider.getConstructor(new Class[]{}).newInstance(new Object[]{});
        Method method = provider.getDeclaredMethod(GET_SETTINGS_METHOD);
        Object invoke = method.invoke(instance);
        if (invoke instanceof Map) {
            Map<String, Object> settingsMap = (Map<String, Object>) invoke;
            if (CollectionUtils.isNotEmpty(settingsMap)) {
                settingsMap.forEach((k, v) -> entityInfo.getSettingsMap().putIfAbsent(k, v));
            }
        }
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
        entityInfo.getColumnMappingMap().putIfAbsent(mappingColumn, field.getName());
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

    /**
     * 根据配置,驼峰转下划线
     *
     * @param origin    原始字段
     * @param camelCase 是否转换
     * @return 转换后字段
     */
    private static String camelToUnderline(String origin, boolean camelCase) {
        if (camelCase) {
            origin = StringUtils.camelToUnderline(origin);
        }
        return origin;
    }

    /**
     * 初始化类与日期格式化关系map
     *
     * @param fieldType  字段类型
     * @param fieldName  字段名
     * @param entityInfo 实体类信息缓存
     * @param clazz      实体类
     * @param dateFormat 日期格式化pattern
     */
    private static void initClassDateFormatMap(FieldType fieldType, String fieldName, EntityInfo entityInfo, Class<?> clazz, String dateFormat) {
        if (FieldType.DATE.equals(fieldType)) {
            Map<Class<?>, Map<String, String>> classDateFormatMap = entityInfo.getClassDateFormatMap();
            Map<String, String> dateFormatMap = Optional.ofNullable(classDateFormatMap.get(clazz)).orElse(new HashMap<>());
            dateFormatMap.putIfAbsent(fieldName, dateFormat);
            classDateFormatMap.putIfAbsent(clazz, dateFormatMap);
        }
    }

    /**
     * 日期按注解中指定的pattern格式化
     *
     * @param name             字段名
     * @param value            值
     * @param columnMappingMap 字段与es索引字段映射关系map
     * @param dateFormatMap    日期格式化patternMap
     * @return 格式化后的日期
     */
    private static Object formatDate(String name, Object value, Map<String, String> columnMappingMap, Map<String, String> dateFormatMap) {
        return Optional.ofNullable(columnMappingMap.get(name))
                .flatMap(i -> Optional.ofNullable(dateFormatMap.get(i)))
                .map(pattern -> {
                    if (value instanceof Date) {
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                        return sdf.format(value);
                    } else if (value instanceof LocalDate) {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
                        return ((LocalDate) value).format(dtf);
                    } else if (value instanceof LocalDateTime) {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
                        return ((LocalDateTime) value).format(dtf);
                    }
                    return value;
                }).orElse(value);
    }

}
