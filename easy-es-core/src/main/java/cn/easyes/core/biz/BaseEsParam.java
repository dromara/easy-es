package cn.easyes.core.biz;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 核心 基本参数
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
public class BaseEsParam {
    /**
     * 存放必须满足的条件列表(必须满足,相当于MySQL中的and)
     */
    private List<FieldValueModel> mustList = new ArrayList<>();
    /**
     * 存放多字段单值的必须满足条件列表 multiMatchQuery
     */
    private List<FieldValueModel> mustMultiFieldList = new ArrayList<>();
    /**
     * 存放必须满足的条件列表,区别是不计算得分(必须满足,与must区别是不计算得分,效率更高)
     */
    private List<FieldValueModel> filterList = new ArrayList<>();
    /**
     * 存放或条件列表(或,相当于MySQL中的or)
     */
    private List<FieldValueModel> shouldList = new ArrayList<>();
    /**
     * 存放多字段单值的或条件列表
     */
    private List<FieldValueModel> shouldMultiFieldList = new ArrayList<>();
    /**
     * 存放必须不满足的条件列表(否,相当于MySQL中的!=)
     */
    private List<FieldValueModel> mustNotList = new ArrayList<>();
    /**
     * 存放大于的条件列表(大于,相当于mysql中的>)
     */
    private List<FieldValueModel> gtList = new ArrayList<>();
    /**
     * 存放小于的条件列表(小于,相当于mysql中的<)
     */
    private List<FieldValueModel> ltList = new ArrayList<>();
    /**
     * 存放大于等于的条件列表(大于等于,相当于mysql中的>=)
     */
    private List<FieldValueModel> geList = new ArrayList<>();
    /**
     * 存放小于等于的条件列表(小于等于,相当于mysql中的<=)
     */
    private List<FieldValueModel> leList = new ArrayList<>();
    /**
     * 存放必须符合的多值条件列表(相当于mysql中的in)
     */
    private List<FieldValueModel> inList = new ArrayList<>();
    /**
     * 存放必须不符合的多值条件列表(相当于mysql中的not in)
     */
    private List<FieldValueModel> notInList = new ArrayList<>();
    /**
     * 存放为null的条件列表(相当于mysql中的 is null)
     */
    private List<FieldValueModel> isNullList = new ArrayList<>();
    /**
     * 存放不为null的条件列表(相当于mysql中的 not null)
     */
    private List<FieldValueModel> notNullList = new ArrayList<>();
    /**
     * 存放不在指定范围内的条件列表(相当于mysql中的 between)
     */
    private List<FieldValueModel> betweenList = new ArrayList<>();
    /**
     * 存放不在指定范围内的条件列表(相当于mysql中的 not between)
     */
    private List<FieldValueModel> notBetweenList = new ArrayList<>();
    /**
     * 存放左模糊的条件列表(相当于mysql中的like %xxx)
     */
    private List<FieldValueModel> likeLeftList = new ArrayList<>();
    /**
     * 存放右模糊的条件列表(相当于mysql中的like xxx%)
     */
    private List<FieldValueModel> likeRightList = new ArrayList<>();
    /**
     * 参数类型 参见: BaseEsParamTypeEnum
     */
    private Integer type;
    /**
     * must条件转filter
     */
    protected Boolean enableMust2Filter;


    /**
     * 查询模型
     */
    @Data
    @Builder
    public static class FieldValueModel {
        /**
         * 字段名
         */
        private String field;
        /**
         * 值
         */
        private Object value;
        /**
         * 左区间值 仅between,notBetween时使用
         */
        private Object leftValue;
        /**
         * 右区间值 仅between,notBetween时使用
         */
        private Object rightValue;
        /**
         * boost权重值
         */
        private Float boost;
        /**
         * 值列表(仅in操作时使用)
         */
        private List<Object> values;
        /**
         * 查询类型 参见:EsQueryTypeEnum
         */
        private Integer esQueryType;
        /**
         * 连接类型 参见:EsAttachTypeEnum 由于should 包含转换的情况 所以转换之后应仍使用原来的连接类型
         */
        private Integer originalAttachType;
        /**
         * 字段列表
         */
        private List<String> fields;
        /**
         * 拓展字段
         */
        private Object ext;
        /**
         * 最小匹配度 百分比
         */
        private int minimumShouldMatch;
        /**
         * nested path
         */
        private String path;
        /**
         * 得分模式or是否计算得分
         */
        private Object scoreMode;
    }

