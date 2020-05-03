/**
 * @ProjectName：wt1814.github.io
 * @ClassName: test
 * @Description: TODO  
 * @Author: wt 
 * @CreateDate: 2020-04-14 21:20
 * @UpdateUser: 
 * @UpdateDate:   
 * @UpdateRemark:
 * @Version: V1.0
 **/
public class test {
    public void write(String key,Object data){
        //1.删除缓存
        redis.delKey(key);
        //2.更新数据
        db.updateData(data);
        //3.休眠1S
        Thread.sleep(1000);
        //2.再次删除缓存
        redis.delKey(key);
    }
}



