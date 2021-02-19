

# Lua
<!-- 
https://blog.csdn.net/a_helloword/article/details/81878759
https://www.cnblogs.com/kaituorensheng/p/11098194.html
https://m.runoob.com/redis/redis-scripting.html
一网打尽Redis Lua脚本并发原子组合操作 
https://mp.weixin.qq.com/s/k0T6M1_gUvnBmviM0GGFKg

理解 pipeline 管道 
https://mp.weixin.qq.com/s?__biz=MzI5NTYwNDQxNA==&mid=2247486058&idx=2&sn=2b4f8764d807692f5ae7221ac88d69b8&chksm=ec5053bbdb27daaddd7a5f9d4e3737d584c13cf1f861d5b82aec443390fcc327ff0f6fe8bdef&scene=21#wechat_redirect
-->
&emsp; Redis 的单个命令都是原子性的，有时候希望能够组合多个 Redis 命令，并让这个组合也能够原子性的执行，甚至可以重复使用。Redis 开发者意识到这种场景还是很普遍的，就在 2.6 版本中引入了一个特性来解决这个问题，这就是 Redis 执行 Lua 脚本。  
&emsp; Redis支持两种方法运行脚本，一种是直接输入一些Lua语言的程序代码，另一种是将Lua语言编写成文件。在实际应用中，一些简单的脚本可以采取第一种方式，对于有一定逻辑的一般采用第二种。而对于采用简单脚本的，Redis支持缓存脚本，只是它会使用SHA-1算法对脚本进行签名，然后把SHA-1标识返回，只要通过这个标识运行就可以了。  
