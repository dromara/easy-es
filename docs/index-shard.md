```java
 /**
  * 设置索引的分片数和副本数
  *
  * @param shards   分片数
  * @param replicas 副本数
  */
settings(Integer shards, Integer replicas);
```
>**Tip:** 可根据实际服务器配置进行设置,合理的设置可提高综合效率和数据可靠性.