package org.dromara.easyes.solon;

import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.common.utils.LogUtils;
import org.dromara.easyes.solon.config.EsAutoConfiguration;
import org.dromara.easyes.solon.config.GeneratorConfiguration;
import org.dromara.easyes.solon.factory.IndexStrategyFactory;
import org.dromara.easyes.solon.register.EsMapperScan;
import org.dromara.easyes.solon.register.MapperScannerRegister;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

/**
 * solon插件实现
 * @author MoJie
 * @since 2.0
 */
public class EasyEsSolonPlugin implements Plugin {

    @Override
    public void start(AppContext context) throws Throwable {
        boolean enable = context.cfg().getBool(BaseEsConstants.ENABLE_PREFIX, true);
        if (!enable) {
            LogUtils.info("===> Easy-Es is not enabled");
            return;
        }
        context.beanMake(EsAutoConfiguration.class);
        context.beanMake(GeneratorConfiguration.class);
        context.beanMake(IndexStrategyFactory.class);
        // 扫描EsMapperScan的包并注册到容器
        context.beanBuilderAdd(EsMapperScan.class, new MapperScannerRegister(context));
    }
}
