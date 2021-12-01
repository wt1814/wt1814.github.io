

# 从头到尾的炕，不得不用【CompletableFuture】整一下

## 背景  
&emsp; app改版，代码重构。  
&emsp; 还记得刚接手这块内容的时候，有个for循环【串行】调用远程，稍微改点东西，就会导致接口超时。  
&emsp; 这次重构，想着优化一下。看调用的对方接口说明：  

```java
@MethodDesc("查询用户在哪个分群，不传分群id则返回所有分群")
List<Integer> queryGroupsByUserId(String var1, List<Integer> var2);
```

&emsp; 敲代码，调试无果。再一次入炕。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/share/share-1.png)  


## 通过CompletableFuture的allOf方法对多个异步执行结果进行处理
&emsp; CompletableFuture和CompletionService的知识，可以查看之前的文章。  

```java
List<Integer> groups= null;

if (list != null && list.size()>0){
    if (list.size() <=20){
        groups= groupServiceApi.queryGroupsByUserId(iid, list);
    }else {
        // 集合拆分
        List<List<Integer>> lists = this.splitList(list, 15);
        // 获取结果
        List<CompletableFuture<List<Integer>>> futures = this.getGroupIds(iid,lists);
        // 多个异步执行结果合并到该集合
        List<Integer> futureUsers = new ArrayList<>();

        // 通过allOf对多个异步执行结果进行处理
        CompletableFuture allFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).whenComplete((v, t) -> {
                // 所有CompletableFuture执行完成后进行遍历
                futures.forEach(future -> {
                    synchronized (this) {
                        // 查询结果合并
                        futureUsers.addAll(future.getNow(null));
                    }
                });
            });
        // 阻塞等待所有CompletableFuture执行完成
        allFuture.get();
        // 对合并后的结果集进行去重处理
        groups = futureUsers.stream().distinct().collect(Collectors.toList());
    }
}
```


```java
/**
    * list拆分
    * @param list
    * @param groupSize
    * @return
    */
private List<List<Integer>> splitList(List<Integer> list , int groupSize){
    int length = list.size();
    // 计算可以分成多少组
    int num = ( length + groupSize - 1 )/groupSize ; // TODO
    List<List<Integer>> newList = new ArrayList<>(num);
    for (int i = 0; i < num; i++) {
        // 开始位置
        int fromIndex = i * groupSize;
        // 结束位置
        int toIndex = (i+1) * groupSize < length ? ( i+1 ) * groupSize : length ;
        newList.add(list.subList(fromIndex,toIndex)) ;
    }
    return  newList ;
}
```

```java
/**
    * 异步获取结果
    * @param iid
    * @param lists
    * @return
    */
private List<CompletableFuture<List<Integer>>> getGroupIds(String iid,List<List<Integer>> lists){

    List<CompletableFuture<List<Integer>>> completableFutures =  new ArrayList<>();
    for (List<Integer> list:lists){

        CompletableFuture<List<Integer>> listCompletableFuture = CompletableFuture.supplyAsync(() -> groupServiceApi.queryGroupsByUserId(iid, list));
        completableFutures.add(listCompletableFuture);

    }
    return completableFutures;

}
```
