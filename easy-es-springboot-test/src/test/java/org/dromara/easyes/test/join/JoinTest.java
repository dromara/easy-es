package org.dromara.easyes.test.join;


import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.conditions.update.LambdaEsUpdateWrapper;
import org.dromara.easyes.core.toolkit.EntityInfoHelper;
import org.dromara.easyes.core.toolkit.FieldUtils;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Author;
import org.dromara.easyes.test.entity.Comment;
import org.dromara.easyes.test.entity.Contact;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.AuthorMapper;
import org.dromara.easyes.test.mapper.CommentMapper;
import org.dromara.easyes.test.mapper.ContactMapper;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * 父子类型测试 其结构如下所示,Document文档有子文档Author(作者)和Comment(评论),其中Author还有个子文档Contact(联系方式)
 * 下述结构可参考加在Document上的自定义注解@Join和@Node来表达
 * <pre>
 *         Document
 *       /          \
 *    Comment       Author
 *                      \
 *                    Contact
 * </pre>
 *
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@DisplayName("easy-es父子类型相关功能单元测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class JoinTest {
    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private AuthorMapper authorMapper;
    @Resource
    private ContactMapper contactMapper;

    /**
     * 固定路由,确保后续CRUD中所有父子文档均在统一分片上
     */
    private final static String FIXED_ROUTING = "testRouting";


    @Test
    @Order(0)
    public void testCreateIndex() {
        // 0.前置操作 创建索引 需确保索引托管模式处于manual手动挡,若为自动挡则会冲突.
        boolean success = documentMapper.createIndex();
        Assertions.assertTrue(success);
    }

    @Test
    @Order(1)
    public void testInsert() throws InterruptedException {
        // 新新增父文档,然后再插入子文档
        String parentId = "doc-1";
        Document root = new Document();
        root.setEsId(parentId);
        root.setTitle("我是父文档的标题");
        root.setContent("father doc");
        documentMapper.insert(FIXED_ROUTING, root);
        Thread.sleep(2000);


        // 插入子文档1
        Comment nodeA1 = new Comment();
        nodeA1.setId("comment-1");
        nodeA1.setCommentContent("test1");
        // 这里特别注意,子文档必须指定其路由和父亲文档相同,否则傻儿子找不到爹别怪我没提醒 (es语法如此,非框架限制)
        commentMapper.insert(FIXED_ROUTING, parentId, nodeA1);

        // 插入子文档2
        Comment nodeA2 = new Comment();
        nodeA2.setId("comment-2");
        nodeA2.setCommentContent("test2");
        commentMapper.insert(FIXED_ROUTING, parentId, nodeA2);

        // 插入子文档3
        Author nodeB1 = new Author();
        nodeB1.setAuthorId("author-1");
        nodeB1.setAuthorName("tom");
        authorMapper.insert(FIXED_ROUTING, parentId, nodeB1);

        // 插入子文档4
        Author nodeB2 = new Author();
        nodeB2.setAuthorId("author-2");
        nodeB2.setAuthorName("cat");
        authorMapper.insert(FIXED_ROUTING, parentId, nodeB2);
        Thread.sleep(2000);

        // 插入孙子文档1(把孙子1挂在子文档3上)
        Contact child1 = new Contact();
        child1.setContactId("contact-1");
        child1.setAddress("zhejiang province");
        contactMapper.insert(FIXED_ROUTING, nodeB1.getAuthorId(), child1);

        // 插入孙子文档2(把孙子2挂在子文档3上)
        Contact child2 = new Contact();
        child2.setContactId("contact-2");
        child2.setAddress("hangzhou city");
        contactMapper.insert(FIXED_ROUTING, nodeB1.getAuthorId(), child2);

        // 插入孙子文档3(把孙子3挂在子文档4上)
        Contact child3 = new Contact();
        child3.setContactId("contact-3");
        child3.setAddress("binjiang region");
        contactMapper.insert(FIXED_ROUTING, nodeB2.getAuthorId(), child3);

        // es写入数据有延迟 适当休眠 保证后续查询结果正确
        Thread.sleep(2000);
    }

    @Test
    @Order(2)
    public void testSelect() {
        // 温馨提示,下面wrapper中的type实际上就是索引JoinField中指定的父子名称,与原生语法是一致的
        // case1: hasChild查询,返回的是相关的父文档 所以查询用父文档实体及其mapper
        LambdaEsQueryWrapper<Document> documentWrapper = new LambdaEsQueryWrapper<>();
        documentWrapper.hasChild("comment", w -> w.eq(FieldUtils.val(Comment::getCommentContent), "test1"));
        List<Document> documents = documentMapper.selectList(documentWrapper);
        System.out.println(documents);

        LambdaEsQueryWrapper<Author> authorWrapper = new LambdaEsQueryWrapper<>();
        authorWrapper.hasChild("contact", w -> w.match(FieldUtils.val(Contact::getAddress), "city"));
        List<Author> authors = authorMapper.selectList(authorWrapper);
        System.out.println(authors);

        // case2: hasParent查询,返回的是相关的子文档 所以查询用子文档实体及其mapper
        LambdaEsQueryWrapper<Comment> commentWrapper = new LambdaEsQueryWrapper<>();
        commentWrapper.like(Comment::getCommentContent, "test");
        // 字段名称你也可以不用FieldUtils.val,直接传入字符串也行
        commentWrapper.hasParent("document", w -> w.match("content", "father"));
        List<Comment> comments = commentMapper.selectList(commentWrapper);
        System.out.println(comments);

        // case2.1: 孙子查爹的情况
        LambdaEsQueryWrapper<Contact> contactWrapper = new LambdaEsQueryWrapper<>();
        contactWrapper.hasParent("author", w -> w.eq(FieldUtils.val(Author::getAuthorName), "cat"));
        List<Contact> contacts = contactMapper.selectList(contactWrapper);
        System.out.println(contacts);

        // case2.2: 2.1的简写
        LambdaEsQueryWrapper<Contact> contactWrapper1 = new LambdaEsQueryWrapper<>();
        // hasParent之所以可以不指定parentType简写是因为框架可以通过@Join注解中指定的父子关系自动推断出其父type,因此用户可以不指定父type直接查询,但hasChild不能简写,因为一个父亲可能有多个孩子,但一个孩子只能有一个亲爹
        contactWrapper1.hasParent(w -> w.eq(FieldUtils.val(Author::getAuthorName), "cat"));
        List<Contact> contacts1 = contactMapper.selectList(contactWrapper1);
        System.out.println(contacts1);

        // case3: parentId查询,返回的是相关的子文档,与case2类似,所以查询用子文档实体及其mapper
        commentWrapper = new LambdaEsQueryWrapper<>();
        commentWrapper.parentId("doc-1", "comment");
        List<Comment> commentList = commentMapper.selectList(commentWrapper);
        System.out.println(commentList);

        contactWrapper = new LambdaEsQueryWrapper<>();
        contactWrapper.parentId("author-2", "contact");
        List<Contact> contactList = contactMapper.selectList(contactWrapper);
        System.out.println(contactList);
    }

    @Test
    @Order(3)
    public void testUpdate() {
        // case1: 父文档/子文档 根据各自的id更新
        Document document = new Document();
        document.setEsId("doc-1");
        document.setTitle("我是隔壁老王标题");
        documentMapper.updateById(FIXED_ROUTING, document);

        Contact contact = new Contact();
        contact.setContactId("contact-2");
        contact.setAddress("update address");
        contactMapper.updateById(FIXED_ROUTING, contact);

        // case2: 父文档/子文档 根据各自条件更新
        Comment comment = new Comment();
        comment.setCommentContent("update comment content");
        LambdaEsUpdateWrapper<Comment> wrapper = new LambdaEsUpdateWrapper<>();
        wrapper.eq(Comment::getCommentContent, "test1");
        wrapper.routing(FIXED_ROUTING);
        commentMapper.update(comment, wrapper);
    }

    @Test
    @Order(4)
    public void testDelete() {
        // case1: 父文档/子文档 根据各自的id删除
        documentMapper.deleteById(FIXED_ROUTING, "doc-1");

        //case2: 父文档/子文档 根据各自条件删除
        LambdaEsQueryWrapper<Comment> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.like(Comment::getCommentContent, "test")
                .routing(FIXED_ROUTING);
        commentMapper.delete(wrapper);
    }

    @Test
    @Order(5)
    public void testDeleteIndex() {
        // 测试完成,删除索引
        boolean deleted = documentMapper.deleteIndex(EntityInfoHelper.getEntityInfo(Document.class).getIndexName());
        Assertions.assertTrue(deleted);
    }

}
