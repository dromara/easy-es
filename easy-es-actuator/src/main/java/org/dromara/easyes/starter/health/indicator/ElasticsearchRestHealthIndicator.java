package org.dromara.easyes.starter.health.indicator;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.HealthStatus;
import co.elastic.clients.elasticsearch.cluster.ElasticsearchClusterClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.dromara.easyes.core.cache.BaseCache;
import org.dromara.easyes.core.kernel.BaseEsMapper;
import org.dromara.easyes.core.kernel.BaseEsMapperImpl;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Elasticsearch健康检查指示器
 * <p>
 */
public class ElasticsearchRestHealthIndicator extends AbstractHealthIndicator implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        Map<String, Object> details = new HashMap<>();
        Map<String, BaseEsMapper> beansOfType = applicationContext.getBeansOfType(BaseEsMapper.class);
        if (CollectionUtils.isEmpty(beansOfType)) {
            builder.up();
            return;
        }
        Collection<BaseEsMapperImpl<?>> allImpl = BaseCache.getAllImpl();
        if (CollectionUtils.isEmpty(allImpl)) {
            builder.up();
            return;
        }
        Collection<ElasticsearchClient> elasticsearchClients = allImpl
                .stream()
                .map(BaseEsMapperImpl::getClient)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (ElasticsearchClient elasticsearchClient : elasticsearchClients) {
            ElasticsearchClusterClient cluster = elasticsearchClient.cluster();
            HealthResponse health = cluster.health();
            HealthStatus status = health.status();
            if (status.equals(HealthStatus.Red)) {
                ElasticsearchTransport transport =  cluster._transport();
                if (transport instanceof RestClientTransport ) {
                    RestClientTransport restTransport = (RestClientTransport)transport;
                    RestClient restClient = restTransport.restClient();
                    List<Node> nodes = restClient.getNodes();
                    String clusterName = health.clusterName();
                    List<String> message = new ArrayList<>();

                    for (Node node : nodes) {
                        HttpHost host = node.getHost();
                        message.add("node:" + host.getHostName() + ":" + host.getPort());
                    }
                    details.put(clusterName,message);
                }
            }
        }
        if (CollectionUtils.isEmpty(details)) {
            builder.up();
            return;
        }
        builder.withDetails(details);
        builder.outOfService();
    }
}
