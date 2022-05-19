````java
enableMust2Filter(boolean enable)
enableMust2Filter(boolean condition, boolean enable)
````

> Whether to convert the must query conditions into filter query conditions, you can directly specify whether the conditions of this query are converted in the wrapper. If not specified, it will be obtained from the global configuration file. If it is not configured in the configuration file, it will not be converted by default .
> The must query condition calculates the score, and the filter does not calculate the score. Therefore, in the query scenario that does not need to calculate the score, enabling this configuration can improve the query performance a little.