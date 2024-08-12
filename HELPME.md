



sentinel：  
http://182.92.69.8:9091/#/login  账户密码：sentinel  



redis   

cd /usr/local/redis-6.2.7  
./src/redis-server config/redis.conf


rocketmq

cd /usr/local/rocketmq-4.9.3/bin
nohup sh mqnamesrv &
nohup sh mqbroker -n 182.92.69.8:9876 &


nacos  

sh startup.sh -m standalone


