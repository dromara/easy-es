> **Tip**
> 如果您用过Mybatis-Plus的话,您基本上可以无需多看此文档即可直接上手使用
> Easy-Es是Mybatis-Plus在Elastic Search的平替版.


我们将通过一个简单的 Demo 来阐述 Easy-Es 的强大功能，在此之前，我们假设您已经：

- 拥有 Java 开发环境以及相应 IDE
- 熟悉MySQL
- 熟悉 Spring Boot (推荐版本2.5.x +)
- 熟悉 Maven
- 了解Es基本概念
- 已安装Es **推荐7.x+版本**(没有安装的可自行百度教程,建议再装一个es-head插件,便于可视化验证),低版本可能存在API不兼容或其它未知情况,因为底层采用RestHighLevelClient而非RestLowLevelClient,本Demo采用Es版本为7.10.0

> **TIP**
> 如果您懒得看下述教程,也可以下载[Springboo集成demo](demo.md)直接运行

## 初始化工程

---

创建一个空的 Spring Boot 工程
> **TIP**
> 可以使用 [Spring Initializer](https://start.spring.io/)快速初始化一个 Spring Boot 工程

## 
## 添加依赖

---

**Maven:**
```xml
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
> **Tips:** Latest Version: [点此获取](https://img.shields.io/github/v/release/xpc1024/easy-es?include_prereleases&logo=xpc&style=plastic)


## 配置

---

在 application.yml 配置文件中添加EasyEs必须的相关配置：
```yaml
easy-es:
  enable: true #默认为true,若为false则认为不启用本框架
  address : 127.0.0.1:9200 # es的连接地址,必须含端口 若为集群,则可以用逗号隔开 例如:127.0.0.1:9200,127.0.0.2:9200
  username: elastic #若无 则可省略此行配置
  password: WG7WVmuNMtM4GwNYkyWH #若无 则可省略此行配置
```
其它配置暂可省略,后面有章节详细介绍EasyEs的配置

在 Spring Boot 启动类中添加 @EsMapperScan 注解，扫描 Mapper 文件夹：
```java
@SpringBootApplication
@EsMapperScan("com.xpc.easyes.sample.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```
## 背景

---

现有一张Document文档表，随着数据量膨胀,其查询效率已经无法满足产品需求,其表结构如下,我们打算将此表内容迁移至Es搜索引擎,提高查询效率

| id | title | content |
| --- | --- | --- |
| 主键 | 标题 | 内容 |

## 编码

---

编写实体类Document.java（此处使用了 [Lombok](https://www.projectlombok.org/)简化代码）
```java
@Data
public class Document {
    /**
     * es中的唯一id
     */	
    private String id;
    /**
     * 文档标题
     */
    private String title;
    /**
     * 文档内容
     */
    private String content;
}
```
> Tips:
> - 上面字段名称以及下划线转自动驼峰,字段在ES中的存储类型,分词器等均可配置,在后续章节会有介绍.
> - String类型默认会被EE创建为keyword类型,keyword类型支持精确查询等
> - 如需分词查询,可像上面content一样,在字段上加上@TableField注解并指明字段类型为text,并指定分词器.

编写Mapper类 DocumentMapper.java
```java
public interface DocumentMapper extends BaseEsMapper<Document> {
}
```
## 
**前置操作:**启动项目,由Easy-Es自动帮您创建索引(相当于MySQL等数据库中的表),有了索引才能进行后续CRUD操作.索引托管成功后,您可在控制台看到:===> Congratulations auto process index by Easy-Es is done !
> **Tips:**
> - 后续如若索引有更新,索引重建,更新,数据迁移等工作默认都由EE自动帮您完成,当然您也可以通过配置关闭索引自动托管,可通过EE提供的API手动维护或es-head等插件维护.
> - 自动托管模式(0.9.9+版本支持),相关配置及详细介绍可在后面章节中看到,此处您只管将这些烦人的步骤交给EE去自动处理即可.
> - 若您EE版本低于该版本,可通过EE提供的API手动维护索引

## 开始使用(CRUD)

---

添加测试类，进行功能测试：

> 测试新增: 新增一条数据(相当于MySQL中的Insert操作)

```java
    @Test
    public void testInsert() {
        // 测试插入数据
        Document document = new Document();
        document.setTitle("老汉");
        document.setContent("推*技术过硬");
        String id = documentMapper.insert(document);
        System.out.println(id);
    }
```
> 测试查询:根据条件查询指定数据(相当于MySQL中的Select操作)

```java
    @Test
    public void testSelect() {
        // 测试查询
        String title = "老汉";
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle,title);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);
        Assert.assertEquals(title,document.getTitle());
    }
```
> 测试更新:更新数据(相当于MySQL中的Update操作)

```java
    @Test
    public void testUpdate() {
        // 测试更新 更新有两种情况 分别演示如下:
        // case1: 已知id, 根据id更新 (为了演示方便,此id是从上一步查询中复制过来的,实际业务可以自行查询)
        String id = "krkvN30BUP1SGucenZQ9";
        String title1 = "隔壁老王";
        Document document1 = new Document();
        document1.setId(id);
        document1.setTitle(title1);
        documentMapper.updateById(document1);

        // case2: id未知, 根据条件更新
        LambdaEsUpdateWrapper<Document> wrapper = new LambdaEsUpdateWrapper<>();
        wrapper.eq(Document::getTitle,title1);
        Document document2 = new Document();
        document2.setTitle("隔壁老李");
        document2.setContent("推*技术过软");
        documentMapper.update(document2,wrapper);

        // 关于case2 还有另一种省略实体的简单写法,这里不演示,后面章节有介绍,语法与MP一致
    }
```
经过一顿猛如虎的更新操作 我们来看看标题最终变成了什么?

![image.png](https://iknow.hs.net/bdb9bbeb-70e2-46ac-9229-3a36f1001987.png)

查询结果证实了我们更新没有问题,这里无意冒犯"老李",仅供演示,如有得罪,请海涵.

> 测试删除:删除数据(相当于MySQL中的Delete操作)

```java
    @Test
    public void testDelete() {
        // 测试删除数据 删除有两种情况:根据id删或根据条件删
        // 鉴于根据id删过于简单,这里仅演示根据条件删,以老李的名义删,让老李心理平衡些
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "隔壁老李";
        wrapper.eq(Document::getTitle,title);
        int successCount = documentMapper.delete(wrapper);
        System.out.println(successCount);
    }
```
> **TIP**
> 下面完整的代码示例请移步：[Easy-Es-Sample](https://gitee.com/easy-es/easy-es/tree/master/easy-es-sample/src/test/java/com/xpc/easyes/sample/test)

## 小结

---

通过以上几个简单的步骤，我们就实现了 Document的索引创建及CRUD 功能,最终也帮老李洗白了.
从以上步骤中，我们可以看到集成Easy-Es非常的简单，只需要引入 starter 工程，并配置 mapper 扫描路径即可。
但Easy-Es 的强大远不止这些功能，想要详细了解 Easy-Es 的强大功能？那就继续往下看吧！
