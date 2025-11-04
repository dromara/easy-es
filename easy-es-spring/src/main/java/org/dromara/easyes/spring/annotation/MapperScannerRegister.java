package org.dromara.easyes.spring.annotation;

import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.common.utils.LogUtils;
import org.dromara.easyes.spring.MapperScannerConfigurer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.dromara.easyes.common.constants.BaseEsConstants.COMMA;

/**
 * 注册bean
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class MapperScannerRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EsMapperScan.class.getName()));
        List<String> basePackages = new ArrayList<>();
        // 默认已注解标记为主，如果没有则尝试寻找easy-es配置 scan
        if (mapperScanAttrs != null) {
            basePackages = Arrays.stream(mapperScanAttrs.getStringArray("value"))
                .filter(StringUtils::hasText)
                .map(map -> {
                    // 判断是否需要处理${}变量
                    if (map.contains("${") && map.contains("}")) {
                        String basePackage = this.environment.resolvePlaceholders(map);
                        LogUtils.formatInfo("Scan Easy-Es Mapper[%s -> %s]", map, basePackage);
                        return basePackage;
                    }
                    return map;
                })
                .collect(Collectors.toList());
        } else {
            // 如果在环境变量中取扫描的包，需要从easy-es.mappers进行取值，与mybatis配置mapper相似，但这里不做配置推荐
            String propertyValue = this.environment.getProperty("easy-es.mappers", String.class, BaseEsConstants.EMPTY_STR);
            if (org.dromara.easyes.common.utils.StringUtils.isNotBlank(propertyValue)) {
                basePackages = Arrays.asList(propertyValue.split(COMMA));
            }
        }
        // 如果扫描的包不为空，则进行mapperInterface的注册
        if (!basePackages.isEmpty()) {
            // 注册bean
            registerBeanDefinitions(registry, generateBaseBeanName(importingClassMetadata),
                    StringUtils.toStringArray(basePackages));
        }
    }

    /**
     * 通过BeanDefinition注册bean，在bean注册前处理interface中bean参数问题，interface与普通bean不同，
     * 使用了jdk动态代理，需要确定注册的实际interface class，就需要通过BeanDefinition来追加属性，
     * 当前使用到了spring的构造
     *
     * @param registry     spring bean扫描注册器
     * @param basePackages 扫描的包
     */
    void registerBeanDefinitions(BeanDefinitionRegistry registry, String beanName, String... basePackages) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        builder.addPropertyValue("basePackage", String.join(COMMA, basePackages));
        // for spring-native
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata) {
        return importingClassMetadata.getClassName() + "#" + MapperScannerRegister.class.getSimpleName() + "#" + 0;
    }

}
