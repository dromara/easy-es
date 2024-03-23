package org.dromara.easyes.test.mapper;

import org.dromara.easyes.annotation.EsDS;
import org.dromara.easyes.core.core.BaseEsMapper;
import org.dromara.easyes.test.entity.Document;

/**
 * mapper 相当于Mybatis-plus的mapper
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@EsDS("ds1")
public interface DocumentMapper extends BaseEsMapper<Document> {
}
