> The EE index module provides the following APIs for users to make convenient calls
> - indexName needs to be specified manually by the user
> - Object Wrapper as Conditional Constructor

````java
     // get index information
     GetIndexResponse getIndex();
     // Get the specified index information
     GetIndexResponse getIndex(String indexName);
     // is there an index
     Boolean existsIndex(String indexName);
     // Create indexes with one key based on entities and custom annotations
     Boolean createIndex();
     // create index
     Boolean createIndex(LambdaEsIndexWrapper<T> wrapper);
     // update index
     Boolean updateIndex(LambdaEsIndexWrapper<T> wrapper);
     // delete the specified index
     Boolean deleteIndex(String indexName);
````
For the code demonstration corresponding to the interface, please click the outline according to the name to enter the specific page to view, or refer to the source code test module->test directory->index package for the code