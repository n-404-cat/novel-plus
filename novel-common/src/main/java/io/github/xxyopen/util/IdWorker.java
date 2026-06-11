package io.github.xxyopen.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * 雪花算法 ID 生成器。
 *
 * <p>兼容说明：</p>
 * <p>部分 macOS 或受限运行环境下，可能无法从当前网卡获取硬件地址，
 * 原始实现会在读取 MAC 地址时直接触发空指针，导致应用启动失败。
 * 这里保留原有算法结构，只在获取数据中心 ID 时增加兜底逻辑，
 * 确保本机开发环境在无法读取网卡信息时仍然可以正常启动。</p>
 */
public enum IdWorker {
    INSTANCE;

    private static final long EPOCH = 1288834974657L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static long lastTimestamp = -1L;

    private long sequence = 0L;
    private final long datacenterId;
    private final long workerId;

    IdWorker() {
        this.datacenterId = getDatacenterId();
        this.workerId = getMaxWorkerId(datacenterId);
    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                "Clock moved backwards. Refusing to generate id for %d milliseconds",
                lastTimestamp - timestamp
            ));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
            | (datacenterId << DATACENTER_ID_SHIFT)
            | (workerId << WORKER_ID_SHIFT)
            | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    private long getMaxWorkerId(long datacenterId) {
        StringBuilder builder = new StringBuilder();
        builder.append(datacenterId);
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        if (runtimeName != null && !runtimeName.isEmpty()) {
            builder.append(runtimeName.split("@")[0]);
        }
        return (builder.toString().hashCode() & 0xffffL) % (MAX_WORKER_ID + 1);
    }

    private long getDatacenterId() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
            if (networkInterface == null) {
                // 当前地址没有绑定网卡时，回退到固定数据中心编号，避免本机启动失败。
                return 1L;
            }

            byte[] mac = networkInterface.getHardwareAddress();
            if (mac == null || mac.length < 2) {
                // macOS 或容器环境下可能拿不到硬件地址，这里直接回退到稳定默认值。
                return 1L;
            }

            long id = ((0x000000FFL & mac[mac.length - 1])
                | ((0x0000FF00L & (((long) mac[mac.length - 2]) << 8)))) >> 6;
            return id % (MAX_DATACENTER_ID + 1);
        } catch (Throwable ignored) {
            // 获取网卡信息失败时不阻断应用启动，退回到固定数据中心编号。
            return 1L;
        }
    }
}
