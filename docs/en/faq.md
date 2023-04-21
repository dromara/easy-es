> Since EE has not been used by a large number of users, there is no FAQ for the time being, and it will continue to be updated in the future. Here are some common questions I encountered that may be asked

1. During the trial process, an error was reported: java.lang.reflect.UndeclaredThrowableException
```
Caused by: [daily_document] ElasticsearchStatusException[Elasticsearch exception [type=index_not_found_exception, reason=no such index [daily_document]]]
```
If your error message and cause are consistent with the above, please check whether the index name is correctly configured, check the global configuration, and annotate the configuration. If the configuration is correct, it may be that the index does not exist. You can check whether the specified index already exists through the es-head visualization tool If there is no such index, it can be quickly created through the API provided by EE.

2. Dependence conflict

Although the EE framework is light enough and I try to avoid using too many other dependencies during the development process, it is still difficult to guarantee that dependency conflicts with the host project will occur with a very small probability. If there is a dependency conflict, the developer can use the migration Except for repetitive dependencies or unified version numbers to resolve, all EE dependencies that may conflict are as follows:
```xml
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
          	<version>1.18.12</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>7.10.1</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch</groupId>
            <artifactId>elasticsearch</artifactId>
            <version>7.10.1</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.79</version>
        </dependency>
        <dependency>
             <groupId>commons-codec</groupId>
             <artifactId>commons-codec</artifactId>
             <version>1.6</version>
        </dependency>
```
