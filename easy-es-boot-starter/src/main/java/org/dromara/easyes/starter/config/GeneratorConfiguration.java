package org.dromara.easyes.starter.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.dromara.easyes.core.config.GeneratorConfig;
import org.dromara.easyes.core.toolkit.Generator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 代码生成注册
 * @author MoJie
 * @since 2.0
 */
@Component
public class GeneratorConfiguration extends Generator {

    @Autowired
    private ElasticsearchClient client;

    @Override
    public Boolean generate(GeneratorConfig config) {
        super.generateEntity(config, this.client);
        return Boolean.TRUE;
    }
}
