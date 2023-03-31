<p align="center">
  <a href="https://github.com/xpc1024/easy-es">
   <img alt="East-Es-Logo" src="https://iknow.hs.net/6361ec1d-edca-4358-98c1-e7a309e15a39.png">
  </a>
</p>

<p align="center">
  Born To Simplify Development
</p>

<p align="center">
  <a href="https://search.maven.org/search?q=g:io.github.xpc1024%20a:easy-*">
    <img alt="maven" src="https://img.shields.io/github/v/release/xpc1024/easy-es?include_prereleases&logo=xpc&style=plastic">
  </a>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

## What is Easy-Es?

Easy-Es is a powerfully enhanced toolkit of RestHighLevelClient for simplify development. This toolkit provides some efficient, useful, out-of-the-box features for ElasticSearch. By using Easy-Es, you can use MySQL syntax to complete Es queries. Use it can effectively save your development time.

## Official website

https://easy-es.cn/#/en/

## Links
- [中文版](https://github.com/xpc1024/easy-es/blob/main/README-ZH.md)
- [Documentation](https://www.yuque.com/laohan-14b9d/tald79/qf7ns2)
- [Samples](https://github.com/xpc1024/easy-es/tree/main/easy-es-sample)
- [Demo in Springboot](https://easy-es.cn/#/en/demo)

## Features

-   Auto configuration on startup
-   Out-of-the-box interfaces for operate es
-   Powerful and flexible where condition wrapper
-   Lambda-style API
-   Automatic paging operation
-   Support high-level syntax such as highlighting and weighting and Geo etc
-   ...

## Compare

> Demand: Query all documents with title equals "Hi" and author equals "Guy"



```java
// Use Easy-Es to complete the query with only 3 lines of code
LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
wrapper.eq(Document::getTitle, "Hi").eq(Document::getCreator, "Guy");
List<Document> documents = documentMapper.selectList(wrapper);
```

```java
// Query with RestHighLevelClient requires 11 lines of code, not including parsing JSON code
String indexName = "document";
SearchRequest searchRequest = new SearchRequest(indexName);
BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
TermQueryBuilder titleTerm = QueryBuilders.termQuery("title", "Hi");
TermsQueryBuilder creatorTerm = QueryBuilders.termsQuery("creator", "Guy");
boolQueryBuilder.must(titleTerm);
boolQueryBuilder.must(creatorTerm);
SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
searchSourceBuilder.query(boolQueryBuilder);
searchRequest.source(searchSourceBuilder);
try {
    SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    // Then parse the DocumentList from searchResponse in various ways, omitting these codes...
    } catch (IOException e) {
            e.printStackTrace();
    }
```

> The above is just a simple query demonstration. The more complex the actual query scene, the better the effect, which can save 3-5 times the amount of code on average.
## Getting started

-   Add Easy-Es dependency
    - Latest Version: [![Maven Central](https://img.shields.io/github/v/release/xpc1024/easy-es?include_prereleases&logo=xpc&style=plastic)](https://search.maven.org/search?q=g:io.github.xpc1024%20a:easy-*)
    - Maven:
      ```xml
      <dependency>
        <groupId>cn.easy-es</groupId>
        <artifactId>easy-es-boot-starter</artifactId>
        <version>Latest Version</version>
      </dependency>
      ```
    - Gradle
      ```groovy
      compile group: 'io.github.xpc1024', name: 'easy-es-boot-starter', version: 'Latest Version'
      ```
-   Add mapper file extends BaseEsMapper interface

    ```java
    public interface DocumentMapper extends BaseMapper<Document> {
    }
    ```

- Use it
  ``` java
  LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
  wrapper.eq(Document::getTitle,"Hello World")
         .eq(Document::getCreator,"Guy");
  List<Document> documentList = documentMapper.selectList();
  
  ```
    Easy-Es will execute the following Query:
    ```json
    {"query":{"bool":{"must":[{"term":{"title":{"value":"Hello World","boost":1.0}}},{"term":{"creator":{"value":"Guy","boost":1.0}}}],"adjust_pure_negative":true,"boost":1.0}}}
    ```
  
  The syntax of this query in MySQL is:
  ```sql
   SELECT * FROM document WHERE title = 'Hello World' AND creator = 'Guy'
  ```
  
> This showcase is just a small part of Easy-Es features. If you want to learn more, please refer to the [documentation](https://www.yuque.com/laohan-14b9d/tald79/qf7ns2).

## SUPPORT

In the early stage of project promotion, I hope everyone can give a little bit of three links: ⭐Star, 👀Watch, fork📌, support the spirit of open source, let more people see 
and use this project, thank you very much!


## Syntax comparison with MySQL
|  MySQL   | Easy-Es  |
|  ----  | ----  |
| and  | and |
| or | or |
| = | eq |
| != | ne|
| &gt; | gt |
| >= | ge |
| &lt; | lt |
| <= | le |
| like '%field%' | like |
| not like '%field%' |notLike|
| like '%field' | likeLeft|
| like 'field%' | likeRight |
| between | between |
| notBetween | notBetween |
| is null | isNull |
| is notNull | isNotNull |
| in | in |
| not in | notIn |
| group by | groupBy |
| order by | orderBy |
|min |min |
|max |max |
|avg |avg |
|sum |sum |
|sum |sum |
| - | orderByAsc |
| - | orderByDesc |
| - | match |
|- |highLight |
| ... | ... |

---

## Donate
[Donate Easy-Es](https://easy-es.cn/#/en/donate)


## License

Easy-Es is under the Apache 2.0 license. See the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0) file for details.
