

# Yaml文件配置详解
<!-- 
k8s的yaml文件配置详解
https://www.cnblogs.com/arrow-kejin/p/10058758.html
k8s资源限制
https://blog.51cto.com/14154700/2453845
使用Kubernetes进行零停机滚动更新
https://www.jianshu.com/p/c63c9efaeac3
-->

## k8s的yaml文件配置详解  

<!-- 

k8s配置文件模板
https://www.cnblogs.com/g2thend/p/11837649.html
-->

```yaml
apiVersion: v1             #指定api版本，此值必须在kubectl apiversion中  
kind: Pod                  #指定创建资源的角色/类型  
metadata:                  #资源的元数据/属性  
  name: django-pod         #资源的名字，在同一个namespace中必须唯一  
  labels:                  #设定资源的标签，使这个标签在service网络中备案，以便被获知
    k8s-app: django
    version: v1  
    kubernetes.io/cluster-service: "true"  
  annotations:             #设置自定义注解列表  
    - name: String         #设置自定义注解名字  
spec:                      #设置该资源的内容  
  restartPolicy: Always    #表示自动重启，一直都会有这个容器运行
  nodeSelector:            #选择node节点14     zone: node1  
  containers:  
  - name: django-pod        #容器的名字  
    image: django:v1.1      #容器使用的镜像地址  
    imagePullPolicy: Never #三个选择Always、Never、IfNotPresent，每次启动时检查和更新（从registery）images的策略，
                           # Always，每次都检查
                           # Never，每次都不检查（不管本地是否有）
                           # IfNotPresent，如果本地有就不检查，如果没有就拉取
    command: ['sh']        #启动容器的运行命令，将覆盖容器中的Entrypoint,对应Dockefile中的ENTRYPOINT  
    args: ["$(str)"]       #启动容器的命令参数，对应Dockerfile中CMD参数  
    env:                   #指定容器中的环境变量  
    - name: str            #变量的名字  
      value: "/etc/run.sh" #变量的值  
    resources:             #资源管理
      requests:            #容器运行时，最低资源需求，也就是说最少需要多少资源容器才能正常运行  
        cpu: 0.1           #CPU资源（核数），两种方式，浮点数或者是整数+m，0.1=100m，最少值为0.001核（1m）
        memory: 32Mi       #内存使用量  
      limits:              #资源限制  
        cpu: 0.5  
        memory: 32Mi  
    ports:  
    - containerPort: 8080    #容器开发对外的端口
      name: uwsgi          #名称
      protocol: TCP  
    livenessProbe:         #pod内容器健康检查的设置
      httpGet:             #通过httpget检查健康，返回200-399之间，则认为容器正常  
        path: /            #URI地址  
        port: 8080  
        #host: 127.0.0.1   #主机地址  
        scheme: HTTP  
      initialDelaySeconds: 180 #表明第一次检测在容器启动后多长时间后开始  
      timeoutSeconds: 5    #检测的超时时间  
      periodSeconds: 15    #检查间隔时间  
      #也可以用这种方法  
      #exec: 执行命令的方法进行监测，如果其退出码不为0，则认为容器正常  
      #  command:  
      #    - cat  
      #    - /tmp/health  
      #也可以用这种方法  
      #tcpSocket: //通过tcpSocket检查健康   
      #  port: number   
    lifecycle:             #生命周期管理(钩子)  
      postStart:           #容器运行之前运行的任务  
        exec:  
          command:  
            - 'sh'  
            - 'yum upgrade -y'  
      preStop:             #容器关闭之前运行的任务  
        exec:  
          command: ['service httpd stop']  
    volumeMounts:          #挂载设置
    - name: volume         #挂载设备的名字，与volumes[*].name 需要对应    
      mountPath: /data     #挂载到容器的某个路径下  
      readOnly: True  
  volumes:                 #定义一组挂载设备  
  - name: volume           #定义一个挂载设备的名字  
    #meptyDir: {}  
    hostPath:  
      path: /opt           #挂载设备类型为hostPath，路径为宿主机下的/opt
```

<!-- 
apiVersion: v1 # 【必须】版本号
kind: Pod # 【必选】Pod
metadata: # 【必选-Object】元数据
  name: String # 【必选】 Pod的名称
  namespace: String # 【必选】 Pod所属的命名空间
  labels: # 【List】 自定义标签列表
    - name: String
  annotations: # 【List】 自定义注解列表
    - name: String
