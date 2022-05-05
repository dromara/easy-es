package com.xpc.easyes.core.conditions;

import com.xpc.easyes.core.cache.GlobalConfigCache;
import com.xpc.easyes.core.common.EntityInfo;
import com.xpc.easyes.core.config.GlobalConfig;
import com.xpc.easyes.core.enums.AggregationTypeEnum;
import com.xpc.easyes.core.enums.BaseEsParamTypeEnum;
import com.xpc.easyes.core.enums.EsAttachTypeEnum;
import com.xpc.easyes.core.params.AggregationParam;
import com.xpc.easyes.core.params.BaseEsParam;
import com.xpc.easyes.core.params.GeoParam;
import com.xpc.easyes.core.toolkit.*;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.*;
import java.util.stream.Collectors;

import static com.xpc.easyes.core.constants.BaseEsConstants.*;
import static com.xpc.easyes.core.enums.BaseEsParamTypeEnum.*;

/**
 * 核心 wrpeer处理类
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
    public static SearchSourceBuilder buildSearchSourceBuilder(LambdaEsQueryWrapper<?> wrapper, Class<?> entityClass) {
        // 初始化boolQueryBuilder 参数
        BoolQueryBuilder boolQueryBuilder = initBoolQueryBuilder(wrapper.baseEsParamList, entityClass);

        // 初始化全表扫描查询参数
        Optional.ofNullable(wrapper.matchAllQuery).ifPresent(p -> boolQueryBuilder.must(QueryBuilders.matchAllQuery()));

        // 初始化searchSourceBuilder 参数
        SearchSourceBuilder searchSourceBuilder = initSearchSourceBuilder(wrapper, entityClass);

        // 初始化geo相关参数
        Optional.ofNullable(wrapper.geoParam).ifPresent(geoParam -> setGeoQuery(geoParam, boolQueryBuilder, entityClass));

        // 设置boolQuery参数
        searchSourceBuilder.query(boolQueryBuilder);
        return searchSourceBuilder;
    }

    /**
     * 初始化BoolQueryBuilder 整个框架的核心
     *
     * @param baseEsParamList 基础参数列表
     * @param entityClass     实体类
     * @return BoolQueryBuilder
     */
    public static BoolQueryBuilder initBoolQueryBuilder(List<BaseEsParam> baseEsParamList, Class<?> entityClass) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 用于连接and,or条件内的多个查询条件,包装成boolQuery
        BoolQueryBuilder inner = null;
        // 是否有外层or
        boolean hasOuterOr = false;
        for (int i = 0; i < baseEsParamList.size(); i++) {
            BaseEsParam baseEsParam = baseEsParamList.get(i);
            boolean hasLogicOperator = Objects.equals(BaseEsParamTypeEnum.AND_LEFT_BRACKET.getType(), baseEsParam.getType())
                    || Objects.equals(OR_LEFT_BRACKET.getType(), baseEsParam.getType());
            if (hasLogicOperator) {
                // 说明有and或者or
                for (int j = i + 1; j < baseEsParamList.size(); j++) {
                    if (Objects.equals(baseEsParamList.get(j).getType(), OR_ALL.getType())) {
                        // 说明左括号内出现了内层or查询条件
                        for (int k = i + 1; k < j; k++) {
                            // 内层or只会出现在中间,此处将内层or之前的查询条件类型进行处理
                            BaseEsParam.setUp(baseEsParamList.get(k));
                        }
                    }
                }
                inner = QueryBuilders.boolQuery();
            }

            // 此处处理所有内外层or后面的查询条件类型
            if (Objects.equals(baseEsParam.getType(), OR_ALL.getType())) {
                hasOuterOr = true;
            }
            if (hasOuterOr) {
                BaseEsParam.setUp(baseEsParam);
            }

            // 处理括号中and和or的最终连接类型 and->must, or->should
            if (Objects.equals(AND_RIGHT_BRACKET.getType(), baseEsParam.getType())) {
                boolQueryBuilder.must(inner);
                inner = null;
            }
            if (Objects.equals(OR_RIGHT_BRACKET.getType(), baseEsParam.getType())) {
                boolQueryBuilder.should(inner);
                inner = null;
            }

            // 添加字段名称,值,查询类型等
            if (Objects.isNull(inner)) {
                addQuery(baseEsParam, boolQueryBuilder, entityInfo);
            } else {
                addQuery(baseEsParam, inner, entityInfo);
            }
        }
        return boolQueryBuilder;
    }

    /**
     * 初始化SearchSourceBuilder
     *
     * @param wrapper 条件
     * @return SearchSourceBuilder
     */
    private static SearchSourceBuilder initSearchSourceBuilder(LambdaEsQueryWrapper<?> wrapper, Class<?> entityClass) {
        // 获取自定义字段map
        Map<String, String> mappingColumnMap = EntityInfoHelper.getEntityInfo(entityClass).getMappingColumnMap();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 设置高亮
        setHighLight(wrapper, mappingColumnMap, searchSourceBuilder);

        // 设置用户指定的各种排序规则
        setSort(wrapper, mappingColumnMap, searchSourceBuilder);

        // 设置查询或不查询字段
        setFetchSource(wrapper, mappingColumnMap, searchSourceBuilder);

        // 设置聚合参数
        setAggregations(wrapper, mappingColumnMap, searchSourceBuilder);

        // 设置查询起止参数
        Optional.ofNullable(wrapper.from).ifPresent(searchSourceBuilder::from);
        MyOptional.ofNullable(wrapper.size).ifPresent(searchSourceBuilder::size, DEFAULT_SIZE);

        // 查询超过一万条, trackTotalHists自动开启
        if (searchSourceBuilder.size() > DEFAULT_SIZE) {
            searchSourceBuilder.trackTotalHits(true);
        }

        return searchSourceBuilder;
    }

    /**
     * 初始化GeoBoundingBoxQueryBuilder
     *
     * @param geoParam Geo相关参数
     * @return GeoBoundingBoxQueryBuilder
     */
    private static GeoBoundingBoxQueryBuilder initGeoBoundingBoxQueryBuilder(GeoParam geoParam) {
        // 参数校验
        boolean invalidParam = Objects.isNull(geoParam)
                || (Objects.isNull(geoParam.getTopLeft()) || Objects.isNull(geoParam.getBottomRight()));
        if (invalidParam) {
            return null;
        }

        GeoBoundingBoxQueryBuilder builder = QueryBuilders.geoBoundingBoxQuery(geoParam.getField());
        Optional.ofNullable(geoParam.getBoost()).ifPresent(builder::boost);
        builder.setCorners(geoParam.getTopLeft(), geoParam.getBottomRight());
        return builder;
    }

    /**
     * 初始化GeoDistanceQueryBuilder
     *
     * @param geoParam Geo相关参数
     * @return GeoDistanceQueryBuilder
     */
    private static GeoDistanceQueryBuilder initGeoDistanceQueryBuilder(GeoParam geoParam) {
        // 参数校验
        boolean invalidParam = Objects.isNull(geoParam)
                || (Objects.isNull(geoParam.getDistanceStr()) && Objects.isNull(geoParam.getDistance()));
        if (invalidParam) {
            return null;
        }

        GeoDistanceQueryBuilder builder = QueryBuilders.geoDistanceQuery(geoParam.getField());
        Optional.ofNullable(geoParam.getBoost()).ifPresent(builder::boost);
        // 距离来源: 双精度类型+单位或字符串类型
        Optional.ofNullable(geoParam.getDistanceStr()).ifPresent(builder::distance);
        Optional.ofNullable(geoParam.getDistance())
                .ifPresent(distance -> builder.distance(distance, geoParam.getDistanceUnit()));
        Optional.ofNullable(geoParam.getCentralGeoPoint()).ifPresent(builder::point);
        return builder;
    }

    /**
     * 初始化 GeoPolygonQueryBuilder
     *
     * @param geoParam Geo相关参数
     * @return GeoPolygonQueryBuilder
     */
    private static GeoPolygonQueryBuilder initGeoPolygonQueryBuilder(GeoParam geoParam) {
        // 参数校验
        boolean invalidParam = Objects.isNull(geoParam) || CollectionUtils.isEmpty(geoParam.getGeoPoints());
        if (invalidParam) {
            return null;
        }

        GeoPolygonQueryBuilder builder = QueryBuilders.geoPolygonQuery(geoParam.getField(), geoParam.getGeoPoints());
        Optional.ofNullable(geoParam.getBoost()).ifPresent(builder::boost);
        return builder;
    }

    /**
     * 初始化 GeoShapeQueryBuilder
     *
     * @param geoParam Geo相关参数
     * @return GeoShapeQueryBuilder
     */
    @SneakyThrows
    private static GeoShapeQueryBuilder initGeoShapeQueryBuilder(GeoParam geoParam) {
        // 参数校验
        boolean invalidParam = Objects.isNull(geoParam)
                || (Objects.isNull(geoParam.getIndexedShapeId()) && Objects.isNull(geoParam.getGeometry()));
        if (invalidParam) {
            return null;
        }

        GeoShapeQueryBuilder builder = QueryBuilders.geoShapeQuery(geoParam.getField(), geoParam.getGeometry());
        Optional.ofNullable(geoParam.getShapeRelation()).ifPresent(builder::relation);
        Optional.ofNullable(geoParam.getBoost()).ifPresent(builder::boost);
        return builder;
    }


    /**
     * 设置Geo相关查询参数 geoBoundingBox, geoDistance, geoPolygon, geoShape
     *
     * @param geoParam         geo参数
     * @param boolQueryBuilder boolQuery参数建造者
     */
    private static void setGeoQuery(GeoParam geoParam, BoolQueryBuilder boolQueryBuilder, Class<?> entityClass) {
        // 获取配置信息
        Map<String, String> mappingColumnMap = EntityInfoHelper.getEntityInfo(entityClass).getMappingColumnMap();
        GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();

        // 使用实际字段名称覆盖实体类字段名称
        String realField = getRealField(geoParam.getField(), mappingColumnMap, dbConfig);
        geoParam.setField(realField);

        GeoBoundingBoxQueryBuilder geoBoundingBox = initGeoBoundingBoxQueryBuilder(geoParam);
        doGeoSet(geoParam.isIn(), geoBoundingBox, boolQueryBuilder);

        GeoDistanceQueryBuilder geoDistance = initGeoDistanceQueryBuilder(geoParam);
        doGeoSet(geoParam.isIn(), geoDistance, boolQueryBuilder);

        GeoPolygonQueryBuilder geoPolygon = initGeoPolygonQueryBuilder(geoParam);
        doGeoSet(geoParam.isIn(), geoPolygon, boolQueryBuilder);

        GeoShapeQueryBuilder geoShape = initGeoShapeQueryBuilder(geoParam);
        doGeoSet(geoParam.isIn(), geoShape, boolQueryBuilder);
    }

    /**
     * 根据查询是否在指定范围内设置geo查询过滤条件
     *
     * @param isIn
     * @param queryBuilder
     * @param boolQueryBuilder
     */
    private static void doGeoSet(Boolean isIn, QueryBuilder queryBuilder, BoolQueryBuilder boolQueryBuilder) {
        Optional.ofNullable(queryBuilder)
                .ifPresent(present -> {
                    if (isIn) {
                        boolQueryBuilder.filter(present);
                    } else {
                        boolQueryBuilder.mustNot(present);
                    }
                });
    }


    /**
     * 添加进参数容器
     *
     * @param baseEsParam      基础参数
     * @param boolQueryBuilder es boolQueryBuilder
     */
    private static void addQuery(BaseEsParam baseEsParam, BoolQueryBuilder boolQueryBuilder, EntityInfo entityInfo) {
        baseEsParam.getMustList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.MUST.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));
        // 多字段情形
        baseEsParam.getMustMultiFieldList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(), EsAttachTypeEnum.MUST.getType(),
                        getRealFields(fieldValueModel.getFields(), entityInfo.getMappingColumnMap()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getMinimumShouldMatch(), fieldValueModel.getBoost()));

        baseEsParam.getFilterList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.FILTER.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));

        baseEsParam.getShouldList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.SHOULD.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()),
                        fieldValueModel.getValue() == null ? fieldValueModel.getValues() : fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));

        // 多字段情形
        baseEsParam.getMustMultiFieldList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(), EsAttachTypeEnum.SHOULD.getType(),
                        getRealFields(fieldValueModel.getFields(), entityInfo.getMappingColumnMap()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getMinimumShouldMatch(), fieldValueModel.getBoost()));

        baseEsParam.getMustNotList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.MUST_NOT.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));

        baseEsParam.getGtList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.GT.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));

        baseEsParam.getLtList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.LT.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));

        baseEsParam.getGeList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.GE.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));

        baseEsParam.getLeList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.LE.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));

        baseEsParam.getBetweenList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.BETWEEN.getType(), entityInfo.getMappingColumn(fieldValueModel.getField()),
                        fieldValueModel.getLeftValue(), fieldValueModel.getRightValue(), fieldValueModel.getBoost()));

        baseEsParam.getNotBetweenList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.NOT_BETWEEN.getType(), entityInfo.getMappingColumn(fieldValueModel.getField()),
                        fieldValueModel.getLeftValue(), fieldValueModel.getRightValue(), fieldValueModel.getBoost()));

        baseEsParam.getInList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.IN.getType(), entityInfo.getMappingColumn(fieldValueModel.getField()),
                        fieldValueModel.getValues(), fieldValueModel.getBoost()));

        baseEsParam.getNotInList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.NOT_IN.getType(), entityInfo.getMappingColumn(fieldValueModel.getField()),
                        fieldValueModel.getValues(), fieldValueModel.getBoost()));

        baseEsParam.getIsNullList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.NOT_EXISTS.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()), Optional.empty(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));

        baseEsParam.getNotNullList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.EXISTS.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()), Optional.empty(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));

        baseEsParam.getLikeLeftList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.LIKE_LEFT.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));

        baseEsParam.getLikeRightList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        EsAttachTypeEnum.LIKE_RIGHT.getType(), fieldValueModel.getOriginalAttachType(),
                        entityInfo.getMappingColumn(fieldValueModel.getField()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getBoost()));
    }

    /**
     * 查询字段中是否包含id
     *
     * @param idField 字段
     * @param wrapper 条件
     * @return 是否包含的布尔值
     */
    public static boolean includeId(String idField, LambdaEsQueryWrapper<?> wrapper) {
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
     * @param wrapper             参数包装类
     * @param mappingColumnMap    字段映射map
     * @param searchSourceBuilder 查询参数建造者
     */
    private static void setFetchSource(LambdaEsQueryWrapper<?> wrapper, Map<String, String> mappingColumnMap, SearchSourceBuilder searchSourceBuilder) {
        if (ArrayUtils.isEmpty(wrapper.include) && ArrayUtils.isEmpty(wrapper.exclude)) {
            return;
        }
        // 获取配置
        GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();
        String[] includes = getRealFields(wrapper.include, mappingColumnMap, dbConfig);
        String[] excludes = getRealFields(wrapper.exclude, mappingColumnMap, dbConfig);
        searchSourceBuilder.fetchSource(includes, excludes);
    }


    /**
     * 设置高亮参数
     *
     * @param wrapper             参数包装类
     * @param mappingColumnMap    字段映射map
     * @param searchSourceBuilder 查询参数建造者
     */
    private static void setHighLight(LambdaEsQueryWrapper<?> wrapper, Map<String, String> mappingColumnMap, SearchSourceBuilder searchSourceBuilder) {
        // 获取配置
        GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();

        // 设置高亮字段
        if (!CollectionUtils.isEmpty(wrapper.highLightParamList)) {
            wrapper.highLightParamList.forEach(highLightParam -> {
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                highLightParam.getFields().forEach(field -> {
                    String customField = mappingColumnMap.get(field);
                    if (Objects.nonNull(customField)) {
                        highlightBuilder.field(customField);
                    } else {
                        if (dbConfig.isMapUnderscoreToCamelCase()) {
                            highlightBuilder.field(StringUtils.camelToUnderline(field));
                        } else {
                            highlightBuilder.field(field);
                        }
                    }
                });
                highlightBuilder.preTags(highLightParam.getPreTag());
                highlightBuilder.postTags(highLightParam.getPostTag());
                searchSourceBuilder.highlighter(highlightBuilder);
            });
        }
    }

    /**
     * 设置排序参数
     *
     * @param wrapper             参数包装类
     * @param mappingColumnMap    字段映射map
     * @param searchSourceBuilder 查询参数建造者
     */
    private static void setSort(LambdaEsQueryWrapper<?> wrapper, Map<String, String> mappingColumnMap, SearchSourceBuilder searchSourceBuilder) {
        // 获取配置
        GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();

        // 设置排序字段
        if (CollectionUtils.isNotEmpty(wrapper.sortParamList)) {
            wrapper.sortParamList.forEach(sortParam -> {
                SortOrder sortOrder = sortParam.getIsAsc() ? SortOrder.ASC : SortOrder.DESC;
                sortParam.getFields().forEach(field -> {
                    FieldSortBuilder fieldSortBuilder;
                    String customField = mappingColumnMap.get(field);
                    if (Objects.nonNull(customField)) {
                        fieldSortBuilder = new FieldSortBuilder(customField).order(sortOrder);
                    } else {
                        if (dbConfig.isMapUnderscoreToCamelCase()) {
                            fieldSortBuilder = new FieldSortBuilder(StringUtils.camelToUnderline(field)).order(sortOrder);
                        } else {
                            fieldSortBuilder = new FieldSortBuilder(field).order(sortOrder);
                        }
                    }
                    searchSourceBuilder.sort(fieldSortBuilder);
                });
            });
        }

        // 设置以String形式指定的排序字段及规则
        if (CollectionUtils.isNotEmpty(wrapper.orderByParams)) {
            wrapper.orderByParams.forEach(orderByParam -> {
                // 设置排序字段
                FieldSortBuilder fieldSortBuilder;
                String customField = mappingColumnMap.get(orderByParam.getOrder());
                if (Objects.nonNull(customField)) {
                    fieldSortBuilder = new FieldSortBuilder(customField);
                } else {
                    if (dbConfig.isMapUnderscoreToCamelCase()) {
                        fieldSortBuilder = new FieldSortBuilder(StringUtils.camelToUnderline(orderByParam.getOrder()));
                    } else {
                        fieldSortBuilder = new FieldSortBuilder(orderByParam.getOrder());
                    }
                }

                // 设置排序规则
                if (SortOrder.ASC.toString().equalsIgnoreCase(orderByParam.getSort())) {
                    fieldSortBuilder.order(SortOrder.ASC);
                }
                if (SortOrder.DESC.toString().equalsIgnoreCase(orderByParam.getSort())) {
                    fieldSortBuilder.order(SortOrder.DESC);
                }
                searchSourceBuilder.sort(fieldSortBuilder);
            });
        }

        // 设置用户自定义的sorts
        if (CollectionUtils.isNotEmpty(wrapper.sortBuilders)) {
            wrapper.sortBuilders.forEach(searchSourceBuilder::sort);
        }

        // 设置得分排序规则
        Optional.ofNullable(wrapper.sortOrder)
                .ifPresent(sortOrder -> searchSourceBuilder.sort(SCORE_FIELD, sortOrder));
    }


    /**
     * 设置聚合参数
     *
     * @param wrapper             参数包装类
     * @param mappingColumnMap    字段映射map
     * @param searchSourceBuilder 查询参数建造者
     */
    private static void setAggregations(LambdaEsQueryWrapper<?> wrapper, Map<String, String> mappingColumnMap,
                                        SearchSourceBuilder searchSourceBuilder) {
        // 设置折叠(去重)字段
        Optional.ofNullable(wrapper.distinctField)
                .ifPresent(distinctField -> {
                    searchSourceBuilder.collapse(new CollapseBuilder(distinctField));
                    searchSourceBuilder.aggregation(AggregationBuilders.cardinality(REPEAT_NUM_KEY).field(distinctField));
                });

        // 其它聚合
        List<AggregationParam> aggregationParamList = wrapper.aggregationParamList;
        if (CollectionUtils.isEmpty(aggregationParamList)) {
            return;
        }

        // 获取配置
        GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();

        // 构建聚合树
        AggregationBuilder root = null;
        AggregationBuilder cursor = null;
        for (AggregationParam aggParam : aggregationParamList) {
            String realField = getRealField(aggParam.getField(), mappingColumnMap, dbConfig);
            AggregationBuilder builder = getRealAggregationBuilder(aggParam.getAggregationType(), aggParam.getName(), realField);
            if (root == null) {
                root = builder;
                cursor = root;
            } else {
                cursor.subAggregation(builder);
                cursor = builder;
            }
        }
        searchSourceBuilder.aggregation(root);
    }

    /**
     * 根据聚合类型获取具体的聚合建造者
     *
     * @param aggType   聚合类型
     * @param name      聚合返回桶的名称 保持原字段名称
     * @param realField 原字段名称
     * @return 聚合建造者
     */
    private static AggregationBuilder getRealAggregationBuilder(AggregationTypeEnum aggType, String name, String realField) {
        AggregationBuilder aggregationBuilder;
        switch (aggType) {
            case AVG:
                aggregationBuilder = AggregationBuilders.avg(name).field(realField);
                break;
            case MIN:
                aggregationBuilder = AggregationBuilders.min(name).field(realField);
                break;
            case MAX:
                aggregationBuilder = AggregationBuilders.max(name).field(realField);
                break;
            case SUM:
                aggregationBuilder = AggregationBuilders.sum(name).field(realField);
                break;
            case TERMS:
                aggregationBuilder = AggregationBuilders.terms(name).field(realField).size(Integer.MAX_VALUE);
                break;
            default:
                throw new UnsupportedOperationException("不支持的聚合类型,参见AggregationTypeEnum");
        }
        return aggregationBuilder;
    }

    /**
     * 获取实际字段名
     *
     * @param field            原字段名
     * @param mappingColumnMap 字段映射关系map
     * @param dbConfig         配置
     * @return 实际字段名
     */
    private static String getRealField(String field, Map<String, String> mappingColumnMap, GlobalConfig.DbConfig dbConfig) {
        String customField = mappingColumnMap.get(field);
        if (Objects.nonNull(customField)) {
            return customField;
        } else {
            if (dbConfig.isMapUnderscoreToCamelCase()) {
                return StringUtils.camelToUnderline(field);
            } else {
                return field;
            }
        }
    }

    /**
     * 获取实际字段名数组
     *
     * @param fields           原字段名数组
     * @param mappingColumnMap 字段映射关系map
     * @param dbConfig         配置
     * @return 实际字段数组
     */
    private static String[] getRealFields(String[] fields, Map<String, String> mappingColumnMap, GlobalConfig.DbConfig dbConfig) {
        return Arrays.stream(fields)
                .map(field -> getRealField(field, mappingColumnMap, dbConfig))
                .collect(Collectors.toList())
                .toArray(new String[]{});
    }

    /**
     * 获取实际字段名数组
     *
     * @param fields           原字段名数组
     * @param mappingColumnMap 字段映射关系map
     * @return 实际字段数组
     */
    private static List<String> getRealFields(List<String> fields, Map<String, String> mappingColumnMap) {
        return Arrays.stream(getRealFields(fields.toArray(new String[0]), mappingColumnMap, GlobalConfigCache.getGlobalConfig().getDbConfig()))
                .collect(Collectors.toList());
    }

}
