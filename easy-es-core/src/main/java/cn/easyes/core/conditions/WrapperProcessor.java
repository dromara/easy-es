package cn.easyes.core.conditions;

import cn.easyes.common.enums.AggregationTypeEnum;
import cn.easyes.common.utils.ArrayUtils;
import cn.easyes.common.utils.CollectionUtils;
import cn.easyes.common.utils.MyOptional;
import cn.easyes.common.utils.StringUtils;
import cn.easyes.core.biz.*;
import cn.easyes.core.cache.GlobalConfigCache;
import cn.easyes.core.config.GlobalConfig;
import cn.easyes.core.toolkit.EntityInfoHelper;
import cn.easyes.core.toolkit.EsQueryTypeUtil;
import cn.easyes.core.toolkit.FieldUtils;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.util.*;

import static cn.easyes.common.constants.BaseEsConstants.DEFAULT_SIZE;
import static cn.easyes.common.constants.BaseEsConstants.REPEAT_NUM_KEY;
import static cn.easyes.common.enums.BaseEsParamTypeEnum.*;
import static cn.easyes.common.enums.EsAttachTypeEnum.*;
import static org.elasticsearch.index.query.QueryBuilders.geoShapeQuery;

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
        BoolQueryBuilder boolQueryBuilder = initBoolQueryBuilder(wrapper.baseEsParamList, wrapper.enableMust2Filter, entityClass);

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
     * @param baseEsParamList   参数列表
     * @param enableMust2Filter 是否开启must转换filter
     * @param entityClass       实体类
     * @return BoolQueryBuilder
     */
    public static BoolQueryBuilder initBoolQueryBuilder(List<BaseEsParam> baseEsParamList, Boolean enableMust2Filter,
                                                        Class<?> entityClass) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();

        // 获取内层or和内外层or总数,用于处理 是否有外层or:全部重置; 如果仅内层OR,只重置内层.
        OrCount orCount = getOrCount(baseEsParamList);
        // 根节点
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 用于连接and,or条件内的多个查询条件,包装成boolQuery
        BoolQueryBuilder inner = null;
        //正式封装参数
        int start = 0;
        int end = 0;
        int remainSetUp = orCount.getOrInnerCount();
        boolean hasSetUp = false;
        for (int i = 0; i < baseEsParamList.size(); i++) {
            BaseEsParam baseEsParam = baseEsParamList.get(i);
            if (orCount.getOrAllCount() > orCount.getOrInnerCount()) {
                // 存在外层or 统统重置
                BaseEsParam.setUp(baseEsParam);
            } else {
                if (!hasSetUp) {
                    // 处理or在内层的情况,仅重置括号中的内容
                    for (int j = i; j < baseEsParamList.size(); j++) {
                        BaseEsParam andOr = baseEsParamList.get(j);
                        if (AND_LEFT_BRACKET.getType().equals(andOr.getType()) || OR_LEFT_BRACKET.getType().equals(andOr.getType())) {
                            // 找到了and/or的开始标志
                            start = j;
                        }

                        if (AND_RIGHT_BRACKET.getType().equals(andOr.getType()) || OR_RIGHT_BRACKET.getType().equals(andOr.getType())) {
                            // 找到了and/or的结束标志
                            end = j;
                        }
                        if (remainSetUp > 0 && end > start) {
                            // 重置内层or
                            remainSetUp--;
                            for (int k = start; k < end; k++) {
                                BaseEsParam.setUp(baseEsParamList.get(k));
                                hasSetUp = true;
                            }
                        }
                    }
                }
            }

            boolean hasLogicOperator = AND_LEFT_BRACKET.getType().equals(baseEsParam.getType())
                    || OR_LEFT_BRACKET.getType().equals(baseEsParam.getType());
            if (hasLogicOperator) {
                // 说明有and或者or 需要将括号中的内容置入新的boolQuery
                inner = QueryBuilders.boolQuery();
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
            Optional.ofNullable(enableMust2Filter).ifPresent(baseEsParam::setEnableMust2Filter);
            if (Objects.isNull(inner)) {
                addQuery(baseEsParam, boolQueryBuilder, entityInfo, dbConfig);
            } else {
                addQuery(baseEsParam, inner, entityInfo, dbConfig);
            }
        }
        return boolQueryBuilder;
    }

    /**
     * 获取内层or和内外层or总数
     *
     * @param baseEsParamList 参数列表
     * @return 内外侧or总数信息
     */
    private static OrCount getOrCount(List<BaseEsParam> baseEsParamList) {
        OrCount orCount = new OrCount();
        int start;
        int end = 0;
        int orAllCount = 0;
        int orInnerCount = 0;
        for (int i = 0; i < baseEsParamList.size(); i++) {
            BaseEsParam baseEsParam = baseEsParamList.get(i);
            if (OR_ALL.getType().equals(baseEsParam.getType())) {
                orAllCount++;
            }
            boolean hasLogicOperator = AND_LEFT_BRACKET.getType().equals(baseEsParam.getType())
                    || OR_LEFT_BRACKET.getType().equals(baseEsParam.getType());
            if (hasLogicOperator) {
                start = i;
                for (int j = i; j < baseEsParamList.size(); j++) {
                    BaseEsParam andOr = baseEsParamList.get(j);
                    if (AND_RIGHT_BRACKET.getType().equals(andOr.getType()) || OR_RIGHT_BRACKET.getType().equals(andOr.getType())) {
                        end = j;
                    }

                    if (start < end) {
                        for (int k = start; k < end; k++) {
                            if (OR_ALL.getType().equals(baseEsParamList.get(k).getType())) {
                                orInnerCount++;
                            }
                        }
                        break;
                    }
                }
            }
        }

        orCount.setOrAllCount(orAllCount);
        orCount.setOrInnerCount(orInnerCount);
        return orCount;
    }

    /**
     * 初始化SearchSourceBuilder
     *
     * @param wrapper 条件
     * @return SearchSourceBuilder
     */
    private static SearchSourceBuilder initSearchSourceBuilder(LambdaEsQueryWrapper<?> wrapper, Class<?> entityClass) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        // 获取自定义字段map
        Map<String, String> mappingColumnMap = entityInfo.getMappingColumnMap();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 设置高亮
        setHighLight(entityInfo.getHighLightParams(), searchSourceBuilder);

        // 设置用户指定的各种排序规则
        setSort(wrapper, mappingColumnMap, searchSourceBuilder);

        // 设置查询或不查询字段
        setFetchSource(wrapper, mappingColumnMap, searchSourceBuilder);

        // 设置聚合参数
        setAggregations(wrapper, mappingColumnMap, searchSourceBuilder);

        // 设置查询起止参数
        Optional.ofNullable(wrapper.from).ifPresent(searchSourceBuilder::from);
        MyOptional.ofNullable(wrapper.size).ifPresent(searchSourceBuilder::size, DEFAULT_SIZE);

        if (searchSourceBuilder.size() > DEFAULT_SIZE) {
            // 查询超过一万条, trackTotalHists自动开启
            searchSourceBuilder.trackTotalHits(true);
        } else {
            // 根据全局配置决定是否开启
            searchSourceBuilder.trackTotalHits(GlobalConfigCache.getGlobalConfig().getDbConfig().isEnableTrackTotalHits());
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

        // 构造查询参数
        GeoShapeQueryBuilder builder;
        if (StringUtils.isNotBlank(geoParam.getIndexedShapeId())) {
            builder = geoShapeQuery(geoParam.getField(), geoParam.getIndexedShapeId());
        } else {
            builder = geoShapeQuery(geoParam.getField(), geoParam.getGeometry());
        }

        Optional.ofNullable(geoParam.getShapeRelation()).ifPresent(builder::relation);
        Optional.ofNullable(geoParam.getBoost()).ifPresent(builder::boost);
        return builder;
    }


    /**
     * 设置Geo相关查询参数 geoBoundingBox, geoDistance, geoPolygon, geoShape
     *
     * @param geoParam         geo参数
     * @param boolQueryBuilder boolQuery参数建造者
     * @param entityClass      实体类
     */
    public static void setGeoQuery(GeoParam geoParam, BoolQueryBuilder boolQueryBuilder, Class<?> entityClass) {
        // 获取配置信息
        Map<String, String> mappingColumnMap = EntityInfoHelper.getEntityInfo(entityClass).getMappingColumnMap();
        GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();

        // 使用实际字段名称覆盖实体类字段名称
        String realField = FieldUtils.getRealField(geoParam.getField(), mappingColumnMap, dbConfig);
        geoParam.setField(realField);

        GeoBoundingBoxQueryBuilder geoBoundingBox = initGeoBoundingBoxQueryBuilder(geoParam);
        doGeoSet(geoParam.isIn(), geoBoundingBox, boolQueryBuilder, dbConfig);

        GeoDistanceQueryBuilder geoDistance = initGeoDistanceQueryBuilder(geoParam);
        doGeoSet(geoParam.isIn(), geoDistance, boolQueryBuilder, dbConfig);

        GeoPolygonQueryBuilder geoPolygon = initGeoPolygonQueryBuilder(geoParam);
        doGeoSet(geoParam.isIn(), geoPolygon, boolQueryBuilder, dbConfig);

        GeoShapeQueryBuilder geoShape = initGeoShapeQueryBuilder(geoParam);
        doGeoSet(geoParam.isIn(), geoShape, boolQueryBuilder, dbConfig);
    }

    /**
     * 根据查询是否在指定范围内设置geo查询过滤条件
     *
     * @param isIn
     * @param queryBuilder
     * @param boolQueryBuilder
     */
    private static void doGeoSet(Boolean isIn, QueryBuilder queryBuilder, BoolQueryBuilder boolQueryBuilder, GlobalConfig.DbConfig dbConfig) {
        Optional.ofNullable(queryBuilder)
                .ifPresent(present -> {
                    if (isIn) {
                        if (dbConfig.isEnableMust2Filter()) {
                            boolQueryBuilder.filter(present);
                        } else {
                            boolQueryBuilder.must(present);
                        }
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
    private static void addQuery(BaseEsParam baseEsParam, BoolQueryBuilder boolQueryBuilder, EntityInfo entityInfo,
                                 GlobalConfig.DbConfig dbConfig) {
        // 获取must是否转filter 默认不转,以wrapper中指定的优先级最高,全局次之
        boolean enableMust2Filter = Objects.isNull(baseEsParam.getEnableMust2Filter()) ? dbConfig.isEnableMust2Filter() :
                baseEsParam.getEnableMust2Filter();

        baseEsParam.getMustList().forEach(fieldValueModel -> EsQueryTypeUtil.addQueryByType(boolQueryBuilder,
                MUST.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        // 多字段情形
        baseEsParam.getMustMultiFieldList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(), MUST.getType(),
                        fieldValueModel.getOriginalAttachType(), enableMust2Filter, FieldUtils.getRealFields(fieldValueModel.getFields(),
                                entityInfo.getMappingColumnMap()), fieldValueModel.getValue(), fieldValueModel.getExt(),
                        fieldValueModel.getMinimumShouldMatch(), fieldValueModel.getBoost()));

        baseEsParam.getFilterList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, FILTER.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getShouldList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, SHOULD.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        // 多字段情形
        baseEsParam.getShouldMultiFieldList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, fieldValueModel.getEsQueryType(),
                        SHOULD.getType(), fieldValueModel.getOriginalAttachType(), enableMust2Filter,
                        FieldUtils.getRealFields(fieldValueModel.getFields(), entityInfo.getMappingColumnMap()), fieldValueModel.getValue(),
                        fieldValueModel.getExt(), fieldValueModel.getMinimumShouldMatch(), fieldValueModel.getBoost()));

        baseEsParam.getMustNotList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, MUST_NOT.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getGtList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, GT.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getLtList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, LT.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getGeList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, GE.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getLeList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, LE.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getBetweenList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, BETWEEN.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getNotBetweenList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, NOT_BETWEEN.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getInList().forEach(fieldValueModel -> EsQueryTypeUtil.addQueryByType(boolQueryBuilder,
                IN.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getNotInList().forEach(fieldValueModel -> EsQueryTypeUtil.addQueryByType(boolQueryBuilder,
                NOT_IN.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getIsNullList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, NOT_EXISTS.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getNotNullList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, EXISTS.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getLikeLeftList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, LIKE_LEFT.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));

        baseEsParam.getLikeRightList().forEach(fieldValueModel ->
                EsQueryTypeUtil.addQueryByType(boolQueryBuilder, LIKE_RIGHT.getType(), enableMust2Filter, fieldValueModel, entityInfo, dbConfig));
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
        String[] includes = FieldUtils.getRealFields(wrapper.include, mappingColumnMap, dbConfig);
        String[] excludes = FieldUtils.getRealFields(wrapper.exclude, mappingColumnMap, dbConfig);
        searchSourceBuilder.fetchSource(includes, excludes);
    }


    /**
     * 设置高亮参数
     *
     * @param highLightParams     高亮参数列表
     * @param searchSourceBuilder 查询参数建造者
     */
    private static void setHighLight(List<HighLightParam> highLightParams, SearchSourceBuilder searchSourceBuilder) {
        if (CollectionUtils.isEmpty(highLightParams)) {
            return;
        }

        // 封装高亮参数
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highLightParams.forEach(highLightParam -> {
            if (StringUtils.isNotBlank(highLightParam.getHighLightField())) {

                //field
                HighlightBuilder.Field field = new HighlightBuilder.Field(highLightParam.getHighLightField());
                field.highlighterType(highLightParam.getHighLightType().getValue());
                highlightBuilder.field(field);

                highlightBuilder.fragmentSize(highLightParam.getFragmentSize());
                highlightBuilder.preTags(highLightParam.getPreTag());
                highlightBuilder.postTags(highLightParam.getPostTag());
            }
        });
        searchSourceBuilder.highlighter(highlightBuilder);
    }

    /**
     * 初始化高亮参数建造者
     *
     * @param highlightBuilder   高亮参数建造者
     * @param highLightParamList 高亮参数列表
     */
    private static void initHighlightBuilder(HighlightBuilder highlightBuilder, List<HighLightParam> highLightParamList) {
        if (!CollectionUtils.isEmpty(highLightParamList)) {
            highLightParamList.forEach(highLightParam -> {
                if (StringUtils.isNotBlank(highLightParam.getHighLightField())) {
                    //field
                    HighlightBuilder.Field field = new HighlightBuilder.Field(highLightParam.getHighLightField());
                    field.highlighterType(highLightParam.getHighLightType().getValue());
                    highlightBuilder.field(field);

                    highlightBuilder.field(highLightParam.getHighLightField());
                    highlightBuilder.preTags(highLightParam.getPreTag());
                    highlightBuilder.postTags(highLightParam.getPostTag());
                }
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

        // 批量设置排序字段
        if (CollectionUtils.isNotEmpty(wrapper.baseSortParams)) {
            wrapper.baseSortParams.forEach(baseSortParam -> {
                // 获取es中的实际字段 有可能已经被用户自定义或者驼峰转成下划线
                String realField = Objects.isNull(baseSortParam.getSortField()) ?
                        null : FieldUtils.getRealField(baseSortParam.getSortField(), mappingColumnMap, dbConfig);
                SortBuilder<?> sortBuilder = getSortBuilder(realField, baseSortParam);
                Optional.ofNullable(sortBuilder).ifPresent(searchSourceBuilder::sort);
            });
        }

        // 设置以String形式指定的自定义排序字段及规则(此类排序通常由前端传入,满足部分用户个性化需求)
        if (CollectionUtils.isNotEmpty(wrapper.orderByParams)) {
            wrapper.orderByParams.forEach(orderByParam -> {
                // 设置排序字段
                FieldSortBuilder fieldSortBuilder = new FieldSortBuilder(orderByParam.getOrder());

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
    }


    /**
     * 获取排序器
     *
     * @param realField     实际字段名称
     * @param baseSortParam 排序参数
     * @return 排序器
     */
    private static SortBuilder<?> getSortBuilder(String realField, BaseSortParam baseSortParam) {
        switch (baseSortParam.getOrderTypeEnum()) {
            case FIELD:
                return SortBuilders.fieldSort(realField).order(baseSortParam.getSortOrder());
            case SCORE:
                return SortBuilders.scoreSort().order(baseSortParam.getSortOrder());
            case GEO:
                return SortBuilders.geoDistanceSort(realField, baseSortParam.getGeoPoints())
                        .order(baseSortParam.getSortOrder())
                        .geoDistance(baseSortParam.getGeoDistance())
                        .unit(baseSortParam.getUnit());
            case CUSTOMIZE:
                return baseSortParam.getSortBuilder();
            default:
                return null;
        }
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
        // 获取配置
        GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();

        // 设置折叠(去重)字段
        Optional.ofNullable(wrapper.distinctField)
                .ifPresent(distinctField -> {
                    String realField = FieldUtils.getRealField(distinctField, mappingColumnMap, dbConfig);
                    searchSourceBuilder.collapse(new CollapseBuilder(realField));
                    searchSourceBuilder.aggregation(AggregationBuilders.cardinality(REPEAT_NUM_KEY).field(realField));
                });

        // 其它聚合
        List<AggregationParam> aggregationParamList = wrapper.aggregationParamList;
        if (CollectionUtils.isEmpty(aggregationParamList)) {
            return;
        }

        // 构建聚合树
        AggregationBuilder root = null;
        AggregationBuilder cursor = null;
        for (AggregationParam aggParam : aggregationParamList) {
            String realField = FieldUtils.getRealField(aggParam.getField(), mappingColumnMap, dbConfig);
            AggregationBuilder builder = getRealAggregationBuilder(aggParam.getAggregationType(), aggParam.getName(), realField);
            if (aggParam.isEnablePipeline()) {
                // 管道聚合, 构造聚合树
                if (root == null) {
                    root = builder;
                    cursor = root;
                } else {
                    cursor.subAggregation(builder);
                    // 解决max、min、avg和sum聚合函数不支持sub-aggregations的问题
                    if (builder instanceof TermsAggregationBuilder) {
                        cursor = builder;
                    }
                }
            } else {
                // 非管道聚合
                searchSourceBuilder.aggregation(builder);
            }

        }
        Optional.ofNullable(root).ifPresent(searchSourceBuilder::aggregation);
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
        // 解决同一个字段聚合多次，如min(starNum), max(starNum) 字段名重复问题
        name += aggType.getValue();
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
}
