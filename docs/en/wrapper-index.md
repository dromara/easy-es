````java
index(String indexName)
eq(boolean condition, String indexName)
````
> You can use wrapper.index(String indexName) to specify which index this query is to act on. If this query is to be queried from multiple indexes, the index names can be separated by commas, for example, wrapper.eq("index1"," indexes").
> The index name specified in the wrapper has the highest priority. If it is not specified, the index name configured in the entity class is taken. If the entity class is not configured, the lowercase entity name is taken as the index name of the current query.
> For the case where there is no wrapper in the interface such as insert/delete/update, if you need to specify the index name, you can directly add the index name to the input parameter of the corresponding interface, please refer to the following example:

````java
     Document document = new Document();
     // Omit the code that assigns value to document
     String indexName = "man";
     insert(document, indexName);
````