    /**
     * 重置查询条件 主要用于处理or查询条件
     *
     * @param baseEsParam 基础参数
     */
    public static void setUp(BaseEsParam baseEsParam) {
        // 获取原查询条件
        List<FieldValueModel> mustList = baseEsParam.getMustList();
        List<FieldValueModel> mustNotList = baseEsParam.getMustNotList();
        List<FieldValueModel> mustMultiFieldList = baseEsParam.getMustMultiFieldList();
        List<FieldValueModel> filterList = baseEsParam.getFilterList();
        List<FieldValueModel> shouldList = baseEsParam.getShouldList();
        List<FieldValueModel> shouldMultiFieldList = baseEsParam.getShouldMultiFieldList();
        List<FieldValueModel> gtList = baseEsParam.getGtList();
        List<FieldValueModel> ltList = baseEsParam.getLtList();
        List<FieldValueModel> geList = baseEsParam.getGeList();
        List<FieldValueModel> leList = baseEsParam.getLeList();
        List<FieldValueModel> betweenList = baseEsParam.getBetweenList();
        List<FieldValueModel> notBetweenList = baseEsParam.getNotBetweenList();
        List<FieldValueModel> inList = baseEsParam.getInList();
        List<FieldValueModel> notInList = baseEsParam.getNotInList();
        List<FieldValueModel> isNullList = baseEsParam.getIsNullList();
        List<FieldValueModel> notNullList = baseEsParam.getNotNullList();
        List<FieldValueModel> likeLeftList = baseEsParam.getLikeLeftList();
        List<FieldValueModel> likeRightList = baseEsParam.getLikeRightList();

        // 把原来必须满足的条件转入should列表
        shouldList.addAll(mustList);
        shouldList.addAll(mustNotList);
        shouldList.addAll(filterList);
        shouldList.addAll(gtList);
        shouldList.addAll(ltList);
        shouldList.addAll(geList);
        shouldList.addAll(leList);
        shouldList.addAll(betweenList);
        shouldList.addAll(notBetweenList);
        shouldList.addAll(inList);
        shouldList.addAll(notInList);
        shouldList.addAll(isNullList);
        shouldList.addAll(notNullList);
        shouldList.addAll(likeLeftList);
        shouldList.addAll(likeRightList);
        shouldMultiFieldList.addAll(mustMultiFieldList);
        baseEsParam.setShouldList(shouldList);

        // 置空原必须满足的条件列表
        baseEsParam.setMustList(Collections.EMPTY_LIST);
        baseEsParam.setMustNotList(Collections.EMPTY_LIST);
        baseEsParam.setFilterList(Collections.EMPTY_LIST);
        baseEsParam.setGtList(Collections.EMPTY_LIST);
        baseEsParam.setLtList(Collections.EMPTY_LIST);
        baseEsParam.setGeList(Collections.EMPTY_LIST);
        baseEsParam.setLeList(Collections.EMPTY_LIST);
        baseEsParam.setBetweenList(Collections.EMPTY_LIST);
        baseEsParam.setNotBetweenList(Collections.EMPTY_LIST);
        baseEsParam.setInList(Collections.EMPTY_LIST);
        baseEsParam.setNotInList(Collections.EMPTY_LIST);
        baseEsParam.setIsNullList(Collections.EMPTY_LIST);
        baseEsParam.setNotNullList(Collections.EMPTY_LIST);
        baseEsParam.setLikeLeftList(Collections.EMPTY_LIST);
        baseEsParam.setLikeRightList(Collections.EMPTY_LIST);
        baseEsParam.setMustMultiFieldList(Collections.EMPTY_LIST);
    }
}
