````java
allEq(Map<R, V> params)
allEq(Map<R, V> params, boolean null2IsNull)
allEq(boolean condition, Map<R, V> params, boolean null2IsNull)

````
 All [eq](eq.md) (or individual [isNull](isNull.md))
> **Individual parameter description:**
> params : key is the database field name, value is the field value
null2IsNull : If it is true, the [isNull](isNull.md) method is called when the value of the map is null, and when it is false, the value of null is ignored.

- Example 1: allEq({id:1,name:"Pharaoh",age:null})--->id = 1 and name = 'Pharaoh' and age is null
- Example 2: allEq({id:1,name:"Pharaoh",age:null}, false)--->id = 1 and name = 'Pharaoh'

````java
allEq(BiPredicate<R, V> filter, Map<R, V> params)
allEq(BiPredicate<R, V> filter, Map<R, V> params, boolean null2IsNull)
allEq(boolean condition, BiPredicate<R, V> filter, Map<R, V> params, boolean null2IsNull)

````
> **Individual parameter description:**
> filter : filter function, whether to allow the field to be passed into the comparison condition
params and null2IsNull : same as above

- Example 1: allEq((k,v) -> k.indexOf("a") >= 0, {id:1,name:"Pharaoh",age:null})--->name = 'Old king' and age is null
- Example 2: allEq((k,v) -> k.indexOf("a") >= 0, {id:1,name:"Pharaoh",age:null}, false)--->name = 'Pharaoh'