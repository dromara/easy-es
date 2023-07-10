package org.dromara.easyes.core.core;

import org.apache.lucene.search.join.ScoreMode;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.core.biz.EntityFieldInfo;
import org.dromara.easyes.core.biz.OrderByParam;
import org.dromara.easyes.core.conditions.function.*;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.geometry.Geometry;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 链式抽象条件构造器
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings({"serial", "unchecked"})
public abstract class AbstractChainWrapper<T, R, Children extends AbstractChainWrapper<T, R, Children, Param>, Param>
        extends Wrapper<T> implements Compare<Children, R>, Join<Children>, Func<Children, R>, Nested<Param, Children>,
        Geo<Children, R>, Query<Children, T, R>, Update<Children, R>, Index<Children, R> {

    protected final Children typedThis = (Children) this;
    /**
     * 子类所包装的具体 Wrapper 类型
     */
    protected Param wrapperChildren;

    /**
     * 必须的构造函数
     */
    public AbstractChainWrapper() {
    }

    public AbstractWrapper getWrapper() {
        return (AbstractWrapper) wrapperChildren;
    }


    @Override
    public <V> Children allEq(boolean condition, Map<String, V> params) {
        getWrapper().allEq(condition, params);
        return typedThis;
    }

    @Override
    public <V> Children allEq(boolean condition, BiPredicate<String, V> filter, Map<String, V> params) {
        getWrapper().allEq(condition, filter, params);
        return typedThis;
    }

    @Override
    public Children eq(boolean condition, R column, Object val, Float boost) {
        getWrapper().eq(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children gt(boolean condition, R column, Object val, Float boost) {
        getWrapper().gt(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children ge(boolean condition, R column, Object val, Float boost) {
        getWrapper().ge(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children lt(boolean condition, R column, Object val, Float boost) {
        getWrapper().lt(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children le(boolean condition, R column, Object val, Float boost) {
        getWrapper().le(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children between(boolean condition, R column, Object val1, Object val2, Float boost) {
        getWrapper().between(condition, column, val1, val2, boost);
        return typedThis;
    }

    @Override
    public Children match(boolean condition, R column, Object val, Float boost) {
        getWrapper().match(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children matchPhrase(boolean condition, R column, Object val, Float boost) {
        getWrapper().matchPhrase(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children matchAllQuery(boolean condition) {
        getWrapper().matchAllQuery(condition);
        return typedThis;
    }

    @Override
    public Children matchPhrasePrefixQuery(boolean condition, R column, Object val, int maxExpansions, Float boost) {
        getWrapper().matchPhrasePrefixQuery(condition, column, val, maxExpansions, boost);
        return typedThis;
    }

    @Override
    public Children multiMatchQuery(boolean condition, Object val, Operator operator, int minimumShouldMatch, Float boost, R... columns) {
        getWrapper().multiMatchQuery(condition, val, operator, minimumShouldMatch, boost, columns);
        return typedThis;
    }

    @Override
    public Children queryStringQuery(boolean condition, String queryString, Float boost) {
        getWrapper().queryStringQuery(condition, queryString, boost);
        return typedThis;
    }

    @Override
    public Children prefixQuery(boolean condition, R column, String prefix, Float boost) {
        getWrapper().prefixQuery(condition, column, prefix, boost);
        return typedThis;
    }

    @Override
    public Children like(boolean condition, R column, Object val, Float boost) {
        getWrapper().like(condition, column, val, boost);
        return typedThis;
    }


    @Override
    public Children likeLeft(boolean condition, R column, Object val, Float boost) {
        getWrapper().likeLeft(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children likeRight(boolean condition, R column, Object val, Float boost) {
        getWrapper().likeRight(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children isNotNull(boolean condition, R column, Float boost) {
        getWrapper().isNotNull(condition, column, boost);
        return typedThis;
    }

    @Override
    public Children in(boolean condition, R column, Collection<?> coll, Float boost) {
        getWrapper().in(condition, column, coll, boost);
        return typedThis;
    }

    @Override
    public Children orderBy(boolean condition, boolean isAsc, R... columns) {
        getWrapper().orderBy(condition, isAsc, columns);
        return typedThis;
    }

    @Override
    public Children orderBy(boolean condition, List<OrderByParam> orderByParams) {
        getWrapper().orderBy(condition, orderByParams);
        return typedThis;
    }

    @Override
    public Children groupBy(boolean condition, boolean pipeline, R... columns) {
        getWrapper().groupBy(condition, pipeline, columns);
        return typedThis;
    }

    @Override
    public Children termsAggregation(boolean condition, boolean pipeline, R... column) {
        getWrapper().termsAggregation(condition, pipeline, column);
        return typedThis;
    }

    @Override
    public Children avg(boolean condition, boolean pipeline, R... columns) {
        getWrapper().avg(condition, pipeline, columns);
        return typedThis;
    }

    @Override
    public Children min(boolean condition, boolean pipeline, R... columns) {
        getWrapper().min(condition, pipeline, columns);
        return typedThis;
    }

    @Override
    public Children max(boolean condition, boolean pipeline, R... columns) {
        getWrapper().max(condition, pipeline, columns);
        return typedThis;
    }

    @Override
    public Children sum(boolean condition, boolean pipeline, R... columns) {
        getWrapper().sum(condition, pipeline, columns);
        return typedThis;
    }

    @Override
    public Children sort(boolean condition, List<SortBuilder<?>> sortBuilders) {
        getWrapper().sort(condition, sortBuilders);
        return typedThis;
    }

    @Override
    public Children distinct(boolean condition, R column) {
        getWrapper().distinct(condition, column);
        return typedThis;
    }

    @Override
    public Children or(boolean condition, Consumer<Param> consumer) {
        return null;
    }

    @Override
    public Children and(boolean condition, Consumer<Param> consumer) {
        getWrapper().and(condition, consumer);
        return typedThis;
    }

    @Override
    public Children match(R column, Object val, Float boost) {
        getWrapper().match(column, val, boost);
        return typedThis;
    }

    @Override
    public Children gt(R column, Object val, Float boost) {
        getWrapper().gt(column, val, boost);
        return typedThis;
    }

    @Override
    public Children ge(R column, Object val, Float boost) {
        getWrapper().ge(column, val, boost);
        return typedThis;
    }

    @Override
    public Children lt(R column, Object val, Float boost) {
        getWrapper().lt(column, val, boost);
        return typedThis;
    }

    @Override
    public Children le(R column, Object val, Float boost) {
        getWrapper().le(column, val, boost);
        return typedThis;
    }

    @Override
    public Children between(R column, Object val1, Object val2, Float boost) {
        getWrapper().between(column, val1, val2, boost);
        return typedThis;
    }

    @Override
    public Children like(R column, Object val, Float boost) {
        getWrapper().like(column, val, boost);
        return typedThis;
    }

    @Override
    public Children likeLeft(R column, Object val, Float boost) {
        getWrapper().likeLeft(column, val, boost);
        return typedThis;
    }

    @Override
    public Children likeRight(R column, Object val, Float boost) {
        getWrapper().likeRight(column, val, boost);
        return typedThis;
    }

    @Override
    public Children sort(boolean condition, SortBuilder<?> sortBuilder) {
        getWrapper().sort(condition, sortBuilder);
        return typedThis;
    }

    @Override
    public Children sortByScore(boolean condition, SortOrder sortOrder) {
        getWrapper().sortByScore(condition, sortOrder);
        return typedThis;
    }

    @Override
    public Children geoBoundingBox(boolean condition, R column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        getWrapper().geoBoundingBox(condition, column, topLeft, bottomRight, boost);
        return typedThis;
    }

    @Override
    public Children geoDistance(boolean condition, R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        getWrapper().geoDistance(condition, column, distance, distanceUnit, centralGeoPoint, boost);
        return typedThis;
    }

    @Override
    public Children geoDistance(boolean condition, R column, String distance, GeoPoint centralGeoPoint, Float boost) {
        getWrapper().geoDistance(condition, column, distance, centralGeoPoint, boost);
        return typedThis;
    }

    @Override
    public Children geoPolygon(boolean condition, R column, List<GeoPoint> geoPoints, Float boost) {
        getWrapper().geoPolygon(condition, column, geoPoints, boost);
        return typedThis;
    }

    @Override
    public Children geoShape(boolean condition, R column, String indexedShapeId, Float boost) {
        getWrapper().geoShape(condition, column, indexedShapeId, boost);
        return typedThis;
    }

    @Override
    public Children geoShape(boolean condition, R column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        getWrapper().geoShape(condition, column, geometry, shapeRelation, boost);
        return typedThis;
    }

    @Override
    public Children eq(boolean condition, String column, Object val, Float boost) {
        getWrapper().eq(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children match(boolean condition, String column, Object val, Float boost) {
        getWrapper().match(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children matchPhrase(boolean condition, String column, Object val, Float boost) {
        getWrapper().matchPhrase(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children matchPhrasePrefixQuery(boolean condition, String column, Object val, int maxExpansions, Float boost) {
        getWrapper().matchPhrasePrefixQuery(condition, column, val, maxExpansions, boost);
        return typedThis;
    }

    @Override
    public Children multiMatchQuery(boolean condition, Object val, Operator operator, int minimumShouldMatch, Float boost, String... columns) {
        getWrapper().multiMatchQuery(condition, val, operator, minimumShouldMatch, boost, columns);
        return typedThis;
    }

    @Override
    public Children prefixQuery(boolean condition, String column, String prefix, Float boost) {
        getWrapper().prefixQuery(condition, column, prefix, boost);
        return typedThis;
    }

    @Override
    public Children gt(boolean condition, String column, Object val, ZoneId timeZone, String format, Float boost) {
        getWrapper().gt(condition, column, val, timeZone, format, boost);
        return typedThis;
    }

    @Override
    public Children ge(boolean condition, String column, Object val, ZoneId timeZone, String format, Float boost) {
        getWrapper().ge(condition, column, val, timeZone, format, boost);
        return typedThis;
    }

    @Override
    public Children lt(boolean condition, String column, Object val, ZoneId timeZone, String format, Float boost) {
        getWrapper().ge(condition, column, val, timeZone, format, boost);
        return typedThis;
    }

    @Override
    public Children le(boolean condition, String column, Object val, ZoneId timeZone, String format, Float boost) {
        getWrapper().le(condition, column, val, timeZone, format, boost);
        return typedThis;
    }

    @Override
    public Children between(boolean condition, String column, Object from, Object to, ZoneId timeZone, String format, Float boost) {
        getWrapper().between(condition, column, from, to, timeZone, format, boost);
        return typedThis;
    }

    @Override
    public Children like(boolean condition, String column, Object val, Float boost) {
        getWrapper().like(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children likeLeft(boolean condition, String column, Object val, Float boost) {
        getWrapper().likeLeft(condition, column, val, boost);
        return typedThis;

    }

    @Override
    public Children likeRight(boolean condition, String column, Object val, Float boost) {
        getWrapper().likeRight(condition, column, val, boost);
        return typedThis;
    }

    @Override
    public Children orderBy(boolean condition, boolean isAsc, String... columns) {
        getWrapper().orderBy(condition, isAsc, columns);
        return typedThis;
    }

    @Override
    public Children in(boolean condition, String column, Collection<?> coll, Float boost) {
        getWrapper().in(condition, column, coll, boost);
        return typedThis;
    }

    @Override
    public Children isNotNull(boolean condition, String column, Float boost) {
        getWrapper().isNotNull(condition, column, boost);
        return typedThis;
    }

    @Override
    public Children groupBy(boolean condition, boolean enablePipeline, String... columns) {
        getWrapper().groupBy(condition, enablePipeline, columns);
        return typedThis;
    }

    @Override
    public Children termsAggregation(boolean condition, boolean enablePipeline, String... column) {
        getWrapper().termsAggregation(condition, enablePipeline, column);
        return typedThis;
    }

    @Override
    public Children avg(boolean condition, boolean enablePipeline, String... columns) {
        getWrapper().avg(condition, enablePipeline, columns);
        return typedThis;
    }

    @Override
    public Children min(boolean condition, boolean enablePipeline, String... columns) {
        getWrapper().min(condition, enablePipeline, columns);
        return typedThis;
    }

    @Override
    public Children max(boolean condition, boolean enablePipeline, String... columns) {
        getWrapper().max(condition, enablePipeline, columns);
        return typedThis;
    }

    @Override
    public Children sum(boolean condition, boolean enablePipeline, String... columns) {
        getWrapper().sum(condition, enablePipeline, columns);
        return typedThis;
    }

    @Override
    public Children distinct(boolean condition, String column) {
        getWrapper().distinct(column);
        return typedThis;
    }

    @Override
    public Children geoBoundingBox(boolean condition, String column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        getWrapper().geoBoundingBox(condition, column, topLeft, bottomRight, boost);
        return typedThis;
    }

    @Override
    public Children geoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        getWrapper().geoDistance(condition, column, distance, distanceUnit, centralGeoPoint, boost);
        return typedThis;
    }

    @Override
    public Children geoDistance(boolean condition, String column, String distance, GeoPoint centralGeoPoint, Float boost) {
        getWrapper().geoDistance(condition, column, distance, centralGeoPoint, boost);
        return typedThis;
    }

    @Override
    public Children geoPolygon(boolean condition, String column, List<GeoPoint> geoPoints, Float boost) {
        getWrapper().geoPolygon(condition, column, geoPoints, boost);
        return typedThis;
    }

    @Override
    public Children geoShape(boolean condition, String column, String indexedShapeId, Float boost) {
        getWrapper().geoShape(condition, column, indexedShapeId, boost);
        return typedThis;
    }

    @Override
    public Children geoShape(boolean condition, String column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        getWrapper().geoShape(condition, column, geometry, shapeRelation, boost);
        return typedThis;
    }

    @Override
    public Children hasChild(boolean condition, String type, String column, Object val, ScoreMode scoreMode, Float boost) {
        getWrapper().hasChild(condition, type, column, val, scoreMode, boost);
        return typedThis;
    }

    @Override
    public Children hasParent(boolean condition, String type, String column, Object val, boolean score, Float boost) {
        getWrapper().hasParent(condition, type, column, val, score, boost);
        return typedThis;
    }

    @Override
    public Children parentId(boolean condition, Object parentId, String type, Float boost) {
        getWrapper().parentId(condition, parentId, type, boost);
        return typedThis;
    }

    @Override
    public Children matchAllQuery(boolean condition, Float boost) {
        getWrapper().matchAllQuery(condition, boost);
        return typedThis;
    }


    @Override
    public Children orderByDistanceAsc(boolean condition, String column, DistanceUnit unit, GeoDistance geoDistance, GeoPoint... geoPoints) {
        getWrapper().orderByDistanceAsc(condition, column, unit, geoDistance, geoPoints);
        return typedThis;
    }

    @Override
    public Children orderByDistanceDesc(boolean condition, String column, DistanceUnit unit, GeoDistance geoDistance, GeoPoint... geoPoints) {
        getWrapper().orderByDistanceDesc(condition, column, unit, geoDistance, geoPoints);
        return typedThis;
    }

    @Override
    public Children exists(boolean condition, String column, Float boost) {
        getWrapper().exists(condition, column, boost);
        return typedThis;
    }

    @Override
    public Children or(boolean condition) {
        getWrapper().or(condition);
        return typedThis;
    }

    @Override
    public Children not(boolean condition) {
        getWrapper().not(condition);
        return typedThis;
    }

    @Override
    public Children filter(boolean condition) {
        getWrapper().filter(condition);
        return typedThis;
    }

    @Override
    public Children must(boolean condition, Consumer<Param> consumer) {
        getWrapper().must(condition, consumer);
        return typedThis;
    }

    @Override
    public Children should(boolean condition, Consumer<Param> consumer) {
        getWrapper().should(condition, consumer);
        return typedThis;
    }

    @Override
    public Children filter(boolean condition, Consumer<Param> consumer) {
        getWrapper().filter(condition, consumer);
        return typedThis;
    }

    @Override
    public Children not(boolean condition, Consumer<Param> consumer) {
        getWrapper().not(condition, consumer);
        return typedThis;
    }

    @Override
    public Children nested(boolean condition, String path, Consumer<Param> consumer, ScoreMode scoreMode) {
        getWrapper().nested(condition, path, consumer, scoreMode);
        return typedThis;
    }

    @Override
    public Children from(Integer from) {
        getWrapper().from(from);
        return typedThis;
    }

    @Override
    public Children size(Integer size) {
        getWrapper().size(size);
        return typedThis;
    }

    @Override
    public Children limit(Integer n) {
        getWrapper().limit(n);
        return typedThis;
    }

    @Override
    public Children limit(Integer m, Integer n) {
        getWrapper().limit(m, n);
        return typedThis;
    }

    @Override
    public Children setSearchSourceBuilder(boolean condition, SearchSourceBuilder searchSourceBuilder) {
        getWrapper().setSearchSourceBuilder(condition, searchSourceBuilder);
        return typedThis;
    }

    @Override
    public Children mix(boolean condition, QueryBuilder queryBuilder) {
        getWrapper().mix(condition, queryBuilder);
        return typedThis;
    }

    @Override
    public Children select(String... columns) {
        getWrapper().select(columns);
        return typedThis;
    }

    @Override
    public Children select(Predicate<EntityFieldInfo> predicate) {
        getWrapper().select(predicate);
        return typedThis;
    }

    @Override
    public Children select(Class<T> entityClass, Predicate<EntityFieldInfo> predicate) {
        getWrapper().select(entityClass, predicate);
        return typedThis;
    }

    @Override
    public Children notSelect(String... columns) {
        getWrapper().notSelect(columns);
        return typedThis;
    }

    @Override
    public Children minScore(Float score) {
        getWrapper().minScore(score);
        return typedThis;
    }

    @Override
    public Children trackScores() {
        getWrapper().trackScores();
        return typedThis;
    }

    @Override
    public Children index(boolean condition, String... indexNames) {
        getWrapper().index(condition, indexNames);
        return typedThis;
    }

    @Override
    public Children set(boolean condition, String column, Object val) {
        getWrapper().set(condition, column, val);
        return typedThis;
    }

    @Override
    public Children indexName(String... indexNames) {
        getWrapper().indexName(indexNames);
        return typedThis;
    }

    @Override
    public Children preference(boolean condition, String preference) {
        getWrapper().preference(condition, preference);
        return typedThis;
    }

    @Override
    public Children maxResultWindow(Integer maxResultWindow) {
        getWrapper().maxResultWindow(maxResultWindow);
        return typedThis;
    }

    @Override
    public Children settings(Integer shards, Integer replicas) {
        getWrapper().settings(shards, replicas);
        return typedThis;
    }

    @Override
    public Children settings(Settings settings) {
        getWrapper().settings(settings);
        return typedThis;
    }

    @Override
    public Children mapping(Map<String, Object> mapping) {
        getWrapper().mapping(mapping);
        return typedThis;
    }

    @Override
    public Children mapping(String column, FieldType fieldType, String analyzer, String searchAnalyzer, String dateFormat, Boolean fieldData, Float boost) {
        getWrapper().mapping(column, fieldType, analyzer, searchAnalyzer, dateFormat, fieldData, boost);
        return typedThis;
    }

    @Override
    public Children createAlias(String aliasName) {
        getWrapper().createAlias(aliasName);
        return typedThis;
    }

    @Override
    public Children join(String column, String parentName, String childName) {
        getWrapper().join(column, parentName, childName);
        return typedThis;
    }
}
