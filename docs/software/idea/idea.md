

# idea
## springboot在idea的RunDashboard如何显示
<!-- 
https://jingyan.baidu.com/article/ce4366495a1df73773afd3d3.html
-->

## iml文件  


&emsp; IDEA中的.iml文件是项目标识文件，缺少了这个文件，IDEA就无法识别项目。跟Eclipse的.project文件性质是一样的。并且这些文件不同的设备上的内容也会有差异，所以在管理项目的时候，.project和.iml文件都需要忽略掉。  

&emsp; 在缺少.iml文件项目下运行mvn idea:module，完成后将自动生成.iml文件。  

