package com.wuw.ucenter.server.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * MySQL读写分离数据源
 */
@Slf4j
@Configuration
public class MySQLDataSourceConfig {

    @Value("${mysql-ds.master.jdbc-url}")
    private String masterJdbcUrl;

    @Value("${mysql-ds.master.username}")
    private String masterUsername;

    @Value("${mysql-ds.master.password}")
    private String masterPassword;

    @Value("${mysql-ds.replica-1.jdbc-url}")
    private String replica1JdbcUrl;

    @Value("${mysql-ds.replica-1.username}")
    private String replica1Username;

    @Value("${mysql-ds.replica-1.password}")
    private String replica1Password;



    @Bean
    public DataSource dataSource() throws SQLException {
        //数据源Map
        Map<String, DataSource> dsMap = new HashMap<>();
        //配置主库
        HikariDataSource masterDs = new HikariDataSource();
        masterDs.setDriverClassName("com.mysql.cj.jdbc.Driver");
        masterDs.setJdbcUrl(masterJdbcUrl);
        masterDs.setMinimumIdle(16);
        masterDs.setMaximumPoolSize(64);
        masterDs.setConnectionInitSql("SET NAMES utf8mb4");
        masterDs.setUsername(masterUsername);
        masterDs.setPassword(masterPassword);
        dsMap.put("master_ds", masterDs);
        //配置读库1
        HikariDataSource replicaDs1 = new HikariDataSource();
        replicaDs1.setDriverClassName("com.mysql.cj.jdbc.Driver");
        replicaDs1.setJdbcUrl(replica1JdbcUrl);
        replicaDs1.setMinimumIdle(32);
        replicaDs1.setMaximumPoolSize(128);
        replicaDs1.setConnectionInitSql("SET NAMES utf8mb4");
        replicaDs1.setUsername(replica1Username);
        replicaDs1.setPassword(replica1Password);
        dsMap.put("replica_ds_1", replicaDs1);
/*        //配置读库2
        HikariDataSource replicaDs2 = new HikariDataSource();
        replicaDs2.setDriverClassName("com.mysql.cj.jdbc.Driver");
        replicaDs2.setJdbcUrl(replica2JdbcUrl);
        replicaDs2.setMinimumIdle(32);
        replicaDs2.setMaximumPoolSize(128);
        replicaDs2.setConnectionInitSql("SET NAMES utf8mb4");
        replicaDs2.setUsername(replica2Username);
        replicaDs2.setPassword(replica2Password);
        dsMap.put("replica_ds_2", replicaDs2);*/
        //配置读写数据源名称
        Properties dsProperties = new Properties();
        dsProperties.put("write-data-source-name", "master_ds");
        // todo dsProperties.put("read-data-source-names", "replica_ds_1,replica_ds_2");
        dsProperties.put("read-data-source-names", "replica_ds_1");

        //主从数据源配置
        List<ReadwriteSplittingDataSourceRuleConfiguration> dsConfigurations = new ArrayList<>();
        dsConfigurations.add(new ReadwriteSplittingDataSourceRuleConfiguration("ds", "static", dsProperties, "load_balancer"));
        //负载均衡算法配置
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalanceMap = new HashMap<>();
        loadBalanceMap.put("load_balancer", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()));
        ReadwriteSplittingRuleConfiguration ruleConfiguration = new ReadwriteSplittingRuleConfiguration(dsConfigurations, loadBalanceMap);
        //创建DS
        log.info("创建 ShardingSphere 读写分离数据源");
        return ShardingSphereDataSourceFactory.createDataSource(dsMap, Arrays.asList(ruleConfiguration), new Properties());
    }
}
