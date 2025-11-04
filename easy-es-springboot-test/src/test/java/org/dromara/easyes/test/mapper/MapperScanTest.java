package org.dromara.easyes.test.mapper;

import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.core.toolkit.EntityInfoHelper;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper2.AuthorMapper;
import org.dromara.easyes.test.mapper3.CommentMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author MoJie
 * @since 2.0
 */
@DisplayName("easy-es多包扫描单元测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = MapperScanEsApplication.class)
public class MapperScanTest {

    @Resource
    private ContactMapper contactMapper;
    @Resource
    private AuthorMapper authorMapper;
    @Resource
    private CommentMapper commentMapper;

    @Test
    @Order(0)
    public void testCreateIndex1() {
        boolean success = contactMapper.createIndex();
        Assertions.assertTrue(success);
    }

    @Test
    @Order(1)
    public void testDeleteIndex1() {
        boolean deleted = contactMapper.deleteIndex();
        Assertions.assertTrue(deleted);
    }

    @Test
    @Order(2)
    public void testCreateIndex2() {
        boolean success = authorMapper.createIndex();
        Assertions.assertTrue(success);
    }

    @Test
    @Order(3)
    public void testDeleteIndex2() {
        boolean success = authorMapper.deleteIndex();
        Assertions.assertTrue(success);
    }

    @Test
    @Order(4)
    public void testCreateIndex3() {
        boolean success = commentMapper.createIndex();
        Assertions.assertTrue(success);
    }

    @Test
    @Order(5)
    public void testDeleteIndex3() {
        boolean success = commentMapper.deleteIndex();
        Assertions.assertTrue(success);
    }

}
