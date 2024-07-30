package org.dromara.easyes.core.biz;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.alibaba.fastjson.serializer.SerializeFilter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.easyes.annotation.rely.IdType;
import org.dromara.easyes.annotation.rely.JoinField;
import org.dromara.easyes.annotation.rely.RefreshPolicy;
import org.dromara.easyes.common.constants.BaseEsConstants;
import org.elasticsearch.client.RequestOptions;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.dromara.easyes.annotation.rely.AnnotationConstants.DEFAULT_MAX_RESULT_WINDOW;
import static org.dromara.easyes.common.constants.BaseEsConstants.ZERO;

/**
 * 实体类信息
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@Accessors(chain = true)
public class EntityInfo {
    /**
     * 请求配置 默认值为官方内置的默认配置
     */
    private RequestOptions requestOptions = RequestOptions.DEFAULT;
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
     * 配置的路由
     */
    private String routing;
    /**
     * 新索引名(由EE在更新索引时自动创建)
     */
    private String releaseIndexName;
    /***
     * 重试成功的索引名
     */
    private String retrySuccessIndexName;
    /**
     * 最大返回数
     */
    private Integer maxResultWindow = DEFAULT_MAX_RESULT_WINDOW;
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
     * 得分字段名
     */
    private String scoreField;
    /**
     * 得分保留小数位,默认不处理,保持es返回值,效率更高
     */
    private int scoreDecimalPlaces = ZERO;
    /**
     * 距离字段名列表
     */
    private List<String> distanceFields = new ArrayList<>();
    /**
     * 距离保留小数位 每个排序器可自定义其保留位数,默认不处理,保持es返回值,效率更高
     */
    private List<Integer> distanceDecimalPlaces = new ArrayList<>();
    /**
     * join字段名称
     */
    private String joinFieldName;
    /**
     * join类型别名
     */
    private String joinAlias;
    /**
     * join类型 父别名
     */
    private String parentJoinAlias;
    /**
     * join关系字段类 默认为JoinField.class
     */
    private Class<?> joinFieldClass = JoinField.class;
    /**
     * 嵌套类的字段信息列表
     */
    private Map<Class<?>, List<EntityFieldInfo>> nestedFieldListMap = new HashMap<>();
    /**
     * 表字段信息列表
     */
    private List<EntityFieldInfo> fieldList;
    /**
     * join类型-子字段信息列表
     */
    private List<EntityFieldInfo> childFieldList = new ArrayList<>();
    /**
     * 标记id字段属于哪个类
     */
    private Class<?> clazz;
    /**
     * 父子类型,子类
     */
    private Class<?> childClass;
    /**
     * 父子类型-是否子文档 默认为否
     */
    private boolean child = false;
    /**
     * 父子类型是否开启急切全局序数
     */
    private boolean eagerGlobalOrdinals = true;
    /**
     * 当前主类的高亮字段列表
     */
    private List<HighLightParam> highlightParams = new ArrayList<>();
    /**
     * 嵌套类-高亮字段列表
     */
    private Map<Class<?>, List<HighLightParam>> nestedHighLightParamsMap = new HashMap<>();
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
     * 嵌套类 实体字段->高亮返回结果字段
     */
    private final Map<Class<?>, Map<String, String>> nestedHighlightFieldMap = new HashMap<>();
    /**
     * 实体字段名->es字段类型
     */
    private final Map<String, String> fieldTypeMap = new HashMap<>();
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
     * 嵌套类型 实体字段名->字段类型
     */
    private final Map<Class<?>, Map<String, String>> nestedClassFieldTypeMap = new HashMap<>();
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
     * 父子类型 join字段名字 K为父,v为子
     */
    private final Map<String, Object> relationMap = new HashMap<>();
    /**
     * 父子类型 join字段类 K为父,v为子
     */
    private final Map<Class<?>, List<Class<?>>> relationClassMap = new HashMap<>();
    /**
     * 数据刷新策略
     */
    private RefreshPolicy refreshPolicy;
    /**
     * 通过自定义注解指定的索引settings
     */
    private final Map<String, Object> settingsMap = new HashMap<>();
    /**
     * 日期字段格式规则Map
     */
//    private Map<String, String> dateFormatMap = new HashMap<>();
    private Map<Class<?>, Map<String, String>> classDateFormatMap = new HashMap<>();

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
        return Optional.ofNullable(mappingColumnMap.get(column)).orElse(column);
    }

    /**
     * 获取es字段映射实体字段名
     *
     * @param column es中的字段名
     * @return 字段名
     */
    public String getColumnMapping(String column) {
        return Optional.ofNullable(columnMappingMap.get(column)).orElse(column);
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
                .orElse(Collections.emptyMap());
    }


}
