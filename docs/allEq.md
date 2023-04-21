```java
allEq(Map<R, V> params)
allEq(Map<R, V> params, boolean null2IsNull)
allEq(boolean condition, Map<R, V> params, boolean null2IsNull)

```
 全部[eq](eq.md)(或个别[isNull](isNull.md))
> **个别参数说明:**
> params : key为数据库字段名,value为字段值
null2IsNull : 为true则在map的value为null时调用[ isNull]( isNull.md)方法,为false时则忽略value为null的

- 例1: allEq({id:1,name:"老王",age:null})--->id = 1 and name = '老王' and age is null
- 例2: allEq({id:1,name:"老王",age:null}, false)--->id = 1 and name = '老王'

```java
allEq(BiPredicate<R, V> filter, Map<R, V> params)
allEq(BiPredicate<R, V> filter, Map<R, V> params, boolean null2IsNull)
allEq(boolean condition, BiPredicate<R, V> filter, Map<R, V> params, boolean null2IsNull) 

```
> **个别参数说明:**
> filter : 过滤函数,是否允许字段传入比对条件中
params 与 null2IsNull : 同上

- 例1: allEq((k,v) -> k.indexOf("a") >= 0, {id:1,name:"老王",age:null})--->name = '老王' and age is null
- 例2: allEq((k,v) -> k.indexOf("a") >= 0, {id:1,name:"老王",age:null}, false)--->name = '老王'

 

