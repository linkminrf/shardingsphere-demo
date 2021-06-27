package com.lonk.config.precise;


import com.lonk.mapper.nosharding.ShardConfigMapper;
import com.lonk.model.ShardConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;


/**
 * @author lonk
 * @desc  默认分表策略
 */
@Slf4j
@Service("commonTableShardingAlgorithm")
public class CommonTableShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        // physicsTable= setValue(preciseShardingValue);
        log.info("----->db 分表：{}", collection);
        String physicsTable = null;

        int index = (int) (preciseShardingValue.getValue() % collection.size());
        if (StringUtils.isBlank(physicsTable)) {
            int tmp = 0;
            for (String value : collection) {
                if (tmp == index) {
                    physicsTable = value;
                    break;
                }
                tmp++;
            }
        }
        log.info("----->分片键是={}，逻辑表是={},分片值是={}",preciseShardingValue.getColumnName(), preciseShardingValue.getLogicTableName(), preciseShardingValue.getValue());
        return physicsTable;
    }

}
