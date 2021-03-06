package com.pgy.ups.pay.quartz.configuration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.alibaba.druid.pool.DruidDataSource;

import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.api.config.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;

@Configuration
public class ShardingConfiguration {

	private Logger logger = LoggerFactory.getLogger(ShardingConfiguration.class);

	private String[] fromSystems = { "meiqi", "shurong" };

	private String databaseName = "ups-pay";
    
	@Value("${druid.config.path}")
    private String druidConfig;
    
	@Bean("dataSource")
	public DataSource getShardingDataSource() {

		Properties pro = new Properties();
		try {
			pro.load(new ClassPathResource(druidConfig).getInputStream());
		} catch (Exception e) {
			logger.error("druid.properties读取失败：{}", e);
			throw new RuntimeException("druid.properties读取失败");
		}
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.configFromPropety(pro);

		// 配置分片规则
		ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
		shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
		shardingRuleConfig.getTableRuleConfigs().add(getOrderPushTableRuleConfiguration());
		shardingRuleConfig.getTableRuleConfigs().add(getTableRuleConfiguration("ups_t_user_sign","from_system"));
		shardingRuleConfig.getTableRuleConfigs().add(getTableRuleConfiguration("ups_t_user_sign_log","from_system"));

		Map<String, DataSource> map = new HashMap<>();
		map.put(databaseName, dataSource);

		// 获取数据源对象
		try {
			DataSource shardingDataSource = ShardingDataSourceFactory.createDataSource(map, shardingRuleConfig,
					new ConcurrentHashMap<>(), new Properties());
			return shardingDataSource;
		} catch (Exception e) {
			logger.error("获取shardingDataSource失败：{}", e);
			throw new RuntimeException("获取shardingDataSource失败"); 
		}

	}

	private TableRuleConfiguration getOrderTableRuleConfiguration() {
		// 配置ups_t_order表规则
		TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
		String logicTable = "ups_t_order";
		orderTableRuleConfig.setLogicTable(logicTable);
		orderTableRuleConfig.setActualDataNodes(getTables(logicTable));
		// 分表策略
		StandardShardingStrategyConfiguration csc = new StandardShardingStrategyConfiguration("from_system",
				new MyTableShardingAlgorithm());
		orderTableRuleConfig.setTableShardingStrategyConfig(csc);
		return orderTableRuleConfig;
	}

	private TableRuleConfiguration getOrderPushTableRuleConfiguration() {
		TableRuleConfiguration orderPushTableRuleConfig = new TableRuleConfiguration();
		String logicTable = "ups_t_order_push";
		orderPushTableRuleConfig.setLogicTable(logicTable);
		orderPushTableRuleConfig.setActualDataNodes(getTables(logicTable));
		// 分表策略
		StandardShardingStrategyConfiguration csc = new StandardShardingStrategyConfiguration("from_system",
				new MyTableShardingAlgorithm());
		orderPushTableRuleConfig.setTableShardingStrategyConfig(csc);
		return orderPushTableRuleConfig;
	}

	private TableRuleConfiguration getTableRuleConfiguration(String table,String column){
		TableRuleConfiguration orderPushTableRuleConfig = new TableRuleConfiguration();
		orderPushTableRuleConfig.setLogicTable(table);
		orderPushTableRuleConfig.setActualDataNodes(getTables(table));
		// 分表策略
		StandardShardingStrategyConfiguration csc = new StandardShardingStrategyConfiguration(column,
				new MyTableShardingAlgorithm());
		orderPushTableRuleConfig.setTableShardingStrategyConfig(csc);
		return orderPushTableRuleConfig;
	}


	/**
     * 拼装所有table节点字符串
     * @param logicTableName
     * @return
     */
	private String getTables(String logicTableName) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fromSystems.length; i++) {
			sb.append(databaseName + "." + logicTableName + "_" + fromSystems[i].toLowerCase());
			if (i != fromSystems.length - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

}

class MyTableShardingAlgorithm implements PreciseShardingAlgorithm<String> {

	@Override
	public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
		for (String name : availableTargetNames) {
			if (StringUtils.endsWithIgnoreCase(name, shardingValue.getValue())) {
				return name;
			}
		}
		return null;
	}

}
