package cn.easyes.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;

import java.io.IOException;

/**
 * elasticsearch 版本工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EsVersionUtil {

    /**
     * 支持的版本 目前支持版本为7.xx
     */
    private final static String supportedVersion = "7";

    /**
     * 获取es jar包版本
     *
     * @param restHighLevelClient es 高级客户端
     * @return jar version
     */
    public static String getJarVersion(RestHighLevelClient restHighLevelClient) {
        String version = restHighLevelClient.getClass().getPackage().getImplementationVersion();
        LogUtils.formatInfo("Elasticsearch jar version:%s", version);
        return version;
    }

    /**
     * 获取elasticsearch client 版本
     *
     * @param restHighLevelClient es高级客户端
     * @return client version
     */
    public static String getClientVersion(RestHighLevelClient restHighLevelClient) {
        MainResponse info;
        try {
            info = restHighLevelClient.info(RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String version = info.getVersion().getNumber();
        LogUtils.formatInfo("Elasticsearch client version:%s", version);
        return version;
    }

    /**
     * 校验es client版本及jar包版本
     *
     * @param restHighLevelClient es高级客户端
     */
    public static void verify(RestHighLevelClient restHighLevelClient) {
        // 校验jar包版本是否为推荐使用版本
        String jarVersion = getJarVersion(restHighLevelClient);
        if (!jarVersion.startsWith(supportedVersion)) {
            // 这里抛出异常原因是ee强制依赖于jar包版本，jar包版本不对会导致ee异常
            throw ExceptionUtils.eee("Easy-Es supported elasticsearch jar version is:%s.xx", supportedVersion);
        }
        String clientVersion = getClientVersion(restHighLevelClient);
        if (!clientVersion.startsWith(supportedVersion)) {
            // 这里校验客户端为非强制，客户端版本非推荐版本对应提醒即可，es会报错提醒
            LogUtils.formatWarn("Easy-Es supported elasticsearch client version is:%s.xx", supportedVersion);
        }
        if (!jarVersion.equals(clientVersion)) {
            // 提示jar包与客户端版本不对应，es官方推荐jar包版本对应客户端版本
            LogUtils.formatError("Elasticsearch clientVersion:%s not equals jarVersion:%s", clientVersion, jarVersion);
        }
    }
}
