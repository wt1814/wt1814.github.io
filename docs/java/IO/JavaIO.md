



1. IO操作包含上传、读取、下载：  
&emsp; [读取项目Resources下文件](/docs/java/IO/readResources.md)  
&emsp; [上传下载](/docs/java/IO/Upload.md)  
&emsp; [导入导出](/docs/java/IO/Import.md)  
&emsp; [阿里云OSS](/docs/java/IO/OSS.md)  


2. **<font color = "clime">将大文件数据全部读取到内存中，可能会发生OOM异常。</font>** I/O读写大文件解决方案：  

    * 使用BufferedInputStream进行包装。
    * 逐行读取。
    * 并发读取：1)逐行批次打包；2)大文件拆分成小文件。
    * 零拷贝方案：
        * FileChannel，分配读取到已分配固定长度的 java.nio.ByteBuffer。
        * 内存文件映射，MappedByteBuffer。采用内存文件映射不能读取超过2GB的文件。文件超过2GB，会报异常。


<!--
超赞，压缩20M文件从30秒到1秒的优化过程 
https://mp.weixin.qq.com/s/jxGzGBVNkeL5SKDHRvNJ4A
100000 行级别数据的 Excel 导入优化之路 
https://mp.weixin.qq.com/s/Y1feFfn8VeZsxXw65NYoWQ
https://mp.weixin.qq.com/s/A6C5ttVCroZ4xaDAmdRskg
Java 设置Excel条件格式（高亮条件值、应用单元格值/公式/数据条等类型） 
https://mp.weixin.qq.com/s/h3M2wiJU-QYONi4ewJnVyA


读取服务器本地文件
-->


<!-- 
文件总行数
https://www.cxymm.net/article/cheng9981/82386663

-->
