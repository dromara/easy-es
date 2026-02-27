package org.dromara.easyes.spring.config;

import org.dromara.easyes.common.constants.BaseEsConstants;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * EasyEs 配置加载条件easy-es.enable=true
 * @author MoJie
 * @since 3.0.1
 */
public class EasyEsConfiguredCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return context.getEnvironment().getProperty(BaseEsConstants.ENABLE_PREFIX, Boolean.class, Boolean.TRUE);
    }
}
