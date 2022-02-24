

1. 表结构  

```sql
CREATE TABLE `flow_app_info` (
  `id` int(11) NOT NULL,
  `parentid` int(11) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

2. 实体类

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppInfo {
    private int id;
    private int parentid;
    private String name;
    private List<AppInfo> childList;
}
```

3. mapp.xml

```xml
<resultMap id="appsTree" type="com.lg.business.common.ws.AppInfo">
       <id column="id" property="id" javaType="int"/>
       <result column="parentid" property="parentid" javaType="int"/>
       <result column="name" property="name" javaType="String"/>
       <collection column="id" property="childList" select="getAppsByPid"/>
</resultMap>

<select id="getAllWsAppList" resultMap="appsTree">
       select id,parentid,name from flow_app_info where parentid=0
</select>

<select id="getAppsByPid" resultMap="appsTree">
       select id,parentid,name from flow_app_info where parentid=#{id}
</select>
```
