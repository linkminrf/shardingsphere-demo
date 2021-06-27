package com.lonk.config;


import com.lonk.config.precise.CommonTableShardingAlgorithm;
import com.lonk.config.precise.DatabaseShardingAlgorithm;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.core.strategy.keygen.SnowflakeShardingKeyGenerator;
import org.apache.shardingsphere.core.strategy.keygen.UUIDShardingKeyGenerator;
import org.apache.shardingsphere.core.strategy.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.core.strategy.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;


/**
 * @author lonk
 * @create 2019/4/29 19:56
 */
@Configuration
@MapperScan(basePackages = "com.lonk.mapper.sharding", sqlSessionTemplateRef = "sqlSessionTemplate")
public class SharedDataSourceConfig {

    private String shardOrder0DataSource="db_order_0";
    private String shardOrder1DataSource="db_order_1";

    private String ordersLogicTable="t_order";
    private String ordersActualDataNodes = "db_order_$->{0..1}.t_order_$->{0..1}";

    private String databaseShardingColumn = "user_id";
    private String ordersShardingColumn = "order_id";



    /**
     * Sharding提供了5种分片策略：
     *      StandardShardingStrategyConfiguration:
     *          标准分片策略, 提供对SQL语句中的=, IN和BETWEEN AND的分片操作支持;
     *          StandardShardingStrategy 只支持单分片键，
     *          PreciseShardingAlgorithm 和 RangeShardingAlgorithm 两个分片算法
     *
     *      ComplexShardingStrategyConfiguration:
     *          复合分片策略, 提供对SQL语句中的=, IN和BETWEEN AND的分片操作支持。
     *          ComplexShardingStrategy 支持多分片键;
     *
     *      InlineShardingStrategyConfiguration:
     *          Inline表达式分片策略, 使用Groovy的Inline表达式，提供对SQL语句中的=和IN的分片操作支持
     *
     *      HintShardingStrategyConfiguration:
     *          通过Hint而非SQL解析的方式分片的策略;
     *          用于处理使用Hint行分片的场景;
     *          主要是为了应对分片字段不存在SQL中、数据库表结构中，而存在于外部业务逻辑，
     *
     *      NoneShardingStrategyConfiguration:
     *          不分片的策略
     *
     * Sharding提供了以下4种算法接口：
     *      PreciseShardingAlgorithm
     *      RangeShardingAlgorithm
     *      HintShardingAlgorithm
     *      ComplexKeysShardingAlgorithm
     *
     * @return
     */
    private TableRuleConfiguration getOrderTableRuleConfiguration() {
        // 逻辑表、实际节点
        TableRuleConfiguration orderTableRuleConfig=new TableRuleConfiguration(ordersLogicTable, ordersActualDataNodes);

        // 采用标准分片策略
        orderTableRuleConfig.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration(databaseShardingColumn, new DatabaseShardingAlgorithm()));
        orderTableRuleConfig.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(ordersShardingColumn,new CommonTableShardingAlgorithm()));

        // 表达式分片
        // orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration(databaseShardingColumn,"ds${user_id % 2}"));
        // orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration(ordersShardingColumn, "t_order_${order_id % 2}"));

        // 指定自增字段以及key的生成方式
        // new UUIDShardingKeyGenerator();
        // new SnowflakeShardingKeyGenerator();
        // orderTableRuleConfig.setKeyGeneratorConfig(new KeyGeneratorConfiguration("SNOWFLAKE", "id"));
        return orderTableRuleConfig;
    }



    @Bean(name = "shardingDataSource")
    DataSource getShardingDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        // 订单逻辑表
        shardingRuleConfig.getBindingTableGroups().add(ordersLogicTable);
        // 订单配置表规则
        shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());

        // 默认自增主键生成器
        // shardingRuleConfig.setDefaultKeyGeneratorConfig(new KeyGeneratorConfiguration("SNOWFLAKE", "id"));
        //配置广播表规则列表
        // shardingRuleConfig.getBroadcastTables().add("t_config");


        // 通用匹配规则 TableRuleConfiguration
        // shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration(databaseShardingColumn,  databaseShardingAlgorithm));
        // shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(ordersShardingColumn, commonTableShardingAlgorithm));
        // 配置默认分库规则(不配置分库规则,则只采用分表规则)
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        // 默认库
        shardingRuleConfig.setDefaultDataSourceName(shardOrder0DataSource);


        Properties properties = new Properties();
        properties.setProperty("sql.show",Boolean.TRUE.toString());


        // 构建读写分离配置
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration(
                "ms_ds",
                shardOrder0DataSource,
                Arrays.asList(shardOrder1DataSource),
                new LoadBalanceStrategyConfiguration(new RoundRobinMasterSlaveLoadBalanceAlgorithm ().getType())
                );
        shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig);


        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, properties);
    }

    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("shardingDataSource") DataSource shardingDataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(shardingDataSource);
        // bean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/mybatis-config.xml"));
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/sharding/*.xml"));
        return bean.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(@Qualifier("shardingDataSource") DataSource shardingDataSource) {
        return new DataSourceTransactionManager(shardingDataSource);
    }

    @Bean("sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory)  {
        return new SqlSessionTemplate(sqlSessionFactory);
    }



    private DataSource createDataSource(String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8", "localhost", "3306", dataSourceName));
        result.setUsername("root");
        result.setPassword("123456");
        return result;
    }
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put(shardOrder0DataSource, createDataSource(shardOrder0DataSource));
        result.put(shardOrder1DataSource, createDataSource(shardOrder1DataSource));
        return result;
    }

}