spec: # 【必选-Object】 Pod中容器的详细定义
  containers: # 【必选-List】 Pod中容器的详细定义
    - name: String # 【必选】 容器的名称
      image: String # 【必选】 容器的镜像名称
      imagePullPolicy: [Always | Never | IfNotPresent] # 【String】 每次都尝试重新拉取镜像 | 仅使用本地镜像 | 如果本地有镜像则使用，没有则拉取
      command: [String] # 【List】 容器的启动命令列表，如果不指定，则使用镜像打包时使用的启动命令
      args: [String] # 【List】 容器的启动命令参数列表
      workingDir: String # 容器的工作目录
      volumeMounts: # 【List】 挂载到容器内部的存储卷配置
        - name: String # 引用Pod定义的共享存储卷的名称，需使用volumes[]部分定义的共享存储卷名称
          mountPath: Sting # 存储卷在容器内mount的绝对路径，应少于512个字符
          readOnly: Boolean # 是否为只读模式，默认为读写模式
      ports: # 【List】 容器需要暴露的端口号列表
        - name: String  # 端口的名称
          containerPort: Int # 容器需要监听的端口号
          hostPort: Int # 容器所在主机需要监听的端口号，默认与containerPort相同。设置hostPort时，同一台宿主机将无法启动该容器的第二份副本
          protocol: String # 端口协议，支持TCP和UDP，默认值为TCP
      env: # 【List】 容器运行前需设置的环境变量列表
        - name: String # 环境变量的名称
          value: String # 环境变量的值
      resources: # 【Object】 资源限制和资源请求的设置
        limits: # 【Object】 资源限制的设置
          cpu: String # CPU限制，单位为core数，将用于docker run --cpu-shares参数
          memory: String # 内存限制，单位可以为MB，GB等，将用于docker run --memory参数
        requests: # 【Object】 资源限制的设置
          cpu: String # cpu请求，单位为core数，容器启动的初始可用数量
          memory: String # 内存请求，单位可以为MB，GB等，容器启动的初始可用数量
      livenessProbe: # 【Object】 对Pod内各容器健康检查的设置，当探测无响应几次之后，系统将自动重启该容器。可以设置的方法包括：exec、httpGet和tcpSocket。对一个容器只需要设置一种健康检查的方法
        exec: # 【Object】 对Pod内各容器健康检查的设置，exec方式
          command: [String] # exec方式需要指定的命令或者脚本
        httpGet: # 【Object】 对Pod内各容器健康检查的设置，HTTGet方式。需要指定path、port
          path: String
          port: Number
          host: String
          scheme: String
          httpHeaders:
            - name: String
              value: String
        tcpSocket: # 【Object】 对Pod内各容器健康检查的设置，tcpSocket方式
          port: Number
        initialDelaySeconds: Number # 容器启动完成后首次探测的时间，单位为s
        timeoutSeconds: Number  # 对容器健康检查的探测等待响应的超时时间设置，单位为s，默认值为1s。若超过该超时时间设置，则将认为该容器不健康，会重启该容器。
        periodSeconds: Number # 对容器健康检查的定期探测时间设置，单位为s，默认10s探测一次
        successThreshold: 0
        failureThreshold: 0
      securityContext:
        privileged: Boolean
  restartPolicy: [Always | Never | OnFailure] # Pod的重启策略 一旦终止运行，都将重启 | 终止后kubelet将报告给master，不会重启 | 只有Pod以非零退出码终止时，kubelet才会重启该容器。如果容器正常终止（退出码为0），则不会重启。
  nodeSelector: object # 设置Node的Label，以key:value格式指定，Pod将被调度到具有这些Label的Node上
  imagePullSecrets: # 【Object】 pull镜像时使用的Secret名称，以name:secretkey格式指定
    - name: String
  hostNetwork: Boolean # 是否使用主机网络模式，默认值为false。设置为true表示容器使用宿主机网络，不再使用docker网桥，该Pod将无法在同一台宿主机上启动第二个副本
  volumes: # 【List】 在该Pod上定义的共享存储卷列表
    - name: String # 共享存储卷的名称，volume的类型有很多emptyDir，hostPath，secret，nfs，glusterfs，cephfs，configMap
      emptyDir: {} # 【Object】 类型为emptyDir的存储卷，表示与Pod同生命周期的一个临时目录，其值为一个空对象：emptyDir: {}
      hostPath: # 【Object】 类型为hostPath的存储卷，表示挂载Pod所在宿主机的目录
        path: String # Pod所在主机的目录，将被用于容器中mount的目录
      secret: # 【Object】类型为secret的存储卷，表示挂载集群预定义的secret对象到容器内部
        secretName: String
        items:
          - key: String
            path: String
      configMap: # 【Object】 类型为configMap的存储卷，表示挂载集群预定义的configMap对象到容器内部
        name: String
        items:
          - key: String
            path: String
-->

## 资源限制  


## 零停机滚动更新  

