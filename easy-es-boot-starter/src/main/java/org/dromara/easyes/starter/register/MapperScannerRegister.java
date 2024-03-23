package org.dromara.easyes.starter.register;

import org.dromara.easyes.common.utils.EEVersionUtils;
import org.dromara.easyes.common.utils.LogUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;

/**
 * 注册bean
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class MapperScannerRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    private ResourceLoader resourceLoader;
    private Environment environment;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Boolean enable = Optional.ofNullable(environment.getProperty(ENABLE_PREFIX)).map(Boolean::parseBoolean).orElse(Boolean.TRUE);
        if (!enable) {
            LogUtils.info("===> Easy-Es is not enabled");
            return;
        }

        //打印banner @author dazer007
        boolean banner = Optional.ofNullable(environment.getProperty(ENABLE_BANNER)).map(Boolean::parseBoolean).orElse(Boolean.TRUE);
        if (banner) {
            boolean iKunMode = Optional.ofNullable(environment.getProperty(ENABLE_I_KUN_MODE)).map(Boolean::parseBoolean).orElse(Boolean.FALSE);
            String versionStr = EEVersionUtils.getJarVersion(this.getClass());
            String wechatStr = ":: wechat    :: 252645816, add and become muscle man!      >";
            if (iKunMode) {
                System.out.println("                 鸡你太美\n" +
                        "               鸡你实在太美\n" +
                        "                鸡你是太美\n" +
                        "                 鸡你太美\n" +
                        "              实在是太美鸡你\n" +
                        "         鸡你 实在是太美鸡你 美\n" +
                        "       鸡你  实在是太美鸡美   太美\n" +
                        "      鸡你  实在是太美鸡美      太美\n" +
                        "    鸡你    实在是太美鸡美       太美\n" +
                        "   鸡你    鸡你实在是美太美    美蓝球球球\n" +
                        "鸡 鸡     鸡你实在是太美     篮球篮球球球球\n" +
                        " 鸡      鸡你太美裆鸡太啊     球球蓝篮球球\n" +
                        "         鸡你太美裆裆鸡美       球球球\n" +
                        "          鸡你裆小 j 鸡太美\n" +
                        "           鸡太美    鸡太美\n" +
                        "            鸡美      鸡美\n" +
                        "            鸡美       鸡美\n" +
                        "             鸡美       鸡美\n" +
                        "             鸡太       鸡太\n" +
                        "           鸡 脚       鸡脚\n" +
                        "           皮 鞋       皮鞋金猴\n" +
                        "            金光       金光 大道\n" +
                        "           大道\n" +
                        "      鸡神保佑       永不宕机     永无BUG");
                wechatStr = ":: wechat    :: 252645816, add and join ikun(小黑子) group! >";
            } else {
                System.out.println("\n" +
                        "___                     _  _            ___\n" +
                        "  | __|   __ _     ___    | || |   ___    | __|    ___\n" +
                        "  | _|   / _` |   (_-<     \\_, |  |___|   | _|    (_-<\n" +
                        "  |___|  \\__,_|   /__/_   _|__/   _____   |___|   /__/_\n" +
                        "_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"|_| \"\"\"\"|_|     |_|\"\"\"\"\"|_|\"\"\"\"\"|\n" +
                        "\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\n" +
                        "----------------------------------------------------------->"
                );
            }


            // 版本长度并不固定,比如beta版,所以需要特殊处理
            int width = 43;
            int blank = width - versionStr.length();
            StringBuilder sb = new StringBuilder();
            sb.append(":: version   :: ")
                    .append(versionStr);
            for (int i = 0; i < blank; i++) {
                sb.append(" ");
            }
            sb.append(">");
            if (iKunMode) {
                System.out.println("----------------------------------------------------------->");
            }
            System.out.println(":: project   :: Easy-Es                                    >");
            System.out.println(sb);
            System.out.println(":: home      :: https://easy-es.cn/                        >");
            System.out.println(":: community :: https://dromara.org/                       >");
            System.out.println(wechatStr);
            System.out.println("----------------------------------------------------------->");
        }

        AnnotationAttributes mapperScanAttrs = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EsMapperScan.class.getName()));
        if (mapperScanAttrs != null) {
            registerBeanDefinitions(mapperScanAttrs, registry);
        }
    }

    void registerBeanDefinitions(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry) {
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        // this check is needed in Spring 3.1
        Optional.ofNullable(resourceLoader).ifPresent(scanner::setResourceLoader);
        List<String> basePackages = new ArrayList<>();
        basePackages.addAll(
                Arrays.stream(annoAttrs.getStringArray("value"))
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList()));

        scanner.registerFilters();
        scanner.doScan(StringUtils.toStringArray(basePackages));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
