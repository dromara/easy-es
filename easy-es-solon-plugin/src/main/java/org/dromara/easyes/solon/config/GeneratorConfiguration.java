package org.dromara.easyes.solon.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.dromara.easyes.core.config.GeneratorConfig;
import org.dromara.easyes.core.toolkit.Generator;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * 代码生成注册
 * @author MoJie
 * @since 2.0
 */
@Component
public class GeneratorConfiguration extends Generator {

    @Inject
    private ElasticsearchClient client;

    @Override
    public Boolean generate(GeneratorConfig config) {
        super.generateEntity(config, this.client);
        return Boolean.TRUE;
    }
}
