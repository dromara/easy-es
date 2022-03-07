<p align="center">
  <a href="https://www.yuque.com/laohan-14b9d/foyrfa/naw1ie">
   <img alt="East-Es-Logo" src="https://iknow.hs.net/6361ec1d-edca-4358-98c1-e7a309e15a39.png">
  </a>
</p>

<p align="center">
  为简化开发工作、提高生产效率而生
</p>

<p align="center">
  <a href="https://search.maven.org/search?q=g:io.github.xpc1024%20a:easy-*">
    <img alt="maven" src="https://img.shields.io/github/v/release/xpc1024/easy-es?include_prereleases&logo=xpc&style=plastic">
  </a>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

# 简介 | Intro

Easy-Es是一款简化ElasticSearch搜索引擎操作的开源框架,简化`CRUD`操作,可以更好的帮助开发者减轻开发负担

底层采用Es官方提供的RestHighLevelClient,保证其原生性能及拓展性.

技术讨论 QQ 群 ：247637156 

微信群请先添加作者微信,由作者拉入 (亦可咨询健身问题,作者是健身教练)

项目推广初期,还望大家能够不吝点点三连:⭐Star,👀Watch,fork📌,支持一下国产开源,让更多人看到和使用本项目,感谢!

# 优点 | Advantages

- **屏蔽语言差异:** 开发者只需要会MySQL语法即可使用Es

- **代码量极少:** 与直接使用RestHighLevelClient相比,相同的查询平均可以节3-5倍左右的代码量
- **零魔法值:** 字段名称直接从实体中获取,无需输入字段名称字符串这种魔法值
- **零额外学习成本:** 开发者只要会国内最受欢迎的Mybatis-Plus语法,即可无缝迁移至Easy-Es
- **降低开发者门槛:** 即便是只了解ES基础的初学者也可以轻松驾驭ES完成绝大多数需求的开发
- **功能强大:** 支持MySQL的几乎全部功能,且对ES特有的分词,权重,高亮,地理位置Geo等功能都支持
- **完善的中英文文档:** 提供了中英文双语操作文档,文档全面可靠,帮助您节省更多时间
- **...**

## 对比 | Compare
> 需求:查询出文档标题为 "中国功夫"且作者为"老汉"的所有文档
```java
// 使用Easy-Es仅需3行代码即可完成查询
LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
wrapper.eq(Document::getTitle, "中国功夫").eq(Document::getCreator, "老汉");
List<Document> documents = documentMapper.selectList(wrapper);
```

```java
// 传统方式, 直接用RestHighLevelClient进行查询 需要11行代码,还不包含解析JSON代码
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
> * 以上只是简单查询演示,实际使用场景越复杂,效果就越好,平均可节省3-5倍代码量
> * 上述功能仅供演示,仅为Easy-Es支持功能的冰山一角

## 相关链接 | Links

- [Switch To English](https://github.com/xpc1024/easy-es/blob/main/README.md)
- [文档](https://www.yuque.com/laohan-14b9d/foyrfa/naw1ie)
- [功能示例](https://github.com/xpc1024/easy-es/tree/main/easy-es-sample)
- [Springboot集成Demo](https://www.yuque.com/laohan-14b9d/foyrfa/pbo22k)

# Latest Version: [![Maven Central](https://img.shields.io/github/v/release/xpc1024/easy-es?include_prereleases&logo=xpc&style=plastic)](https://search.maven.org/search?q=g:io.github.xpc1024%20a:easy-*)

**Maven:**
``` xml
<dependency>
    <groupId>io.github.xpc1024</groupId>
    <artifactId>easy-es-boot-starter</artifactId>
    <version>Latest Version</version>
</dependency>
```
**Gradle:**
```groovy
compile group: 'io.github.xpc1024', name: 'easy-es-boot-starter', version: 'Latest Version'
```

# 其他开源项目 | Other Project

- [健身计划一键生成系统](https://github.com/xpc1024/plan-all)

# 期望 | Futures

> 欢迎提出更好的意见，帮助完善 Easy-Es

# 版权 | License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

# 捐赠 | Donate

[捐赠记录,感谢你们的支持！](https://www.yuque.com/laohan-14b9d/foyrfa/ipxxr2)

[捐赠 Easy-Es](https://www.yuque.com/laohan-14b9d/foyrfa/wn1iha)

# 关注我 | About Me

[CSDN博客](https://blog.csdn.net/lovexiaotaozi?spm=3001.5343)

QQ | 微信:252645816
