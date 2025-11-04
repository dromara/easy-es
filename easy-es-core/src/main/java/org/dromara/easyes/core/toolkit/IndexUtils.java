package org.dromara.easyes.core.toolkit;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.AcknowledgedResponse;
import co.elastic.clients.elasticsearch._types.BulkIndexByScrollFailure;
import co.elastic.clients.elasticsearch._types.ExpandWildcard;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.indices.update_aliases.Action;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.easyes.annotation.rely.Analyzer;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.common.enums.JdkDataTypeEnum;
import org.dromara.easyes.common.enums.ProcessIndexStrategyEnum;
import org.dromara.easyes.common.property.GlobalConfig;
import org.dromara.easyes.common.utils.*;
import org.dromara.easyes.core.biz.*;
import org.dromara.easyes.core.cache.GlobalConfigCache;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static org.dromara.easyes.annotation.rely.AnnotationConstants.DEFAULT_ALIAS;
import static org.dromara.easyes.annotation.rely.AnnotationConstants.DEFAULT_MAX_RESULT_WINDOW;
import static org.dromara.easyes.common.constants.BaseEsConstants.*;

/**
 * 索引工具类
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexUtils {
    /**
     * 多字段索引名
     */
    private static final String FIELDS_KEY;
    /**
     * ignore_above默认值
     */
    private static final int DEFAULT_IGNORE_ABOVE;
    /**
     * ignore_above 字段的索引名
     */
    private static final String IGNORE_ABOVE_KEY;
    /**
     * index.analysis.normalizer.lowercase_normalizer.type值
     */
    private static final String CUSTOM;
    /**
     * 忽略index.analysis.normalizer.lowercase_normalizer.filter
     */
    private static final String LOWERCASE;
    /**
     * dims索引字段名
     */
    private static final String DIMS_KEY;
    /**
     * 复制索引字段名
     */
    private static final String COPY_TO_KEY;

    /**
     * 急切全局系数,join父子类型默认字段
     */
    private static final String EAGER_GLOBAL_ORDINALS;

    static {
        FIELDS_KEY = "fields";
        DEFAULT_IGNORE_ABOVE = 256;
        IGNORE_ABOVE_KEY = "ignore_above";
        CUSTOM = "custom";
        LOWERCASE = "lowercase";
        DIMS_KEY = "dims";
        EAGER_GLOBAL_ORDINALS = "eager_global_ordinals";
        COPY_TO_KEY = "copy_to";
    }

    /**
     * 是否存在索引
     *
     * @param client    ElasticsearchClient
     * @param indexName 索引名
     * @return 是否存在
     */
    public static boolean existsIndex(ElasticsearchClient client, String indexName) {
        ExistsRequest request = ExistsRequest.of(x -> x.index(indexName));
        try {
            return client.indices().exists(request).value();
        } catch (IOException e) {
            throw ExceptionUtils.eee("existsIndex exception indexName: %s", e, indexName);
        }
    }

    /**
     * 创建索引
     *
     * @param client     ElasticsearchClient
     * @param entityInfo 实体信息
     * @param indexParam 创建索引参数
     * @return 是否创建成功
     */
    public static boolean createIndex(ElasticsearchClient client, EntityInfo entityInfo, CreateIndexParam indexParam) {
        CreateIndexRequest createIndexRequest = CreateIndexRequest.of(x -> {
            // ======================== 索引信息 =========================
            x.index(indexParam.getIndexName());

            // ======================== settings ========================

            // 用户未指定的settings信息
            if (Objects.isNull(indexParam.getSettings())) {
                IndexSettings.Builder settings = indexParam.getIndexSettings();
                // 只要有其中一个字段加了忽略大小写,则在索引中创建此自定义配置,否则无需创建,不浪费资源
                boolean ignoreCase = indexParam.getEsIndexParamList() != null && indexParam.getEsIndexParamList().stream()
                        .anyMatch(EsIndexParam::isIgnoreCase);
                if (ignoreCase) {
                    // 忽略大小写配置
                    settings.analysis(b -> b.normalizer(LOWERCASE_NORMALIZER, c -> c.custom(d -> d.filter(LOWERCASE))));
                }
                x.settings(settings.build());
            } else {
                // 用户自定义settings
                x.settings(indexParam.getSettings().build());
            }

            // ======================== mapping ========================

            if (Objects.isNull(indexParam.getMapping())) {
                // 用户未指定mapping 根据注解自动推断
                TypeMapping.Builder mapping = initMapping(entityInfo, indexParam.getEsIndexParamList());
                x.mappings(mapping.build());
            } else {
                // 用户自定义的mapping优先级NO.1
                x.mappings(indexParam.getMapping().build());
            }
            // 别名信息
            Optional.ofNullable(indexParam.getAliasName()).ifPresent(aliasName -> x.aliases(aliasName, y -> y));
            return x;
        });

        // 创建索引
        try {
            PrintUtils.printDsl(createIndexRequest, client);
            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest);
            return createIndexResponse.acknowledged();
        } catch (IOException e) {
            throw ExceptionUtils.eee("create index exception createIndexRequest: %s ", e, createIndexRequest.toString());
        }
    }

    /**
     * 创建空索引,不含字段,仅框架内部使用
     *
     * @param client    ElasticsearchClient
     * @param indexName 索引名
     * @return 是否创建成功
     */
    public static boolean createEmptyIndex(ElasticsearchClient client, String indexName) {
        CreateIndexRequest request = CreateIndexRequest.of(x -> x.index(indexName));
        CreateIndexResponse createIndexResponse;
        try {
            PrintUtils.printDsl(request, client);
            createIndexResponse = client.indices().create(request);
        } catch (IOException e) {
            LogUtils.info("===> distribute lock index has created");
            return Boolean.TRUE;
        }
        return createIndexResponse.acknowledged();
    }

    /**
     * 获取索引信息
     *
     * @param client    ElasticsearchClient
     * @param indexName 索引名
     * @return 索引信息
     */
    public static EsIndexInfo getIndexInfo(ElasticsearchClient client, String indexName) {
        GetIndexResponse getIndexResponse = getIndex(client, indexName);
        return parseGetIndexResponse(getIndexResponse, indexName);
    }

    /**
     * 获取索引信息
     *
     * @param client    ElasticsearchClient
     * @param indexName 索引名
     * @return 索引信息
     */
    public static GetIndexResponse getIndex(ElasticsearchClient client, String indexName) {
        GetIndexRequest request = GetIndexRequest.of(x -> x.index(indexName));
        try {
            PrintUtils.printDsl(request, client);
            return client.indices().get(request);
        } catch (IOException e) {
            throw ExceptionUtils.eee("getIndex exception indexName: %s", e, indexName);
        }
    }

    /**
     * 添加默认索引别名
     *
     * @param client    ElasticsearchClient
     * @param indexName 索引名
     */
    public static void addDefaultAlias(ElasticsearchClient client, String indexName) {
        addAliases(client, indexName, DEFAULT_ALIAS);
    }

    /**
     * 添加别名
     *
     * @param client    ElasticsearchClient
     * @param indexName 索引名
     * @param aliases   别名数组，可以是单个
     * @return 是否添加成功
     */
    public static Boolean addAliases(ElasticsearchClient client, String indexName, String... aliases) {
        Action action = Action.of(x -> x.add(y -> y.aliases(Arrays.asList(aliases)).index(indexName)));
        UpdateAliasesRequest request = UpdateAliasesRequest.of(x -> x.actions(action));
        try {
            PrintUtils.printDsl(request, client);
            UpdateAliasesResponse response = client.indices().updateAliases(request);
            return response.acknowledged();
        } catch (IOException e) {
            LogUtils.warn("addDefaultAlias exception", e.toString());
            return Boolean.FALSE;
        }
    }

    /**
     * 重建索引时的数据迁移,从旧索引迁移至新索引
     *
     * @param client           ElasticsearchClient
     * @param oldIndexName     旧索引名
     * @param releaseIndexName 新索引名
     * @param maxResultWindow  最大返回数
     * @return 是否操作成功
     */
    public static boolean reindex(ElasticsearchClient client, String oldIndexName, String releaseIndexName, Integer maxResultWindow) {
        int reindexTimeOutHours = GlobalConfigCache.getGlobalConfig().getReindexTimeOutHours();
        ReindexRequest request = ReindexRequest.of(a -> {
            a
                    .source(b -> b.index(oldIndexName))
                    .dest(c -> c.index(releaseIndexName).opType(BaseEsConstants.DEFAULT_DEST_OP_TYPE))
                    .conflicts(BaseEsConstants.DEFAULT_CONFLICTS)
                    .refresh(Boolean.TRUE)
                    .timeout(x -> x.time(reindexTimeOutHours + "h"));
            // batchSize须小于等于maxResultWindow,否则es报错
            if (DEFAULT_MAX_RESULT_WINDOW > maxResultWindow) {
                a.size(maxResultWindow.longValue());
            }
            return a;
        });
        try {
            PrintUtils.printDsl(request, client);
            ReindexResponse response = client.reindex(request);
            List<BulkIndexByScrollFailure> failures = response.failures();
            return CollectionUtils.isEmpty(failures);
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

        IndexState indexState = getIndexResponse.result().get(indexName);
        Map<String, Alias> aliases = indexState.aliases();
        IndexSettings settings = indexState.settings();
        TypeMapping mappings = indexState.mappings();

        // 设置是否已存在默认别名
        esIndexInfo.setHasDefaultAlias(aliases != null && aliases.containsKey(DEFAULT_ALIAS));

        // 设置分片、副本、最大返回数等
        Optional.ofNullable(settings)
                .ifPresent(p -> {
                    Optional.ofNullable(p.index())
                            .flatMap(i -> Optional.ofNullable(i.numberOfShards()))
                            .ifPresent(s -> esIndexInfo.setShardsNum(Integer.parseInt(s)));
                    Optional.ofNullable(p.index())
                            .flatMap(i -> Optional.ofNullable(i.numberOfReplicas()))
                            .ifPresent(r -> esIndexInfo.setReplicasNum(Integer.parseInt(r)));
                    Optional.ofNullable(p.index())
                            .flatMap(i -> Optional.ofNullable(i.maxResultWindow()))
                            .ifPresent(esIndexInfo::setMaxResultWindow);
                });

        // 设置mapping信息
        if (mappings != null) {
            TypeMapping.Builder builder = new TypeMapping.Builder()
                    .allField(mappings.allField())
                    .dateDetection(mappings.dateDetection())
                    .dynamic(mappings.dynamic())
                    .dynamicDateFormats(mappings.dynamicDateFormats())
                    .dynamicTemplates(mappings.dynamicTemplates())
                    .fieldNames(mappings.fieldNames())
                    .indexField(mappings.indexField())
                    .meta(mappings.meta())
                    .numericDetection(mappings.numericDetection())
                    .properties(mappings.properties())
                    .routing(mappings.routing())
                    .size(mappings.size())
                    .source(mappings.source())
                    .runtime(mappings.runtime())
                    .enabled(mappings.enabled());
            esIndexInfo.setBuilder(builder);
        }

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

        // 否则根据类型推断,String以及找不到的类型一律被当做keyword_text复核类型处理
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
                type = FieldType.SCALED_FLOAT.getType();
                break;
            case STRING:
            case CHAR:
                type = FieldType.KEYWORD_TEXT.getType();
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
                return FieldType.OBJECT.getType();
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
    public static TypeMapping.Builder initMapping(EntityInfo entityInfo, List<EsIndexParam> indexParamList) {
        TypeMapping.Builder mapping = new TypeMapping.Builder();
        if (CollectionUtils.isEmpty(indexParamList)) {
            return mapping;
        }

        GlobalConfig.DbConfig dbConfig = Optional.ofNullable(GlobalConfigCache.getGlobalConfig())
                .map(GlobalConfig::getDbConfig)
                .orElse(new GlobalConfig.DbConfig());

        Map<String, Property> propertyMap = new HashMap<>();
        initInfo(entityInfo, dbConfig, propertyMap, indexParamList);
        mapping.properties(propertyMap);

        // 父子类型
        if (CollectionUtils.isNotEmpty(entityInfo.getRelationMap())) {
            mapping.properties(entityInfo.getJoinFieldName(), x -> x.join(y -> y
                            .relations(entityInfo.getRelationMap())
//                    .eagerGlobalOrdinals(entityInfo.isEagerGlobalOrdinals())
            ));
        }

        return mapping;
    }

    /**
     * 初始化索引info信息
     *
     * @param entityInfo     实体信息
     * @param dbConfig       配置
     * @param properties     字段属性
     * @param indexParamList 索引参数列表
     * @return info信息
     */
    private static Map<String, Property> initInfo(EntityInfo entityInfo, GlobalConfig.DbConfig dbConfig,
                                                  Map<String, Property> properties, List<EsIndexParam> indexParamList) {
        // 主键
        if (entityInfo.isId2Source()) {
            String idFieldName = entityInfo.getKeyProperty();
            if (dbConfig.isMapUnderscoreToCamelCase()) {
                idFieldName = StringUtils.camelToUnderline(idFieldName);
            }
            properties.put(idFieldName, KeywordProperty.of(a -> a)._toProperty());
        }

        // 其他字段
        indexParamList.forEach(indexParam -> {
            // 驼峰处理
            String fieldName = indexParam.getFieldName();
            if (dbConfig.isMapUnderscoreToCamelCase()) {
                fieldName = StringUtils.camelToUnderline(fieldName);
            }

            if (FieldType.BYTE.getType().equals(indexParam.getFieldType())) {
                ByteNumberProperty property = ByteNumberProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.SHORT.getType().equals(indexParam.getFieldType())) {
                ShortNumberProperty property = ShortNumberProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.INTEGER.getType().equals(indexParam.getFieldType())) {
                IntegerNumberProperty property = IntegerNumberProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.LONG.getType().equals(indexParam.getFieldType())) {
                LongNumberProperty property = LongNumberProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.FLOAT.getType().equals(indexParam.getFieldType())) {
                FloatNumberProperty property = FloatNumberProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.DOUBLE.getType().equals(indexParam.getFieldType())) {
                DoubleNumberProperty property = DoubleNumberProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.HALF_FLOAT.getType().equals(indexParam.getFieldType())) {
                HalfFloatNumberProperty property = HalfFloatNumberProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.SCALED_FLOAT.getType().equals(indexParam.getFieldType())) {
                Double scalingFactor = Optional.ofNullable(indexParam.getScalingFactor())
                        .map(NumericUtils::formatNumberWithOneDecimal).orElse(DEFAULT_SCALING_FACTOR);
                ScaledFloatNumberProperty property = ScaledFloatNumberProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    a.scalingFactor(scalingFactor);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.BOOLEAN.getType().equals(indexParam.getFieldType())) {
                BooleanProperty property = BooleanProperty.of(a -> {
                    a.boost(indexParam.getBoost());
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.DATE.getType().equals(indexParam.getFieldType())) {
                DateProperty property = DateProperty.of(a -> {
                    a.boost(indexParam.getBoost());
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    a.format(indexParam.getDateFormat());
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.BINARY.getType().equals(indexParam.getFieldType())) {
                BinaryProperty property = BinaryProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.KEYWORD.getType().equals(indexParam.getFieldType())) {
                KeywordProperty property = KeywordProperty.of(a -> {
                    a.boost(indexParam.getBoost());
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    a.normalizer(indexParam.isIgnoreCase() ? LOWERCASE_NORMALIZER : null);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.TEXT.getType().equals(indexParam.getFieldType())) {
                int ignoreAbove = Optional.ofNullable(indexParam.getIgnoreAbove()).orElse(DEFAULT_IGNORE_ABOVE);
                TextProperty property = TextProperty.of(a -> {
                    a.boost(indexParam.getBoost());
                    a.boost(indexParam.getBoost());
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    Optional.ofNullable(indexParam.getAnalyzer()).map(String::toLowerCase).ifPresent(a::analyzer);
                    Optional.ofNullable(indexParam.getSearchAnalyzer()).map(String::toLowerCase).ifPresent(a::searchAnalyzer);
                    MyOptional.ofNullable(indexParam.getFieldData()).ifTrue(a::fielddata);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.KEYWORD_TEXT.getType().equals(indexParam.getFieldType())) {
                int ignoreAbove = Optional.ofNullable(indexParam.getIgnoreAbove()).orElse(DEFAULT_IGNORE_ABOVE);
                TextProperty property = TextProperty.of(a -> {
                    a.boost(indexParam.getBoost());
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    a.fields(FieldType.KEYWORD.getType(), c -> c
                            .keyword(d -> {
                                d.ignoreAbove(ignoreAbove);
                                if (indexParam.isIgnoreCase()) {
                                    d.normalizer(LOWERCASE_NORMALIZER);
                                }
                                return d;
                            })
                    );
                    Optional.ofNullable(indexParam.getAnalyzer()).map(String::toLowerCase).ifPresent(a::analyzer);
                    Optional.ofNullable(indexParam.getSearchAnalyzer()).map(String::toLowerCase).ifPresent(a::searchAnalyzer);
                    MyOptional.ofNullable(indexParam.getFieldData()).ifTrue(a::fielddata);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.WILDCARD.getType().equals(indexParam.getFieldType())) {
                int ignoreAbove = Optional.ofNullable(indexParam.getIgnoreAbove()).orElse(DEFAULT_IGNORE_ABOVE);
                WildcardProperty property = WildcardProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.NESTED.getType().equals(indexParam.getFieldType())) {
                // 递归
                List<EntityFieldInfo> nestedFields = entityInfo.getNestedOrObjectFieldListMap().get(indexParam.getNestedClass());
                List<EsIndexParam> esIndexParams = initIndexParam(entityInfo, indexParam.getNestedClass(), nestedFields);
                Map<String, Property> nested = initInfo(entityInfo, dbConfig, new HashMap<>(), esIndexParams);
                NestedProperty property = NestedProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    a.properties(nested);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.OBJECT.getType().equals(indexParam.getFieldType())) {
                // 递归
                List<EntityFieldInfo> nestedFields = entityInfo.getNestedOrObjectFieldListMap().get(indexParam.getNestedClass());
                List<EsIndexParam> esIndexParams = initIndexParam(entityInfo, indexParam.getNestedClass(), nestedFields);
                Map<String, Property> nested = initInfo(entityInfo, dbConfig, new HashMap<>(), esIndexParams);
                ObjectProperty property = ObjectProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    a.properties(nested);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.JOIN.getType().equals(indexParam.getFieldType())) {
                return;
            }
            if (FieldType.GEO_POINT.getType().equals(indexParam.getFieldType())) {
                GeoPointProperty property = GeoPointProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.GEO_SHAPE.getType().equals(indexParam.getFieldType())) {
                GeoShapeProperty property = GeoShapeProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.IP.getType().equals(indexParam.getFieldType())) {
                IpProperty property = IpProperty.of(a -> {
                    a.boost(indexParam.getBoost());
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.COMPLETION.getType().equals(indexParam.getFieldType())) {
                CompletionProperty property = CompletionProperty.of(a -> {
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    Optional.ofNullable(indexParam.getAnalyzer()).map(String::toLowerCase).ifPresent(a::analyzer);
                    Optional.ofNullable(indexParam.getSearchAnalyzer()).map(String::toLowerCase).ifPresent(a::searchAnalyzer);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.TOKEN.getType().equals(indexParam.getFieldType())) {
                TokenCountProperty property = TokenCountProperty.of(a -> {
                    a.boost(indexParam.getBoost());
                    buildCopyTo(a, entityInfo.isIndexEqualStage(), indexParam.getCopyToList());
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.ATTACHMENT.getType().equals(indexParam.getFieldType())) {
                return;
            }
            if (FieldType.PERCOLATOR.getType().equals(indexParam.getFieldType())) {
                PercolatorProperty property = PercolatorProperty.of(a -> {
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
                return;
            }
            if (FieldType.DENSE_VECTOR.getType().equals(indexParam.getFieldType())) {
                DenseVectorProperty property = DenseVectorProperty.of(a -> {
                    Optional.ofNullable(indexParam.getDims()).ifPresent(a::dims);
                    buildInnerFields(a, indexParam);
                    return a;
                });
                properties.put(fieldName, property._toProperty());
            }
        });
        return properties;
    }

    /**
     * copyTo字段处理
     *
     * @param builder    构建builder
     * @param indexParam 索引字段信息
     */
    private static void buildInnerFields(
            PropertyBase.AbstractBuilder<?> builder,
            EsIndexParam indexParam
    ) {
        // 设置内部字段
        if (CollectionUtils.isEmpty(indexParam.getInnerFieldParamList())) {
            return;
        }

        Map<String, Property> fieldsMap = new HashMap<>();
        if (FieldType.KEYWORD_TEXT.getType().equals(indexParam.getFieldType())) {
            fieldsMap.put(FieldType.KEYWORD.getType(), Property.of(c -> c.keyword(d -> {
                if (indexParam.isIgnoreCase()) {
                    d.normalizer(LOWERCASE_NORMALIZER);
                }
                return d;
            })));
        }

        indexParam.getInnerFieldParamList().forEach(innerFieldParam -> {
            Integer innerIgnoreAbove = innerFieldParam.getIgnoreAbove();
            if (FieldType.BYTE.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), ByteNumberProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty());
                return;
            }
            if (FieldType.SHORT.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), ShortNumberProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty());
                return;
            }
            if (FieldType.INTEGER.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), IntegerNumberProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty());
                return;
            }
            if (FieldType.LONG.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), LongNumberProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty());
                return;
            }
            if (FieldType.FLOAT.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), FloatNumberProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty());
            }
            if (FieldType.DOUBLE.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), DoubleNumberProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty());
            }
            if (FieldType.HALF_FLOAT.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), HalfFloatNumberProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty());
                return;
            }
            if (FieldType.SCALED_FLOAT.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), ScaledFloatNumberProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty());
                return;
            }
            if (FieldType.BOOLEAN.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), BooleanProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty());
                return;
            }
            if (FieldType.DATE.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), DateProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty());
                return;
            }
            if (FieldType.BINARY.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), BinaryProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty());
                return;
            }
            if (FieldType.KEYWORD.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), KeywordProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove)
                                .normalizer(indexParam.isIgnoreCase() ? LOWERCASE_NORMALIZER : null))
                        ._toProperty());
                return;
            }
            if (FieldType.TEXT.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), TextProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove)
                                .analyzer(innerFieldParam.getAnalyzer())
                                .searchAnalyzer(innerFieldParam.getSearchAnalyzer()))
                        ._toProperty());
                return;
            }
            if (FieldType.KEYWORD_TEXT.getType().equals(innerFieldParam.getFieldType())) {
                throw ExceptionUtils.eee("The fieldType FieldType.KEYWORD_TEXT just for mainIndexField, can not be used in @InnerIndexField");
            }
            if (FieldType.WILDCARD.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), WildcardProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty()
                );
                return;
            }
            if (FieldType.NESTED.getType().equals(innerFieldParam.getFieldType())) {
                throw ExceptionUtils.eee("The fieldType FieldType.NESTED just for mainIndexField, can not be used in @InnerIndexField");
            }
            if (FieldType.OBJECT.getType().equals(innerFieldParam.getFieldType())) {
                throw ExceptionUtils.eee("The fieldType FieldType.OBJECT just for mainIndexField, can not be used in @InnerIndexField");
            }
            if (FieldType.JOIN.getType().equals(innerFieldParam.getFieldType())) {
                throw ExceptionUtils.eee("The fieldType FieldType.OBJECT just for mainIndexField, can not be used in @InnerIndexField");
            }
            if (FieldType.GEO_POINT.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), GeoPointProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty()
                );
                return;
            }
            if (FieldType.GEO_SHAPE.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), GeoShapeProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty()
                );
                return;
            }
            if (FieldType.IP.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), IpProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty()
                );
                return;
            }
            if (FieldType.COMPLETION.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), CompletionProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove)
                                .analyzer(innerFieldParam.getAnalyzer())
                                .searchAnalyzer(innerFieldParam.getSearchAnalyzer()))
                        ._toProperty()
                );
                return;
            }
            if (FieldType.TOKEN.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), TokenCountProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty()
                );
                return;
            }
            if (FieldType.ATTACHMENT.getType().equals(innerFieldParam.getFieldType())) {
                return;
            }
            if (FieldType.PERCOLATOR.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), PercolatorProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty()
                );
                return;
            }
            if (FieldType.DENSE_VECTOR.getType().equals(innerFieldParam.getFieldType())) {
                fieldsMap.put(innerFieldParam.getColumn(), DenseVectorProperty
                        .of(a -> a.ignoreAbove(innerIgnoreAbove))
                        ._toProperty()
                );
            }
        });
        builder.fields(fieldsMap);
    }

    /**
     * copyTo字段处理
     *
     * @param builder         构建builder
     * @param indexEqualStage 是否判定索引相同阶段
     * @param copyToList      copyTo字段
     */
    private static void buildCopyTo(
            CorePropertyBase.AbstractBuilder<?> builder,
            boolean indexEqualStage,
            List<String> copyToList
    ) {
        if (CollectionUtils.isNotEmpty(copyToList)) {
            if (indexEqualStage) {
                // 判定索引是否发生变动时,用数组,因为es返回的是数组
                builder.copyTo(copyToList);
            } else {
                // 创建时,用逗号隔开,直接用数组会创建索引失败
                builder.copyTo(String.join(COMMA, copyToList));
            }
        }
    }

    /**
     * 原子操作: 删除旧索引别名,将旧索的引别名添加至新索引
     *
     * @param client           ElasticsearchClient
     * @param oldIndexName     旧索引
     * @param releaseIndexName 新索引
     * @return 是否成功
     */
    public static boolean changeAliasAtomic(ElasticsearchClient client, String oldIndexName, String releaseIndexName) {
        UpdateAliasesRequest request = UpdateAliasesRequest.of(a -> a
                .actions(b -> b.add(c -> c.index(releaseIndexName).alias(DEFAULT_ALIAS)))
                .actions(b -> b.removeIndex(c -> c.index(oldIndexName)))
        );
        try {
            AcknowledgedResponse response = client.indices().updateAliases(request);
            return response.acknowledged();
        } catch (IOException e) {
            throw ExceptionUtils.eee("changeAlias exception oldIndexName: %s, releaseIndexName: %s", e, oldIndexName, releaseIndexName);
        }
    }

    /**
     * 删除索引
     *
     * @param client    ElasticsearchClient
     * @param indexName 索引名
     * @return 是否删除成功
     */
    public static boolean deleteIndex(ElasticsearchClient client, String indexName) {
        DeleteIndexRequest deleteIndexRequest = DeleteIndexRequest.of(a -> a.index(indexName)
                .allowNoIndices(true).ignoreUnavailable(true).expandWildcards(ExpandWildcard.None));
        try {
            AcknowledgedResponse acknowledgedResponse = client.indices().delete(deleteIndexRequest);
            return acknowledgedResponse.acknowledged();
        } catch (IOException e) {
            throw ExceptionUtils.eee("deleteIndex exception indexName: %s", e, indexName);
        }
    }

    /**
     * 根据配置生成创建索引参数
     *
     * @param entityInfo 配置信息
     * @param clazz      实体类
     * @return 创建索引参数
     */
    public static CreateIndexParam getCreateIndexParam(EntityInfo entityInfo, Class<?> clazz) {
        // 初始化字段信息参数
        List<EsIndexParam> esIndexParamList = initIndexParam(entityInfo, clazz, entityInfo.getFieldList());

        // 追加join父子类型字段信息
        if (CollectionUtils.isNotEmpty(entityInfo.getChildFieldList())) {
            List<EsIndexParam> childEsIndexParamList = initIndexParam(entityInfo, clazz, entityInfo.getChildFieldList());
            esIndexParamList.addAll(childEsIndexParamList);
        }

        // 设置创建参数
        CreateIndexParam createIndexParam = new CreateIndexParam();
        createIndexParam.setEsIndexParamList(esIndexParamList);
        createIndexParam.setAliasName(entityInfo.getAliasName());
        createIndexParam.setIndexName(entityInfo.getIndexName());

        // 如果有设置新索引名称,则用新索引名覆盖原索引名进行创建
        Optional.ofNullable(entityInfo.getReleaseIndexName()).ifPresent(createIndexParam::setIndexName);

        // settingsMap
        Optional.ofNullable(entityInfo.getIndexSettings()).ifPresent(createIndexParam::setIndexSettings);

        return createIndexParam;
    }

    /**
     * 初始化索引参数
     *
     * @param entityInfo 实体信息
     * @param fieldList  字段列表
     * @param clazz      实体类
     * @return 索引参数列表
     */
    public static List<EsIndexParam> initIndexParam(EntityInfo entityInfo, Class<?> clazz, List<EntityFieldInfo> fieldList) {
        List<EntityFieldInfo> copyFieldList = new ArrayList<>(fieldList);

        List<EsIndexParam> esIndexParamList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(copyFieldList)) {
            Map<String, String> dateFormatMap = entityInfo.getClassDateFormatMap().get(clazz);
            copyFieldList.forEach(field -> {
                EsIndexParam esIndexParam = new EsIndexParam();
                // base
                String esFieldType = IndexUtils.getEsFieldType(field.getFieldType(), field.getColumnType());
                esIndexParam.setFieldType(esFieldType);
                if (field.isFieldData()) {
                    esIndexParam.setFieldData(true);
                }
                esIndexParam.setFieldName(field.getMappingColumn());
                esIndexParam.setScalingFactor(field.getScalingFactor());
                esIndexParam.setDims(field.getDims());
                esIndexParam.setCopyToList(field.getCopyToList());

                // 嵌套类型
                if (FieldType.NESTED.equals(field.getFieldType()) || FieldType.OBJECT.equals(field.getFieldType())) {
                    esIndexParam.setNestedClass(entityInfo.getPathClassMap().get(field.getColumn()));
                }

                // 分词器
                if (!Analyzer.NONE.equals(field.getAnalyzer())) {
                    esIndexParam.setAnalyzer(field.getAnalyzer());
                }
                if (!Analyzer.NONE.equals(field.getSearchAnalyzer())) {
                    if (!Objects.equals(field.getAnalyzer(), field.getSearchAnalyzer())) {
                        esIndexParam.setSearchAnalyzer(field.getSearchAnalyzer());
                    }
                }
                esIndexParam.setIgnoreCase(field.isIgnoreCase());

                // 日期处理
                Optional.ofNullable(dateFormatMap)
                        .flatMap(i -> Optional.ofNullable(i.get(field.getColumn())))
                        .ifPresent(esIndexParam::setDateFormat);

                // 内部字段处理
                final List<EntityFieldInfo.InnerFieldInfo> innerFieldInfoList = field.getInnerFieldInfoList();
                if (CollectionUtils.isNotEmpty(innerFieldInfoList)) {
                    List<EsIndexParam.InnerFieldParam> innerFieldParamList = new ArrayList<>();
                    innerFieldInfoList.forEach(innerFieldInfo -> {
                        EsIndexParam.InnerFieldParam innerFieldParam = new EsIndexParam.InnerFieldParam();
                        innerFieldParam.setColumn(innerFieldInfo.getColumn());
                        if (!Analyzer.NONE.equals(innerFieldInfo.getAnalyzer())) {
                            innerFieldParam.setAnalyzer(innerFieldInfo.getAnalyzer());
                        }
                        if (!Analyzer.NONE.equals(innerFieldInfo.getSearchAnalyzer())) {
                            innerFieldParam.setSearchAnalyzer(innerFieldInfo.getSearchAnalyzer());
                        }
                        innerFieldParam.setFieldType(innerFieldInfo.getFieldType().getType());
                        innerFieldParam.setIgnoreAbove(innerFieldInfo.getIgnoreAbove());
                        innerFieldParamList.add(innerFieldParam);
                    });
                    esIndexParam.setInnerFieldParamList(innerFieldParamList);
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
     * @param clazz       实体类
     * @return 是否需要更新索引
     */
    public static boolean isIndexNeedChange(EsIndexInfo esIndexInfo, EntityInfo entityInfo, Class<?> clazz) {
        // 根据当前实体类及自定义注解配置, 生成最新的Mapping信息
        List<EsIndexParam> esIndexParamList = IndexUtils.initIndexParam(entityInfo, clazz, entityInfo.getFieldList());

        // 追加join父子类型字段信息
        if (CollectionUtils.isNotEmpty(entityInfo.getChildFieldList())) {
            List<EsIndexParam> childEsIndexParamList = initIndexParam(entityInfo, clazz, entityInfo.getChildFieldList());
            esIndexParamList.addAll(childEsIndexParamList);
        }

        // 根据实体类注解信息构建mapping
        entityInfo.setIndexEqualStage(true);
        TypeMapping.Builder builderFromEntity = IndexUtils.initMapping(entityInfo, esIndexParamList);

        // 与查询到的已知index对比是否发生改变
        TypeMapping.Builder builderFromIndex = esIndexInfo.getBuilder();
        Map<String, Property> propertiesOfEntity = builderFromEntity.build().properties();
        Map<String, Property> propertiesOfIndex = builderFromIndex.build().properties();
        return !PropertyComparator.isPropertyMapEqual(propertiesOfEntity,propertiesOfIndex);
    }

    /**
     * 追加后缀重试是否存在索引,若存在,则更新当前被激活的索引名
     *
     * @param entityInfo 配置信息
     * @param client     ElasticsearchClient
     * @return 是否存在索引
     */
    public static boolean existsIndexWithRetryAndSetActiveIndex(EntityInfo entityInfo, ElasticsearchClient client) {
        boolean exists = existsIndexWithRetry(entityInfo, client);

        // 重置当前激活索引
        Optional.ofNullable(entityInfo.getRetrySuccessIndexName()).ifPresent(entityInfo::setIndexName);
        return exists;
    }

    /**
     * 追加后缀重试是否存在索引
     *
     * @param entityInfo 配置信息
     * @param client     ElasticsearchClient
     * @return 是否存在索引
     */
    public static boolean existsIndexWithRetry(EntityInfo entityInfo, ElasticsearchClient client) {
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
     * @param client           ElasticsearchClient
     */
    public static void saveReleaseIndex(String releaseIndexName, ElasticsearchClient client) {
        Map<String, Object> map = new HashMap<>();
        map.put(ACTIVE_INDEX_KEY, releaseIndexName);
        map.put(GMT_MODIFIED, System.currentTimeMillis());
        IndexRequest<?> indexRequest = IndexRequest.of(x -> x
                .index(LOCK_INDEX)
                .document(map)
        );
        try {
            client.index(indexRequest);
        } catch (IOException e) {
            LogUtils.formatError("saveReleaseIndex error, releaseIndexName:%s, e:%s", releaseIndexName, e.toString());
        }
    }

    /**
     * 激活最新索引
     *
     * @param client      ElasticsearchClient
     * @param entityClass 实体类
     * @param maxRetry    重试次数
     */
    public static void activeReleaseIndex(ElasticsearchClient client, Class<?> entityClass, int maxRetry) {
        // 请求并获取最新的一条索引
        SearchRequest searchRequest = SearchRequest.of(a -> a
                .index(LOCK_INDEX)
                .fields(b -> b.field(ACTIVE_INDEX_KEY))
                .sort(b -> b.field(c -> c.field(GMT_MODIFIED).order(SortOrder.Desc)))
                .size(ONE)
        );
        SearchResponse<Map<String, String>> response = null;
        try {
            response = client.search(searchRequest, new TypeReference<Map<String, Object>>() {
            }.getType());
        } catch (Throwable e) {
            LogUtils.warn("Active failed, The machine that acquired lock is migrating, will try again later");
        }

        // 激活当前客户端的索引为最新索引
        AtomicBoolean activated = new AtomicBoolean(false);
        Optional.ofNullable(response).ifPresent(r -> r.hits().hits().forEach(searchHit -> Optional.ofNullable(searchHit.source())
                .flatMap(hit -> Optional.ofNullable(hit.get(ACTIVE_INDEX_KEY)))
                .ifPresent(indexName -> {
                    EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
                    entityInfo.setIndexName(indexName);

                    // 父子类型,须将所有子孙文档的索引也激活为最新索引
                    entityInfo.getRelationClassMap().forEach((k, v) -> {
                        Optional.ofNullable(EntityInfoHelper.getEntityInfo(k)).ifPresent(i -> i.setIndexName(indexName));
                        if (CollectionUtils.isNotEmpty(v)) {
                            v.forEach(node -> Optional.ofNullable(EntityInfoHelper.getEntityInfo(node)).ifPresent(i -> i.setIndexName(indexName)));
                        }
                    });
                    activated.set(Boolean.TRUE);
                })));

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
     * @param client      ElasticsearchClient
     */
    public static void supplyAsync(BiFunction<Class<?>, ElasticsearchClient, Boolean> biFunction, Class<?> entityClass, ElasticsearchClient client) {
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
