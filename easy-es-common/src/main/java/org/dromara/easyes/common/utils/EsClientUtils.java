package org.dromara.easyes.common.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.dromara.easyes.common.enums.SchemaEnum;
import org.dromara.easyes.common.property.EasyEsProperties;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;

/**
 * @author lyy
 */
public class EsClientUtils {

    public static final String DEFAULT_DS = "DEFAULT_DS";

    private final static Map<String, ElasticsearchClient> restHighLevelClientMap = new ConcurrentHashMap<>();

    public EsClientUtils() {
    }

    public static ElasticsearchClient getElasticsearchClient(String restHighLevelClientId) {
        if (DEFAULT_DS.equals(restHighLevelClientId)) {
            return restHighLevelClientMap.values()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> ExceptionUtils.eee("Could not found ElasticsearchClient,restHighLevelClientId:%s", restHighLevelClientId));
        }
        ElasticsearchClient client = restHighLevelClientMap.get(restHighLevelClientId);
        if (client == null) {
            LogUtils.formatError("restHighLevelClientId: %s can not find any data source, please check your config", restHighLevelClientId);
            throw ExceptionUtils.eee("Cloud not found ElasticsearchClient,restHighLevelClientId:%s", restHighLevelClientId);
        }
        return client;
    }

    public static void registerClient(String restHighLevelClientId, Supplier<ElasticsearchClient> restHighLevelClient) {
        EsClientUtils.restHighLevelClientMap.putIfAbsent(restHighLevelClientId, restHighLevelClient.get());
    }

    public static ElasticsearchClient buildClient(EasyEsProperties easyEsConfigProperties, ObjectMapper objectMapper,
                                                  EasyEsHeadersCustomizer headersCustomizer) {
        // 处理地址
        String address = easyEsConfigProperties.getAddress();
        if (StringUtils.isEmpty(address)) {
            throw ExceptionUtils.eee("please config the es address");
        }
        if (!address.contains(COLON)) {
            throw ExceptionUtils.eee("the address must contains port and separate by ':'");
        }
        String schema = StringUtils.isEmpty(easyEsConfigProperties.getSchema())
                ? DEFAULT_SCHEMA : easyEsConfigProperties.getSchema();
        List<HttpHost> hostList = new ArrayList<>();
        Arrays.stream(easyEsConfigProperties.getAddress().split(COMMA))
                .forEach(item -> hostList.add(new HttpHost(item.split(COLON)[0],
                        Integer.parseInt(item.split(COLON)[1]), schema)));

        // 转换成 HttpHost 数组
        HttpHost[] httpHost = hostList.toArray(new HttpHost[]{});

        // 构建连接对象
        RestClientBuilder builder = RestClient.builder(httpHost);
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            // 设置心跳时间,最大连接数,最大连接路由
            Optional.ofNullable(easyEsConfigProperties.getKeepAliveMillis()).ifPresent(p -> httpClientBuilder
                    .setKeepAliveStrategy((response, context) -> p));
            Optional.ofNullable(easyEsConfigProperties.getMaxConnTotal()).ifPresent(httpClientBuilder::setMaxConnTotal);
            Optional.ofNullable(easyEsConfigProperties.getMaxConnPerRoute()).ifPresent(httpClientBuilder::setMaxConnPerRoute);

            // 设置账号密码
            String username = easyEsConfigProperties.getUsername();
            String password = easyEsConfigProperties.getPassword();
            if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(username, password));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }

            // https ssl ignore
            if (SchemaEnum.https.name().equals(schema)) {
                try {
                    // 信任所有
                    SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build();
                    SSLIOSessionStrategy sessionStrategy = new SSLIOSessionStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
                    httpClientBuilder.disableAuthCaching();
                    httpClientBuilder.setSSLStrategy(sessionStrategy);
                } catch (Exception e) {
                    LogUtils.error("restHighLevelClient build SSLContext exception: %s", e.getMessage());
                    throw ExceptionUtils.eee(e);
                }
            }
            // 兼容性配置和请求头自定义配置
            buildCompatible(httpClientBuilder, easyEsConfigProperties, headersCustomizer);
            return httpClientBuilder;
        });

        // 设置超时时间之类的
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            Optional.ofNullable(easyEsConfigProperties.getConnectTimeout()).ifPresent(requestConfigBuilder::setConnectTimeout);
            Optional.ofNullable(easyEsConfigProperties.getSocketTimeout()).ifPresent(requestConfigBuilder::setSocketTimeout);
            Optional.ofNullable(easyEsConfigProperties.getConnectionRequestTimeout())
                    .ifPresent(requestConfigBuilder::setConnectionRequestTimeout);
            return requestConfigBuilder;
        });
        // 如果是驼峰转下划线, 则增加序列化器
        if (easyEsConfigProperties.getGlobalConfig().getDbConfig().isMapUnderscoreToCamelCase()) {
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        }
        return new ElasticsearchClient(new RestClientTransport(builder.build(),
                new JacksonJsonpMapper(objectMapper)));
    }

    /**
     * 兼容性配置
     *
     * @param httpClientBuilder httpClientBuilder
     */
    private static void buildCompatible(HttpAsyncClientBuilder httpClientBuilder, EasyEsProperties properties, EasyEsHeadersCustomizer headersCustomizer) {
        if (properties.isCompatible()) {
            httpClientBuilder.addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                // 这里主动编辑请求头，避免兼容性问题报错
                // 在es8中，默认的请求头 Accept 字段为：
                // Accept: application/vnd.elasticsearch+json; compatible-with=8
                // 访问低版本es集群有可能出现
                // [es/create] failed:
                // [media_type_header_exception] Invalid media-type value
                // on headers [Accept, Content-Type]
                // 通过拦截器编辑请求头以避免此错误！
                request.setHeader("Accept", "application/json");
            });
            httpClientBuilder.setDefaultHeaders(new ArrayList<Header>() {{
                add(new BasicHeader("Accept", "application/json"));
                add(new BasicHeader("Content-Type", "application/json"));
                add(new BasicHeader("Connection", "Keep-Alive"));
                add(new BasicHeader("Charset", "UTF-8"));
            }});
            // 这部分为了避免旧版本406报错（如果es更新到了8.xx，把这个去掉即可）
            httpClientBuilder.addInterceptorLast((HttpResponseInterceptor)
                    (response, context) ->
                            response.addHeader("X-Elastic-Product", "Elasticsearch"));
        }
        // 如果自定义了请求头，则添加
        Optional.ofNullable(headersCustomizer).ifPresent(consumer -> httpClientBuilder
                .addInterceptorLast((HttpRequestInterceptor) (request, context) ->
                    consumer.customizer().forEach(request::addHeader)));
    }

    public ElasticsearchClient getClient(String clientId) {
        return getElasticsearchClient(clientId);
    }
}