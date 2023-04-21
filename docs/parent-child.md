> **前言:** ES底层是Lucene,由于Lucene实际上是不支持嵌套类型的,所有文档都是以扁平的结构存储在Lucene中,ES对父子文档的支持,实际上也是采取了一种投机取巧的方式实现的.

> 父子文档均以独立的文档存入,然后添加关联关系,且父子文档必须在同一分片,由于父子类型文档并没有减少文档数量,而且增加了父子绑定关系,会导致查询效率低下,因此我们并不建议您在实际开发中使用父子类型.

> ES本身更适合"大宽表"模式,不要带着传统关系型数据库那种思维方式去使用ES,我们完全可以通过把多张表中的字段和内容合并到一张表(一个索引)中,来完成期望功能,尽可能规避父子类型的使用,不仅效率高,功能也更强大.

> 当然存在即合理,也确实有个别场景下,不可避免的会用到父子类型,作为全球首屈一指的ES-ORM框架,我们对此也提供了支持,用户可以不用,但我们不能没有!

> 关于父子类型和嵌套类型的选择:如果对文档的写多于读,那么建议你选择父子类型,如果文档读多于写, 那么请选择嵌套类型.


# 1.父子类型使用步骤
## 1.1创建索引

- 自动挡模式:
```java
    /**
     * 父文档
     */
    @TableName(childClass = Comment.class)
    public class Document{
        // 省略其它字段...
       /**
        * 须通过注解在父文档及子文档的实体类中指明其类型为Join,及其父名称和子名称,这里的JoinField类框架已内置,无需重复造轮子
        * JoinField类全路径为cn.easyes.common.params.JoinField,如果你非要自己造轮子,也支持,那么需要在@TableField注解中指明joinFieldClass=你造的轮子
        */
        @TableField(fieldType = FieldType.JOIN, parentName = "document", childName = "comment")
        private JoinField joinField;
    }
    /**
     * 子文档
     */
    @TableName(child = true)
    public class Comment {
        // 省略其它字段...
        /**
         * 父子关系字段 须通过注解在父文档及子文档的实体类中指明其类型为Join,子文档中的父子关系可省略
        */
        @TableField(fieldType = FieldType.JOIN)
        private JoinField joinField;
    }
```
>  **注意:** 务必像上面示例一样,在父文档的类上加注解@TableName指明其子文档类,在子文档的类上加注解@TableName,指明child=true,并在JoinField类的@TableField注解中指定类型为fieldType=JOIN及其parentName,childName,否则会导致框架无法正常工作

- 手动挡模式
 - 方式一:
按照自动挡模式,配置好注解,然后直接调用一键生成API生成索引 (v0.9.30+ 版本支持)

```java
documentMapper.createIndex();
```
 - 方式二:
纯手工打造,所有字段自己安排一遍,不推荐,麻烦得很
```java
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        // 省略其它代码
        wrapper.join("joinField", "document", "comment");
```
>  **注意:** 在手动挡模式下,主类上的注解依然不能少,框架运行时需要用到父子关系,在方式二还需要通过wrapper指定该嵌套字段,然后完成索引创建/更新


## 1.2 CRUD
注意父子类型由于都是独立的文档,独立的实体类,所以各自都需要有各自的mapper
CRUD示例:
```java
    @Test
    public void testInsert() {
        // 测试新增父子文档,此处开启自动挡模式,父子类型索引已被自动处理
        // 新新增父文档,然后再插入子文档
        Document document = new Document();
        document.setId("1");
        document.setTitle("父文档的标题");
        document.setContent("父文档的内容");
        JoinField joinField = new JoinField();
        joinField.setName("document");
        document.setJoinField(joinField);
        documentMapper.insert(document);

        // 插入子文档
        Comment comment = new Comment();
        comment.setId("2");
        comment.setCommentContent("文档的评论1");

        // 这里特别注意,子文档必须指定其父文档的id,否则找不到父文档别怪我没提醒
        joinField.setParent("1");
        joinField.setName("comment");
        comment.setJoinField(joinField);
        commentMapper.insert(comment);

        // 插入子文档2
        Comment comment1 = new Comment();
        comment1.setId("3");
        comment1.setCommentContent("文档的评论2");
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
        document.setId("1");
        document.setTitle("父标题");
        documentMapper.updateById(document);

        // case2: 父文档/子文档 根据各自条件更新
        Comment comment = new Comment();
        comment.setCommentContent("更新后的评论");
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
```
相关demo可参考源码的test模块->test目录->join包