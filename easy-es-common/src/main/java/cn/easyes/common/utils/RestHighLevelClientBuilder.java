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
     * 构建RestHighLevelClient
     *
     * @param builder 构建连接对象
     * @return es高级客户端
     */
    public static RestHighLevelClient build(RestClientBuilder builder) {
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);
        // 检验es版本是否对应
        EsVersionUtil.verify(restHighLevelClient);
        return restHighLevelClient;
    }

}
