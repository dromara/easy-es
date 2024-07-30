package org.dromara.easyes.common.utils;

import org.elasticsearch.client.RestHighLevelClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lyy
 */
public class RestHighLevelClientUtils {

    public static final String DEFAULT_DS = "DEFAULT_DS";

    private final static Map<String, RestHighLevelClient> restHighLevelClientMap = new ConcurrentHashMap<>();

    public RestHighLevelClientUtils() {
    }

    public static RestHighLevelClient getRestHighLevelClient(String restHighLevelClientId) {
        if (DEFAULT_DS.equals(restHighLevelClientId)) {
            return restHighLevelClientMap.values()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> ExceptionUtils.eee("Could not found RestHighLevelClient,restHighLevelClientId:%s", restHighLevelClientId));
        }
        RestHighLevelClient restHighLevelClient = restHighLevelClientMap.get(restHighLevelClientId);
        if (restHighLevelClient == null) {
            LogUtils.formatError("restHighLevelClientId: %s can not find any data source, please check your config", restHighLevelClientId);
            throw ExceptionUtils.eee("Cloud not found RestHighLevelClient,restHighLevelClientId:%s", restHighLevelClientId);
        }
        return restHighLevelClient;
    }

    public RestHighLevelClient getClient(String restHighLevelClientId) {
        return RestHighLevelClientUtils.getRestHighLevelClient(restHighLevelClientId);
    }

    public static void registerRestHighLevelClient(String restHighLevelClientId, RestHighLevelClient restHighLevelClient) {
        RestHighLevelClientUtils.restHighLevelClientMap.putIfAbsent(restHighLevelClientId, restHighLevelClient);
    }

}