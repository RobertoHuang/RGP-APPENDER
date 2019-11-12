/**
 * FileName: LogSender
 * Author:   HuangTaiHong
 * Date:     2019-11-11
 * Description: 日志发送器.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.core;

import com.alibaba.fastjson.JSON;
import roberto.growth.process.core.exception.LogParameterErrorException;
import roberto.growth.process.core.queue.LogDataQueue;
import roberto.growth.process.core.queue.impl.DiskQueue;
import roberto.growth.process.core.queue.impl.MemoryQueue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 〈日志发送器.〉
 *
 * @author HuangTaiHong
 * @since 2019-11-11
 */
public class LogSender {
    /**
     * Builder.
     *
     * @return the log sender builder.
     * @author HuangTaiHong
     * @since 2019.11.11 19:29:07
     */
    public static LogSender.Builder builder() {
        return new LogSender.Builder();
    }

    final LogDataQueue logDataQueue;
    volatile boolean shutdown = false;

    private final SenderStatusReporter reporter;
    private final ExecutorService tasksExecutor;
    private final LogContext logContext;

    /**
     * Instantiates a new Log sender.
     *
     * @param logContext    the log context
     * @param logDataQueue  the log data queue
     * @param reporter      the reporter
     * @param tasksExecutor the tasks executor
     * @author HuangTaiHong
     * @since 2019.11.11 19:29:07
     */
    public LogSender(final LogContext logContext, final LogDataQueue logDataQueue, final SenderStatusReporter reporter, final ExecutorService tasksExecutor) {
        this.logContext = logContext;
        this.logDataQueue = logDataQueue;
        this.reporter = reporter;
        this.tasksExecutor = tasksExecutor;
        // TODO 需要观察是否需要异步启动
    }

