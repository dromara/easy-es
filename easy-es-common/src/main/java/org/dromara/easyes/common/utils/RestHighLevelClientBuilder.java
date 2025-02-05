package org.dromara.easyes.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import static org.dromara.easyes.common.constants.BaseEsConstants.UNKNOWN;

/**
 * elasticsearch 构造器
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestHighLevelClientBuilder {
    /**
     * 支持的版本 目前支持版本为7.17.8 稳定无漏洞版
     */
    private final static String SUPPORTED_JAR_VERSION = "7.17.8";
    /**
     * 支持的客户端版本 目前支持7.xx 推荐7.17.8
     */
    private final static String SUPPORTED_CLIENT_VERSION = "7";

    /**
     * 构建RestHighLevelClient
     *
     * @param builder 构建连接对象
     * @return es高级客户端
     */
    public static RestHighLevelClient build(RestClientBuilder builder) {
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);
        // 检验es版本是否对应
        verify(restHighLevelClient);
        return restHighLevelClient;
    }

    /**
     * 校验es client版本及jar包版本
     *
     * @param restHighLevelClient es高级客户端
     */
    private static void verify(RestHighLevelClient restHighLevelClient) {
        // 校验jar包版本是否为推荐使用版本
        String jarVersion = EEVersionUtils.getJarVersion(restHighLevelClient.getClass());
        LogUtils.formatInfo("Elasticsearch jar version:%s", jarVersion);
        if (!jarVersion.equals(SUPPORTED_JAR_VERSION) && !UNKNOWN.equals(jarVersion)) {
            LogUtils.formatError("Easy-Es supported elasticsearch and restHighLevelClient jar version is:%s ,Please resolve the dependency conflict!", SUPPORTED_JAR_VERSION);
        }
        String clientVersion = EEVersionUtils.getClientVersion(restHighLevelClient);
        LogUtils.formatInfo("Elasticsearch client version:%s", clientVersion);
        if (!clientVersion.startsWith(SUPPORTED_CLIENT_VERSION)) {
            // 这里校验客户端为非强制，客户端版本非推荐版本对应提醒即可，es会报错提醒
            LogUtils.formatWarn("Easy-Es supported elasticsearch client version is:%s.xx", SUPPORTED_CLIENT_VERSION);
        }
        if (!jarVersion.equals(clientVersion)) {
            // 提示jar包与客户端版本不对应，es官方推荐jar包版本对应客户端版本
            LogUtils.formatWarn("Elasticsearch clientVersion:%s not equals jarVersion:%s, It does not affect your use, but we still recommend keeping it consistent!", clientVersion, jarVersion);
        }
    }
}
