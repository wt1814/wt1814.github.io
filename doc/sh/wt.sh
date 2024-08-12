
cd /workspace/life-career
nohup docsify serve -p3002 &


cd /usr/local/apache-tomcat-8.5.70/bin/
./shutdown.sh
nohup ./startup.sh &

cd /usr/local/redis-6.2.7/src
nohup ./redis-server config/redis.conf &

cd /usr/local/rocketmq-4.9.3/bin
nohup sh mqnamesrv &
nohup sh mqbroker -n 182.92.69.8:9876 &

cd /usr/local/nacos/bin
sh startup.sh -m standalone




