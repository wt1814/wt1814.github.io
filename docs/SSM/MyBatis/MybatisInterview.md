


# Mybatis面试题


## Mybatis动态sql
Mybatis动态sql是做什么的？都有哪些动态sql？能简述一下动态sql的执行原理不？ 
Mybatis动态sql可以让我们在Xml映射文件内，以标签的形式编写动态sql，完成逻辑判断和动态拼接sql的功能。  
Mybatis提供了9种动态sql标签：trim|where|set|foreach|if|choose|when|otherwise|bind。  
其执行原理为，使用OGNL从sql参数对象中计算表达式的值，根据表达式的值动态拼接sql，以此来完成动态sql的功能。  