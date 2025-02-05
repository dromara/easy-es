package org.dromara.easyes.spring;

import lombok.Setter;
import org.dromara.easyes.common.utils.EEVersionUtils;
import org.dromara.easyes.common.utils.LogUtils;
import org.dromara.easyes.spring.config.ClassPathMapperScanner;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;

/**
 * spring配置类扫描
 * @author MoJie
 * @since 2.0
 */
public class MapperScannerConfigurer
        implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {

    @Setter
    private String basePackage;

    private ApplicationContext applicationContext;

    private String beanName;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        Boolean enable = getEnvironment().getProperty(ENABLE_PREFIX, Boolean.TYPE, true);
        if (!enable) {
            LogUtils.info("===> Easy-Es is not enabled");
            return;
        }
        this.printBanner();
        Map<String, PropertyResourceConfigurer> prcs = applicationContext
                .getBeansOfType(PropertyResourceConfigurer.class, false, false);
        // 如果spring动态配置上下文中存在，那么到上下文对象中获取/比如在*.properties中配置了键值对
        if (!prcs.isEmpty() && applicationContext instanceof ConfigurableApplicationContext) {
            BeanDefinition mapperScannerBean = ((ConfigurableApplicationContext) applicationContext).getBeanFactory()
                    .getBeanDefinition(beanName);
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerBeanDefinition(beanName, mapperScannerBean);
            for (PropertyResourceConfigurer prc : prcs.values()) {
                prc.postProcessBeanFactory(factory);
            }
            PropertyValues values = mapperScannerBean.getPropertyValues();
            this.basePackage = getPropertyValue("basePackage", values);
        }

        // 取变量
        this.basePackage = Optional.ofNullable(this.basePackage)
                .map(getEnvironment()::resolvePlaceholders).orElse(null);
        // 做扫包的操作了、与注解扫包类似，只不过这里是spring配置方式
        // 在mybatis中配置了很多扫描拦截属性，这里放到后面拓展
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(beanDefinitionRegistry, getEnvironment());
        scanner.registerFilters();
        scanner.doScan(this.basePackage);
    }

    private String getPropertyValue(String propertyName, PropertyValues values) {
        PropertyValue property = values.getPropertyValue(propertyName);
        if (property == null) {
            return null;
        }
        Object value = property.getValue();
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return value.toString();
        }
        if (value instanceof TypedStringValue) {
            return ((TypedStringValue) value).getValue();
        }
        return null;
    }

    private Environment getEnvironment() {
        return this.applicationContext.getEnvironment();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    /**
     * bean 注册完成后校验属性是否注入
     * @author MoJie
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.basePackage, "Property 'basePackage' is required. mapper扫包路径为必填。");
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 打印banner
     * @author MoJie
     */
    private void printBanner() {
        //打印banner @author dazer007
        Boolean banner = getEnvironment().getProperty(ENABLE_BANNER, Boolean.class, true);
        if (banner) {
            Boolean iKunMode = getEnvironment().getProperty(ENABLE_I_KUN_MODE, Boolean.TYPE, false);
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
    }

}
