package com.xpc.easyes.core.toolkit;

import com.xpc.easyes.core.cache.GlobalConfigCache;
import com.xpc.easyes.core.common.EntityFieldInfo;
import com.xpc.easyes.core.common.EntityInfo;
import com.xpc.easyes.core.config.GlobalConfig;
import com.xpc.easyes.core.constants.BaseEsConstants;
import com.xpc.easyes.core.enums.Analyzer;
import com.xpc.easyes.core.enums.FieldType;
import com.xpc.easyes.core.enums.JdkDataTypeEnum;
import com.xpc.easyes.core.params.CreateIndexParam;
import com.xpc.easyes.core.params.EsIndexInfo;
import com.xpc.easyes.core.params.EsIndexParam;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static com.xpc.easyes.core.constants.BaseEsConstants.*;

/**
 * 索引工具类
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexUtils {

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
            throw ExceptionUtils.eee("existsIndex exception", indexName, e);
        }
    }

    /**
     * 创建索引
     *
     * @param client     RestHighLevelClient
     * @param indexParam 创建索引参数
     * @return 是否创建成功
     */
    public static boolean createIndex(RestHighLevelClient client, CreateIndexParam indexParam) {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexParam.getIndexName());

        // 分片个副本信息
        Settings.Builder settings = Settings.builder();
        Optional.ofNullable(indexParam.getShardsNum()).ifPresent(shards -> settings.put(BaseEsConstants.SHARDS_FIELD, shards));
        Optional.ofNullable(indexParam.getReplicasNum()).ifPresent(replicas -> settings.put(BaseEsConstants.REPLICAS_FIELD, replicas));
        createIndexRequest.settings(settings);

        // mapping信息
        if (Objects.isNull(indexParam.getMapping())) {
            Map<String, Object> mapping = initMapping(indexParam.getEsIndexParamList());
            createIndexRequest.mapping(mapping);
        } else {
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
            throw ExceptionUtils.eee("create index exception ", createIndexRequest, e);
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
            System.out.println("already created");
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
    public static EsIndexInfo getIndex(RestHighLevelClient client, String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        GetIndexResponse getIndexResponse;
        try {
            getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("getIndex exception", indexName, e);
        }
        return parseGetIndexResponse(getIndexResponse, indexName);
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
        aliasActions.alias(DEFAULT_ALIAS);
        indicesAliasesRequest.addAliasAction(aliasActions);
        try {
            client.indices().updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
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
        reindexRequest.setDestOpType(DEFAULT_DEST_OP_TYPE);
        reindexRequest.setConflicts(DEFAULT_CONFLICTS);
        reindexRequest.setRefresh(Boolean.TRUE);
        try {
            BulkByScrollResponse response = client.reindex(reindexRequest, RequestOptions.DEFAULT);
            List<BulkItemResponse.Failure> bulkFailures = response.getBulkFailures();
            if (CollectionUtils.isEmpty(bulkFailures)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } catch (IOException e) {
            throw ExceptionUtils.eee("reindex exception", oldIndexName, releaseIndexName, e);
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
                            if (DEFAULT_ALIAS.equals(aliasMetadata.alias())) {
                                esIndexInfo.setHasDefaultAlias(Boolean.TRUE);
                            }
                        }));

        // 设置分片及副本数
        Optional.ofNullable(getIndexResponse.getSettings())
                .flatMap(settingsMap -> Optional.ofNullable(settingsMap.get(indexName)))
                .ifPresent(p -> {
                    String shardsNumStr = p.get(SHARDS_NUM_KEY);
                    Optional.ofNullable(shardsNumStr)
                            .ifPresent(s -> esIndexInfo.setShardsNum(Integer.parseInt(s)));
                    String replicasNumStr = p.get(REPLICAS_NUM_KEY);
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
     * @param indexParamList 索引参数列表
     * @return 索引mapping
     */
    public static Map<String, Object> initMapping(List<EsIndexParam> indexParamList) {
        Map<String, Object> mapping = new HashMap<>(1);
        if (CollectionUtils.isEmpty(indexParamList)) {
            return mapping;
        }
        Map<String, Object> properties = new HashMap<>(indexParamList.size());
        GlobalConfig.DbConfig dbConfig = Optional.ofNullable(GlobalConfigCache.getGlobalConfig())
                .map(GlobalConfig::getDbConfig)
                .orElse(new GlobalConfig.DbConfig());

        indexParamList.forEach(indexParam -> {
            Map<String, Object> info = new HashMap<>();
            info.put(BaseEsConstants.TYPE, indexParam.getFieldType());
            // 设置分词器
            if (FieldType.TEXT.getType().equals(indexParam.getFieldType())) {
                Optional.ofNullable(indexParam.getAnalyzer())
                        .ifPresent(analyzer ->
                                info.put(BaseEsConstants.ANALYZER, indexParam.getAnalyzer().toString().toLowerCase()));
                Optional.ofNullable(indexParam.getSearchAnalyzer())
                        .ifPresent(searchAnalyzer ->
                                info.put(BaseEsConstants.SEARCH_ANALYZER, indexParam.getSearchAnalyzer().toString().toLowerCase()));
            }

            // 驼峰处理
            String fieldName = indexParam.getFieldName();
            if (dbConfig.isMapUnderscoreToCamelCase()) {
                fieldName = StringUtils.camelToUnderline(fieldName);
            }
            properties.put(fieldName, info);
        });

        mapping.put(BaseEsConstants.PROPERTIES, properties);
        return mapping;
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
                IndicesAliasesRequest.AliasActions.Type.ADD).index(releaseIndexName).alias(DEFAULT_ALIAS);
        IndicesAliasesRequest.AliasActions removeAction = new IndicesAliasesRequest.AliasActions(
                IndicesAliasesRequest.AliasActions.Type.REMOVE).index(oldIndexName).alias(DEFAULT_ALIAS);

        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        indicesAliasesRequest.addAliasAction(addIndexAction);
        indicesAliasesRequest.addAliasAction(removeAction);
        try {
            AcknowledgedResponse acknowledgedResponse = client.indices().updateAliases(indicesAliasesRequest,
                    RequestOptions.DEFAULT);
            return acknowledgedResponse.isAcknowledged();
        } catch (IOException e) {
            throw ExceptionUtils.eee("changeAlias exception", oldIndexName, releaseIndexName, e);
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
            throw ExceptionUtils.eee("deleteIndex exception", indexName, e);
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
        List<EntityFieldInfo> fieldList = entityInfo.getFieldList();
        List<EsIndexParam> esIndexParamList = initIndexParam(fieldList);

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
     * @param fieldList 字段列表
     * @return 索引参数列表
     */
    public static List<EsIndexParam> initIndexParam(List<EntityFieldInfo> fieldList) {
        List<EsIndexParam> esIndexParamList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fieldList)) {
            fieldList.forEach(field -> {
                EsIndexParam esIndexParam = new EsIndexParam();
                String esFieldType = IndexUtils.getEsFieldType(field.getFieldType(), field.getColumnType());
                esIndexParam.setFieldType(esFieldType);
                esIndexParam.setFieldName(field.getMappingColumn());
                if (!Analyzer.NONE.equals(field.getAnalyzer())) {
                    esIndexParam.setAnalyzer(field.getAnalyzer());
                }
                if (!Analyzer.NONE.equals(field.getSearchAnalyzer())) {
                    if (!Objects.equals(field.getAnalyzer(), field.getSearchAnalyzer())) {
                        esIndexParam.setSearchAnalyzer(field.getSearchAnalyzer());
                    }
                }
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
        List<EsIndexParam> esIndexParamList = IndexUtils.initIndexParam(entityInfo.getFieldList());
        Map<String, Object> mapping = IndexUtils.initMapping(esIndexParamList);

        // 与查询到的已知index对比是否发生改变
        Map<String, Object> esIndexInfoMapping = Objects.isNull(esIndexInfo.getMapping())
                ? new HashMap<>(0) : esIndexInfo.getMapping();
        return !mapping.equals(esIndexInfoMapping);
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
            String retryIndexName = entityInfo.getIndexName() + S_SUFFIX + i;
            exists = IndexUtils.existsIndex(client, retryIndexName);
            if (exists) {
                entityInfo.setRetrySuccessIndexName(retryIndexName);
                break;
            }
        }
        return exists;
    }

    /**
     * 异步执行索引托管操作
     *
     * @param biFunction  索引变更方法
     * @param entityClass 实体类
     * @param client      RestHighLevelClient
     */
    public static void supplyAsync(BiFunction<Class<?>, RestHighLevelClient, Boolean> biFunction, Class<?> entityClass, RestHighLevelClient client) {
        CompletableFuture.supplyAsync(() -> {
            GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();
            if (!dbConfig.isDistributed()) {
                // 非分布式项目, 直接处理
                return biFunction.apply(entityClass, client);
            }
            try {
                // 尝试获取分布式锁
                boolean lock = LockUtils.tryLock(client, entityClass.getSimpleName().toLowerCase(), LOCK_MAX_RETRY);
                if (!lock) {
                    return Boolean.FALSE;
                }
                return biFunction.apply(entityClass, client);
            } finally {
                LockUtils.release(client, entityClass.getSimpleName().toLowerCase(), LOCK_MAX_RETRY);
            }
        }).whenCompleteAsync((status, throwable) -> {
            if (status) {
                LogUtils.info("===> Congratulations auto process index by Easy-Es is done !");
            } else {
                LogUtils.info("===> Unfortunately, auto process index by Easy-Es failed, please check your configuration");
            }
            Optional.ofNullable(throwable).ifPresent(Throwable::printStackTrace);
        });

    }

}
