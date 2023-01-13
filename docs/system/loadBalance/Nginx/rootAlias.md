

# root和alias的区别  
<!-- 
https://blog.csdn.net/tuoni123/article/details/79712246
-->



root：真实的路径是root指定的值加上location指定的值 。    
alias 正如其名，alias指定的路径是location的别名，不管location的值怎么写，资源的 真实路径都是 alias 指定的路径    

其他区别：  

1、 alias 只能作用在location中，而root可以存在server、http和location中。  
2、 alias 后面必须要用 “/” 结束，否则会找不到文件，而 root 则对 ”/” 可有可无。  

