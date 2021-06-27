package com.lonk.config.precise;


import com.lonk.mapper.nosharding.ShardConfigMapper;
import com.lonk.model.ShardConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;


/**
 * @author lonk
 * @desc 分库策略：
 *      通常以时间或用户ID分库
 */
@Slf4j
@Service("databaseShardingAlgorithm")
public class DatabaseShardingAlgorithm implements PreciseShardingAlgorithm<Long>{

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        // 定义分库算法
        // physicDatabase = getShardConfig(physicDatabase, subValue);
        int index = (int) (preciseShardingValue.getValue() % collection.size());

        log.info("----->db 分库：{}", collection);
        String physicDatabase = null;
        if (StringUtils.isBlank(physicDatabase)) {
            int tmp = 0;
            for (String value : collection) {
                if (tmp == index) {
                    physicDatabase = value;
                    break;
                }
                tmp++;
            }
        }
        log.info("----->分片键是={}，逻辑表是={},分片值是={}",preciseShardingValue.getColumnName(),preciseShardingValue.getLogicTableName(),preciseShardingValue.getValue());
        return physicDatabase;
    }

}
