package org.dromara.easyes.core.config;

import lombok.Data;

/**
 * generator config 代码生成器配置项
 *
 * @author hwy
 **/
@Data
public class GeneratorConfig {
    /**
     * indexName 需要生成的索引名称
     */
    private String indexName;
    /**
     * dest package path 生成模型的目标包路径
     */
    private String destPackage;
    /**
     * enable underline to camel case true by default, 是否开启下划线转驼峰 默认开启
     */
    private boolean enableUnderlineToCamelCase = true;
    /**
     * enable lombok true by default, 是否开启lombok 默认开启
     */
    private boolean enableLombok = true;

}
