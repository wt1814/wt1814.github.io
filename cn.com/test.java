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



    static final int hash(Object key) {
        int h;
        //1. 允许key为null，hash = 0
        //2. ^，异或运算
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

}



