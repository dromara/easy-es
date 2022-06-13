> **Foreword:** The bottom layer of ES is Lucene. Since Lucene does not actually support nested types, all documents are stored in Lucene in a flat structure. ES's support for parent and child documents actually adopts a done in an opportunistic way.

> The parent and child documents are stored as independent documents, and then the association relationship is added, and the parent and child documents must be in the same shard. Because the parent-child type document does not reduce the number of documents, and increases the parent-child binding relationship, the query efficiency will be low, so We do not recommend that you use parent-child types in actual development.

> ES itself is more suitable for the "large wide table" mode. Don't use ES with the way of thinking of traditional relational databases. We can completely combine the fields and contents of multiple tables into one table (an index). Complete the desired functions and avoid the use of parent-child types as much as possible, which is not only efficient, but also more powerful.

> Of course, it is reasonable to exist, and there are indeed some scenarios where parent-child types are inevitably used. As the world's leading ES-ORM framework, we also provide support for this, users can not use it, but we can't live without it!

> About the choice of parent-child type and nested type: If the document is written more than read, then it is recommended that you choose the parent-child type, if the document is read more than written, then please choose the nested type.


# 1. Steps for using parent-child type
## 1.1 Create index

- Automatic transmission mode:
````java
    /**
     * parent document
     */
    @TableName(childClass = Comment.class)
    public class Document{
        // Omit other fields...
       /**
        * The type of Join, and its parent name and child name must be indicated in the entity class of the parent document and child document through annotations. The JoinField class framework here is built-in, and there is no need to repeat the wheel
        * The full path of the JoinField class is cn.easyes.common.params.JoinField. If you have to build your own wheels and support it, you need to specify joinFieldClass=the wheel you built in the @TableField annotation
        */
        @TableField(fieldType = FieldType.JOIN, parentName = "document", childName = "comment")
        private JoinField joinField;
    }
    /**
     * subdocument
     */
    @TableName(child = true)
    public class Comment {
        // Omit other fields...
        /**
         * The parent-child relationship field must be marked as Join in the entity class of the parent document and child document through annotations, and the parent-child relationship in the child document can be omitted
        */
        @TableField(fieldType = FieldType.JOIN)
        private JoinField joinField;
    }
````
> **Note:** Be sure to add the annotation @TableName to the class of the parent document to indicate its child document class, and add the annotation @TableName to the class of the child document to indicate child=true as in the example above, and add the annotation @TableName to the class of the JoinField class. The specified type in the @TableField annotation is fieldType=JOIN and its parentName, childName, otherwise the framework will not work properly

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
        wrapper.join("joinField", "document", "comment");
````
> **Note:** In the manual mode, the annotations on the main class still cannot be less, and the parent-child relationship needs to be used when the framework is running. In the second method, the nested field needs to be specified through the wrapper, and then the index creation/update is completed.


## 1.2 CRUD
Note that since the parent and child types are independent documents and independent entity classes, each needs to have its own mapper
CRUD example:
````java
    @Test
    public void testInsert() {
        // Test the new parent-child document, where the automatic block mode is turned on, and the parent-child type index has been automatically processed
        // Newly add the parent document, and then insert the child document
        Document document = new Document();
        document.setId("1");
        document.setTitle("I am the title of the parent document");
        document.setContent("I am the content of the parent document");
        JoinField joinField = new JoinField();
        joinField.setName("document");
        document.setJoinField(joinField);
        documentMapper.insert(document);

        // insert subdocument
        Comment comment = new Comment();
        comment.setId("2");
        comment.setCommentContent("I am the comment 1 of the document");

        // Special attention here, the sub-document must specify its father's id, otherwise you can't find the father, don't blame me for not reminding
        joinField.setParent("1");
        joinField.setName("comment");
        comment.setJoinField(joinField);
        commentMapper.insert(comment);

        // insert subdocument 2
        Comment comment1 = new Comment();
        comment1.setId("3");
        comment1.setCommentContent("I am the comment 2 of the document");
        comment1.setJoinField(joinField);
        commentMapper.insert(comment1);
    }

    @Test
    public void testSelect() {
        // Warm reminder, the type in the wrapper below is actually the parentName and childName specified in the JoinField field annotation @TableField, which is consistent with the native syntax
        // case1: hasChild query, which returns the related parent document, so the query uses the parent document entity and its mapper
        LambdaEsQueryWrapper<Document> documentWrapper = new LambdaEsQueryWrapper<>();
        documentWrapper.hasChild("comment", FieldUtils.val(Comment::getCommentContent), "comment");
        List<Document> documents = documentMapper.selectList(documentWrapper);
        System.out.println(documents);

        // case2: hasParent query, which returns related subdocuments, so the query uses subdocument entities and their mappers
        LambdaEsQueryWrapper<Comment> commentWrapper = new LambdaEsQueryWrapper<>();
        // You can also use FieldUtils.val for the field name, or pass in a string directly
        commentWrapper.hasParent("document", "content", "content");
        List<Comment> comments = commentMapper.selectList(commentWrapper);
        System.out.println(comments);

        // case3: parentId query, which returns related subdocuments, similar to case2, so the query uses subdocument entities and their mappers
        commentWrapper = new LambdaEsQueryWrapper<>();
        commentWrapper.parentId("1", "comment");
        List<Comment> commentList = commentMapper.selectList(commentWrapper);
        System.out.println(commentList);
    }

    @Test
    public void testUpdate() {
        // case1: parent document/child document is updated according to their respective id
        Document document = new Document();
        document.setId("1");
        document.setTitle("I am the title of Pharaoh next door");
        documentMapper.updateById(document);

        // case2: parent document/child document are updated according to their respective conditions
        Comment comment = new Comment();
        comment.setCommentContent("I am the comment of the pharaoh next door");
        LambdaEsUpdateWrapper<Comment> wrapper = new LambdaEsUpdateWrapper<>();
        wrapper.match(Comment::getCommentContent, "Comment");
        commentMapper.update(comment, wrapper);
    }

    @Test
    public void testDelete() {
        // case1: parent document/child document are deleted according to their respective ids
        documentMapper.deleteById("1");

        //case2: The parent document/child document is deleted according to their respective conditions
        LambdaEsQueryWrapper<Comment> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Comment::getCommentContent, "Comment");
        commentMapper.delete(wrapper);
    }
````
For related demos, please refer to the test module of the source code -> test directory -> join package