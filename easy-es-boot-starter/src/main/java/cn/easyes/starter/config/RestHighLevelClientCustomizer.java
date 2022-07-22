/**
 * bravo.org
 * Copyright (c) 2018-2019 ALL Rights Reserved
 */
package cn.easyes.starter.config;

import org.elasticsearch.client.RestClientBuilder;

/**
 * @author jojocodeX
 * @version @Id: RestHighLevelClientCustomizer.java, v 0.1 2022年07月22日 16:07 hejianbing Exp $
 */
@FunctionalInterface
public interface RestHighLevelClientCustomizer {

    void customize(RestClientBuilder builder);

}