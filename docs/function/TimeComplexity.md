


# 时间复杂度

## 常见时间复杂度


```java

```


&emsp; **<font color = "red">有选择语句的时间复杂度：计算时间复杂度时选择运行时间最长的分支。</font>**  

```java
public void choice(int n){

    if (n >100){
        for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                System.out.println(i*j);
            }
        }
    }else {
        for (int i = 0; i < n; i++){
            System.out.println(i);
        }
    }

}
```


## 时间复杂度计算  

```java
// 分析：T(O) = n(当i=1时，里层循环执行次数) + (n -1)(当i=2时，里层循环执行次数) + ... ... + 2(当i=n-2时，里层循环执行次数) + 1(当i=n-1时，里层循环执行次数)
//           = O(n^2)
public void c(int n){
    for (int i = 0; i < n; i++){
        for (int j = i; j < n; j++){
            System.out.println(i+"，"+j);
        }
    }
}
```



![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-49.png)  
