package org.dromara.easyes.core.toolkit;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.NdJsonpSerializable;
import co.elastic.clients.transport.ElasticsearchTransportBase;
import co.elastic.clients.transport.Endpoint;
import co.elastic.clients.util.BinaryData;
import jakarta.json.stream.JsonGenerator;
import org.dromara.easyes.common.property.GlobalConfig;
import org.dromara.easyes.common.utils.LogUtils;
import org.dromara.easyes.core.cache.GlobalConfigCache;

import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import static org.dromara.easyes.common.constants.BaseEsConstants.DSL_PREFIX;
import static org.dromara.easyes.common.constants.BaseEsConstants.I_KUN_PREFIX;

/**
 * dsl打印工具类
 *
 * @author jaime
 * @version 1.0
 * @since 2025/2/26
 */
public class PrintUtils {
    /**
     * 根据全局配置决定是否控制台打印DSL语句
     * 参考{@link ElasticsearchTransportBase#prepareTransportRequest(Object, Endpoint)}获取相关数据
     *
     * @param request es请求参数
     * @param <RequestT> 泛型
     * @param <ResponseT> 泛型
     * @param <ErrorT> 泛型
     * @param mapper  序列化mapper
     */
    public static <RequestT, ResponseT, ErrorT> void printDsl(RequestT request, JsonpMapper mapper) {
        if (request == null) {
            return;
        }

        String method = "";
        String fullUrl = "";
        String dsl = "";
        try {
            // 端点
            @SuppressWarnings("unchecked")
            Endpoint<RequestT, ResponseT, ErrorT> endpoint = (Endpoint<RequestT, ResponseT, ErrorT>) request.getClass()
                    .getDeclaredField("_ENDPOINT").get(null);
            // 实际请求参数
            Object body = endpoint.body(request);

            method = endpoint.method(request);
            fullUrl = buildFullUrl(endpoint.requestUrl(request), endpoint.queryParameters(request));
            dsl = buildDsl(mapper, body);
        } catch (Exception e) {
            // No endpoint, ignore
        }

        printDsl(method, fullUrl, dsl);
    }

    /**
     * 打印dsl
     *
     * @param method  请求方法
     * @param fullUrl 请求路径
     * @param dsl     dsl
     */
    public static void printDsl(String method, String fullUrl, String dsl) {
        GlobalConfig globalConfig = GlobalConfigCache.getGlobalConfig();
        if (globalConfig.isPrintDsl()) {
            String prefix = globalConfig.isIKunMode() ? I_KUN_PREFIX : DSL_PREFIX;
            LogUtils.info(prefix +
                          "\n" + method + " " + fullUrl +
                          "\n" + dsl
            );
        }
    }

    /**
     * 打印dsl
     *
     * @param sql sql
     */
    public static void printSql(String sql) {
        GlobalConfig globalConfig = GlobalConfigCache.getGlobalConfig();
        if (globalConfig.isPrintDsl()) {
            String prefix = globalConfig.isIKunMode() ? I_KUN_PREFIX : DSL_PREFIX;
            LogUtils.info(prefix + "\n" + sql);
        }
    }

    /**
     * 根据全局配置决定是否控制台打印DSL语句
     *
     * @param request es请求参数
     * @param client  es客户端
     * @param <RequestT> 泛型
     */
    public static <RequestT> void printDsl(RequestT request, ElasticsearchClient client) {
        printDsl(request, client._jsonpMapper());
    }

    /**
     * 创建完成的请求路径. 包含请求参数
     *
     * @param path   路径
     * @param params 参数
     * @return String
     */
    public static String buildFullUrl(String path, Map<String, String> params) {
        StringBuilder fullUrlSb = new StringBuilder(path);
        String delim = "?";
        for (Map.Entry<String, String> param : params.entrySet()) {
            fullUrlSb.append(delim);
            delim = "&";
            try {
                fullUrlSb.append(param.getKey()).append("=").append(URLEncoder.encode(param.getValue(), "UTF-8"));
            } catch (Exception ignore) {
            }
        }
        return fullUrlSb.toString();
    }

    /**
     * 构建dsl
     *
     * @param mapper 序列化mapper
     * @param body   实际请求
     * @return String
     */
    private static String buildDsl(
            JsonpMapper mapper,
            Object body
    ) {
        if (body == null || body instanceof BinaryData) {
            return "";
        }

        if (!(body instanceof NdJsonpSerializable)) {
            return toJsonString(body, mapper);
        }
        NdJsonpSerializable nd = (NdJsonpSerializable) body;

        StringBuilder bodyStr = new StringBuilder();
        collectNdJsonLines(bodyStr, nd, mapper);
        return bodyStr.toString();
    }

    /**
     * 递归获取多个json的字符串
     *
     * @param body   结果字符串
     * @param value  当前字段
     * @param mapper 序列化mapper
     */
    public static void collectNdJsonLines(StringBuilder body, NdJsonpSerializable value, JsonpMapper mapper) {
        Iterator<?> values = value._serializables();
        while (values.hasNext()) {
            Object item = values.next();
            if (item == null) {
                // skip
            } else if (item instanceof NdJsonpSerializable && item != value) {
                NdJsonpSerializable nd = (NdJsonpSerializable) item; // do not recurse on the item itself
                collectNdJsonLines(body, nd, mapper);
            } else {
                body.append(toJsonString(item, mapper)).append("\n");
            }
        }
    }

    public static String toJsonString(Object value, JsonpMapper mapper) {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = mapper.jsonProvider().createGenerator(writer);
        mapper.serialize(value, generator);
        generator.close();
        return writer.toString();
    }
}
