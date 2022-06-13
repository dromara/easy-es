> **Foreword:** The bottom layer of ES is Lucene. Since Lucene does not actually support nested types, all documents are stored in Lucene in a flat structure. ES's support for nested documents actually adopts It's done in an opportunistic way.

> Nested documents are stored as independent documents, and then add associations, which will lead to a nested type of document, the bottom layer actually stores N pieces of data, and the update will be linked to nine types of updates, resulting in efficiency Low, and for nested types, its query function is also limited, and does not support functions such as aggregation sorting, so we do not recommend that you use this type in actual development.

> ES itself is more suitable for the "large wide table" mode. Don't use ES with the way of thinking of traditional relational databases. We can completely combine the fields and contents of multiple tables into one table (an index). Complete the desired function and avoid the use of nested types as much as possible, which is not only efficient, but also more powerful.

> Of course, it is reasonable to exist, and there are indeed some scenarios where nested types are inevitably used. As the world's leading ES-ORM framework, we also provide support for this, users can not use it, but we can't live without it!


# 1. Steps for using nested types
## 1.1 Create index

- Automatic transmission mode:
````java
    public class Document{
        // Omit other fields...
        /**
         * Nested type
         */
        @TableField(fieldType = FieldType.NESTED, nestedClass = User.class)
        private List<User> users;
    }
````
> **Note:** Be sure to specify the type as fieldType=NESTED and its nestedClass as in the above example, otherwise the framework will not work properly

- Manual transmission mode
 - method one:
According to the automatic mode, configure the annotation, and then directly call the one-click generation API to generate the index (v0.9.30+ version support)

````java
documentMapper.createIndex();
````
 - Method 2:
Purely handmade, all fields are arranged by yourself, not recommended, very troublesome
````java
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        // omit other code
        wrapper.mapping(Document::getUsers, FieldType.NESTED)
````
> **Note:** In manual mode, in addition to specifying the nestedClass through the annotation @TableField, you also need to specify the nested field through the wrapper, and then complete the index creation/update


## 1.2 CRUD
Among them, there is no difference between additions and deletions and the use of non-nested types, so I won't go into details here.
Query example:
````java
    @Test
    public void testNestedMatch() {
        // Nested query The query content matches the talent and the user name in the nested data matches the data of "User 1"
        // To get the field name of the nested class, we provide the tool class FieldUtils.val to help users get the field name through the lambda function. Of course, if you don't want to use it, you can also pass a string directly
        EsWrappers.lambdaQuery(Document.class)
            .match(Document::getContent, "talent");
            .nestedMatch(Document::getUsers, FieldUtils.val(User::getUsername), "User");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
````
For related demos, please refer to the source code test module -> test directory -> nested package