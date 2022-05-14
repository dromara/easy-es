> **Foreword:** The bottom layer of ES is Lucene. Since Lucene does not actually support nested types, all documents are stored in Lucene in a flat structure. ES's support for nested and parent-child documents is actually also It was done in an opportunistic way.

> Nested documents/sub-documents are stored as independent documents, and then add associations, which will lead to a nested type of document, the bottom layer actually stores N pieces of data, and the update will be linked to nine family-style updates , resulting in low efficiency, and for nested/parent-child types, its query function is also limited, and it does not support functions such as aggregation sorting, so we do not recommend that you use these two types in actual development.

> ES itself is more suitable for the "large wide table" mode. Don't use ES with the way of thinking of traditional relational databases. We can completely combine the fields and contents of multiple tables into one table (an index). Complete the desired function and avoid the use of nesting and parent-child types as much as possible, which is not only efficient, but also more powerful.

> Of course, it is reasonable to exist, and there are indeed some scenarios where nesting and parent-child types are inevitably used. We also provide support, users can not use it, but we can't live without it!


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
> **Note:** Be sure to specify the type as fieldType=NESTED and its nestedClass as in the example above, otherwise the framework will not work properly

- Manual transmission mode

 
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
        // To get the field name of the nested class, we provide a tool class FieldUtils.val to help users get the field name through the lambda function. Of course, if you don't want to use it, you can also pass a string directly
        EsWrappers.lambdaQuery(Document.class)
            .match(Document::getContent, "talent");
            .nestedMatch(Document::getUsers, FieldUtils.val(User::getUsername), "User");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
````

# 2. Steps to use parent-child type
The parent-child type has a great impact on the existing functions of the framework. Due to the time in this issue, only a part of the functions have been added for the time being, and the loop has not been closed yet. Users can temporarily avoid it through native queries.

This issue only briefly provides two query APIs to simplify common parent-child type queries. Subsequent iterations will focus on continuing to improve the parent-child type function.
````java
    @Test
    public void testChildMatch() {
        // Parent-child type matching query The query content matches the data of the user name matching "user" in the sub-document. Here, the parent document is document and the sub-document is faq as an example
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "talent");
        // To get the field name of the nested class, we provide a tool class FieldUtils.val to help users get the field name through the lambda function. Of course, if you don't want to use it, you can also pass a string directly
        wrapper.childMatch("users", FieldUtils.val(User::getUsername), "Users");
        SearchResponse search = documentMapper.search(wrapper);
        System.out.println(search);
    }

    @Test
    public void testParentMatch() {
        // Parent-child type matching query Query documents that contain technology in the parent document
        LambdaEsQueryWrapper<Faq> wrapper = new LambdaEsQueryWrapper<>();
        // For field name acquisition, we provide a tool class FieldUtils.val to help users obtain field names through lambda functions. Of course, if you don't want to use them, you can also pass strings directly
        wrapper.parentMatch("document", FieldUtils.val(Document::getContent), "Technology");
        SearchResponse search = faqMapper.search(wrapper);
        System.out.println(search);
    }
````