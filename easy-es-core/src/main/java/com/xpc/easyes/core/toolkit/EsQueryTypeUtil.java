package com.xpc.easyes.core.toolkit;

import com.xpc.easyes.core.common.EntityInfo;
import com.xpc.easyes.core.config.GlobalConfig;
import com.xpc.easyes.core.enums.JoinTypeEnum;
import com.xpc.easyes.core.params.BaseEsParam;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.HasParentQueryBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.xpc.easyes.core.constants.BaseEsConstants.*;
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
     * @param boolQueryBuilder 参数连接器
     * @param attachType       连接类型
     * @param model            参数
     * @param entityInfo       实体信息
     * @param dbConfig         全局配置
     */
    public static void addQueryByType(BoolQueryBuilder boolQueryBuilder, Integer attachType, BaseEsParam.FieldValueModel
            model, EntityInfo entityInfo, GlobalConfig.DbConfig dbConfig) {
        Integer queryType = model.getEsQueryType();
        Object value = model.getValue();
        Float boost = model.getBoost();
        String path = model.getPath();
        Integer originalAttachType = model.getOriginalAttachType();
        String field = model.getField();
        // 自定义字段名称及驼峰和嵌套字段名称的处理
        if (StringUtils.isBlank(path)) {
            field = FieldUtils.getRealField(field, entityInfo.getMappingColumnMap(), dbConfig);
        } else {
            // 嵌套或父子类型
            field = FieldUtils.getRealField(field, entityInfo.getNestedMappingColumnMapByPath(path), dbConfig);
        }

        // 封装查询参数
        if (Objects.equals(queryType, TERM_QUERY.getType())) {
            // 封装精确查询参数
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(field, value).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, termQueryBuilder);
        } else if (Objects.equals(queryType, TERMS_QUERY.getType())) {
            // 此处兼容由or转入shouldList的in参数
            Collection<?> values = Objects.isNull(value) ? model.getValues() : (Collection<?>) value;
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(field, values).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, termsQueryBuilder);
        } else if (Objects.equals(queryType, MATCH_PHASE.getType())) {
            // 封装模糊分词查询参数(分词必须按原关键词顺序)
            MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery(field, value).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, matchPhraseQueryBuilder);
        } else if (Objects.equals(queryType, MATCH_PHRASE_PREFIX.getType())) {
            MatchPhrasePrefixQueryBuilder matchPhrasePrefixQueryBuilder = QueryBuilders.matchPhrasePrefixQuery(field, value)
                    .maxExpansions((Integer) model.getExt()).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, matchPhrasePrefixQueryBuilder);
        } else if (Objects.equals(queryType, PREFIX_QUERY.getType())) {
            PrefixQueryBuilder prefixQueryBuilder = QueryBuilders.prefixQuery(field, value.toString()).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, prefixQueryBuilder);
        } else if (Objects.equals(queryType, QUERY_STRING_QUERY.getType())) {
            QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(value.toString()).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, queryStringQueryBuilder);
        } else if (Objects.equals(queryType, MATCH_QUERY.getType())) {
            // 封装模糊分词查询参数(可无序)
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(field, value).boost(boost);
            if (StringUtils.isBlank(path)) {
                setQueryBuilder(boolQueryBuilder, attachType, matchQueryBuilder);
            } else {
                // 嵌套类型
                if (JoinTypeEnum.NESTED.equals(model.getExt())) {
                    matchQueryBuilder = QueryBuilders.matchQuery(path + PATH_FIELD_JOIN + field, value).boost(boost);
                    NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(model.getPath(), matchQueryBuilder, (ScoreMode) model.getScoreMode());
                    setQueryBuilder(boolQueryBuilder, attachType, nestedQueryBuilder);
                } else if (JoinTypeEnum.HAS_CHILD.equals(model.getExt())) {
                    HasChildQueryBuilder hasChildQueryBuilder = new HasChildQueryBuilder(path, matchQueryBuilder, (ScoreMode) model.getScoreMode());
                    setQueryBuilder(boolQueryBuilder, attachType, hasChildQueryBuilder);
                } else if (JoinTypeEnum.HAS_PARENT.equals(model.getExt())) {
                    HasParentQueryBuilder hasParentQueryBuilder = new HasParentQueryBuilder(path, matchQueryBuilder, (Boolean) model.getScoreMode());
                    setQueryBuilder(boolQueryBuilder, attachType, hasParentQueryBuilder);
                }
            }
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
     * 添加查询类型 适用于多字段单值情形
     *
     * @param boolQueryBuilder 参数连接器
     * @param queryType        查询类型
     * @param attachType       连接类型
     * @param fields           字段列表
     * @param value            值
     * @param ext              拓展字段
     * @param minShouldMatch   最小匹配百分比
     * @param boost            权重
     */
    public static void addQueryByType(BoolQueryBuilder boolQueryBuilder, Integer queryType, Integer attachType,
                                      List<String> fields, Object value, Object ext, Integer minShouldMatch, Float boost) {
        if (Objects.equals(queryType, MULTI_MATCH_QUERY.getType())) {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(value, fields.toArray(new String[0])).boost(boost);
            if (ext instanceof Operator) {
                Operator operator = (Operator) ext;
                multiMatchQueryBuilder.operator(operator);
                multiMatchQueryBuilder.minimumShouldMatch(minShouldMatch + PERCENT);
            }
            setQueryBuilder(boolQueryBuilder, attachType, multiMatchQueryBuilder);
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
     *
     * @param boolQueryBuilder  参数连接器
     * @param attachType        连接类型
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
