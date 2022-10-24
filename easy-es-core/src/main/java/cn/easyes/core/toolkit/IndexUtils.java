package cn.easyes.core.toolkit;

import cn.easyes.common.constants.Analyzer;
import cn.easyes.common.constants.BaseEsConstants;
import cn.easyes.common.enums.FieldType;
import cn.easyes.common.enums.JdkDataTypeEnum;
import cn.easyes.common.enums.ProcessIndexStrategyEnum;
import cn.easyes.common.params.DefaultChildClass;
import cn.easyes.common.utils.*;
import cn.easyes.core.biz.*;
import cn.easyes.core.cache.GlobalConfigCache;
import cn.easyes.core.config.GlobalConfig;
import com.alibaba.fastjson.JSONObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static cn.easyes.common.constants.BaseEsConstants.*;


/**
 * 索引工具类
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexUtils {

    private static final String FIELDS_KEY;
    private static final Map<String, Object> FIELDS_MAP;
    private static final int IGNORE_ABOVE;
    private static final String IGNORE_ABOVE_KEY;


    static {
        FIELDS_MAP = new HashMap<>();
        FIELDS_KEY = "fields";
        IGNORE_ABOVE = 256;
        IGNORE_ABOVE_KEY = "ignore_above";
        Map<String, Object> keywordsMap = new HashMap<>();
        keywordsMap.put(TYPE, FieldType.KEYWORD.getType());
        keywordsMap.put(IGNORE_ABOVE_KEY, IGNORE_ABOVE);
        FIELDS_MAP.put(FieldType.KEYWORD.getType(), keywordsMap);
    }

    /**
     * 是否存在索引
     *
     * @param client    RestHighLevelClient
     * @param indexName 索引名
     * @return 是否存在
     */
    public static boolean existsIndex(RestHighLevelClient client, String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("existsIndex exception indexName: %s", e, indexName);
        }
    }

    /**
     * 创建索引
     *
     * @param client     RestHighLevelClient
     * @param entityInfo 实体信息
     * @param indexParam 创建索引参数
     * @return 是否创建成功
     */
    public static boolean createIndex(RestHighLevelClient client, EntityInfo entityInfo, CreateIndexParam indexParam) {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexParam.getIndexName());

        // 设置settings信息
        if (Objects.isNull(indexParam.getSettings())) {
            // 分片个副本信息
            Settings.Builder settings = Settings.builder();
            Optional.ofNullable(indexParam.getShardsNum()).ifPresent(shards -> settings.put(BaseEsConstants.SHARDS_FIELD, shards));
            Optional.ofNullable(indexParam.getReplicasNum()).ifPresent(replicas -> settings.put(BaseEsConstants.REPLICAS_FIELD, replicas));
            createIndexRequest.settings(settings);
        } else {
            // 用户自定义settings
            createIndexRequest.settings(indexParam.getSettings());
        }

        // mapping信息
        if (Objects.isNull(indexParam.getMapping())) {
            Map<String, Object> mapping = initMapping(entityInfo, indexParam.getEsIndexParamList());
            createIndexRequest.mapping(mapping);
        } else {
            // 用户自定义的mapping
            createIndexRequest.mapping(indexParam.getMapping());
        }

        // 别名信息
        Optional.ofNullable(indexParam.getAliasName()).ifPresent(aliasName -> {
            Alias alias = new Alias(aliasName);
            createIndexRequest.alias(alias);
        });

        // 创建索引
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            return createIndexResponse.isAcknowledged();
        } catch (IOException e) {
            throw ExceptionUtils.eee("create index exception createIndexRequest: %s ", e, createIndexRequest.toString());
        }
    }

    /**
     * 创建空索引,不含字段,仅框架内部使用
     *
     * @param client    RestHighLevelClient
     * @param indexName 索引名
     * @return 是否创建成功
     */
    public static boolean createEmptyIndex(RestHighLevelClient client, String indexName) {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        CreateIndexResponse createIndexResponse;
        try {
            createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            LogUtils.info("===> distribute lock index has created");
            return Boolean.TRUE;
        }
        return createIndexResponse.isAcknowledged();
    }

    /**
     * 获取索引信息
     *
     * @param client    RestHighLevelClient
     * @param indexName 索引名
     * @return 索引信息
     */
    public static EsIndexInfo getIndexInfo(RestHighLevelClient client, String indexName) {
        GetIndexResponse getIndexResponse = getIndex(client, indexName);
        return parseGetIndexResponse(getIndexResponse, indexName);
    }

    /**
     * 获取索引信息
     *
     * @param client    RestHighLevelClient
     * @param indexName 索引名
     * @return 索引信息
     */
    public static GetIndexResponse getIndex(RestHighLevelClient client, String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            return client.indices().get(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("getIndex exception indexName: %s", e, indexName);
        }
    }

    /**
     * 添加默认索引别名
     *
     * @param client    RestHighLevelClient
     * @param indexName 索引名
     */
    public static void addDefaultAlias(RestHighLevelClient client, String indexName) {
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        IndicesAliasesRequest.AliasActions aliasActions =
                new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD);
        aliasActions.index(indexName);
        aliasActions.alias(BaseEsConstants.DEFAULT_ALIAS);
        indicesAliasesRequest.addAliasAction(aliasActions);
        try {
            client.indices().updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            LogUtils.warn("addDefaultAlias exception", e.toString());
        }

    }

    /**
     * 重建索引时的数据迁移,从旧索引迁移至新索引
     *
     * @param client           RestHighLevelClient
     * @param oldIndexName     旧索引名
     * @param releaseIndexName 新索引名
     * @return 是否操作成功
     */
    public static boolean reindex(RestHighLevelClient client, String oldIndexName, String releaseIndexName) {
        ReindexRequest reindexRequest = new ReindexRequest();
        reindexRequest.setSourceIndices(oldIndexName);
        reindexRequest.setDestIndex(releaseIndexName);
        reindexRequest.setDestOpType(BaseEsConstants.DEFAULT_DEST_OP_TYPE);
        reindexRequest.setConflicts(BaseEsConstants.DEFAULT_CONFLICTS);
        reindexRequest.setRefresh(Boolean.TRUE);
        reindexRequest.setTimeout(TimeValue.MAX_VALUE);
        try {
            BulkByScrollResponse response = client.reindex(reindexRequest, RequestOptions.DEFAULT);
            List<BulkItemResponse.Failure> bulkFailures = response.getBulkFailures();
            if (CollectionUtils.isEmpty(bulkFailures)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } catch (IOException e) {
            throw ExceptionUtils.eee("reindex exception oldIndexName:%s, releaseIndexName: %s", e, oldIndexName, releaseIndexName);
        }
    }

    /**
     * 解析索引信息
     *
     * @param getIndexResponse es返回的response
     * @param indexName        索引名
     * @return 索引信息
     */
    public static EsIndexInfo parseGetIndexResponse(GetIndexResponse getIndexResponse, String indexName) {
        EsIndexInfo esIndexInfo = new EsIndexInfo();

        // 设置是否已存在默认别名
        esIndexInfo.setHasDefaultAlias(Boolean.FALSE);
        Optional.ofNullable(getIndexResponse.getAliases())
                .flatMap(aliases -> Optional.ofNullable(aliases.get(indexName)))
                .ifPresent(aliasMetadataList ->
                        aliasMetadataList.forEach(aliasMetadata -> {
                            if (BaseEsConstants.DEFAULT_ALIAS.equals(aliasMetadata.alias())) {
                                esIndexInfo.setHasDefaultAlias(Boolean.TRUE);
                            }
                        }));

        // 设置分片及副本数
        Optional.ofNullable(getIndexResponse.getSettings())
                .flatMap(settingsMap -> Optional.ofNullable(settingsMap.get(indexName)))
                .ifPresent(p -> {
                    String shardsNumStr = p.get(BaseEsConstants.SHARDS_NUM_KEY);
                    Optional.ofNullable(shardsNumStr)
                            .ifPresent(s -> esIndexInfo.setShardsNum(Integer.parseInt(s)));
                    String replicasNumStr = p.get(BaseEsConstants.REPLICAS_NUM_KEY);
                    Optional.ofNullable(replicasNumStr)
                            .ifPresent(r -> esIndexInfo.setReplicasNum(Integer.parseInt(r)));
                });

        // 设置mapping信息
        Optional.ofNullable(getIndexResponse.getMappings())
                .flatMap(stringMappingMetadataMap -> Optional.ofNullable(stringMappingMetadataMap.get(indexName))
                        .flatMap(mappingMetadata -> Optional.ofNullable(mappingMetadata.getSourceAsMap())))
                .ifPresent(esIndexInfo::setMapping);

        return esIndexInfo;
    }

    /**
     * 根据注解/字段类型名称获取在es中的索引类型
     *
     * @param fieldType 注解中指定的es索引类型
     * @param typeName  字段类型
     * @return 推断出的es中的索引类型
     */
    public static String getEsFieldType(FieldType fieldType, String typeName) {
        if (Objects.nonNull(fieldType) && !FieldType.NONE.equals(fieldType)) {
            // 如果用户有自定义字段类型,则使用该类型
            return fieldType.getType();
        }

        // 否则根据类型推断,String以及找不到的类型一律被当做keyword处理
        JdkDataTypeEnum jdkDataType = JdkDataTypeEnum.getByType(typeName.toLowerCase());
        String type;
        switch (jdkDataType) {
            case BYTE:
                type = FieldType.BYTE.getType();
                break;
            case SHORT:
                type = FieldType.SHORT.getType();
                break;
            case INT:
            case INTEGER:
                type = FieldType.INTEGER.getType();
                break;
            case LONG:
                type = FieldType.LONG.getType();
                break;
            case FLOAT:
                type = FieldType.FLOAT.getType();
                break;
            case DOUBLE:
                type = FieldType.DOUBLE.getType();
                break;
            case BIG_DECIMAL:
            case STRING:
            case CHAR:
                type = FieldType.KEYWORD.getType();
                break;
            case BOOLEAN:
                type = FieldType.BOOLEAN.getType();
                break;
            case DATE:
            case LOCAL_DATE:
            case LOCAL_DATE_TIME:
                type = FieldType.DATE.getType();
                break;
            case LIST:
                type = FieldType.TEXT.getType();
                break;
            default:
                return FieldType.KEYWORD.getType();
        }
        return type;
    }


    /**
     * 初始化索引mapping
     *
     * @param entityInfo     实体信息
     * @param indexParamList 索引参数列表
     * @return 索引mapping
     */
    public static Map<String, Object> initMapping(EntityInfo entityInfo, List<EsIndexParam> indexParamList) {
        Map<String, Object> mapping = new HashMap<>(1);
        if (CollectionUtils.isEmpty(indexParamList)) {
            return mapping;
        }
        Map<String, Object> properties = new HashMap<>(indexParamList.size());
        GlobalConfig.DbConfig dbConfig = Optional.ofNullable(GlobalConfigCache.getGlobalConfig())
                .map(GlobalConfig::getDbConfig)
                .orElse(new GlobalConfig.DbConfig());

        initInfo(entityInfo, dbConfig, properties, indexParamList);
        mapping.put(BaseEsConstants.PROPERTIES, properties);
        return mapping;
    }


    /**
     * 初始化索引info信息
     *
     * @param entityInfo     实体信息
     * @param dbConfig       配置
     * @param properties     字段属性容器
     * @param indexParamList 索引参数列表
     * @return info信息
     */
    private static Map<String, Object> initInfo(EntityInfo entityInfo, GlobalConfig.DbConfig dbConfig, Map<String, Object> properties, List<EsIndexParam> indexParamList) {
        indexParamList.forEach(indexParam -> {
            Map<String, Object> info = new HashMap<>();
            Optional.ofNullable(indexParam.getDateFormat()).ifPresent(format -> info.put(BaseEsConstants.FORMAT, indexParam.getDateFormat()));

            // 设置type
            if (FieldType.KEYWORD_TEXT.getType().equals(indexParam.getFieldType())) {
                info.put(BaseEsConstants.TYPE, FieldType.TEXT.getType());
                info.put(FIELDS_KEY, FIELDS_MAP);
            } else {
                info.put(BaseEsConstants.TYPE, indexParam.getFieldType());
            }

            // 是否text类型或keyword_text类型
            boolean containsTextType = FieldType.TEXT.getType().equals(indexParam.getFieldType()) ||
                    FieldType.KEYWORD_TEXT.getType().equals(indexParam.getFieldType());
            if (containsTextType) {
                // 设置分词器
                Optional.ofNullable(indexParam.getAnalyzer())
                        .ifPresent(analyzer ->
                                info.put(BaseEsConstants.ANALYZER, indexParam.getAnalyzer().toLowerCase()));
                Optional.ofNullable(indexParam.getSearchAnalyzer())
                        .ifPresent(searchAnalyzer ->
                                info.put(BaseEsConstants.SEARCH_ANALYZER, indexParam.getSearchAnalyzer().toLowerCase()));

                // 设置是否对text类型进行聚合处理
                MyOptional.ofNullable(indexParam.getFieldData()).ifTrue(fieldData -> info.put(FIELD_DATA, fieldData));
            }

            // 设置权重
            Optional.ofNullable(indexParam.getBoost()).ifPresent(boost -> info.put(BOOST_KEY, indexParam.getBoost()));

            // 设置父子类型关系
            if (FieldType.JOIN.getType().equals(indexParam.getFieldType())) {
                Map<String, Object> relation = new HashMap<>(1);
                relation.put(indexParam.getParentName(), indexParam.getChildName());
                info.put(EAGER_GLOBAL_ORDINALS_KEY, Boolean.TRUE);
                info.put(BaseEsConstants.RELATIONS, relation);
            }

            // 设置嵌套类型
            if (FieldType.NESTED.getType().equals(indexParam.getFieldType())) {
                // 递归
                List<EntityFieldInfo> nestedFields = entityInfo.getNestedFieldListMap().get(indexParam.getNestedClass());
                List<EsIndexParam> esIndexParams = initIndexParam(entityInfo, nestedFields, true);
                Map<String, Object> nested = initInfo(entityInfo, dbConfig, new HashMap<>(), esIndexParams);
                info.put(BaseEsConstants.PROPERTIES, nested);
            }

            // 驼峰处理
            String fieldName = indexParam.getFieldName();
            if (dbConfig.isMapUnderscoreToCamelCase()) {
                fieldName = StringUtils.camelToUnderline(fieldName);
            }
            properties.put(fieldName, info);
        });
        return properties;
    }

    /**
     * 原子操作: 删除旧索引别名,将旧索的引别名添加至新索引
     *
     * @param client           RestHighLevelClient
     * @param oldIndexName     旧索引
     * @param releaseIndexName 新索引
     * @return 是否成功
     */
    public static boolean changeAliasAtomic(RestHighLevelClient client, String oldIndexName, String releaseIndexName) {
        IndicesAliasesRequest.AliasActions addIndexAction = new IndicesAliasesRequest.AliasActions(
                IndicesAliasesRequest.AliasActions.Type.ADD).index(releaseIndexName).alias(BaseEsConstants.DEFAULT_ALIAS);
        IndicesAliasesRequest.AliasActions removeAction = new IndicesAliasesRequest.AliasActions(
                IndicesAliasesRequest.AliasActions.Type.REMOVE).index(oldIndexName).alias(BaseEsConstants.DEFAULT_ALIAS);

        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        indicesAliasesRequest.addAliasAction(addIndexAction);
        indicesAliasesRequest.addAliasAction(removeAction);
        try {
            AcknowledgedResponse acknowledgedResponse = client.indices().updateAliases(indicesAliasesRequest,
                    RequestOptions.DEFAULT);
            return acknowledgedResponse.isAcknowledged();
        } catch (IOException e) {
            throw ExceptionUtils.eee("changeAlias exception oldIndexName: %s, releaseIndexName: %s", e, oldIndexName, releaseIndexName);
        }
    }

    /**
     * 删除索引
     *
     * @param client    RestHighLevelClient
     * @param indexName 索引名
     * @return 是否删除成功
     */
    public static boolean deleteIndex(RestHighLevelClient client, String indexName) {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
        deleteIndexRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
        try {
            AcknowledgedResponse acknowledgedResponse = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            return acknowledgedResponse.isAcknowledged();
        } catch (IOException e) {
            throw ExceptionUtils.eee("deleteIndex exception indexName: %s", e, indexName);
        }
    }

    /**
     * 根据配置生成创建索引参数
     *
     * @param entityInfo 配置信息
     * @return 创建索引参数
     */
    public static CreateIndexParam getCreateIndexParam(EntityInfo entityInfo) {
        // 初始化字段信息参数
        List<EsIndexParam> esIndexParamList = initIndexParam(entityInfo, entityInfo.getFieldList(), false);

        // 设置创建参数
        CreateIndexParam createIndexParam = new CreateIndexParam();
        createIndexParam.setEsIndexParamList(esIndexParamList);
        createIndexParam.setAliasName(entityInfo.getAliasName());
        createIndexParam.setShardsNum(entityInfo.getShardsNum());
        createIndexParam.setReplicasNum(entityInfo.getReplicasNum());
        createIndexParam.setIndexName(entityInfo.getIndexName());

        // 如果有设置新索引名称,则用新索引名覆盖原索引名进行创建
        Optional.ofNullable(entityInfo.getReleaseIndexName()).ifPresent(createIndexParam::setIndexName);
        return createIndexParam;
    }

    /**
     * 初始化索引参数
     *
     * @param entityInfo 实体信息
     * @param fieldList  字段列表
     * @param isNested   是否嵌套
     * @return 索引参数列表
     */
    public static List<EsIndexParam> initIndexParam(EntityInfo entityInfo, List<EntityFieldInfo> fieldList, boolean isNested) {
        List<EntityFieldInfo> copyFieldList = new ArrayList<>();
        copyFieldList.addAll(fieldList);
        // 针对子文档处理, 嵌套类无需重复追加
        boolean addChild = !DefaultChildClass.class.equals(entityInfo.getChildClass()) && !isNested;
        if (addChild) {
            // 追加子文档信息
            List<EntityFieldInfo> childFieldList = Optional.ofNullable(entityInfo.getChildClass())
                    .flatMap(childClass -> Optional.ofNullable(EntityInfoHelper.getEntityInfo(childClass))
                            .map(EntityInfo::getFieldList))
                    .orElse(new ArrayList<>(0));
            if (!CollectionUtils.isEmpty(childFieldList)) {
                childFieldList.forEach(child -> {
                    // 添加子文档中除JoinField以外的字段
                    if (!entityInfo.getJoinFieldName().equals(child.getMappingColumn())) {
                        copyFieldList.add(child);
                    }
                });
            }
        }

        List<EsIndexParam> esIndexParamList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(copyFieldList)) {
            copyFieldList.forEach(field -> {
                EsIndexParam esIndexParam = new EsIndexParam();
                String esFieldType = IndexUtils.getEsFieldType(field.getFieldType(), field.getColumnType());
                esIndexParam.setFieldType(esFieldType);
                if (field.isFieldData()) {
                    esIndexParam.setFieldData(field.isFieldData());
                }
                esIndexParam.setFieldName(field.getMappingColumn());
                esIndexParam.setDateFormat(field.getDateFormat());
                if (FieldType.NESTED.equals(field.getFieldType())) {
                    esIndexParam.setNestedClass(entityInfo.getPathClassMap().get(field.getColumn()));
                }
                if (!Analyzer.NONE.equals(field.getAnalyzer())) {
                    esIndexParam.setAnalyzer(field.getAnalyzer());
                }
                if (!Analyzer.NONE.equals(field.getSearchAnalyzer())) {
                    if (!Objects.equals(field.getAnalyzer(), field.getSearchAnalyzer())) {
                        esIndexParam.setSearchAnalyzer(field.getSearchAnalyzer());
                    }
                }

                Optional.ofNullable(field.getParentName()).ifPresent(esIndexParam::setParentName);
                Optional.ofNullable(field.getChildName()).ifPresent(esIndexParam::setChildName);
                esIndexParamList.add(esIndexParam);
            });
        }
        return esIndexParamList;
    }

    /**
     * 判断索引是否需要变更
     *
     * @param esIndexInfo es中的索引信息
     * @param entityInfo  配置中的索引信息
     * @return 是否需要更新索引
     */
    public static boolean isIndexNeedChange(EsIndexInfo esIndexInfo, EntityInfo entityInfo) {
        if (!entityInfo.getShardsNum().equals(esIndexInfo.getShardsNum())) {
            return Boolean.TRUE;
        }
        if (!entityInfo.getReplicasNum().equals(esIndexInfo.getReplicasNum())) {
            return Boolean.TRUE;
        }

        // 根据当前实体类及自定义注解配置, 生成最新的Mapping信息
        List<EsIndexParam> esIndexParamList = IndexUtils.initIndexParam(entityInfo, entityInfo.getFieldList(), false);

        Map<String, Object> mapping = IndexUtils.initMapping(entityInfo, esIndexParamList);

        // 与查询到的已知index对比是否发生改变
        return !mapping.equals(esIndexInfo.getMapping());
    }

    /**
     * 追加后缀重试是否存在索引,若存在,则更新当前被激活的索引名
     *
     * @param entityInfo 配置信息
     * @param client     RestHighLevelClient
     * @return 是否存在索引
     */
    public static boolean existsIndexWithRetryAndSetActiveIndex(EntityInfo entityInfo, RestHighLevelClient client) {
        boolean exists = existsIndexWithRetry(entityInfo, client);

        // 重置当前激活索引
        Optional.ofNullable(entityInfo.getRetrySuccessIndexName()).ifPresent(entityInfo::setIndexName);
        return exists;
    }

    /**
     * 追加后缀重试是否存在索引
     *
     * @param entityInfo 配置信息
     * @param client     RestHighLevelClient
     * @return 是否存在索引
     */
    public static boolean existsIndexWithRetry(EntityInfo entityInfo, RestHighLevelClient client) {
        boolean exists = IndexUtils.existsIndex(client, entityInfo.getIndexName());
        if (exists) {
            entityInfo.setRetrySuccessIndexName(entityInfo.getIndexName());
            return true;
        }

        // 重试 看加了后缀的_s0 和_s1是否存在
        for (int i = 0; i <= 1; i++) {
            String retryIndexName = entityInfo.getIndexName() + BaseEsConstants.S_SUFFIX + i;
            exists = IndexUtils.existsIndex(client, retryIndexName);
            if (exists) {
                entityInfo.setRetrySuccessIndexName(retryIndexName);
                break;
            }
        }
        return exists;
    }


    /**
     * 保存最新索引
     *
     * @param releaseIndexName 最新索引名称
     * @param client           RestHighLevelClient
     */
    public static void saveReleaseIndex(String releaseIndexName, RestHighLevelClient client) {
        IndexRequest indexRequest = new IndexRequest(LOCK_INDEX);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ACTIVE_INDEX_KEY, releaseIndexName);
        jsonObject.put(GMT_MODIFIED, System.currentTimeMillis());
        indexRequest.source(jsonObject.toJSONString(), XContentType.JSON);
        try {
            client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            LogUtils.formatError("saveReleaseIndex error, releaseIndexName:%s, e:%s", releaseIndexName, e.toString());
        }
    }

    /**
     * 激活最新索引
     *
     * @param client      RestHighLevelClient
     * @param entityClass 实体类
     * @param maxRetry    重试次数
     */
    public static void activeReleaseIndex(RestHighLevelClient client, Class<?> entityClass, int maxRetry) {
        // 请求并获取最新的一条索引
        SearchRequest searchRequest = new SearchRequest(LOCK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchField(ACTIVE_INDEX_KEY);
        searchSourceBuilder.sort(GMT_MODIFIED, SortOrder.DESC);
        searchSourceBuilder.size(ONE);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = null;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (Throwable e) {
            LogUtils.warn("Active failed, The machine that acquired lock is migrating, will try again later");
        }

        // 激活当前客户端的索引为最新索引
        AtomicBoolean activated = new AtomicBoolean(false);
        Optional.ofNullable(response)
                .ifPresent(r -> Arrays.stream(r.getHits().getHits())
                        .forEach(searchHit -> {
                            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                            Optional.ofNullable(sourceAsMap.get(ACTIVE_INDEX_KEY))
                                    .ifPresent(indexName -> {
                                        if (indexName instanceof String) {
                                            EntityInfoHelper.getEntityInfo(entityClass).setIndexName((String) indexName);
                                            activated.set(Boolean.TRUE);
                                        }
                                    });
                        }));

        // 达到最大重试次数仍未成功,则终止流程,避免浪费资源
        int activeReleaseIndexMaxRetry = GlobalConfigCache.getGlobalConfig().getActiveReleaseIndexMaxRetry();
        if (maxRetry >= activeReleaseIndexMaxRetry) {
            if (activated.get()) {
                LogUtils.info("Current client index has been successfully activated");
            } else {
                LogUtils.error("Active release index failed after max number of retry, Please check whether the indexing of the first got lock client is successful");
            }
            throw new RuntimeException();
        }

    }

    /**
     * 异步执行索引托管操作
     *
     * @param biFunction  索引变更方法
     * @param entityClass 实体类
     * @param client      RestHighLevelClient
     */
    public static void supplyAsync(BiFunction<Class<?>, RestHighLevelClient, Boolean> biFunction, Class<?> entityClass, RestHighLevelClient client) {
        CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> {
            GlobalConfig globalConfig = GlobalConfigCache.getGlobalConfig();
            if (!globalConfig.isDistributed()) {
                // 非分布式项目, 直接处理
                return biFunction.apply(entityClass, client);
            }
            try {
                // 尝试获取分布式锁
                boolean lock = LockUtils.tryLock(client, entityClass.getSimpleName().toLowerCase(), BaseEsConstants.LOCK_MAX_RETRY);
                if (!lock) {
                    // 未获取到分布式锁的机器,需要等待获取到锁的机器完成索引由旧到新的迁移,期间会不断重试并激活该新索引
                    AtomicInteger maxTry = new AtomicInteger(ZERO);
                    if (ProcessIndexStrategyEnum.SMOOTHLY.equals(globalConfig.getProcessIndexMode())) {
                        // 平滑模式下,需要将未获取到锁的客户端的索引延迟激活为最新索引
                        Executors.newSingleThreadScheduledExecutor()
                                .scheduleWithFixedDelay(() -> activeReleaseIndex(client, entityClass, maxTry.addAndGet(ONE)), INITIAL_DELAY, globalConfig.getActiveReleaseIndexFixedDelay(), TimeUnit.SECONDS);
                    }
                    LogUtils.warn("retry get distribute lock failed, please check whether other resources have been preempted or deadlocked");
                    return Boolean.FALSE;
                }
                return biFunction.apply(entityClass, client);
            } finally {
                LockUtils.release(client, entityClass.getSimpleName().toLowerCase(), BaseEsConstants.LOCK_MAX_RETRY);
            }
        }).exceptionally((throwable) -> {
            Optional.ofNullable(throwable).ifPresent(e -> {
                e.printStackTrace();
                LogUtils.error("process index exception:", e.toString());
            });
            return Boolean.FALSE;
        }).whenCompleteAsync((success, throwable) -> {
            if (success) {
                LogUtils.info("===> Congratulations auto process index by Easy-Es is done !");
            } else {
                LogUtils.warn("===> Unfortunately, auto process index by Easy-Es failed, please check your configuration");
                // 未成功完成迁移,需清除新创建的索引,避免新旧索引同时存在
                Optional.ofNullable(EntityInfoHelper.getEntityInfo(entityClass).getReleaseIndexName())
                        .ifPresent(releaseIndexName -> deleteIndex(client, releaseIndexName));
            }
        });

        // 是否开启阻塞 默认开启 运行测试模块时建议开启阻塞,否则测试用例跑完后,主线程退出,但异步线程可能还没跑完,可能出现死锁
        // 生产环境或迁移数据量比较大的情况下,可以配置开启非阻塞,这样服务启动更快 数据迁移异步进行
        GlobalConfig globalConfig = GlobalConfigCache.getGlobalConfig();
        if (globalConfig.isAsyncProcessIndexBlocking()) {
            completableFuture.join();
        }
    }

}
