> ES的参数实际上是以树形数据结构封装的,所以在ES中的AND及OR没有办法做到和MySQL中一致,为了实现和MP几乎一致的语法,作者那糟老头子头发都想没了...

> 好在最后,基本上做到了99%相似度的语法,仍有1%需要各位用户学习一下.

MySQL和ES语法对应关系表格如下
|  MySQL  | ES |
|  ----  | ----  |
| and(条件1,条件2...) | must BoolQueryBuilder |
| or(条件1,条件2...)  | should BoolQueryBuilder |
| or 连接  | should |


> AND 介绍,EE中的AND其实就是将AND括号中的多个查询条件封装进一个BoolQueryBuilder中作为整体,然后该整体与其他参数默认以Must封装,功能类似MySQL中的 AND(条件1,条件2,条件3...)
AND API
```java
and(Consumer<Param> consumer)
and(boolean condition, Consumer<Param> consumer)
```

- AND 
- 例: and(i -> i.eq(Document::getTitle, "Hello").ne(Document::getCreator, "Guy"))--->and (title ='Hello' and creator != 'Guy' )

> OR 介绍,EE中的OR和MP中的OR一样,支持2种,一种是or(),作为连接符,另一种是or(条件1,条件2,条件3).
- 第一种or():用于把or()连接符前面和后面的must条件统统重置为should查询条件
- 第二种or(条件1,条件2,条件3...): 用于将括号中的多个查询条件封装进一个BoolQueryBuilder中作为整体,然后该整体与其它参数默认以Should封装,功能类似MySQL中的OR(条件1,条件2,条件3...)
- 第三种特殊情况,就是第一种or()连接符出现在and(条件1.or().条件2...)或or(条件1.or().条件2...)中,此时or()将must条件重置为should条件的范围仅限于括号内,括号外面的查询条件不受影响.

```java
or()
or(boolean condition)
```

- 拼接 OR**注意事项:**主动调用or表示紧接着下一个**方法**不是用and连接!(不调用or则默认为使用and连接)
- 例: eq("Document::getId",1).or().eq(Document::getTitle,"Hello")--->id = 1 or title ='Hello'

```java
or(Consumer<Param> consumer)
or(boolean condition, Consumer<Param> consumer)
```

- OR 
- 例: or(i -> i.eq(Document::getTitle, "Hello").ne(Document::getCreator, "Guy"))--->or (title ='Hello' and status != 'Guy' )

- 特殊情况
- 例: eq(Document::getTitle,"Hello")
     .and(i->i.eq(Document::getCreator,"Bob").or().eq(Document::getCreator,"Tom"))---> title="Hello" and(creator="Bob" or creator="Tom")

---

除此之外,有一部分使用场景是如下图这样的,所有查询字段,查询类型,匹配规则等都是不固定的,由用户自由来选,这种情况下,采用上面的语法代码会非常难写,不妨使用queryStringQuery API来解决,用它来解决,整个语法就更像MySQL了,而且灵活性和效率都很高.

![image1](https://iknow.hs.net/7bcf189a-053a-48fa-85d6-ef8b763d427a.png)


>前置知识学习:正式进入主题前,我们先来了解下ES的索引,因为有很多小白不懂ES索引,所以这里简单说一下ES的keyword类型和text类型,以免下面踩坑,已经了解的可直接跳过此段介绍.
ES中的keyword类型,和MySQL中的字段基本上差不多,当我们需要对查询字段进行精确匹配,左模糊,右模糊,全模糊,排序聚合等操作时,需要该字段的索引类型为keyword类型,当我们需要对字段进行分词查询时,需要该字段的类型为text类型,并且指定分词器(不指定就用ES默认分词器,效果通常不理想).当同一个字段,我们既需要把它当keyword类型使用,又需要把它当text类型使用时,此时我们的索引类型为keyword_text类型,EE中可以对字段添加注解@TableField(fieldType = FieldType.KEYWORD_TEXT),如此该字段就会被创建为keyword+text双类型如下图所示,值得注意的是,当我们把该字段当做keyword类型查询时,ES要求传入的字段名称为"字段名.keyword",当把该字段当text类型查询时,直接使用原字段名即可.


![image2](https://iknow.hs.net/72818af6-7cc3-4833-b7a7-dbff845ce73e.png)

还需要注意的是,如果一个字段的索引类型被创建为仅为keyword类型(如下图所示)查询时,则不需要在其名称后面追加.keyword,直接查询就行.

![image3](https://iknow.hs.net/87335e55-1fe3-44ed-920b-61354383e85a.png)

---

啰嗦完了,正式进入主题,queryStringQuery API:

```java
queryStringQuery(String queryString);
```

其中queryString字符串就是我们的查询条件,我们可以用StringBuilder把查询字段和值拼接进去,组装成最终的查询语句.
以上图为例,我演示一个场景,请忽略场景合理性,因为是我瞎xx选的:假设我的查询条件是:字段:创建者 等于老王,且创建者分词匹配"隔壁"(比如:隔壁老汉,隔壁老王),或者创建者包含大猪蹄子,对应的代码如下:
```java
    @Test
    public void testQueryString() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        StringBuilder sb = new StringBuilder();
        sb.append("(")
                .append("(")
                .append("creator.keyword")
                .append(":")
                .append("老王")
                .append(")")
                .append("AND")
                .append("(")
                .append("creator")
                .append(":")
                .append("隔壁")
                .append(")")
                .append(")")
                .append("OR")
                .append("(")
                .append("creator.keyword")
                .append(":")
                .append("*大猪蹄子*")
                .append(")");
        // sb最终拼接为:((creator.keyword:老王)AND(creator:隔壁))OR(creator.keyword:*大猪蹄子*) ,可以说和MySQL语法非常相似了
        wrapper.queryStringQuery(sb.toString());
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
前端参数页面每传递一行查询参数,我们往sb中append对应参数就完事了,是不是很简单,没错,但是代码不优雅,可咋整? 老汉已经给你们想好出路了,我们提供了工具类,其全路径为:cn.easyes.core.toolkit.QueryUtils
我们用使用该工具类重构上面的代码,如下:
```java
    @Test
    public void testQueryStringQueryMulti() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String queryStr = QueryUtils.combine(Link.OR,
                QueryUtils.buildQueryString(Document::getCreator, "老王", Query.EQ, Link.AND),
                QueryUtils.buildQueryString(Document::getCreator, "隔壁", Query.MATCH))
                + QueryUtils.buildQueryString(Document::getCreator, "*大猪蹄子*", Query.EQ);
        wrapper.queryStringQuery(queryStr);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
是不是优雅了很多,其中的枚举Query和Link我也已经为你们封装好了,直接使用即可,不懂其枚举含义也可以直接点开查看,我在源码中有详细注释.