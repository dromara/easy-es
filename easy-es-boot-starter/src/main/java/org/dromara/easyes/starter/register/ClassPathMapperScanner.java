package org.dromara.easyes.starter.register;

import org.dromara.easyes.core.kernel.BaseEsMapper;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Arrays;
import java.util.Set;

/**
 * 扫描指定路径下的所有接口
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class ClassPathMapperScanner extends ClassPathBeanDefinitionScanner {
    public ClassPathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void registerFilters() {
        // default include filter that accepts all classes
        addIncludeFilter((metadataReader, metadataReaderFactory) -> {
            // 跳过非ee的mapper,比如瞎几把写的接口,没有继承BaseEsMapper
            String className = metadataReader.getClassMetadata().getClassName();
            try {
                Class<?> clazz = Class.forName(className);
                return BaseEsMapper.class.isAssignableFrom(clazz);
            } catch (ClassNotFoundException e) {
                logger.debug("mapper not found" + e);
            }
            return true;
        });

        // exclude package-info.java
        addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            String className = metadataReader.getClassMetadata().getClassName();
            return className.endsWith("package-info");
        });
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (beanDefinitions.isEmpty()) {
            logger.warn("No Easy-Es mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            processBeanDefinitions(beanDefinitions);
        }
        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            String beanClassName = definition.getBeanClassName();
            logger.debug("Creating MapperFactoryBean with name '" + holder.getBeanName()
                    + "' and '" + beanClassName + "' mapperInterface");

            // the mapper interface is the original class of the bean
            // but, the actual class of the bean is MapperFactoryBean
            definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
            definition.setBeanClass(MapperFactoryBean.class);

            logger.debug("Enabling autowire by type for MapperFactoryBean with name '" + holder.getBeanName() + "'.");
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }
    }


    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            logger.warn("Skipping MapperFactoryBean with name '" + beanName
                    + "' and '" + beanDefinition.getBeanClassName() + "' mapperInterface"
                    + ". Bean already defined with the same name!");
            return false;
        }
    }
}
