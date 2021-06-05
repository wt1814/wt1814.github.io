
&emsp; 内存中的结构主要包括Buffer Pool，Change Buffer、Adaptive Hash Index以及Log Buffer四部分。 **<font color = "clime">如果从内存上来看，Change Buffer和Adaptive Hash Index占用的内存都属于Buffer Pool，Log Buffer占用的内存与 Buffer Pool独立。</font>** 即InnoDB内存主要有两大部分：缓冲池、重做日志缓冲。  
&emsp; [BufferPool](/docs/SQL/bufferPoolNew.md)  
&emsp; [ChangeBuffer](/docs/SQL/ChangeBuffer.md)  
&emsp; [AdaptiveHashIndex](/docs/SQL/AdaptiveHashIndex.md)  