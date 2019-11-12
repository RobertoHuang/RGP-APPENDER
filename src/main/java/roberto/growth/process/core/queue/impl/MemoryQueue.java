/**
 * FileName: MemoryQueue
 * Author:   HuangTaiHong
 * Date:     2019-11-11
 * Description: 内存Queue.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.core.queue.impl;

import roberto.growth.process.core.LogSender;
import roberto.growth.process.core.SenderStatusReporter;
import roberto.growth.process.core.queue.LogDataQueue;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 〈内存Queue.〉
 *
 * @author HuangTaiHong
 * @since 2019-11-11
 */
public class MemoryQueue implements LogDataQueue {
    // 1MB
    private static final int MB_IN_BYTES = 1024 * 1024;
    private static final int DONT_LIMIT_QUEUE_SPACE = -1;

    /**
     * Builder.
     *
     * @param context the context
     * @return the memory queue . builder
     * @author HuangTaiHong
     * @since 2019.11.11 22:31:00
     */
    public static MemoryQueue.Builder builder(final LogSender.Builder context) {
        return new MemoryQueue.Builder(context);
    }

    private final ConcurrentLinkedDeque<byte[]> logBuffer;

    private final boolean dontCheckLogsCountLimit;
    private final boolean dontCheckEnoughMemorySpace;

    // 日志容量
    private final long capacityInBytes;
    // 日志条数限制
    private final long logCountLimit;

    private final AtomicLong size;
    private final AtomicLong logCounter;

    private final SenderStatusReporter reporter;

    /**
     * 构造函数
     *
     * @param capacityInBytes the capacity in bytes
     * @param logCountLimit   the log count limit
     * @param reporter        the reporter
     * @author HuangTaiHong
     * @since 2019.11.11 22:31:00
     */
    public MemoryQueue(final long capacityInBytes, final long logCountLimit, final SenderStatusReporter reporter) {
        this.capacityInBytes = capacityInBytes;
        this.logCountLimit = logCountLimit;
        this.reporter = reporter;
        this.logBuffer = new ConcurrentLinkedDeque<>();
        this.dontCheckEnoughMemorySpace = capacityInBytes == DONT_LIMIT_QUEUE_SPACE;
        this.dontCheckLogsCountLimit = logCountLimit == DONT_LIMIT_QUEUE_SPACE;
        this.size = new AtomicLong(0L);
        this.logCounter = new AtomicLong(0L);
    }

    @Override
    public void enqueue(final byte[] logData) {
        if (this.isEnoughSpace()) {
            this.logBuffer.add(logData);
            // 日志大小累加
            this.size.addAndGet(logData.length);
            // 日志条数累加
            this.logCounter.incrementAndGet();
        }
    }

    @Override
    public byte[] dequeue() {
        final byte[] logData;
        try {
            logData = this.logBuffer.remove();
            this.size.addAndGet(-logData.length);
            this.logCounter.decrementAndGet();
            return logData;
        } catch (NoSuchElementException e) {
            this.reporter.warn("队列获取数据失败", e);
        }
        return new byte[0];
    }

    @Override
    public boolean isEmpty() {
        return this.logBuffer.isEmpty();
    }

    @Override
    public void close() {
        logBuffer.clear();
    }

    /**
     * 空间容量检查
     * 1、内存阈值
     * 2、数量条数
     *
     * @return boolean
     * @author HuangTaiHong
     * @since 2019.11.11 22:31:00
     */
    private boolean isEnoughSpace() {
        if (!this.dontCheckLogsCountLimit && this.logCounter.get() >= this.logCountLimit) {
            this.reporter.warn(String.format("日志条数超过阈值：%d 条, 丢弃日志", this.logCountLimit));
            return false;
        } else if (!this.dontCheckEnoughMemorySpace && this.size.get() >= this.capacityInBytes) {
            this.reporter.warn(String.format("内存容量超过阈值：%d MB, 丢弃日志", this.capacityInBytes / MB_IN_BYTES));
            return false;
        }
        return true;
    }

    /**
     * 构建类
     */
    public static class Builder {
        // 默认100MB
        private long capacityInBytes;
        private long logCountLimit;
        private SenderStatusReporter reporter;
        private final LogSender.Builder context;

        /**
         * Instantiates a new Builder.
         *
         * @param context the context
         * @author HuangTaiHong
         * @since 2019.11.11 22:31:00
         */
        Builder(final LogSender.Builder context) {
            this.context = context;
            this.logCountLimit = MemoryQueue.DONT_LIMIT_QUEUE_SPACE;
        }

        /**
         * Capacity in bytes.
         *
         * @param capacityInBytes the capacity in bytes
         * @return the memory queue . builder
         * @author HuangTaiHong
         * @since 2019.11.11 22:31:00
         */
        public MemoryQueue.Builder capacityInBytes(final long capacityInBytes) {
            this.capacityInBytes = capacityInBytes;
            return this;
        }

        /**
         * Log count limit.
         *
         * @param logCountLimit the log count limit
         * @return the memory queue . builder
         * @author HuangTaiHong
         * @since 2019.11.11 22:31:00
         */
        public MemoryQueue.Builder logCountLimit(final long logCountLimit) {
            this.logCountLimit = logCountLimit;
            return this;
        }

        /**
         * Reporter.
         *
         * @param reporter the reporter
         * @return the memory queue . builder
         * @author HuangTaiHong
         * @since 2019.11.11 22:31:00
         */
        public MemoryQueue.Builder reporter(final SenderStatusReporter reporter) {
            this.reporter = reporter;
            return this;
        }

        /**
         * End memory queue.
         *
         * @author HuangTaiHong
         * @since 2019.11.11 22:31:00
         */
        public LogSender.Builder endMemoryQueue() {
            this.context.setMemoryQueueBuilder(this);
            return this.context;
        }

        /**
         * Build.
         *
         * @return the memory queue
         * @author HuangTaiHong
         * @since 2019.11.11 22:31:00
         */
        public MemoryQueue build() {
            return new MemoryQueue(this.capacityInBytes, this.logCountLimit, this.reporter);
        }
    }
}
