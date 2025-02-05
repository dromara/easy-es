package org.dromara.easyes.core.toolkit;

import lombok.SneakyThrows;
import org.dromara.easyes.common.utils.CollectionUtils;
import org.dromara.easyes.common.utils.StringUtils;
import org.dromara.easyes.core.biz.EsIndexInfo;
import org.dromara.easyes.core.config.GeneratorConfig;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.*;

import static org.dromara.easyes.common.constants.BaseEsConstants.ZERO;

/**
 * generator 代码生成器
 *
 * @author hwy
 **/
public abstract class Generator {
    private final static String PROPERTIES = "properties";
    private final static String TYPE = "type";

    private final static String NESTED = "nested";

    private final static String NESTED_SUFFIX1 = "s";
    private final static String NESTED_SUFFIX2 = "List";

    private final static Set<String> SKIP_TYPE = new HashSet<>(Arrays.asList("join"));

    public abstract Boolean generate(GeneratorConfig config);

    /**
     * generate model entity 生成实体类
     *
     * @param config 配置
     */
    @SneakyThrows
    public void generateEntity(GeneratorConfig config, RestHighLevelClient client) {
        // get index info
        EsIndexInfo esIndexInfo = IndexUtils.getIndexInfo(client, config.getIndexName());
        Map<String, Object> mapping = esIndexInfo.getMapping();
        if (CollectionUtils.isEmpty(mapping)) {
            return;
        }

        // parse fields info
        LinkedHashMap<String, LinkedHashMap<String, Object>> properties = (LinkedHashMap<String, LinkedHashMap<String, Object>>) mapping.get(PROPERTIES);

        // execute generate
        executeGenerate(properties, config, esIndexInfo, config.getIndexName());
    }

    @SneakyThrows
    private void executeGenerate(LinkedHashMap<String, LinkedHashMap<String, Object>> properties, GeneratorConfig config, EsIndexInfo esIndexInfo, String className) {
        Map<String, String> modelMap = new HashMap<>(properties.size());
        properties.forEach((k, v) -> Optional.ofNullable(v.get(TYPE)).ifPresent(esType -> {
            if (SKIP_TYPE.contains(esType.toString())) {
                return;
            }
            if (NESTED.equals(esType.toString())) {
                // nested recursion
                executeGenerate((LinkedHashMap<String, LinkedHashMap<String, Object>>) v.get(PROPERTIES), config, esIndexInfo, parseClassName(k));
            }
            String javaType = getJavaType(esType.toString(), k);
            modelMap.put(k, javaType);
        }));

        // do generate entity
        EntityGenerator.generateEntity(config, modelMap, className, esIndexInfo.getShardsNum(), esIndexInfo.getReplicasNum());
    }

    private static String parseClassName(String origin) {
        if (StringUtils.isEmpty(origin)) {
            return origin;
        }
        if (origin.endsWith(NESTED_SUFFIX1)) {
            return FieldUtils.firstToUpperCase(origin.substring(ZERO, origin.lastIndexOf(NESTED_SUFFIX1)));
        } else if (origin.endsWith(NESTED_SUFFIX2)) {
            return FieldUtils.firstToUpperCase(origin.substring(ZERO, origin.lastIndexOf(NESTED_SUFFIX2)));
        }
        return origin;
    }

    private static String getJavaType(String esType, String key) {
        if (StringUtils.isNotBlank(esType)) {
            switch (esType) {
                case "long":
                    return "Long";
                case "integer":
                    return "Integer";
                case "short":
                    return "Short";
                case "byte":
                    return "Byte";
                case "double":
                    return "Double";
                case "float":
                    return "Float";
                case "boolean":
                    return "Boolean";
                case "date":
                    return "Date";
                case "keyword":
                case "text":
                case "ip":
                case "geo_shape":
                case "geo_point":
                    return "String";
                case "scaled_float":
                    return "BigDecimal";
                case "nested":
                    return String.format("List<%s>", parseClassName(key));
                default:
                    return "Object";
            }
        }
        return "String";
    }
}
