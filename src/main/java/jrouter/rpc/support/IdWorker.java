/** Copyright 2010-2012 Twitter, Inc. */
package jrouter.rpc.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https://github.com/twitter/snowflake
 * An object that generates IDs.
 * This is broken into a separate class in case
 * we ever want to support multiple worker threads
 * per process
 */
@lombok.Getter
public class IdWorker implements IdGenerator<Long> {

    protected static final Logger LOG = LoggerFactory.getLogger(IdWorker.class);

    private long workerId;

    private long datacenterId;

    private long sequence = 0L;

    //20101104 09:42:54
    private final long twepoch = 1288834974657L;

    private final long workerIdBits = 5L;

    private final long datacenterIdBits = 5L;

    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    private final long sequenceBits = 12L;

    private final long workerIdShift = sequenceBits;

    private final long datacenterIdShift = sequenceBits + workerIdBits;

    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long lastTimestamp = -1L;

    public IdWorker(long workerId, long datacenterId) {
        // sanity check for workerId
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        LOG.info(String.format("worker starting. timestamp left shift %d, datacenter id bits %d, worker id bits %d, sequence bits %d, workerid %d", timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, workerId));
    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            LOG.error(String.format("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp));
            throw new IllegalArgumentException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    /** 根据生产的id解析时间（毫秒） */
    public long parseTimeMillis(long id) {
        return (id >> timestampLeftShift) + twepoch;
    }

    /** 根据生产的id解析datacenterId */
    public long parseDatacenterId(long id) {
        return (id >> datacenterIdShift) & maxDatacenterId;
    }

    /** 根据生产的id解析workerId */
    public long parseWorkerId(long id) {
        return (id >> workerIdShift) & maxWorkerId;
    }

    /** 根据生产的id解析sequence */
    public long parseSequence(long id) {
        return id & sequenceMask;
    }

    @Override
    public Long generateId() {
        return nextId();
    }

}
