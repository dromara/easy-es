package org.dromara.easyes.spring.annotation;

import org.dromara.easyes.spring.MapperScannerConfigurer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 注册bean
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class MapperScannerRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EsMapperScan.class.getName()));
        // 默认已注解标记为主，如果没有则尝试寻找easy-es配置 scan
        if (mapperScanAttrs != null) {
            List<String> basePackages = Arrays.stream(mapperScanAttrs.getStringArray("value"))
                    .filter(StringUtils::hasText).collect(Collectors.toList());
            // 注册bean
            registerBeanDefinitions(registry, generateBaseBeanName(importingClassMetadata, 0),
                    StringUtils.toStringArray(basePackages));
        }
    }

    /**
     * 通过BeanDefinition注册bean，在bean注册前处理interface中bean参数问题，interface与普通bean不同，
     * 使用了jdk动态代理，需要确定注册的实际interface class，就需要通过BeanDefinition来追加属性，
     * 当前使用到了spring的构造
     * @param registry spring bean扫码注册器
     * @param basePackages 扫码的包
     */
    void registerBeanDefinitions(BeanDefinitionRegistry registry, String beanName, String... basePackages) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        builder.addPropertyValue("basePackage", String.join(",", basePackages));
        // for spring-native
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, int index) {
        return importingClassMetadata.getClassName() + "#" + MapperScannerRegister.class.getSimpleName() + "#" + index;
    }

}
