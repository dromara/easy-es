> **前言:** ES底层是Lucene,由于Lucene实际上是不支持嵌套类型的,所有文档都是以扁平的结构存储在Lucene中,ES对嵌套文档的支持,实际上也是采取了一种投机取巧的方式实现的.

> 嵌套的文档均以独立的文档存入,然后添加关联关系,这就会导致,一条嵌套类型的文档,底层实际上存储了N条数据,而且更新时会株连九族式更新,导致效率低下,而且对于嵌套类型,其查询功能也受限,不支持聚合排序等功能,因此我们并不建议您在实际开发中使用这种类型.

> ES本身更适合"大宽表"模式,不要带着传统关系型数据库那种思维方式去使用ES,我们完全可以通过把多张表中的字段和内容合并到一张表(一个索引)中,来完成期望功能,尽可能规避嵌套类型的使用,不仅效率高,功能也更强大.

> 当然存在即合理,也确实有个别场景下,不可避免的会用到嵌套类型,作为全球首屈一指的ES-ORM框架,我们对此也提供了支持,用户可以不用,但我们不能没有!


# 1.嵌套类型使用步骤
## 1.1创建索引

- 自动挡模式:
```java
    public class Document{
        // 省略其它字段...
        /**
         * 嵌套类型 
         */
        @TableField(fieldType = FieldType.NESTED, nestedClass = User.class)
        private List<User> users;
    }
```
>  **注意:** 务必像上面示例一样指定类型为fieldType=NESTED及其nestedClass,否则会导致框架无法正常运行

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
        wrapper.mapping(Document::getUsers, FieldType.NESTED)
```
>  **注意:** 在手动挡模式下,除了要通过注解@TableField指定nestedClass外,还需要通过wrapper指定该嵌套字段,然后完成索引创建/更新


## 1.2 CRUD
其中增删改与非嵌套类型使用无差异,这里不赘述
查询示例:
```java
    @Test
    public void testNestedMatch() {
        // 嵌套查询 查询内容匹配人才且嵌套数据中用户名匹配"用户1"的数据
        // 其中嵌套类的字段名称获取我们提供了工具类FieldUtils.val帮助用户通过lambda函数式获取字段名称,当然如果不想用也可以直接传字符串
        EsWrappers.lambdaQuery(Document.class)
            .match(Document::getContent, "人才");
            .nestedMatch(Document::getUsers, FieldUtils.val(User::getUsername), "用户");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
相关demo可参考源码的test模块->test目录->nested包