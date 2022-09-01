package cn.easyes.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * ee 版本工具类
 *
 * @author dys
 * @since 0.9.80
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EEVersionUtil {

    /**
     * 获取指定类版本号
     * <ul>
     *      <li>只能获取jar包版本，并且打包后META-INF/MANIFEST.MF文件中存在 Implementation-Version</li>
     *      <li>不存在 Implementation-Version 时返回 unknown</li>
     *      <li>如果获取EE本身版本需要打包后获取,在test包测试用例中无法获取</li>
     * </ul>
     *
     * @param <T> objectClass T.getClass()
     * @return classVersion
     */
    public static <T> String getJarVersion(Class<T> objectClass) {
        return Optional.ofNullable(objectClass.getPackage().getImplementationVersion()).
                orElse("unknown");
    }

    /**
     * 获取elasticsearch client 版本
     * <p>
     * elasticsearch 客户端必须通过 restHighLevelClient.info 获取，无法使用getPackage.getImplementationVersion 获取
     * </p>
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
        return info.getVersion().getNumber();
    }
}
