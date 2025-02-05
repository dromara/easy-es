package org.dromara.easyes.test.generator;

import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.core.config.GeneratorConfig;
import org.dromara.easyes.core.toolkit.Generator;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;

/**
 * entity代码生成器测试
 *
 * @author hwy
 **/
@DisplayName("easy-es领域实体生成单元测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class GeneratorTest {
    @Resource
    private Generator generator;

    /**
     * 测试根据已有索引生成领域模型
     */
    @Test
    public void testGenerate() {
        IndexName indexName = Document.class.getAnnotation(IndexName.class);
        GeneratorConfig config = new GeneratorConfig();
        // 将生成的领域模型放置在当前项目的指的包路径下
        String destPackage = "org.dromara.easyes.test.generated";
        config.setDestPackage(destPackage);
        config.setIndexName(indexName.value());
        Boolean success = generator.generate(config);
        Assertions.assertTrue(success,"generate failed!");
    }
}
