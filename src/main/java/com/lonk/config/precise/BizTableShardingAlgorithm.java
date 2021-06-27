package com.lonk.config.precise;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.springframework.stereotype.Service;

import java.util.Collection;


/**
 * @author lonk
 * @desc 业务表分片策略定义
 */
@Data
@Slf4j
@Service("bizTableShardingAlgorithm")
public class BizTableShardingAlgorithm extends CommonTableShardingAlgorithm {

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        return super.doSharding(collection, preciseShardingValue);
    }
}
