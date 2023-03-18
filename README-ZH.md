<p align="center">
  <a href="https://easy-es.cn/">
   <img alt="East-Es-Logo" src="https://iknow.hs.net/042dd639-5bfa-410f-968f-8bbceb8d8ca7.png">
  </a>
</p>

<p align="center">
  您的Star是我继续前进的动力，如果喜欢EE请右上角帮忙点亮星星⭐!
</p>

<p align="center">
  <a href="https://search.maven.org/search?q=g:io.github.xpc1024%20a:easy-*">
    <img alt="maven" src="https://img.shields.io/github/v/release/xpc1024/easy-es?include_prereleases&logo=xpc&style=plastic">
  </a>
  <a href="https://www.murphysec.com/dr/htY0sMYDQaDn4X8iXp" alt="OSCS Status"><img src="https://www.oscs1024.com/platform/badge/dromara/easy-es.git.svg?size=small"/></a>
  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

# 官方地址 | Official website

**easy-es官网** https://easy-es.cn/

**easy-es官方gitee** https://gitee.com/dromara/easy-es

**easy-es官方github** https://github.com/dromara/easy-es

**开源社区dromara** https://dromara.org/

**开源社区码云首页** https://gitee.com/dromara/

> **Tip:** 官网是vue单页面应用，首次访问加载可能比较慢🐢，主公们请耐心等待一下，后续会很快🏹，如偶遇打不开可刷新多尝试几次.

# 简介 | Intro

Easy-Es是一款简化ElasticSearch搜索引擎操作的开源框架,全自动智能索引托管.

目前功能丰富度和易用度及性能已全面领先SpringData-Elasticsearch.

简化`CRUD`及其它高阶操作,可以更好的帮助开发者减轻开发负担

底层采用Es官方提供的RestHighLevelClient,保证其原生性能及拓展性.

技术讨论 QQ 群 ：729148550 群内可在群文件中免费领取 颈椎保护 | 增肌 | 减脂 等健身计划 无套路

微信群请先添加作者微信,由作者拉入 (亦可咨询健身问题,作者是健身教练)

项目推广初期,还望大家能够不吝点点三连:⭐Star,👀Watch,fork📌

支持一下国产开源,让更多人看到和使用本项目,非常感谢!

# 优点 | Advantages

- **全自动索引托管:** 全球开源首创的索引托管模式,开发者无需关心索引的创建更新及数据迁移等繁琐步骤,索引全生命周期皆可托管给框架,由框架自动完成,过程零停机,用户无感知,彻底解放开发者
- **屏蔽语言差异:** 开发者只需要会MySQL语法即可使用Es
- **代码量极少:** 与直接使用RestHighLevelClient相比,相同的查询平均可以节3-8倍左右的代码量
- **零魔法值:** 字段名称直接从实体中获取,无需输入字段名称字符串这种魔法值
- **零额外学习成本:** 开发者只要会国内最受欢迎的Mybatis-Plus语法,即可无缝迁移至Easy-Es
- **降低开发者门槛:** 即便是只了解ES基础的初学者也可以轻松驾驭ES完成绝大多数需求的开发
- **功能强大:** 支持MySQL的几乎全部功能,且对ES特有的分词,权重,高亮,嵌套,地理位置Geo,Ip地址查询等功能都支持
- **安全可靠:** 墨菲安全扫描零风险,且代码单元测试综合覆盖率高达95%以上.
- **完善的中英文文档:** 提供了中英文双语操作文档,文档全面可靠,帮助您节省更多时间
- **...**

# 对比 | Compare

> 需求:查询出文档标题为 "传统功夫"且作者为"码保国"的所有文档
```java
    // 使用Easy-Es仅需1行代码即可完成查询
    List<Document> documents = documentMapper.selectList(EsWrappers.lambdaQuery(Document.class).eq(Document::getTitle, "传统功夫").eq(Document::getCreator, "码保国"));
```


```java
    // 传统方式, 直接用RestHighLevelClient进行查询 需要19行代码,还不包含下划线转驼峰,自定义字段处理及_id处理等代码
    String indexName = "document";
    SearchRequest searchRequest = new SearchRequest(indexName);
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    TermQueryBuilder titleTerm = QueryBuilders.termQuery("title", "传统功夫");
    TermsQueryBuilder creatorTerm = QueryBuilders.termsQuery("creator", "码保国");
    boolQueryBuilder.must(titleTerm);
    boolQueryBuilder.must(creatorTerm);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(boolQueryBuilder);
    searchRequest.source(searchSourceBuilder);
    try {
         SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
         List<Document> documents = Optional.ofNullable(searchResponse)
                .map(SearchResponse::getHits)
                .map(SearchHits::getHits)
                .map(hit->Document document = JSON.parseObject(hit.getSourceAsString(),Document.class))
                .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
```
> * 以上只是简单查询演示,实际使用场景越复杂,效果就越好,平均可节省至少3-8倍代码量
> * 传统功夫,点到为止! 上述功能仅供演示,仅为Easy-Es支持功能的冰山一角,Easy-Es就是这么Easy到不讲武德💪,不用的请耗子尾汁.

# 架构 | Architecture


![Architecture](https://iknow.hs.net/27fb40b8-22d4-45c2-92e0-1471112d5102.jpg)


# 相关链接 | Links


- [Switch To English](https://gitee.com/easy-es/easy-es/blob/master/README_EN.md)
- [功能示例](https://gitee.com/dromara/easy-es/tree/master/easy-es-sample)
- [Springboot集成Demo](https://www.easy-es.cn/pages/e12389/)

# Latest Version: [![Maven Central](https://img.shields.io/github/v/release/xpc1024/easy-es?include_prereleases&logo=xpc&style=plastic)](https://search.maven.org/search?q=g:io.github.xpc1024%20a:easy-*)

**Maven:**
``` xml
<dependency>
    <groupId>cn.easy-es</groupId>
    <artifactId>easy-es-boot-starter</artifactId>
    <version>Latest Version</version>
</dependency>
```
**Gradle:**
```groovy
compile group: 'cn.easy-es', name: 'easy-es-boot-starter', version: 'Latest Version'
```

# 其他开源项目 | Other Project


- [健身计划一键生成系统](https://gitee.com/easy-es/fit-plan)

# 期望 | Futures


> 欢迎提出更好的意见，帮助完善 Easy-Es

# 版权 | License


[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

# 关注我 | About Me


[CSDN博客](https://blog.csdn.net/lovexiaotaozi?spm=3001.5343)

QQ | 微信:252645816

# 知识星球 | Planet Of Knowledge

<img alt="zsxq" src="https://iknow.hs.net/9038b7ab-c0d9-4a87-9492-e839907a8978.png">

# 捐赠 | Donate


[捐赠记录,感谢你们的支持！](https://easy-es.cn/pages/b52ac5/)

> 您的支持是鼓励我们前行的动力，无论金额多少都足够表达您这份心意。

> 如果您愿意捐赠本项目,推荐直接在右下方通过Gitee直接捐赠.

# 赞助商 | Sponsor


> 如果您想支持我们,奈何囊中羞涩,没事,您可以花30秒借花献佛,点击下方链接进入注册,则该赞助商会代您捐赠一笔小钱给我们社区的秃头猛男们买包辣条,您可以通过微信留言"俺请秃头猛男们吃了包辣条",并提供注册截图,我们会铭记您知恩图报,并将备注您为赞助者,优先答疑.

<a href="http://apifox.cn/a103easyse">
  <img alt="ad" src="https://iknow.hs.net/1a62e467-4f64-40d2-9705-9948a6d49982.png">
</a>
