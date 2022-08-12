package cn.easyes.core.toolkit;

import cn.easyes.common.constants.BaseEsConstants;
import cn.easyes.common.enums.EsAttachTypeEnum;
import cn.easyes.common.enums.EsQueryTypeEnum;
import cn.easyes.common.enums.JoinTypeEnum;
import cn.easyes.common.utils.StringUtils;
import cn.easyes.core.biz.BaseEsParam;
import cn.easyes.core.biz.EntityInfo;
import cn.easyes.core.config.GlobalConfig;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.HasParentQueryBuilder;
import org.elasticsearch.join.query.ParentIdQueryBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static cn.easyes.common.constants.BaseEsConstants.PATH_FIELD_JOIN;

/**
 * 核心 查询参数封装工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class EsQueryTypeUtil {
    /**
     * 添加查询类型 精确匹配/模糊匹配/范围匹配等
     *
     * @param boolQueryBuilder  参数连接器
     * @param attachType        连接类型
     * @param enableMust2Filter must条件是否转filter
     * @param model             参数
     * @param entityInfo        实体信息
     * @param dbConfig          全局配置
     */
    public static void addQueryByType(BoolQueryBuilder boolQueryBuilder, Integer attachType, boolean enableMust2Filter,
                                      BaseEsParam.FieldValueModel model, EntityInfo entityInfo, GlobalConfig.DbConfig dbConfig) {

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
        if (Objects.equals(queryType, EsQueryTypeEnum.TERM_QUERY.getType())) {
            // 封装精确查询参数
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(field, value).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, termQueryBuilder);
        } else if (Objects.equals(queryType, EsQueryTypeEnum.TERMS_QUERY.getType())) {
            // 此处兼容由or转入shouldList的in参数
            Collection<?> values = Objects.isNull(value) ? model.getValues() : (Collection<?>) value;
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(field, values).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, termsQueryBuilder);
        } else if (Objects.equals(queryType, EsQueryTypeEnum.MATCH_PHRASE.getType())) {
            // 封装模糊分词查询参数(分词必须按原关键词顺序)
            MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery(field, value).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, matchPhraseQueryBuilder);
        } else if (Objects.equals(queryType, EsQueryTypeEnum.MATCH_PHRASE_PREFIX.getType())) {
            MatchPhrasePrefixQueryBuilder matchPhrasePrefixQueryBuilder = QueryBuilders.matchPhrasePrefixQuery(field, value)
                    .maxExpansions((Integer) model.getExt()).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, matchPhrasePrefixQueryBuilder);
        } else if (Objects.equals(queryType, EsQueryTypeEnum.PREFIX_QUERY.getType())) {
            PrefixQueryBuilder prefixQueryBuilder = QueryBuilders.prefixQuery(field, value.toString()).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, prefixQueryBuilder);
        } else if (Objects.equals(queryType, EsQueryTypeEnum.QUERY_STRING_QUERY.getType())) {
            QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(value.toString()).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, queryStringQueryBuilder);
        } else if (Objects.equals(queryType, EsQueryTypeEnum.MATCH_QUERY.getType())) {
            // 封装模糊分词查询参数(可无序)
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(field, value).boost(boost);
            if (StringUtils.isBlank(path)) {
                setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, matchQueryBuilder);
            } else {
                // 嵌套类型及父子类型处理
                path = FieldUtils.getRealField(path, entityInfo.getMappingColumnMap(), dbConfig);
                if (JoinTypeEnum.NESTED.equals(model.getExt())) {
                    matchQueryBuilder = QueryBuilders.matchQuery(path + PATH_FIELD_JOIN + field, value).boost(boost);
                    NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(path, matchQueryBuilder, (ScoreMode) model.getScoreMode());
                    setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, nestedQueryBuilder);
                } else if (JoinTypeEnum.HAS_CHILD.equals(model.getExt())) {
                    HasChildQueryBuilder hasChildQueryBuilder = new HasChildQueryBuilder(path, matchQueryBuilder, (ScoreMode) model.getScoreMode()).boost(boost);
                    setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, hasChildQueryBuilder);
                } else if (JoinTypeEnum.HAS_PARENT.equals(model.getExt())) {
                    HasParentQueryBuilder hasParentQueryBuilder = new HasParentQueryBuilder(path, matchQueryBuilder, (Boolean) model.getScoreMode()).boost(boost);
                    setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, hasParentQueryBuilder);
                } else if (JoinTypeEnum.PARENT_ID.equals(model.getExt())) {
                    ParentIdQueryBuilder parentIdQueryBuilder = new ParentIdQueryBuilder(path, model.getValue().toString()).boost(boost);
                    setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, parentIdQueryBuilder);
                }
            }
        } else if (Objects.equals(queryType, EsQueryTypeEnum.RANGE_QUERY.getType())) {
            // 封装范围查询参数
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(field).boost(boost);
            if (Objects.equals(originalAttachType, EsAttachTypeEnum.GT.getType())) {
                rangeQueryBuilder.gt(value);
            } else if (Objects.equals(originalAttachType, EsAttachTypeEnum.LT.getType())) {
                rangeQueryBuilder.lt(value);
            } else if (Objects.equals(originalAttachType, EsAttachTypeEnum.GE.getType())) {
                rangeQueryBuilder.gte(value);
            } else if (Objects.equals(originalAttachType, EsAttachTypeEnum.LE.getType())) {
                rangeQueryBuilder.lte(value);
            }
            setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, rangeQueryBuilder);
        } else if (Objects.equals(queryType, EsQueryTypeEnum.EXISTS_QUERY.getType())) {
            // 封装是否存在查询参数
            ExistsQueryBuilder existsQueryBuilder = QueryBuilders.existsQuery(field).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, existsQueryBuilder);
        } else if (Objects.equals(queryType, EsQueryTypeEnum.WILDCARD_QUERY.getType())) {
            String query;
            if (Objects.equals(attachType, EsAttachTypeEnum.LIKE_LEFT.getType())) {
                query = BaseEsConstants.WILDCARD_SIGN + value;
            } else if (Objects.equals(attachType, EsAttachTypeEnum.LIKE_RIGHT.getType())) {
                query = value + BaseEsConstants.WILDCARD_SIGN;
            } else {
                query = BaseEsConstants.WILDCARD_SIGN + value + BaseEsConstants.WILDCARD_SIGN;
            }
            WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(field, query).boost(boost);
            setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, wildcardQueryBuilder);
        }
    }


    /**
     * 添加查询类型 适用于多字段单值情形
     *
     * @param boolQueryBuilder   参数连接器
     * @param queryType          查询类型
     * @param attachType         连接类型
     * @param originalAttachType 初始连接类型
     * @param enableMust2Filter  must是否转filter
     * @param fields             字段列表
     * @param value              值
     * @param ext                拓展字段
     * @param minShouldMatch     最小匹配百分比
     * @param boost              权重
     */
    public static void addQueryByType(BoolQueryBuilder boolQueryBuilder, Integer queryType, Integer attachType, Integer originalAttachType,
                                      boolean enableMust2Filter, List<String> fields, Object value, Object ext, Integer minShouldMatch, Float boost) {
        if (Objects.equals(queryType, EsQueryTypeEnum.MULTI_MATCH_QUERY.getType())) {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(value, fields.toArray(new String[0])).boost(boost);
            if (ext instanceof Operator) {
                Operator operator = (Operator) ext;
                multiMatchQueryBuilder.operator(operator);
                multiMatchQueryBuilder.minimumShouldMatch(minShouldMatch + BaseEsConstants.PERCENT);
            }
            setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, multiMatchQueryBuilder);
        }
    }

    /**
     * 添加查询类型 精确匹配 用于between 操作
     *
     * @param boolQueryBuilder   参数连接器
     * @param queryType          查询类型
     * @param attachType         连接类型
     * @param originalAttachType 初始连接类型
     * @param enableMust2Filter  must是否转filter
     * @param field              字段
     * @param leftValue          左值
     * @param rightValue         右值
     * @param boost              权重
     */
    public static void addQueryByType(BoolQueryBuilder boolQueryBuilder, Integer queryType, Integer attachType, Integer originalAttachType,
                                      boolean enableMust2Filter, String field, Object leftValue, Object rightValue, Float boost) {
        if (Objects.equals(queryType, EsQueryTypeEnum.INTERVAL_QUERY.getType())) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(field).boost(boost);
            rangeQueryBuilder.gte(leftValue).lte(rightValue);
            setQueryBuilder(boolQueryBuilder, attachType, originalAttachType, enableMust2Filter, rangeQueryBuilder);
        }
    }


    /**
     * 设置连接类型 must,filter,should,must not 对应mysql中的and,and,or,!= 丶 not like...
     *
     * @param boolQueryBuilder 参数连接器
     * @param attachType       连接类型
     * @param queryBuilder     匹配参数
     */
    private static void setQueryBuilder(BoolQueryBuilder boolQueryBuilder, Integer attachType, Integer originalAttachType,
                                        boolean enableMust2Filter, QueryBuilder queryBuilder) {
        boolean must = Objects.equals(attachType, EsAttachTypeEnum.MUST.getType()) || Objects.equals(attachType, EsAttachTypeEnum.GT.getType())
                || Objects.equals(attachType, EsAttachTypeEnum.LT.getType()) || Objects.equals(attachType, EsAttachTypeEnum.GE.getType())
                || Objects.equals(attachType, EsAttachTypeEnum.LE.getType()) || Objects.equals(attachType, EsAttachTypeEnum.IN.getType())
                || Objects.equals(attachType, EsAttachTypeEnum.BETWEEN.getType()) || Objects.equals(attachType, EsAttachTypeEnum.EXISTS.getType())
                || Objects.equals(attachType, EsAttachTypeEnum.LIKE_LEFT.getType()) || Objects.equals(attachType, EsAttachTypeEnum.LIKE_RIGHT.getType());
        boolean mustNot = Objects.equals(attachType, EsAttachTypeEnum.MUST_NOT.getType()) || Objects.equals(attachType, EsAttachTypeEnum.NOT_IN.getType())
                || Objects.equals(attachType, EsAttachTypeEnum.NOT_EXISTS.getType()) || Objects.equals(attachType, EsAttachTypeEnum.NOT_BETWEEN.getType());
        if (must) {
            if (enableMust2Filter) {
                boolQueryBuilder.filter(queryBuilder);
            } else {
                boolQueryBuilder.must(queryBuilder);
            }
        } else if (Objects.equals(attachType, EsAttachTypeEnum.FILTER.getType())) {
            boolQueryBuilder.filter(queryBuilder);
        } else if (Objects.equals(attachType, EsAttachTypeEnum.SHOULD.getType())) {
            // 针对or()转换过的should需要保留其原始类型
            if (Objects.equals(originalAttachType, EsAttachTypeEnum.MUST_NOT.getType())) {
                BoolQueryBuilder not = QueryBuilders.boolQuery().mustNot(queryBuilder);
                boolQueryBuilder.should(not);
            } else {
                boolQueryBuilder.should(queryBuilder);
            }
        } else if (mustNot) {
            boolQueryBuilder.mustNot(queryBuilder);
        }
    }

}
