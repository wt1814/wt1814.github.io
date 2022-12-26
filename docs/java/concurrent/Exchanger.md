


# Exchanger，交换器  

<!-- 
另一种形式的栅栏是Exchanger,它是一种两方(Two-Party)栅栏，各方在栅栏位置上交 换数据[CPJ 3.4.3]。当两方执行不对称的操作时，Exchanger会非常有用，例如当一个线程向缓 冲区写入数据，而另一个线程从缓冲区中读取数据。这些线程可以使用Exchanger来汇合，并 将满的缓冲区与空的缓冲区交换。当两个线程通过Exchanger交换对象时，这种交换就把这两 个对象安全地发布给另一方。
数据交换的时机取决于应用程序的响应需求。最简草的方案是，当缓冲区被填满时， 由填充任务进行交换，当缓冲区为空时，由清空任务进行交换/这样会把需要交换的次数 降至最低，但如果新数据的到达率不可预测，那么一些数据的处理过程就将延迟。另一个 方法是，不仅当缓冲被填满时进行交换，并且当缓冲被填充到一定程度并保持一定时间后， 也进行交换。
-->

&emsp; <font color = "clime">Exchanger是一个用于线程间协作的工具类，用于两个线程间交换。Exchanger提供了一个交换的同步点，在这个同步点两个线程能够交换数据。</font>  

&emsp; 具体交换数据是通过exchange()方法来实现的，如果一个线程先执行exchange方法，那么它会同步等待另一个线程也执行exchange方法，这个时候两个线程就都达到了同步点，两个线程就可以交换数据。  
![image](http://182.92.69.8:8081/img/java/concurrent/concurrent-34.png)  

&emsp; 用一个简单的例子来看下Exchanger的具体使用。两方做交易，如果一方先到要等另一方也到了才能交易，交易就是执行exchange方法交换数据。  

```java
public class ExchangerTest {
    private static Exchanger<String> exchanger = new Exchanger<String>();
    static String goods = "电脑";
    static String money = "$1000";

    public static void main(String[] args) throws InterruptedException {
        System.out.println("准备交易，一手交钱一手交货...");
        // 卖家
        new Thread() {
            public void run() {
                System.out.println(getName() + " 卖家到了，已经准备好货：" + goods);
                try {
                    String money = exchanger.exchange(goods);
                    System.out.println(getName() + " 卖家收到钱：" + money);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
        Thread.sleep(3000);
        // 买家
        new Thread() {
            public void run() {
                try {
                    System.out.println(getName() + " 买家到了，已经准备好钱：" + money);
                    String goods = exchanger.exchange(money);
                    System.out.println(getName() + " 买家收到货：" + goods);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }
}
```

&emsp; 输出结果：  

```java
准备交易，一手交钱一手交货...
Thread-0 卖家到了，已经准备好货：电脑
Thread-1 买家到了，已经准备好钱：$1000
Thread-1 买家收到货：电脑
Thread-0 卖家收到钱：$1000
```

 