# [Easy-Es](https://www.yuque.com/laohan-14b9d/foyrfa/naw1ie)
---
![Easy-Es-Logo](http://wmb830.bvimg.com/13869/cee1aea7ab269a00.png)
---

## Easy-Es是什么?
---

Easy-Es是一款简化ElasticSearch(后面简称为Es)搜索引擎操作的开源框架,可以更好的帮助开发者减轻开发负担.

## 优势
---

 **屏蔽语言差异：**开发者只需要会MySQL语法即可使用Es
+ **低码:** 与直接使用RestHighLevelClient相比,相同的查询平均可以节省2-3倍左右的代码量
+ **零魔法值:**字段名称直接从实体中获取,无需输入字段名称字符串这种魔法值
+ **零额外学习成本：**开发者只要会国内最受欢迎的Mybatis-Plus语法,即可无缝迁移至Easy-Es
+ **降低开发者门槛:** 即便是只了解ES基础的初学者也可以轻松驾驭ES完成绝大多数需求的开发
+ **...**

## 完整文档
---
[Easy-Es开发者文档](https://www.yuque.com/laohan-14b9d/foyrfa/qelho0)

## 对比
---
> 需求:查询出文档标题为 "中国功夫"且作者为"老汉"的所有文档

```
// 使用Easy-Es仅需3行代码即可完成查询
LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
wrapper.eq(Document::getTitle, "中国功夫").eq(Document::getCreator, "老汉");
List<Document> documents = documentMapper.selectList(wrapper);
		
```

```
// 传统方式, 直接用RestHighLevelClient进行查询 需要11行代码,还不包含解析代码
String indexName = "document";
SearchRequest searchRequest = new SearchRequest(indexName);
BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
TermQueryBuilder titleTerm = QueryBuilders.termQuery("title", "中国功夫");
TermsQueryBuilder creatorTerm = QueryBuilders.termsQuery("creator", "老汉");
boolQueryBuilder.must(titleTerm);
boolQueryBuilder.must(creatorTerm);
SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
searchSourceBuilder.query(boolQueryBuilder);
searchRequest.source(searchSourceBuilder);
try {
    SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    // 然后从searchResponse中通过各种方式解析出DocumentList 省略这些代码...
    } catch (IOException e) {
            e.printStackTrace();
    }
```

## 快速开始
---
+ 添加Easy-Es依赖

```
        <dependency>
            <groupId>com.github.xpc1024</groupId>
            <artifactId>easy-es-boot-stater</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```
+ 编写Mapper继承父类BaseEsMapper接口

```
public interface DocumentMapper extends BaseEsMapper<Document> {
}

```
+ 使用
> 以下语句将会查询出文档标题为"xxx"的所有文档,相当于MySQL中的:
select * from document where title = "xxx";
```
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle,"xxx");
        List<Document> documents = documentMapper.selectList(wrapper);
```

## 协议
---
Easy-Es采用 [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0)开源协议
