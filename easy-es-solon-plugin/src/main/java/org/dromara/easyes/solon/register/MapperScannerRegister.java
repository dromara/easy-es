package org.dromara.easyes.solon.register;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.dromara.easyes.annotation.EsDS;
import org.dromara.easyes.annotation.Intercepts;
import org.dromara.easyes.common.enums.ProcessIndexStrategyEnum;
import org.dromara.easyes.common.property.EasyEsProperties;
import org.dromara.easyes.common.property.GlobalConfig;
import org.dromara.easyes.common.strategy.AutoProcessIndexStrategy;
import org.dromara.easyes.common.utils.EEVersionUtils;
import org.dromara.easyes.common.utils.EsClientUtils;
import org.dromara.easyes.common.utils.LogUtils;
import org.dromara.easyes.common.utils.TypeUtils;
import org.dromara.easyes.core.biz.EntityInfo;
import org.dromara.easyes.core.cache.BaseCache;
import org.dromara.easyes.core.cache.GlobalConfigCache;
import org.dromara.easyes.core.cache.JacksonCache;
import org.dromara.easyes.core.kernel.BaseEsMapper;
import org.dromara.easyes.core.proxy.EsMapperProxy;
import org.dromara.easyes.core.toolkit.EntityInfoHelper;
import org.dromara.easyes.extension.context.Interceptor;
import org.dromara.easyes.extension.context.InterceptorChain;
import org.dromara.easyes.extension.context.InterceptorChainHolder;
import org.dromara.easyes.solon.factory.IndexStrategyFactory;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Condition;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.util.ClassUtil;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;
import static org.dromara.easyes.common.utils.EsClientUtils.DEFAULT_DS;

/**
 * 注册bean
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Component
@Condition(onBean = EsClientUtils.class)
public class MapperScannerRegister {
    private final AutoProcessIndexStrategy processIndexStrategy;
    private final EsClientUtils esClientUtils;
    private final AppContext context;

    public MapperScannerRegister(EasyEsProperties easyEsProperties,
                                 IndexStrategyFactory indexStrategyFactory,
                                 EsClientUtils esClientUtils,
                                 AppContext context) {
        this.esClientUtils = esClientUtils;
        this.context = context;
        GlobalConfig globalConfig = easyEsProperties.getGlobalConfig();
        GlobalConfigCache.setGlobalConfig(globalConfig);
        if (!ProcessIndexStrategyEnum.MANUAL.equals(globalConfig.getProcessIndexMode())) {
            this.processIndexStrategy = indexStrategyFactory.getByStrategyType(globalConfig.getProcessIndexMode().getStrategyType());
        } else {
            this.processIndexStrategy = null;
            LogUtils.info("===> manual index mode activated");
        }
        doScan();
    }

    private void doScan() {
        EsMapperScan anno = Solon.app().source().getAnnotation(EsMapperScan.class);
        boolean banner = Solon.cfg().getBool(ENABLE_BANNER, true);
        if (banner) {
            boolean iKunMode = Solon.cfg().getBool(ENABLE_I_KUN_MODE, false);
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
        // 查找es mapper
        for (String doScan : anno.value()) {
            // 判断是否需要处理${}变量
            if (doScan.contains("${") && doScan.contains("}")) {
                String basePackage = Solon.cfg().getByTmpl(doScan);
                LogUtils.formatInfo("Scan Easy-Es Mapper[%s -> %s]", doScan, basePackage);
                doScan = basePackage;
            }
            Collection<Class<?>> classPath = ClassUtil.scanClasses(doScan);
            for (Class<?> clazz : classPath) {
                // 跳过非ee的mapper,比如瞎几把写的接口,没有继承BaseEsMapper，继承了的推入容器
                if (BaseEsMapper.class.isAssignableFrom(clazz)) {
                    this.beanWrapPut(clazz);
                }
            }
        }
    }

    /**
     * 将mapper代理对象推入容器
     * @param clazz mapper
     * @author MoJie
     */
    private void beanWrapPut(Class<?> clazz) {
        EsMapperProxy<?> esMapperProxy = new EsMapperProxy<>(clazz, new ConcurrentHashMap<>());
        // 获取实体类
        Class<?> entityClass = TypeUtils.getInterfaceT(clazz, ZERO);

        // 初始化entity缓存
        BaseCache.initEntityCache(entityClass);

        // jackson配置缓存
        JacksonCache.init(EntityInfoHelper.ENTITY_INFO_CACHE);

        //获取动态数据源 若未配置多数据源,则使用默认数据源
        String restHighLevelClientId = Optional.ofNullable(clazz.getAnnotation(EsDS.class))
                .map(EsDS::value).orElse(DEFAULT_DS);
        ElasticsearchClient client = esClientUtils.getClient(restHighLevelClientId);

        // 初始化mapper
        BaseCache.initMapperCache(clazz, entityClass, client);

        // 创建代理
        Object mapperProxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, esMapperProxy);

        // 初始化拦截器链
        InterceptorChain interceptorChain = this.initInterceptorChain();

        // 异步处理索引创建/更新/数据迁移等
        // 父子类型,仅针对父类型创建索引,子类型不创建索引
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        boolean isChild = entityInfo.isChild();
        if (!isChild && processIndexStrategy != null) {
            processIndexStrategy.processIndexAsync(entityClass, client);
        }
        // 将拦截器链推入容器
        context.wrapAndPut(clazz, interceptorChain.pluginAll(mapperProxy));
    }

    /**
     * 拦截器
     * @return {@link InterceptorChain}
     * @author MoJie
     */
    private InterceptorChain initInterceptorChain() {
        InterceptorChainHolder interceptorChainHolder = InterceptorChainHolder.getInstance();
        InterceptorChain interceptorChain = interceptorChainHolder.getInterceptorChain();
        if (interceptorChain == null) {
            synchronized (this) {
                interceptorChainHolder.initInterceptorChain();
                Map<String, Intercepts> beansWithAnnotation = Solon.context().getBeansMapOfType(Intercepts.class);
                beansWithAnnotation.forEach((key, val) -> {
                    if (val instanceof Interceptor) {
                        Interceptor interceptor = (Interceptor) val;
                        interceptorChainHolder.addInterceptor(interceptor);
                    }
                });
            }
        }
        return interceptorChainHolder.getInterceptorChain();
    }

}
