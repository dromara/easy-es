package org.dromara.easyes.core.kernel;

import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.dromara.easyes.common.enums.AggregationTypeEnum;
import org.dromara.easyes.common.enums.EsQueryTypeEnum;
import org.dromara.easyes.common.utils.*;
import org.dromara.easyes.core.biz.*;
import org.dromara.easyes.core.cache.GlobalConfigCache;
import org.dromara.easyes.core.toolkit.EntityInfoHelper;
import org.dromara.easyes.core.toolkit.FieldUtils;
import org.dromara.easyes.core.toolkit.GeoUtils;
import org.dromara.easyes.core.toolkit.TreeBuilder;
import org.elasticsearch.geometry.Geometry;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;
import static org.dromara.easyes.common.enums.EsQueryTypeEnum.*;
import static org.dromara.easyes.core.toolkit.FieldUtils.*;

/**
 * 核心 wrapper处理类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@NoArgsConstructor
public class WrapperProcessor {

    /**
     * 构建es查询入参
     *
     * @param wrapper     条件
     * @param entityClass 实体类
     * @return ES查询参数
     */
    public static SearchRequest.Builder buildSearchBuilder(Wrapper<?> wrapper, Class<?> entityClass) {
        // 初始化boolQueryBuilder 参数
        BoolQuery.Builder boolQueryBuilder = initBoolQueryBuilder(wrapper.paramQueue, entityClass);

        // 初始化searchSourceBuilder 参数
        SearchRequest.Builder searchSourceBuilder = initSearchBuilder(wrapper, entityClass);

        // 设置boolQuery参数
        searchSourceBuilder.query(x -> x.bool(boolQueryBuilder.build()));
        return searchSourceBuilder;
    }


    /**
     * 初始化将树参数转换为BoolQueryBuilder
     *
     * @param paramList   参数列表
     * @param entityClass 实体类
     * @return BoolQueryBuilder
     */
    public static BoolQuery.Builder initBoolQueryBuilder(List<Param> paramList, Class<?> entityClass) {
        // 建立参数森林（无根树）
        List<Param> rootList = paramList.stream().filter(i -> Objects.isNull(i.getParentId())).collect(Collectors.toList());
        TreeBuilder treeBuilder = new TreeBuilder(rootList, paramList);
        List<Param> tree = (List<Param>) treeBuilder.build();
        BoolQuery.Builder rootBool = QueryBuilders.bool();

        // 对森林的每个根节点递归封装 这里看似简单实则很绕很烧脑 整个框架的核心 主要依托树的递归 深度优先遍历 森林 还原lambda条件构造语句
        return getBool(tree, rootBool, EntityInfoHelper.getEntityInfo(entityClass), null);
    }

    /**
     * 递归封装bool查询条件
     *
     * @param bool  BoolQueryBuilder
     * @param param 查询参数
     */
    @SneakyThrows
    private static void initBool(BoolQuery.Builder bool, Param param, EntityInfo entityInfo,
                                 Map<String, String> mappingColumnMap, Map<String, String> fieldTypeMap) {
        List<Param> children = (List<Param>) param.getChildren();
        Query query;
        String realField;
        switch (param.getQueryTypeEnum()) {
            case OR:
            case NOT:
            case FILTER:
                // 渣男行为,*完就不认人了,因为拼接类型在AbstractWrapper中已处理过了 直接跳过
                break;
            case MIX:
                setBool(bool, param.getQuery(), param.getPrevQueryType());
                break;
            case TERM:
                realField = getRealFieldAndSuffix(param.getColumn(), fieldTypeMap, mappingColumnMap, entityInfo);
                query = Query.of(q -> q.term(p ->
                        p.field(realField).value(fieldValue(param.getVal())).boost(param.getBoost())));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case MATCH:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                query = Query.of(q -> q.match(p ->
                        p.field(realField).query(fieldValue(param.getVal())).boost(param.getBoost())));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case MATCH_PHRASE:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                query = Query.of(q -> q.matchPhrase(p ->
                        p.field(realField).query((String) param.getVal()).boost(param.getBoost())));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case MATCH_PHRASE_PREFIX:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                query = Query.of(q -> q.matchPhrasePrefix(p ->
                        p.field(realField).query((String) param.getVal()).boost(param.getBoost()).maxExpansions((int) param.getExt1())));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case MULTI_MATCH:
                List<String> realFields = getRealFields(param.getColumns(), mappingColumnMap, entityInfo);
                query = Query.of(q -> q.multiMatch(p -> p.query((String) param.getVal()).fields(realFields)
                        .operator((Operator) param.getExt1()).minimumShouldMatch(param.getExt2() + PERCENT_SIGN)));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case MATCH_ALL:
                query = Query.of(q -> q.matchAll(p -> p.boost(param.getBoost())));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case QUERY_STRING:
                query = Query.of(q -> q.queryString(p -> p.query(param.getColumn()).boost(param.getBoost())));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case PREFIX:
                realField = getRealFieldAndSuffix(param.getColumn(), fieldTypeMap, mappingColumnMap, entityInfo);
                query = Query.of(q -> q.prefix(p -> p.field(realField).value((String) param.getVal()).boost(param.getBoost())));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case GT:
                realField = getRealFieldAndSuffix(param.getColumn(), fieldTypeMap, mappingColumnMap, entityInfo);
                query = Query.of(q -> q.range(p -> p
                        .field(realField)
                        .gt(JsonData.of(param.getVal()))
                        .timeZone(param.getExt1() == null ? null : ((ZoneId) param.getExt1()).getId())
                        .format((String)param.getExt2())
                        .boost(param.getBoost())
                ));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case GE:
                realField = getRealFieldAndSuffix(param.getColumn(), fieldTypeMap, mappingColumnMap, entityInfo);
                query = Query.of(q -> q.range(p -> p
                        .field(realField)
                        .gte(JsonData.of(param.getVal()))
                        .timeZone(param.getExt1() == null ? null : ((ZoneId) param.getExt1()).getId())
                        .format((String)param.getExt2())
                        .boost(param.getBoost())
                ));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case LT:
                realField = getRealFieldAndSuffix(param.getColumn(), fieldTypeMap, mappingColumnMap, entityInfo);
                query = Query.of(q -> q.range(p -> p
                        .field(realField)
                        .lt(JsonData.of(param.getVal()))
                        .timeZone(param.getExt1() == null ? null : ((ZoneId) param.getExt1()).getId())
                        .format((String)param.getExt2())
                        .boost(param.getBoost())
                ));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case LE:
                realField = getRealFieldAndSuffix(param.getColumn(), fieldTypeMap, mappingColumnMap, entityInfo);
                query = Query.of(q -> q.range(p -> p
                        .field(realField)
                        .lte(JsonData.of(param.getVal()))
                        .timeZone(param.getExt1() == null ? null : ((ZoneId) param.getExt1()).getId())
                        .format((String)param.getExt2())
                        .boost(param.getBoost())
                ));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case BETWEEN:
                realField = getRealFieldAndSuffix(param.getColumn(), fieldTypeMap, mappingColumnMap, entityInfo);
                query = Query.of(q -> q.range(p -> p
                        .field(realField)
                        .gte(JsonData.of(param.getExt1()))
                        .lte(JsonData.of(param.getExt2()))
                        .timeZone(param.getExt3() == null ? null : ((ZoneId) param.getExt3()).getId())
                        .format((String)param.getExt4())
                        .boost(param.getBoost())
                ));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case WILDCARD:
                realField = getRealFieldAndSuffix(param.getColumn(), fieldTypeMap, mappingColumnMap, entityInfo);
                query = Query.of(q -> q.wildcard(p -> p.field(realField).value((String) param.getVal()).boost(param.getBoost())));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case TERMS:
                realField = getRealFieldAndSuffix(param.getColumn(), fieldTypeMap, mappingColumnMap, entityInfo);
                List<FieldValue> fieldValueList = ((Collection<?>) param.getVal()).stream()
                        .map(WrapperProcessor::fieldValue).collect(Collectors.toList());
                query = Query.of(q -> q.terms(p -> p.field(realField).terms(t -> t.value(fieldValueList))));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case EXISTS:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                query = Query.of(q -> q.exists(p -> p.field(realField).boost(param.getBoost())));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case GEO_BOUNDING_BOX:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                query = Query.of(q -> q.geoBoundingBox(p -> p.field(realField)
                        .boundingBox(x -> x.tlbr(y -> y
                                .topLeft((GeoLocation) param.getExt1())
                                .bottomRight((GeoLocation) param.getExt2())
                        ))
                        .boost(param.getBoost())));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case GEO_DISTANCE:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                query = Query.of(q -> q.geoDistance(p -> {
                    String unit = param.getExt1() == null ? DistanceUnit.Kilometers.jsonValue() : ((DistanceUnit) param.getExt1()).jsonValue();
                    Double distance = (Double) param.getVal();
                    p.boost(param.getBoost())
                            .field(realField)
                            .location((GeoLocation) param.getExt2())
                            .distance(distance + unit)
                            .distanceType(GeoDistanceType.Arc)
                            .validationMethod(GeoValidationMethod.Strict);
                    return p;
                }));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case GEO_POLYGON:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                query = Query.of(q -> q.geoPolygon(p -> p.field(realField).polygon(x ->
                        x.points((List<GeoLocation>) param.getVal()))));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case GEO_SHAPE_ID:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                query = Query.of(q -> q.geoShape(p -> p.field(realField).shape(x ->
                        x.indexedShape(y -> y.id((String) param.getVal())))));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case GEO_SHAPE:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                query = QueryBuilders.geoShape()
                        .field(realField)
                        .shape(x -> x
//                                .shape(JsonData.of(WellKnownText.toWKT(val)))
                                        .shape(JsonData.of(GeoUtils.toMap((Geometry) param.getVal())))
                                        .relation((GeoShapeRelation) param.getExt1())
                        )
                        .boost(param.getBoost())
                        .build()._toQuery();
                setBool(bool, query, param.getPrevQueryType());
                break;
            case PARENT_ID:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                query = Query.of(q -> q.parentId(p -> p.type(realField).id((String) param.getVal())));
                setBool(bool, query, param.getPrevQueryType());
                break;
            // 下面几种特殊嵌套类型 需要对孩子节点递归处理
            case NESTED_AND:
            case NESTED_FILTER:
            case NESTED_NOT:
            case NESTED_OR:
                query = Query.of(t -> t.bool(b -> getBool(children, b, entityInfo, null)));
                setBool(bool, query, param.getPrevQueryType());
                break;
            case NESTED:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                String[] split = param.getColumn().split(SIGN);
                String path = split[split.length - 1];
                query = Query.of(b -> b.bool(x -> getBool(children, x, entityInfo, path)));
                NestedQuery.Builder nestedQueryBuilder = QueryBuilders.nested()
                        .path(realField).query(query)
                        .scoreMode((ChildScoreMode) param.getVal());
                // 设置嵌套类型高亮查询参数
                setNestedHighlight(path, param.getColumn(), nestedQueryBuilder, entityInfo);
                // 设置bool查询参数
                setBool(bool, Query.of(x -> x.nested(nestedQueryBuilder.build())), param.getPrevQueryType());
                break;
            case HAS_PARENT:
                // 如果用户没指定type框架可根据entityInfo上下文自行推断出其父type
                String column = Optional.ofNullable(param.getColumn()).orElse(entityInfo.getParentJoinAlias());
                realField = getRealField(column, mappingColumnMap, entityInfo);
                query = Query.of(t -> t.bool(b -> getBool(children, b, entityInfo, param.getColumn())));
                HasParentQuery.Builder hasParentQueryBuilder = new HasParentQuery.Builder()
                        .ignoreUnmapped(false)
                        .parentType(realField)
                        .query(query)
                        .score((boolean) param.getVal());
                setBool(bool, Query.of(x -> x.hasParent(hasParentQueryBuilder.build())), param.getPrevQueryType());
                break;
            case HAS_CHILD:
                realField = getRealField(param.getColumn(), mappingColumnMap, entityInfo);
                query = Query.of(t -> t.bool(b -> getBool(children, b, entityInfo, param.getColumn())));
                HasChildQuery.Builder hasChildQueryBuilder = new HasChildQuery.Builder()
                        .minChildren(1)
                        .maxChildren(Integer.MAX_VALUE)
                        .ignoreUnmapped(false)
                        .type(realField)
                        .query(query)
                        .minChildren(1)
                        .scoreMode((ChildScoreMode) param.getVal());
                setBool(bool, Query.of(x -> x.hasChild(hasChildQueryBuilder.build())), param.getPrevQueryType());
                break;
            default:
                // just ignore,almost never happen
                throw ExceptionUtils.eee("非法参数类型");
        }
    }

    /**
     * 获取FieldValue
     *
     * @param val 原字段值
     * @return FieldValue
     */
    public static FieldValue fieldValue(Object val) {
        if (val == null) {
            return FieldValue.NULL;
        }

        if (val instanceof FieldValue) {
            return (FieldValue) val;
        } else if (val instanceof Long) {
            return FieldValue.of((long) val);
        } else if (val instanceof Integer) {
            return FieldValue.of((int) val);
        } else if (val instanceof Double) {
            return FieldValue.of((double) val);
        } else if (val instanceof Boolean) {
            return FieldValue.of((boolean) val);
        } else if (val instanceof String) {
            return FieldValue.of((String) val);
        }
        return FieldValue.of(val);
    }

    /**
     * 设置嵌套类型高亮查询参数
     *
     * @param path               嵌套path
     * @param column             字段
     * @param nestedQueryBuilder 嵌套查询条件构造器
     * @param entityInfo         实体信息缓存
     */
    private static void setNestedHighlight(String path, String column, NestedQuery.Builder nestedQueryBuilder, EntityInfo entityInfo) {
        // 嵌套类型的高亮查询语句构造
        Class<?> pathClass = entityInfo.getPathClassMap().get(path);
        Optional.ofNullable(pathClass)
                .flatMap(i -> Optional.ofNullable(entityInfo.getNestedOrObjectHighLightParamsMap().get(i)))
                .ifPresent(i -> {
                    // 嵌套类型高亮字段名需要完整的path 例如users.faqs 所以此处用param.column而非path
                    Highlight.Builder highlightBuilder = initHighlightBuilder(i, column);
                    Optional.ofNullable(highlightBuilder)
                            .ifPresent(p -> nestedQueryBuilder.innerHits(x -> x.highlight(p.build())));
                });
    }

    /**
     * 设置节点的bool
     *
     * @param bool         根节点BoolQueryBuilder
     * @param queryBuilder 非根节点BoolQueryBuilder
     * @param parentType   查询类型
     */
    private static void setBool(BoolQuery.Builder bool, Query queryBuilder, EsQueryTypeEnum parentType) {
        if (NESTED_AND.equals(parentType)) {
            bool.must(queryBuilder);
        } else if (NESTED_OR.equals(parentType)) {
            bool.should(queryBuilder);
        } else if (NESTED_FILTER.equals(parentType)) {
            bool.filter(queryBuilder);
        } else if (NESTED_NOT.equals(parentType)) {
            bool.mustNot(queryBuilder);
        } else {
            // by default
            bool.must(queryBuilder);
        }
    }

    /**
     * 递归获取子节点的bool
     *
     * @param paramList  子节点参数列表
     * @param builder    新的根bool
     * @param entityInfo 实体信息缓存
     * @param path       路径
     * @return 子节点bool合集, 统一封装至入参builder中
     */
    private static BoolQuery.Builder getBool(List<Param> paramList, BoolQuery.Builder builder, EntityInfo entityInfo, String path) {
        if (CollectionUtils.isEmpty(paramList)) {
            return builder;
        }

        // 获取字段名称映射关系以及字段类型映射关系
        Map<String, String> mappingColumnMap;
        Map<String, String> fieldTypeMap;
        if (StringUtils.isNotBlank(path)) {
            // 嵌套类型
            Class<?> clazz = entityInfo.getPathClassMap().get(path);
            mappingColumnMap = Optional.ofNullable(entityInfo.getNestedOrObjectClassMappingColumnMap().get(clazz))
                    .orElse(Collections.emptyMap());
            fieldTypeMap = Optional.ofNullable(entityInfo.getNestedOrObjectClassFieldTypeMap().get(clazz))
                    .orElse(Collections.emptyMap());
        } else {
            mappingColumnMap = entityInfo.getMappingColumnMap();
            fieldTypeMap = entityInfo.getFieldTypeMap();
        }

        // 批量初始化每一个参数至BoolQueryBuilder
        paramList.forEach(param -> initBool(builder, param, entityInfo, mappingColumnMap, fieldTypeMap));
        return builder;
    }


    /**
     * 初始化SearchSourceBuilder
     *
     * @param wrapper 条件
     * @return SearchSourceBuilder
     */
    private static SearchRequest.Builder initSearchBuilder(Wrapper<?> wrapper, Class<?> entityClass) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        // 获取自定义字段map
        Map<String, String> mappingColumnMap = entityInfo.getMappingColumnMap();

        SearchRequest.Builder builder = new SearchRequest.Builder();

        // 设置高亮
        setHighLight(entityInfo.getHighlightParams(), builder);

        // 设置用户指定的各种排序规则
        setSort(wrapper, mappingColumnMap, entityInfo, builder);

        // 设置查询或不查询字段
        setFetchSource(wrapper, mappingColumnMap, entityInfo, builder);

        // 设置排除_score 小于 min_score 中指定的最小值的文档
        Optional.ofNullable(wrapper.minScore).ifPresent(builder::minScore);

        // 设置自定义排序时(如 脚本里面使用 _score) 是否计算分数
        Optional.ofNullable(wrapper.trackScores).ifPresent(builder::trackScores);

        // 设置聚合参数
        setAggregations(wrapper, mappingColumnMap, entityInfo, builder);

        // 设置查询起止参数
        Optional.ofNullable(wrapper.from).ifPresent(builder::from);
        MyOptional.ofNullable(wrapper.size).ifPresent(builder::size,
                entityInfo.getMaxResultWindow() != null ? entityInfo.getMaxResultWindow() : DEFAULT_SIZE);

        // 根据全局配置决定是否开启全部查询
        if (GlobalConfigCache.getGlobalConfig().getDbConfig().isEnableTrackTotalHits()) {
            builder.trackTotalHits(x -> x.enabled(true));
        }

        return builder;
    }

    /**
     * 获取兜底索引名称
     * @param entityClass 实体类
     * @param indexName 索引名
     * @param <T> 泛型
     * @return 索引名称
     */
    public static <T> String getIndexName(Class<T> entityClass, String indexName) {
        // 优先按wrapper中指定的索引名,若未指定则取当前全局激活的索引名
        if (StringUtils.isBlank(indexName)) {
            return EntityInfoHelper.getEntityInfo(entityClass).getIndexName();
        }
        return indexName;
    }

    /**
     * 获取兜底索引名称数组
     *
     * @param entityClass 实体类
     * @param indexNames 原始索引名称数组
     * @param <T> 泛型
     * @return 目标索引名称数组
     */
    public static <T> List<String> getIndexName(Class<T> entityClass, String[] indexNames) {
        // 碰到傻狍子用户锤子索引都没指定, 给个兜底
        if (ArrayUtils.isEmpty(indexNames)) {
            return Collections.singletonList(EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
        }

        // 指定了个空字符串之类的,需要给兜底
        return Arrays.stream(indexNames)
                .map(indexName -> getIndexName(entityClass, indexName))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取兜底索引名称数组
     * @param entityClass 实体类
     * @param indexNames 原始索引名称数组
     * @param <T> 泛型
     * @return 目标索引名称数组
     */
    public static <T> List<String> getIndexName(Class<T> entityClass, Collection<String> indexNames) {
        // 碰到傻狍子用户锤子索引都没指定, 给个兜底
        if (CollectionUtils.isEmpty(indexNames)) {
            return Collections.singletonList(EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
        }

        // 指定了个空字符串之类的,需要给兜底
        return indexNames.stream()
                .map(indexName -> getIndexName(entityClass, indexName))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 查询字段中是否包含id
     *
     * @param idField 字段
     * @param wrapper 条件
     * @return 是否包含的布尔值
     */
    public static boolean includeId(String idField, Wrapper<?> wrapper) {
        if (ArrayUtils.isEmpty(wrapper.include) && ArrayUtils.isEmpty(wrapper.exclude)) {
            // 未设置, 默认返回
            return true;
        } else if (ArrayUtils.isNotEmpty(wrapper.include) && Arrays.asList(wrapper.include).contains(idField)) {
            return true;
        } else {
            return ArrayUtils.isNotEmpty(wrapper.exclude) && !Arrays.asList(wrapper.exclude).contains(idField);
        }
    }

    /**
     * 设置查询/不查询字段列表
     *
     * @param wrapper          参数包装类
     * @param mappingColumnMap 字段映射map
     * @param entityInfo       索引信息
     * @param searchBuilder    查询参数建造者
     */
    private static void setFetchSource(Wrapper<?> wrapper, Map<String, String> mappingColumnMap, EntityInfo entityInfo, SearchRequest.Builder searchBuilder) {
        if (ArrayUtils.isEmpty(wrapper.include) && ArrayUtils.isEmpty(wrapper.exclude)) {
            return;
        }

        // 获取实际字段
        List<String> includes = FieldUtils.getRealFields(wrapper.include, mappingColumnMap, entityInfo);
        List<String> excludes = FieldUtils.getRealFields(wrapper.exclude, mappingColumnMap, entityInfo);
        searchBuilder.source(x -> x.filter(y -> y.includes(includes).excludes(excludes)));
    }


    /**
     * 设置高亮参数
     *
     * @param highLightParams 高亮参数列表
     * @param searchBuilder   查询参数建造者
     */
    private static void setHighLight(List<HighLightParam> highLightParams, SearchRequest.Builder searchBuilder) {
        if (CollectionUtils.isEmpty(highLightParams)) {
            return;
        }

        // 初始化高亮参数
        Highlight.Builder highlightBuilder = initHighlightBuilder(highLightParams, null);
        Optional.ofNullable(highlightBuilder).map(Highlight.Builder::build).ifPresent(searchBuilder::highlight);
    }

    private static Highlight.Builder initHighlightBuilder(List<HighLightParam> highLightParams, String path) {
        if (CollectionUtils.isEmpty(highLightParams)) {
            return null;
        }
        // 封装高亮参数
        Highlight.Builder highlightBuilder = new Highlight.Builder();
        highLightParams.forEach(highLightParam -> {
            if (StringUtils.isNotBlank(highLightParam.getHighLightField())) {
                // 嵌套类型 须追加其完整path前缀
                String highlightField = Optional.ofNullable(path).map(i -> i + STR_SIGN + highLightParam.getHighLightField())
                        .orElse(highLightParam.getHighLightField());
                HighlightField field = HighlightField.of(x -> x
                        .preTags(highLightParam.getPreTag())
                        .postTags(highLightParam.getPostTag())
                        .type(a -> a.custom(highLightParam.getHighLightType().getValue()))
                        .requireFieldMatch(highLightParam.getRequireFieldMatch())
                );
                highlightBuilder.fields(highlightField, field);
                highlightBuilder.fragmentSize(highLightParam.getFragmentSize());
                Optional.ofNullable(highLightParam.getNumberOfFragments()).ifPresent(highlightBuilder::numberOfFragments);
            }
        });
        return highlightBuilder;
    }


    /**
     * 设置排序参数
     *
     * @param wrapper          参数包装类
     * @param mappingColumnMap 字段映射map
     * @param entityInfo       索引信息
     * @param searchBuilder    查询参数建造者
     */
    private static void setSort(Wrapper<?> wrapper, Map<String, String> mappingColumnMap, EntityInfo entityInfo, SearchRequest.Builder searchBuilder) {
        // 批量设置排序字段
        if (CollectionUtils.isNotEmpty(wrapper.baseSortParams)) {
            wrapper.baseSortParams.forEach(baseSortParam -> {
                // 获取es中的实际字段 有可能已经被用户自定义或者驼峰转成下划线
                String realField = Objects.isNull(baseSortParam.getSortField()) ?
                        null : getRealFieldAndSuffix(baseSortParam.getSortField(), mappingColumnMap, entityInfo);
                SortOptions sortBuilder = getSortBuilder(realField, baseSortParam);
                Optional.ofNullable(sortBuilder).ifPresent(searchBuilder::sort);
            });
        }

        // 设置以String形式指定的自定义排序字段及规则(此类排序通常由前端传入,满足部分用户个性化需求)
        if (CollectionUtils.isNotEmpty(wrapper.orderByParams)) {
            wrapper.orderByParams.forEach(orderByParam -> {
                // 排序字段名
                String orderColumn = orderByParam.getOrder();
                // 获取配置是否开启了驼峰转换
                if (GlobalConfigCache.getGlobalConfig().getDbConfig().isMapUnderscoreToCamelCase()) {
                    orderColumn = StringUtils.camelToUnderline(orderColumn);
                }
                // 设置排序字段
                FieldSort.Builder fieldSortBuilder = new FieldSort.Builder().field(orderColumn);

                // 设置排序规则
                if (SortOrder.Asc.toString().equalsIgnoreCase(orderByParam.getSort())) {
                    fieldSortBuilder.order(SortOrder.Asc);
                }
                if (SortOrder.Desc.toString().equalsIgnoreCase(orderByParam.getSort())) {
                    fieldSortBuilder.order(SortOrder.Desc);
                }
                searchBuilder.sort(x -> x.field(fieldSortBuilder.build()));
            });
        }
    }


    /**
     * 获取排序器
     *
     * @param realField     实际字段名称
     * @param baseSortParam 排序参数
     * @return 排序器
     */
    private static SortOptions getSortBuilder(String realField, BaseSortParam baseSortParam) {
        switch (baseSortParam.getOrderTypeEnum()) {
            case FIELD:
                return SortOptions.of(x -> x.field(y -> y.field(realField).order(baseSortParam.getSortOrder())));
            case SCORE:
                return SortOptions.of(x -> x.score(y -> y.order(baseSortParam.getSortOrder())));
            case GEO:
                return SortOptions.of(x -> x.geoDistance(y -> y
                        .field(realField)
                        .location(baseSortParam.getGeoPoints())
                        .order(baseSortParam.getSortOrder())
                        .distanceType(baseSortParam.getGeoDistanceType())
                        .unit(baseSortParam.getUnit())
                ));
            case CUSTOMIZE:
                return baseSortParam.getSortBuilder();
            default:
                throw new IllegalArgumentException();
        }
    }


    /**
     * 设置聚合参数
     *
     * @param wrapper             参数包装类
     * @param mappingColumnMap    字段映射map
     * @param entityInfo          索引信息
     * @param searchSourceBuilder 查询参数建造者
     */
    private static void setAggregations(Wrapper<?> wrapper, Map<String, String> mappingColumnMap,
                                        EntityInfo entityInfo, SearchRequest.Builder searchSourceBuilder) {
        // 设置折叠(去重)字段
        Optional.ofNullable(wrapper.distinctField)
                .ifPresent(distinctField -> {
                    String realField = getRealFieldAndSuffix(distinctField, mappingColumnMap, entityInfo);
                    searchSourceBuilder.collapse(x -> x.field(realField));
                    searchSourceBuilder.aggregations(REPEAT_NUM_KEY, x -> x.cardinality(y -> y.field(realField)));
                });

        // 其它聚合
        List<AggregationParam> aggregationParamList = wrapper.aggregationParamList;
        if (CollectionUtils.isEmpty(aggregationParamList)) {
            return;
        }

        // 构建聚合树
        String rootName = null;
        Aggregation.Builder.ContainerBuilder root = null;
        Aggregation.Builder.ContainerBuilder cursor = null;
        for (AggregationParam aggParam : aggregationParamList) {
            String realField = getRealFieldAndSuffix(aggParam.getField(), mappingColumnMap, entityInfo);
            Aggregation.Builder.ContainerBuilder builder = getRealAggregationBuilder(
                    aggParam.getAggregationType(), realField, wrapper.size, wrapper.bucketOrders);
            // 解决同一个字段聚合多次，如min(starNum), max(starNum) 字段名重复问题
            String aggName = aggParam.getName() + aggParam.getAggregationType().getValue();
            if (aggParam.isEnablePipeline()) {
                // 管道聚合, 构造聚合树
                if (root == null) {
                    root = builder;
                    rootName = aggName;
                    cursor = root;
                } else {
                    Aggregation agg = builder.build();
                    cursor.aggregations(aggName, agg);
                    // 解决max、min、avg和sum聚合函数不支持sub-aggregations的问题
                    if (agg._kind().equals(Aggregation.Kind.Terms)) {
                        cursor = builder;
                    }
                }
            } else {
                // 非管道聚合
                if (builder != null) {
                    searchSourceBuilder.aggregations(aggName, builder.build());
                }
            }

        }
        if (root != null) {
            searchSourceBuilder.aggregations(rootName, root.build());
        }

        if (!GlobalConfigCache.getGlobalConfig().getDbConfig().isEnableAggHits()) {
            // 配置关闭了聚合返回结果集Hits, 可提升查询效率
            wrapper.size = ZERO;
        }
    }

    /**
     * 根据聚合类型获取具体的聚合建造者
     *
     * @param aggType   聚合类型
     * @param realField 原字段名称
     * @param size      聚合桶大小
     * @return 聚合建造者
     */
    private static Aggregation.Builder.ContainerBuilder getRealAggregationBuilder(
            AggregationTypeEnum aggType,
            String realField,
            Integer size,
            List<NamedValue<SortOrder>> bucketOrders
    ) {
        // 解决同一个字段聚合多次，如min(starNum), max(starNum) 字段名重复问题
        switch (aggType) {
            case AVG:
                return new Aggregation.Builder().avg(x -> x.field(realField));
            case MIN:
                return new Aggregation.Builder().min(x -> x.field(realField));
            case MAX:
                return new Aggregation.Builder().max(x -> x.field(realField));
            case SUM:
                return new Aggregation.Builder().sum(x -> x.field(realField));
            case TERMS:
                return new Aggregation.Builder().terms(x -> {
                    x.field(realField);
                    Optional.ofNullable(size).ifPresent(x::size);
                    Optional.ofNullable(bucketOrders).ifPresent(x::order);
                    return x;
                });
            default:
                throw new IllegalArgumentException();
        }
    }
}
