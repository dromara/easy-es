package org.dromara.easyes.common.utils;

import java.util.Map;

/**
 * 自定义es header
 *
 * @author MoJie
 * @since 3.0.1
 */
@FunctionalInterface
public interface EasyEsHeadersCustomizer {

    /**
     * 自定义追加es client请求头
     * @author MoJie
     */
    Map<String, String> customizer();

}