    /**
     * Start.
     *
     * @author HuangTaiHong
     * @since 2019.11.11 19:29:07
     */
    public void start() {
        if (logContext.isEnabledBufferQueue()) {
            this.tasksExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    while (!shutdown) {
                        if (LogSender.this.logDataQueue.isEmpty()) {
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        LogSender.this.drainQueueAndSend();
                    }
                }
            });
        }
    }

    /**
     * Stop.
     *
     * @author HuangTaiHong
     * @since 2019.11.11 19:29:07
     */
    @SuppressWarnings("PMD")
    public void stop() {
        this.shutdown = true;
        if (logContext.isEnabledBufferQueue() && !this.logDataQueue.isEmpty()) {
            final ExecutorService executorService = Executors.newSingleThreadExecutor();
            reporter.info("关闭LogSender前, 提交队列中的任务, 超时时间20秒");
            try {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        LogSender.this.drainQueueAndSend();
                    }
                }).get(20L, TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                this.reporter.warn("等待了20秒，但无法完全处理完队列任务。退出");
            } finally {
                executorService.shutdownNow();
            }
        }
        if (tasksExecutor != null) {
            tasksExecutor.shutdownNow();
        }
        if (logDataQueue != null) {
            try {
                logDataQueue.close();
            } catch (IOException e) {
                this.reporter.error("queue close failure", e);
            }
        }
    }

    /**
     * 提供给Appender唯一入口
     *
     * @param logMessage the log message
     * @author HuangTaiHong
     * @since 2019.11.11 19:29:07
     */
    public void writeQueue(final LogMessage logMessage) {
        final String message = JSON.toJSONString(logMessage);
        if (logContext.isEnabledBufferQueue()) {
            this.logDataQueue.enqueue(message.getBytes(StandardCharsets.UTF_8));
        } else {
            System.out.println("从MQ发送消息:" + logMessage);
        }
    }

    /**
     * Drain queue and send.
     *
     * @author HuangTaiHong
     * @since 2019.11.11 19:29:07
     */
    void drainQueueAndSend() {
        while (!this.logDataQueue.isEmpty()) {
            final byte[] bytes = this.logDataQueue.dequeue();
            if (bytes == null || bytes.length < 1) {
                return;
            }
            System.out.println("从MQ发送消息:" + new String(bytes, StandardCharsets.UTF_8));
        }
    }

    public static class Builder {
        private SenderStatusReporter reporter;
        private ScheduledExecutorService tasksExecutor;
        private MemoryQueue.Builder memoryQueueBuilder;
        private DiskQueue.Builder diskQueueBuilder;
        private LogContext logContext;

        /**
         * Tasks executor.
         *
         * @param tasksExecutor the tasks executor
         * @return the builder
         * @author HuangTaiHong
         * @since 2019.11.11 19:29:07
         */
        public Builder tasksExecutor(final ScheduledExecutorService tasksExecutor) {
            this.tasksExecutor = tasksExecutor;
            return this;
        }

        /**
         * Log context.
         *
         * @param logContext the log context
         * @return the builder
         * @author HuangTaiHong
         * @since 2019.11.11 19:29:07
         */
        public Builder logContext(final LogContext logContext) {
            this.logContext = logContext;
            return this;
        }

        /**
         * Reporter.
         *
         * @param reporter the reporter
         * @return the builder
         * @author HuangTaiHong
         * @since 2019.11.11 19:29:07
         */
        public Builder reporter(final SenderStatusReporter reporter) {
            this.reporter = reporter;
            return this;
        }

        /**
         * With memory queue builder.
         *
         * @return the memory queue.builder
         * @author HuangTaiHong
         * @since 2019.11.11 19:29:07
         */
        public MemoryQueue.Builder withMemoryQueueBuilder() {
            if (this.memoryQueueBuilder == null) {
                this.memoryQueueBuilder = MemoryQueue.builder(this);
            }
            return this.memoryQueueBuilder;
        }

        /**
         * With disk queue builder.
         *
         * @return the disk queue.builder
         * @author HuangTaiHong
         * @since 2019.11.11 19:29:07
         */
        public DiskQueue.Builder withDiskQueueBuilder() {
            if (this.diskQueueBuilder == null) {
                this.diskQueueBuilder = DiskQueue.builder(this, this.tasksExecutor);
            }
            return this.diskQueueBuilder;
        }

        /**
         * Sets disk queue builder.
         *
         * @param diskQueueBuilder the disk queue builder
         * @author HuangTaiHong
         * @since 2019.11.11 19:29:07
         */
        public void setDiskQueueBuilder(final DiskQueue.Builder diskQueueBuilder) {
            this.diskQueueBuilder = diskQueueBuilder;
        }

        /**
         * Sets memory queue builder.
         *
         * @param memoryQueueBuilder the memory queue builder
         * @author HuangTaiHong
         * @since 2019.11.11 19:29:07
         */
        public void setMemoryQueueBuilder(final MemoryQueue.Builder memoryQueueBuilder) {
            this.memoryQueueBuilder = memoryQueueBuilder;
        }

        /**
         * Build.
         *
         * @return the log sender
         * @throws LogParameterErrorException the log parameter error exception
         * @author HuangTaiHong
         * @since 2019.11.11 19:29:07
         */
        public LogSender build() throws LogParameterErrorException {
            return new LogSender(this.logContext, this.getLogDataQueue(), this.reporter, this.tasksExecutor);
        }

        /**
         * Gets log data queue.
         *
         * @return the log data queue
         * @throws LogParameterErrorException the log parameter error exception
         * @author HuangTaiHong
         * @since 2019.11.11 19:29:07
         */
        private LogDataQueue getLogDataQueue() throws LogParameterErrorException {
            if (!logContext.isEnabledBufferQueue()) {
                return null;
            }
            if (this.diskQueueBuilder != null) {
                this.diskQueueBuilder.diskSpaceTasks(this.tasksExecutor).reporter(reporter);
                return this.diskQueueBuilder.build();
            } else {
                this.memoryQueueBuilder.reporter(this.reporter);
                return this.memoryQueueBuilder.build();
            }
        }

        /**
         * Gets reporter.
         *
         * @return the reporter
         * @author HuangTaiHong
         * @since 2019.11.11 19:29:07
         */
        public SenderStatusReporter getReporter() {
            return reporter;
        }

        /**
         * Gets log context.
         *
         * @return the log context
         * @author HuangTaiHong
         * @since 2019.11.11 19:29:07
         */
        public LogContext getLogContext() {
            return this.logContext;
        }
    }
}
