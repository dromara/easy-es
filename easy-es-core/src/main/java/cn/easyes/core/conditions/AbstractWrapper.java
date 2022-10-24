package cn.easyes.core.conditions;

import cn.easyes.common.enums.*;
import cn.easyes.common.utils.*;
import cn.easyes.core.biz.*;
import cn.easyes.core.conditions.interfaces.*;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.geometry.Geometry;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static cn.easyes.common.enums.BaseEsParamTypeEnum.*;
import static cn.easyes.common.enums.EsAttachTypeEnum.*;
import static cn.easyes.common.enums.EsQueryTypeEnum.*;
import static cn.easyes.common.enums.JoinTypeEnum.*;
import static cn.easyes.common.enums.OrderTypeEnum.CUSTOMIZE;

/**
 * 抽象Lambda表达式父类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public abstract class AbstractWrapper<T, R, Children extends AbstractWrapper<T, R, Children>> extends Wrapper<T>
        implements Compare<Children, R>, Nested<Children, Children>, Join<Children>, Func<Children, R>, Geo<Children, R> {

    protected final Children typedThis = (Children) this;

    /**
     * 基础查询参数列表
     */
    protected List<BaseEsParam> baseEsParamList;

    /**
     * 基础排序参数列表
     */
    protected List<BaseSortParam> baseSortParams;

    /**
     * 聚合查询参数列表
     */
    protected List<AggregationParam> aggregationParamList;
    /**
     * 折叠去重字段
     */
    protected String distinctField;
    /**
     * geo相关参数
     */
    protected GeoParam geoParam;

    /**
     * 排序参数列表
     */
    protected List<OrderByParam> orderByParams;

    /**
     * 是否查询全部文档
     */
    protected Boolean matchAllQuery;
    /**
     * 实体对象
     */
    protected T entity;
    /**
     * 实体类型
     */
    protected Class<T> entityClass;

    public Children setEntity(T entity) {
        this.entity = entity;
        this.initEntityClass();
        return typedThis;
    }

    public Children setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.initEntityClass();
        return typedThis;
    }

    protected void initEntityClass() {
        if (this.entityClass == null && this.entity != null) {
            this.entityClass = (Class<T>) entity.getClass();
        }
    }

    protected Class<T> getCheckEntityClass() {
        Assert.notNull(entityClass, "entityClass must not null,please set entity before use this method!");
        return entityClass;
    }

    /**
     * 必要的初始化
     */
    protected final void initNeed() {
        baseEsParamList = new ArrayList<>();
        baseSortParams = new ArrayList<>();
        aggregationParamList = new ArrayList<>();
    }

    @Override
    public <V> Children allEq(boolean condition, Map<String, V> params, boolean null2IsNull) {
        if (condition && CollectionUtils.isNotEmpty(params)) {
            params.forEach((k, v) -> {
                if (StringUtils.checkValNotNull(v)) {
                    eq(k, v);
                } else {
                    if (null2IsNull) {
                        isNull(k);
                    }
                }
            });
        }
        return typedThis;
    }

    @Override
    public <V> Children allEq(boolean condition, BiPredicate<String, V> filter, Map<String, V> params, boolean null2IsNull) {
        if (condition && CollectionUtils.isNotEmpty(params)) {
            params.forEach((k, v) -> {
                if (filter.test(k, v)) {
                    if (StringUtils.checkValNotNull(v)) {
                        eq(k, v);
                    } else {
                        if (null2IsNull) {
                            isNull(k);
                        }
                    }
                }
            });
        }
        return typedThis;
    }

    @Override
    public Children eq(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, TERM_QUERY, MUST, column, val, boost);
    }

    @Override
    public Children ne(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, TERM_QUERY, MUST_NOT, column, val, boost);
    }

    @Override
    public Children and(boolean condition, Consumer<Children> consumer) {
        return doIt(condition, consumer, AND_LEFT_BRACKET, AND_RIGHT_BRACKET);
    }

    @Override
    public Children or(boolean condition, Consumer<Children> consumer) {
        return doIt(condition, consumer, OR_LEFT_BRACKET, OR_RIGHT_BRACKET);
    }

    @Override
    public Children match(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, MATCH_QUERY, MUST, column, val, boost);
    }

    @Override
    public Children nestedMatch(boolean condition, String path, String column, Object val, ScoreMode scoreMode, Float boost) {
        return doIt(condition, MATCH_QUERY, MUST, NESTED, path, column, val, scoreMode, boost);
    }

    @Override
    public Children hasChild(boolean condition, String type, String column, Object val, ScoreMode scoreMode, Float boost) {
        return doIt(condition, MATCH_QUERY, MUST, HAS_CHILD, type, column, val, scoreMode, boost);
    }

    @Override
    public Children hasParent(boolean condition, String type, String column, Object val, boolean score, Float boost) {
        return doIt(condition, MATCH_QUERY, MUST, HAS_PARENT, type, column, val, score, boost);
    }

    @Override
    public Children parentId(boolean condition, Object parentId, String type, Float boost) {
        Assert.notNull(parentId, "parentId could not be null");
        return doIt(condition, MATCH_QUERY, MUST, PARENT_ID, type, null, parentId, null, boost);
    }

    @Override
    public Children matchPhrase(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, MATCH_PHRASE, MUST, column, val, boost);
    }

    @Override
    public Children matchAllQuery(boolean condition) {
        if (condition) {
            this.matchAllQuery = true;
        }
        return typedThis;
    }

    @Override
    public Children matchPhrasePrefixQuery(boolean condition, String column, Object val, int maxExpansions, Float boost) {
        return doIt(condition, MATCH_PHRASE_PREFIX, MUST, column, val, maxExpansions, boost);
    }

    @SafeVarargs
    @Override
    public final Children multiMatchQuery(boolean condition, Object val, Operator operator, int minimumShouldMatch, Float boost, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        return doIt(condition, MULTI_MATCH_QUERY, MUST_MULTI_FIELDS, val, operator, minimumShouldMatch, boost, columns);
    }

    @Override
    public Children queryStringQuery(boolean condition, String queryString, Float boost) {
        if (StringUtils.isBlank(queryString)) {
            throw ExceptionUtils.eee("queryString can't be blank");
        }
        return doIt(condition, QUERY_STRING_QUERY, MUST, null, queryString, boost);
    }

    @Override
    public Children prefixQuery(boolean condition, String column, String prefix, Float boost) {
        if (StringUtils.isBlank(prefix)) {
            throw ExceptionUtils.eee("prefix can't be blank");
        }
        return doIt(condition, PREFIX_QUERY, MUST, column, prefix, boost);
    }

    @Override
    public Children notMatch(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, MATCH_QUERY, MUST_NOT, column, val, boost);
    }

    @Override
    public Children gt(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, RANGE_QUERY, EsAttachTypeEnum.GT, column, val, boost);
    }

    @Override
    public Children ge(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, RANGE_QUERY, EsAttachTypeEnum.GE, column, val, boost);
    }

    @Override
    public Children lt(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, RANGE_QUERY, EsAttachTypeEnum.LT, column, val, boost);
    }

    @Override
    public Children le(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, RANGE_QUERY, EsAttachTypeEnum.LE, column, val, boost);
    }

    @Override
    public Children between(boolean condition, String column, Object val1, Object val2, Float boost) {
        return doIt(condition, EsAttachTypeEnum.BETWEEN, column, val1, val2, boost);
    }

    @Override
    public Children notBetween(boolean condition, String column, Object val1, Object val2, Float boost) {
        return doIt(condition, EsAttachTypeEnum.NOT_BETWEEN, column, val1, val2, boost);
    }

    @Override
    public Children or(boolean condition) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            baseEsParam.setType(BaseEsParamTypeEnum.OR_ALL.getType());
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    @Override
    public Children like(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, WILDCARD_QUERY, MUST, column, val, boost);
    }

    @Override
    public Children notLike(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, WILDCARD_QUERY, MUST_NOT, column, val, boost);
    }

    @Override
    public Children likeLeft(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, WILDCARD_QUERY, LIKE_LEFT, column, val, boost);
    }

    @Override
    public Children likeRight(boolean condition, String column, Object val, Float boost) {
        return doIt(condition, WILDCARD_QUERY, LIKE_RIGHT, column, val, boost);
    }

    @Override
    public final Children orderBy(boolean condition, boolean isAsc, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }

        if (condition) {
            Arrays.stream(columns)
                    .forEach(column -> {
                        BaseSortParam baseSortParam = BaseSortParam.builder()
                                .sortField(column)
                                .sortOrder(isAsc ? SortOrder.ASC : SortOrder.DESC)
                                .orderTypeEnum(OrderTypeEnum.FIELD)
                                .build();
                        baseSortParams.add(baseSortParam);
                    });
        }
        return typedThis;
    }

    @Override
    public Children orderBy(boolean condition, List<OrderByParam> orderByParams) {
        if (CollectionUtils.isNotEmpty(orderByParams)) {
            this.orderByParams = orderByParams;
        }
        return typedThis;
    }

    @Override
    public Children orderByDistanceAsc(boolean condition, String column, DistanceUnit unit, GeoDistance geoDistance, GeoPoint... geoPoints) {
        if (ArrayUtils.isNotEmpty(geoPoints)) {
            if (condition) {
                BaseSortParam baseSortParam = BaseSortParam.builder()
                        .sortField(column)
                        .sortOrder(SortOrder.ASC)
                        .orderTypeEnum(OrderTypeEnum.GEO)
                        .geoPoints(geoPoints)
                        .unit(unit)
                        .geoDistance(geoDistance)
                        .build();
                baseSortParams.add(baseSortParam);
            }
        }
        return typedThis;
    }

    @Override
    public Children orderByDistanceDesc(boolean condition, String column, DistanceUnit unit, GeoDistance geoDistance, GeoPoint... geoPoints) {
        if (ArrayUtils.isNotEmpty(geoPoints)) {
            if (condition) {
                BaseSortParam baseSortParam = BaseSortParam.builder()
                        .sortField(column)
                        .sortOrder(SortOrder.DESC)
                        .orderTypeEnum(OrderTypeEnum.GEO)
                        .geoPoints(geoPoints)
                        .unit(unit)
                        .geoDistance(geoDistance)
                        .build();
                baseSortParams.add(baseSortParam);
            }
        }
        return typedThis;
    }

    @Override
    public Children sort(boolean condition, List<SortBuilder<?>> sortBuilders) {
        if (CollectionUtils.isEmpty(sortBuilders)) {
            return typedThis;
        }
        if (condition) {
            sortBuilders.forEach(sortBuilder -> {
                BaseSortParam baseSortParam = BaseSortParam
                        .builder()
                        .orderTypeEnum(CUSTOMIZE)
                        .sortBuilder(sortBuilder)
                        .build();
                baseSortParams.add(baseSortParam);
            });
        }
        return typedThis;
    }

    @Override
    public Children sortByScore(boolean condition, SortOrder sortOrder) {
        if (condition) {
            BaseSortParam baseSortParam = BaseSortParam.builder()
                    .sortOrder(sortOrder)
                    .orderTypeEnum(OrderTypeEnum.SCORE)
                    .build();
            baseSortParams.add(baseSortParam);
        }
        return typedThis;
    }

    @Override
    public Children in(boolean condition, String column, Collection<?> coll, Float boost) {
        if (CollectionUtils.isEmpty(coll)) {
            return typedThis;
        }
        return doIt(condition, EsAttachTypeEnum.IN, column, new ArrayList<>(coll), boost);
    }

    @Override
    public Children notIn(boolean condition, String column, Collection<?> coll, Float boost) {
        if (CollectionUtils.isEmpty(coll)) {
            return typedThis;
        }
        return doIt(condition, EsAttachTypeEnum.NOT_IN, column, new ArrayList<>(coll), boost);
    }

    @Override
    public Children isNull(boolean condition, String column, Float boost) {
        return doIt(condition, EsAttachTypeEnum.NOT_EXISTS, column, boost);
    }

    @Override
    public Children isNotNull(boolean condition, String column, Float boost) {
        return doIt(condition, EsAttachTypeEnum.EXISTS, column, boost);
    }

    @Override
    public final Children groupBy(boolean condition, boolean enablePipeline, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        Arrays.stream(columns).forEach(column -> doIt(condition, enablePipeline, AggregationTypeEnum.TERMS, column));
        return typedThis;
    }

    @Override
    public Children termsAggregation(boolean condition, boolean enablePipeline, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        Arrays.stream(columns).forEach(column -> doIt(condition, enablePipeline, AggregationTypeEnum.TERMS, column));
        return typedThis;
    }

    @Override
    public Children avg(boolean condition, boolean enablePipeline, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        Arrays.stream(columns).forEach(column -> doIt(condition, enablePipeline, AggregationTypeEnum.AVG, column));
        return typedThis;
    }

    @Override
    public Children min(boolean condition, boolean enablePipeline, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        Arrays.stream(columns).forEach(column -> doIt(condition, enablePipeline, AggregationTypeEnum.MIN, column));
        return typedThis;
    }

    @Override
    public Children max(boolean condition, boolean enablePipeline, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        Arrays.stream(columns).forEach(column -> doIt(condition, enablePipeline, AggregationTypeEnum.MAX, column));
        return typedThis;
    }

    @Override
    public Children sum(boolean condition, boolean enablePipeline, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        Arrays.stream(columns).forEach(column -> doIt(condition, enablePipeline, AggregationTypeEnum.SUM, column));
        return typedThis;
    }

    @Override
    public Children distinct(boolean condition, String column) {
        if (condition) {
            this.distinctField = column;
        }
        return typedThis;
    }

    @Override
    public Children geoBoundingBox(boolean condition, String column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        return doIt(condition, column, topLeft, bottomRight, boost, true);
    }

    @Override
    public Children notInGeoBoundingBox(boolean condition, String column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        return doIt(condition, column, topLeft, bottomRight, boost, false);
    }

    @Override
    public Children geoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        return doIt(condition, column, distance, distanceUnit, centralGeoPoint, boost, true);
    }

    @Override
    public Children notInGeoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        return doIt(condition, column, distance, distanceUnit, centralGeoPoint, boost, false);
    }

    @Override
    public Children geoDistance(boolean condition, String column, String distance, GeoPoint centralGeoPoint, Float boost) {
        return doIt(condition, column, distance, centralGeoPoint, boost, true);
    }

    @Override
    public Children notInGeoDistance(boolean condition, String column, String distance, GeoPoint centralGeoPoint, Float boost) {
        return doIt(condition, column, distance, centralGeoPoint, boost, false);
    }

    @Override
    public Children geoPolygon(boolean condition, String column, List<GeoPoint> geoPoints, Float boost) {
        return doIt(condition, column, geoPoints, boost, true);
    }

    @Override
    public Children notInGeoPolygon(boolean condition, String column, Collection<GeoPoint> geoPoints, Float boost) {
        List<GeoPoint> geoPointList = new ArrayList<>(geoPoints);
        return doIt(condition, column, geoPointList, boost, false);
    }

    @Override
    public Children geoShape(boolean condition, String column, String indexedShapeId, Float boost) {
        return doIt(condition, column, indexedShapeId, boost, true);
    }

    @Override
    public Children notInGeoShape(boolean condition, String column, String indexedShapeId, Float boost) {
        return doIt(condition, column, indexedShapeId, boost, false);
    }

    @Override
    public Children geoShape(boolean condition, String column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        return doIt(condition, column, geometry, shapeRelation, boost, true);
    }

    @Override
    public Children notInGeoShape(boolean condition, String column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        return doIt(condition, column, geometry, shapeRelation, boost, false);
    }

    /**
     * 子类返回一个自己的新对象
     *
     * @return 泛型
     */
    protected abstract Children instance();

    /**
     * 封装查询参数 聚合类
     *
     * @param condition           条件
     * @param enablePipeline      是否管道聚合
     * @param aggregationTypeEnum 聚合类型
     * @param column              列
     * @return 泛型
     */
    private Children doIt(boolean condition, boolean enablePipeline, AggregationTypeEnum aggregationTypeEnum, String column) {
        if (condition) {
            AggregationParam aggregationParam = new AggregationParam();
            aggregationParam.setEnablePipeline(enablePipeline);
            aggregationParam.setName(column);
            aggregationParam.setField(column);
            aggregationParam.setAggregationType(aggregationTypeEnum);
            aggregationParamList.add(aggregationParam);
        }
        return typedThis;
    }

    /**
     * 封装查询参数(含AND,OR这种连接操作)
     *
     * @param condition 条件
     * @param consumer  函数
     * @param open      左括号
     * @param close     右括号
     * @return 泛型
     */
    private Children doIt(boolean condition, Consumer<Children> consumer, BaseEsParamTypeEnum open, BaseEsParamTypeEnum close) {
        if (condition) {
            BaseEsParam left = new BaseEsParam();
            left.setType(open.getType());
            baseEsParamList.add(left);
            consumer.accept(instance());
            BaseEsParam right = new BaseEsParam();
            right.setType(close.getType());
            baseEsParamList.add(right);
        }
        return typedThis;
    }

    /**
     * 封装查询参数(普通情况,不带括号)
     *
     * @param condition      条件
     * @param attachTypeEnum 连接类型
     * @param field          字段
     * @param values         值列表
     * @param boost          权重
     * @return 泛型
     */
    private Children doIt(boolean condition, EsAttachTypeEnum attachTypeEnum, String field, List<Object> values, Float boost) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            BaseEsParam.FieldValueModel model =
                    BaseEsParam.FieldValueModel
                            .builder()
                            .field(field)
                            .values(values)
                            .boost(boost)
                            .esQueryType(TERMS_QUERY.getType())
                            .originalAttachType(attachTypeEnum.getType())
                            .build();

            setModel(baseEsParam, model, attachTypeEnum);
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    /**
     * 封装查询参数(普通情况,不带括号)
     *
     * @param condition      条件
     * @param queryTypeEnum  查询类型
     * @param attachTypeEnum 连接类型
     * @param field          字段
     * @param val            值
     * @param boost          权重
     * @return 泛型
     */
    private Children doIt(boolean condition, EsQueryTypeEnum queryTypeEnum, EsAttachTypeEnum attachTypeEnum, String field, Object val, Float boost) {
        return doIt(condition, queryTypeEnum, attachTypeEnum, field, val, null, boost);
    }

    /**
     * 封装查询参数(普通情况,不带括号)
     *
     * @param condition      条件
     * @param queryTypeEnum  查询类型
     * @param attachTypeEnum 连接类型
     * @param field          字段
     * @param val            值
     * @param boost          权重
     * @param ext            拓展字段
     * @return 泛型
     */
    private Children doIt(boolean condition, EsQueryTypeEnum queryTypeEnum, EsAttachTypeEnum attachTypeEnum, String field, Object val, Object ext, Float boost) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            BaseEsParam.FieldValueModel model =
                    BaseEsParam.FieldValueModel
                            .builder()
                            .field(field)
                            .value(val)
                            .boost(boost)
                            .esQueryType(queryTypeEnum.getType())
                            .originalAttachType(attachTypeEnum.getType())
                            .ext(ext)
                            .build();

            setModel(baseEsParam, model, attachTypeEnum);
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    /**
     * 封装查询参数针对is Null / not null 这类无值操作
     *
     * @param condition      条件
     * @param attachTypeEnum 连接类型
     * @param field          字段
     * @param boost          权重
     * @return 泛型
     */
    private Children doIt(boolean condition, EsAttachTypeEnum attachTypeEnum, String field, Float boost) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            BaseEsParam.FieldValueModel model =
                    BaseEsParam.FieldValueModel
                            .builder()
                            .field(field)
                            .boost(boost)
                            .esQueryType(EXISTS_QUERY.getType())
                            .originalAttachType(attachTypeEnum.getType())
                            .build();

            setModel(baseEsParam, model, attachTypeEnum);
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    /**
     * 仅针对between的情况
     *
     * @param condition      条件
     * @param attachTypeEnum 连接类型
     * @param field          字段
     * @param left           左区间
     * @param right          右区间
     * @param boost          权重
     * @return 泛型
     */
    private Children doIt(boolean condition, EsAttachTypeEnum attachTypeEnum, String field, Object left, Object right, Float boost) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            BaseEsParam.FieldValueModel model =
                    BaseEsParam.FieldValueModel
                            .builder()
                            .field(field)
                            .leftValue(left)
                            .rightValue(right)
                            .boost(boost)
                            .esQueryType(INTERVAL_QUERY.getType())
                            .originalAttachType(attachTypeEnum.getType())
                            .build();

            setModel(baseEsParam, model, attachTypeEnum);
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    /**
     * 针对multiMatchQuery
     *
     * @param condition      条件
     * @param queryTypeEnum  查询类型
     * @param attachTypeEnum 连接类型
     * @param val            值
     * @param boost          权重
     * @param columns        字段列表
     * @return 泛型
     */
    private Children doIt(boolean condition, EsQueryTypeEnum queryTypeEnum, EsAttachTypeEnum attachTypeEnum, Object val,
                          Operator operator, int minimumShouldMatch, Float boost, String... columns) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            List<String> fields = Arrays.asList(columns);
            BaseEsParam.FieldValueModel model =
                    BaseEsParam.FieldValueModel
                            .builder()
                            .fields(fields)
                            .value(val)
                            .ext(operator)
                            .minimumShouldMatch(minimumShouldMatch)
                            .boost(boost)
                            .esQueryType(queryTypeEnum.getType())
                            .originalAttachType(attachTypeEnum.getType())
                            .build();
            setModel(baseEsParam, model, attachTypeEnum);
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    private Children doIt(boolean condition, EsQueryTypeEnum queryTypeEnum, EsAttachTypeEnum attachTypeEnum,
                          JoinTypeEnum joinTypeEnum, String path, String column, Object val, Object scoreMode, Float boost) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            BaseEsParam.FieldValueModel model =
                    BaseEsParam.FieldValueModel
                            .builder()
                            .field(column)
                            .path(path)
                            .scoreMode(scoreMode)
                            .value(val)
                            .boost(boost)
                            .ext(joinTypeEnum)
                            .esQueryType(queryTypeEnum.getType())
                            .originalAttachType(attachTypeEnum.getType())
                            .build();

            setModel(baseEsParam, model, attachTypeEnum);
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    /**
     * geoBoundingBox
     *
     * @param condition   条件
     * @param field       字段名
     * @param topLeft     左上点坐标
     * @param bottomRight 右下点坐标
     * @param boost       权重值
     * @return 泛型
     */
    private Children doIt(boolean condition, String field, GeoPoint topLeft, GeoPoint bottomRight, Float boost, boolean isIn) {
        if (condition) {
            this.geoParam = GeoParam.builder()
                    .field(field)
                    .topLeft(topLeft)
                    .bottomRight(bottomRight)
                    .boost(boost)
                    .isIn(isIn)
                    .build();
        }
        return typedThis;
    }

    /**
     * geoDistance 双精度距离类型
     *
     * @param condition       条件
     * @param fieldName       字段名
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点
     * @param boost           权重
     * @return 泛型
     */
    private Children doIt(boolean condition, String fieldName, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost, boolean isIn) {
        if (condition) {
            this.geoParam = GeoParam.builder()
                    .field(fieldName)
                    .boost(boost)
                    .distance(distance)
                    .distanceUnit(distanceUnit)
                    .centralGeoPoint(centralGeoPoint)
                    .isIn(isIn)
                    .build();
        }
        return typedThis;
    }

    /**
     * geoDistance 字符串距离类型
     *
     * @param condition       条件
     * @param fieldName       字段名
     * @param distance        距离 字符串
     * @param centralGeoPoint 中心点
     * @param boost           权重值
     * @return 泛型
     */
    private Children doIt(boolean condition, String fieldName, String distance, GeoPoint centralGeoPoint, Float boost, boolean isIn) {
        if (condition) {
            this.geoParam = GeoParam.builder()
                    .field(fieldName)
                    .boost(boost)
                    .distanceStr(distance)
                    .centralGeoPoint(centralGeoPoint)
                    .isIn(isIn)
                    .build();
        }
        return typedThis;
    }

    /**
     * geoPolygon
     *
     * @param condition 条件
     * @param fieldName 字段名
     * @param geoPoints 多边形点坐标列表
     * @param boost     权重值
     * @return 泛型
     */
    private Children doIt(boolean condition, String fieldName, List<GeoPoint> geoPoints, Float boost, boolean isIn) {
        if (condition) {
            this.geoParam = GeoParam.builder()
                    .field(fieldName)
                    .boost(boost)
                    .geoPoints(geoPoints)
                    .isIn(isIn)
                    .build();
        }
        return typedThis;
    }

    /**
     * 图形 已知图形已被索引的情况
     *
     * @param condition      条件
     * @param fieldName      字段名
     * @param indexedShapeId 已被索引的图形索引id
     * @param boost          权重值
     * @return 泛型
     */
    private Children doIt(boolean condition, String fieldName, String indexedShapeId, Float boost, boolean isIn) {
        if (condition) {
            this.geoParam = GeoParam.builder()
                    .field(fieldName)
                    .boost(boost)
                    .indexedShapeId(indexedShapeId)
                    .isIn(isIn)
                    .build();
        }
        return typedThis;
    }

    /**
     * 图形 GeoShape
     *
     * @param condition 条件
     * @param fieldName 字段名
     * @param geometry  图形
     * @param boost     权重值
     * @return 泛型
     */
    private Children doIt(boolean condition, String fieldName, Geometry geometry, ShapeRelation shapeRelation, Float boost, boolean isIn) {
        if (condition) {
            this.geoParam = GeoParam.builder()
                    .field(fieldName)
                    .boost(boost)
                    .geometry(geometry)
                    .shapeRelation(shapeRelation)
                    .isIn(isIn)
                    .build();
        }
        return typedThis;
    }

    /**
     * 设置查询模型类型
     *
     * @param baseEsParam    基础参数
     * @param model          字段&值模型
     * @param attachTypeEnum 连接类型
     */
    private void setModel(BaseEsParam baseEsParam, BaseEsParam.FieldValueModel model, EsAttachTypeEnum attachTypeEnum) {
        switch (attachTypeEnum) {
            case MUST:
                baseEsParam.getMustList().add(model);
                break;
            case FILTER:
                baseEsParam.getFilterList().add(model);
                break;
            case SHOULD:
                baseEsParam.getShouldList().add(model);
                break;
            case MUST_NOT:
                baseEsParam.getMustNotList().add(model);
                break;
            case GT:
                baseEsParam.getGtList().add(model);
                break;
            case LT:
                baseEsParam.getLtList().add(model);
                break;
            case GE:
                baseEsParam.getGeList().add(model);
                break;
            case LE:
                baseEsParam.getLeList().add(model);
                break;
            case IN:
                baseEsParam.getInList().add(model);
                break;
            case NOT_IN:
                baseEsParam.getNotInList().add(model);
                break;
            case EXISTS:
                baseEsParam.getNotNullList().add(model);
                break;
            case NOT_EXISTS:
                baseEsParam.getIsNullList().add(model);
                break;
            case BETWEEN:
                baseEsParam.getBetweenList().add(model);
                break;
            case NOT_BETWEEN:
                baseEsParam.getNotBetweenList().add(model);
                break;
            case LIKE_LEFT:
                baseEsParam.getLikeLeftList().add(model);
                break;
            case LIKE_RIGHT:
                baseEsParam.getLikeRightList().add(model);
                break;
            case MUST_MULTI_FIELDS:
                baseEsParam.getMustMultiFieldList().add(model);
                break;
            default:
                throw new UnsupportedOperationException("不支持的连接类型,请参见EsAttachTypeEnum");
        }
    }

}
