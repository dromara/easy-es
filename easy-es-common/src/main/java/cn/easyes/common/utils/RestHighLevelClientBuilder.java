package cn.easyes.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * elasticsearch 构造器
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestHighLevelClientBuilder {
    /**
     * 支持的版本 目前支持版本为7.xx 推荐7.14.0
     */
    private final static String supportedVersion = "7";

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
        if (!jarVersion.startsWith(supportedVersion)) {
            // 这里抛出异常原因是ee强制依赖于jar包版本，jar包版本不对会导致ee异常
            throw ExceptionUtils.eee("Easy-Es supported elasticsearch jar version is:%s.xx", supportedVersion);
        }
        String clientVersion = EEVersionUtils.getClientVersion(restHighLevelClient);
        LogUtils.formatInfo("Elasticsearch client version:%s", clientVersion);
        if (!clientVersion.startsWith(supportedVersion)) {
            // 这里校验客户端为非强制，客户端版本非推荐版本对应提醒即可，es会报错提醒
            LogUtils.formatWarn("Easy-Es supported elasticsearch client version is:%s.xx", supportedVersion);
        }
        if (!jarVersion.equals(clientVersion)) {
            // 提示jar包与客户端版本不对应，es官方推荐jar包版本对应客户端版本
            LogUtils.formatWarn("Elasticsearch clientVersion:%s not equals jarVersion:%s", clientVersion, jarVersion);
        }
    }
}
