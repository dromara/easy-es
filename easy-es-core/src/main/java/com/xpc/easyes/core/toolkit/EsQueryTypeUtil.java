package com.xpc.easyes.core.toolkit;

import org.elasticsearch.index.query.*;

import java.util.Collection;
import java.util.Objects;

import static com.xpc.easyes.core.constants.BaseEsConstants.WILDCARD_SIGN;
import static com.xpc.easyes.core.enums.EsAttachTypeEnum.*;
import static com.xpc.easyes.core.enums.EsQueryTypeEnum.*;

/**
 * 核心 查询参数封装工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class EsQueryTypeUtil {
    /**
     * 添加查询类型 精确匹配/模糊匹配/范围匹配等
     *
     * @param boolQueryBuilder   参数连接器
     * @param queryType          查询类型
     * @param attachType         连接类型
     * @param originalAttachType 原始连接类型
     * @param field              字段
     * @param value              值
     * @param boost              权重
     */
    public static void addQueryByType(BoolQueryBuilder boolQueryBuilder, Integer queryType, Integer attachType, Integer originalAttachType, String field, Object value, Float boost) {
        if (Objects.equals(queryType, TERM_QUERY.getType())) {
            // 封装精确查询参数
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(field, value).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, termQueryBuilder);
        } else if (Objects.equals(queryType, MATCH_QUERY.getType())) {
            // 封装模糊分词查询参数
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(field, value).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, matchQueryBuilder);
        } else if (Objects.equals(queryType, RANGE_QUERY.getType())) {
            // 封装范围查询参数
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(field).boost(boost);
            if (Objects.equals(originalAttachType, GT.getType())) {
                rangeQueryBuilder.gt(value);
            } else if (Objects.equals(originalAttachType, LT.getType())) {
                rangeQueryBuilder.lt(value);
            } else if (Objects.equals(originalAttachType, GE.getType())) {
                rangeQueryBuilder.gte(value);
            } else if (Objects.equals(originalAttachType, LE.getType())) {
                rangeQueryBuilder.lte(value);
            }
            setQueryBuilder(boolQueryBuilder, attachType, rangeQueryBuilder);
        } else if (Objects.equals(queryType, EXISTS_QUERY.getType())) {
            // 封装是否存在查询参数
            ExistsQueryBuilder existsQueryBuilder = QueryBuilders.existsQuery(field).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, existsQueryBuilder);
        } else if (Objects.equals(queryType, WILDCARD_QUERY.getType())) {
            String query;
            if (Objects.equals(attachType, LIKE_LEFT.getType())) {
                query = WILDCARD_SIGN + value;
            } else if (Objects.equals(attachType, LIKE_RIGHT.getType())) {
                query = value + WILDCARD_SIGN;
            } else {
                query = WILDCARD_SIGN + value + WILDCARD_SIGN;
            }
            WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(field, query).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, wildcardQueryBuilder);
        }
    }

    /**
     * 添加查询类型 精确匹配 用于in 操作
     *
     * @param boolQueryBuilder 参数连接器
     * @param queryType        查询类型
     * @param attachType       连接类型
     * @param field            字段
     * @param values           值
     * @param boost            权重
     */
    public static void addQueryByType(BoolQueryBuilder boolQueryBuilder, Integer queryType, Integer attachType, String field, Collection<?> values, Float boost) {
        if (Objects.equals(queryType, TERMS_QUERY.getType())) {
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(field, values).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, termsQueryBuilder);
        }
    }

    /**
     * 添加查询类型 精确匹配 用于between 操作
     *
     * @param boolQueryBuilder 参数连接器
     * @param queryType        查询类型
     * @param attachType       连接类型
     * @param field            字段
     * @param leftValue        左值
     * @param rightValue       右值
     * @param boost            权重
     */
    public static void addQueryByType(BoolQueryBuilder boolQueryBuilder, Integer queryType, Integer attachType, String field, Object leftValue, Object rightValue, Float boost) {
        if (Objects.equals(queryType, INTERVAL_QUERY.getType())) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(field).boost(boost);
            rangeQueryBuilder.gte(leftValue).lte(rightValue);
            setQueryBuilder(boolQueryBuilder, attachType, rangeQueryBuilder);
        }
    }


    /**
     * 设置连接类型 must,filter,should,must not 对应mysql中的and,and,or,!= 丶 not like...
     * @param boolQueryBuilder 参数连接器
     * @param attachType 连接类型
     * @param matchQueryBuilder 匹配参数
     */
    private static void setQueryBuilder(BoolQueryBuilder boolQueryBuilder, Integer attachType, QueryBuilder matchQueryBuilder) {
        boolean must = Objects.equals(attachType, MUST.getType()) || Objects.equals(attachType, GT.getType())
                || Objects.equals(attachType, LT.getType()) || Objects.equals(attachType, GE.getType())
                || Objects.equals(attachType, LE.getType()) || Objects.equals(attachType, IN.getType())
                || Objects.equals(attachType, BETWEEN.getType()) || Objects.equals(attachType, EXISTS.getType())
                || Objects.equals(attachType, LIKE_LEFT.getType()) || Objects.equals(attachType, LIKE_RIGHT.getType());
        boolean mustNot = Objects.equals(attachType, MUST_NOT.getType()) || Objects.equals(attachType, NOT_IN.getType())
                || Objects.equals(attachType, NOT_EXISTS.getType()) || Objects.equals(attachType, NOT_BETWEEN.getType());
        if (must) {
            boolQueryBuilder.must(matchQueryBuilder);
        } else if (Objects.equals(attachType, FILTER.getType())) {
            boolQueryBuilder.filter(matchQueryBuilder);
        } else if (Objects.equals(attachType, SHOULD.getType())) {
            boolQueryBuilder.should(matchQueryBuilder);
        } else if (mustNot) {
            boolQueryBuilder.mustNot(matchQueryBuilder);
        }
    }

}
