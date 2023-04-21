We will use a simple Demo to illustrate the powerful functions of Easy-Es. Before that, we assume that you have

● Have a Java development environment and corresponding IDE

● Familiar with MySQL

● Familiar with Spring Boot

● Familiar with Maven

● Understand the basic concepts of Es

● Es is installed Recommended version 7.x+  (you can search for tutorials on Google if you don't have it, it is recommended to install another [es-head plugin](https://github.com/mobz/elasticsearch-head))

> **TIP**
> If you are too lazy to read the following tutorials, you can also download the [Springboo integrated demo](/en/demo.md) and run it directly

## Initialize the project

---

Create an empty Spring Boot project
> **TIP**
> You can use [Spring Initializer](https://start.spring.io/) to quickly initialize a Spring Boot project

## Add dependency

---

**Maven:**
```xml
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
> **Tips:** Latest Version: [Click here to get the latest version](https://img.shields.io/github/v/release/xpc1024/easy-es?include_prereleases&logo=xpc&style=plastic)

## Configuration

---

Add the necessary configuration of EasyEs in the application.yml configuration file:
```yaml
easy-es:
  eanble: true # The default value is true, If the value of enable is false, it is considered that Easy-es is not enabled
  address: 127.0.0.0:9200 # Your elasticsearch address,must contains port, If it is a cluster, please separate with',' just like this: 127.0.0.0:9200,127.0.0.1:9200
  username: elastic # Es username, Not necessary, If it is not set in your elasticsearch, delete this line
  password: WG7WVmuNMtM4GwNYkyWH # Es password, Not necessary, If it is not set, delete this line
```
> Other configurations can be omitted temporarily, the following chapters will introduce the configuration of EasyEs in detail

Add the @EsMapperScan annotation to the Spring Boot startup class to scan the Mapper folder:
```java
@SpringBootApplication
@EsMapperScan("com.xpc.easyes.sample.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```
## backgrounds

---

There is currently a Document table. As the amount of data expands, its query efficiency can no longer meet product requirements. The table structure is as follows. We plan to migrate the content of this table to the Elasticsearch to improve query efficiency.

| id | title | content |
| --- | --- | --- |

## coding

---

Write the entity class Document.java ([Lombok](https://www.projectlombok.org/) is used here to simplify the code)
```java
@Data
public class Document {
    /**
     * just like the primary key in MySQL
     */
    private String id;

    /**
     * title of document
     */
    private String title;
    /**
     * content of document
     */
    // @TableField(fieldType=FieldType.TEXT,analyzer=Analyzer.STANDARD,searchAnalyzer=Analyzer.STANDARD)
    private String content;

    /**
     * creator of document
     */
    private String creator;
}
```
> **Tips:**
> The above field names and underscores are converted to automatic hump, the storage type of the field in ES, the tokenizer, etc. can be configured, which will be introduced in the subsequent chapters.
> The String type will be created as a keyword type by EE by default, and the keyword type supports precise query, etc.
> If you need to query by word segmentation, you can add @TableField annotation to the field and specify that the field type is text, and specify the tokenizer like the content above.

Write the Mapper class DocumentMapper.java
```java
public interface DocumentMapper extends BaseEsMapper<Document> {
}
```
**Pre-operation:** Start the project, and Easy-Es will automatically create an index for you (equivalent to a table in a database such as MySQL). Only with an index can subsequent CRUD operations be performed. After the index is successfully managed, you can see it in the console:= ==> Congratulations auto process index by Easy-Es is done !
> Tips:
> ● If the index is updated, index rebuild, update, data migration and other tasks will be automatically completed by EE by default, of course, you can also disable the automatic hosting of the index through configuration, manual maintenance through the API provided by EE or plug-ins such as es-head maintain.
> ● Automatic hosting mode (supported in version 0.9.9+), the related configuration and detailed introduction can be seen in the following chapters, here you can just hand over these annoying steps to EE for automatic processing.
> ● If your EE version is lower than this version, you can manually maintain the index through the API provided by EE

## Get started (CRUD)

---

Add test class to perform functional test:

> **Test insert:** Add a new piece of data (equivalent to the Insert operation in MySQL)

```java
    @Test
    public void testInsert() {
        // Test insert data
        Document document = new Document();
        document.setTitle("Hello");
        document.setContent("World");
        document.setCreator("Guy")
        String id = documentMapper.insert(document);
        System.out.println(id);
    }
```
> Test query: query the specified data according to the conditions (equivalent to the Select operation in MySQL)

```java
    @Test
    public void testSelect() {
        // test query
        String title = "Hello";
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle,title);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);
        Assert.assertEquals(title,document.getTitle());
    }
```
> Test update: update data (equivalent to the Update operation in MySQL)

```java
    @Test
    public void testUpdate() {
        // There are two cases of update, which are demonstrated as follows:
        // case1: Known id, update according to id (for the convenience of demonstration, this id is copied from the query in the previous step, the actual business can be queried by yourself)
        String id = "krkvN30BUP1SGucenZQ9";
        String title1 = "Nice";
        Document document1 = new Document();
        document1.setId(id);
        document1.setTitle(title1);
        documentMapper.updateById(document1);

        // case2: id unknown, update according to conditions
        LambdaEsUpdateWrapper<Document> wrapper = new LambdaEsUpdateWrapper<>();
        wrapper.eq(Document::getTitle,title1);
        Document document2 = new Document();
        document2.setTitle("Bad");
        documentMapper.update(document2,wrapper);
    }
```
Through the update, the data title is first updated to Nice, and finally updated to Bad.

> Test delete: delete data (equivalent to the Delete operation in MySQL)

```java
    @Test
    public void testDelete() {
        // There are two cases for deletion: delete according to id or delete according to conditions
        // Considering that id deletion is too simple, here is only a demonstration of deletion based on conditions
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "Bad";
        wrapper.eq(Document::getTitle,title);
        int successCount = documentMapper.delete(wrapper);
        System.out.println(successCount);
    }
```
> **TIP**
> Please move to the complete code sample: [Easy-Es-Sample](https://github.com/xpc1024/easy-es/tree/main/easy-es-sample) (test directory)
> We are sorry that currently only the Chinese version is provided, but the code itself is the same

## summary

---

Through the above few simple steps, we have realized Document index creation and CRUD function

From the above steps, we can see that integrating Easy-Es is very simple, just import the starter project and configure the mapper scan path.

But the power of Easy-Es is far more than these functions. Want to learn more about the powerful functions of Easy-Es? Then continue browsing!
