> **Tips**
>Wrapper支持两种方式创建:
> - 直接new,例如 new LambdaEsQueryWrapper<>().
> - 通过EsWrappers.lambdaQuery()创建,可支撑链式编程的场景,对标MP的Wrappers

> **Instruction:**
> - The first input parameter boolean condition that appears below indicates whether the condition is added to the last generated statement, for example: query.like(StringUtils.isNotBlank(name), Entity::getName, name) .eq(age!=null && age >= 0, Entity::getAge, age)
> - The multiple methods in the following code block are all input parameters of individual boolean types from top to bottom, and the default is true
> - The generic Param appearing below are all subclass instances of Wrapper (all have all the methods of AbstractWrapper)
> - The R that appears in the input parameters of the following methods is generic, String in ordinary wrapper, and function in LambdaWrapper (example: Entity::getId, Entity is the entity class, getId is the getMethod of the field id)
> - The R column in the input parameters of the following methods all represent database fields. When the specific type of R is String, it is the database field name (the field name is the database keyword and it is wrapped by an escape character!)! Not the entity data field name! !!, when the specific type of R is SFunction, the project runtime does not support eclipse's own compiler!!!
> - The following examples are all using ordinary wrappers, and the input parameters are Map and List in the form of json!
> - If the input Map or List is empty during use, it will not be added to the final generated SQL!

> **Warn:**
> - Does not support and does not support the transmission of Wrapper in RPC calls
> 
wrapper is heavy
> - The transmission wrapper can be analogous to your controller using a map to receive the value (the development is cool, and the crematorium is maintained)
> - The correct RPC call posture is to write a DTO for transmission, and the callee then performs corresponding operations based on the DTO
> - We refuse to accept any issues or even pr related to RPC transmission Wrapper errors

