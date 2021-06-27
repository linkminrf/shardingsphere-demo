package com.lonk.util;

/**
 * @author: lonk
 * @date: 2021/6/24 12:39
 * @desc:
 */
public class IdWorker {

    /** 工作机器ID(0~31) */
    private long workerId;
    /** 数据中心ID(0~31) */
    private long datacenterId;
    /** 毫秒内序列(0~4095) */
    private long sequence;
    /**起始的时间戳*/
    private long twepoch = 1288834974657L;
    /** 上次生成ID的时间截 */
    private long lastTimestamp = -1L;

    /** 数据标识id所占的位数 */
    private long datacenterIdBits = 5L;
    /** 机器id所占的位数 */
    private long workerIdBits = 5L;
    /** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);
    /** 支持的最大数据标识id，结果是31 */
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    private long sequenceBits = 12L;
    /** 机器ID向左移12位 */
    private long workerIdShift = sequenceBits;
    /** 数据标识id向左移17位(12+5) */
    private long datacenterIdShift = sequenceBits + workerIdBits;
    /** 时间截向左移22位(5+5+12) */
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    /** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
    private long sequenceMask = -1L ^ (-1L << sequenceBits);



    public IdWorker(long workerId, long datacenterId, long sequence) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.sequence = sequence;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp) {
            // 一个毫秒内最多只能有 4096 个数字，这个位运算保证始终就是在 4096 这个范围内
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                long cur = System.currentTimeMillis();
                while (cur <= lastTimestamp) {
                    cur = System.currentTimeMillis();
                }
                timestamp = cur;
            }
        } else {
            sequence = 0;
        }

        // 这儿记录一下最近一次生成 id 的时间戳，单位是毫秒
        lastTimestamp = timestamp;
        // 这儿就是将时间戳左移，放到 41 bit 那儿；
        // 将机房 id 左移放到 5 bit
        // 将机器 id 左移放到 5 bit ；
        // 将序号放最后 12 bit；
        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }


    public static void main(String[] args) {
        IdWorker worker = new IdWorker(1, 1, 1);
        for (int i = 0; i < 5; i++) {
            System.out.println(worker.nextId());
        }
    }

}
