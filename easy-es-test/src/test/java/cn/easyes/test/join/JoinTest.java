package cn.easyes.test.join;

import cn.easyes.common.params.JoinField;
import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.easyes.core.conditions.LambdaEsUpdateWrapper;
import cn.easyes.core.toolkit.FieldUtils;
import cn.easyes.test.TestEasyEsApplication;
import cn.easyes.test.entity.Comment;
import cn.easyes.test.entity.Document;
import cn.easyes.test.mapper.CommentMapper;
import cn.easyes.test.mapper.DocumentMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * 父子类型测试
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Disabled
@SpringBootTest(classes = TestEasyEsApplication.class)
public class JoinTest {
    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private CommentMapper commentMapper;

    @Test
    public void testInsert() {
        // 测试新增父子文档,此处开启自动挡模式,父子类型索引已被自动处理
        // 新新增父文档,然后再插入子文档
        Document document = new Document();
        document.setEsId("1");
        document.setTitle("我是父文档的标题");
        document.setContent("我是父文档的内容");
        JoinField joinField = new JoinField();
        joinField.setName("document");
        document.setJoinField(joinField);
        documentMapper.insert(document);

        // 插入子文档
        Comment comment = new Comment();
        comment.setId("2");
        comment.setCommentContent("我是文档的评论1");

        // 这里特别注意,子文档必须指定其父亲的id,否则找不到爹别怪我没提醒
        joinField.setParent("1");
        joinField.setName("comment");
        comment.setJoinField(joinField);
        commentMapper.insert(comment);

        // 插入子文档2
        Comment comment1 = new Comment();
        comment1.setId("3");
        comment1.setCommentContent("我是文档的评论2");
        comment1.setJoinField(joinField);
        commentMapper.insert(comment1);
    }

    @Test
    public void testSelect() {
        // 温馨提示,下面wrapper中的type实际上就是JoinField字段注解@TableField中指定的parentName和childName,与原生语法是一致的
        // case1: hasChild查询,返回的是相关的父文档 所以查询用父文档实体及其mapper
        LambdaEsQueryWrapper<Document> documentWrapper = new LambdaEsQueryWrapper<>();
        documentWrapper.hasChild("comment", FieldUtils.val(Comment::getCommentContent), "评论");
        List<Document> documents = documentMapper.selectList(documentWrapper);
        System.out.println(documents);

        // case2: hasParent查询,返回的是相关的子文档 所以查询用子文档实体及其mapper
        LambdaEsQueryWrapper<Comment> commentWrapper = new LambdaEsQueryWrapper<>();
        // 字段名称你也可以不用FieldUtils.val,直接传入字符串也行
        commentWrapper.hasParent("document", "content", "内容");
        List<Comment> comments = commentMapper.selectList(commentWrapper);
        System.out.println(comments);

        // case3: parentId查询,返回的是相关的子文档,与case2类似,所以查询用子文档实体及其mapper
        commentWrapper = new LambdaEsQueryWrapper<>();
        commentWrapper.parentId("1", "comment");
        List<Comment> commentList = commentMapper.selectList(commentWrapper);
        System.out.println(commentList);
    }

    @Test
    public void testUpdate() {
        // case1: 父文档/子文档 根据各自的id更新
        Document document = new Document();
        document.setEsId("1");
        document.setTitle("我是隔壁老王标题");
        documentMapper.updateById(document);

        // case2: 父文档/子文档 根据各自条件更新
        Comment comment = new Comment();
        comment.setCommentContent("我是隔壁老王的评论");
        LambdaEsUpdateWrapper<Comment> wrapper = new LambdaEsUpdateWrapper<>();
        wrapper.match(Comment::getCommentContent, "评论");
        commentMapper.update(comment, wrapper);
    }

    @Test
    public void testDelete() {
        // case1: 父文档/子文档 根据各自的id删除
        documentMapper.deleteById("1");

        //case2: 父文档/子文档 根据各自条件删除
        LambdaEsQueryWrapper<Comment> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Comment::getCommentContent, "评论");
        commentMapper.delete(wrapper);
    }

}
