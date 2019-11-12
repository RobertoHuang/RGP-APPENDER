/**
 * FileName: CommonOps
 * Author:   HuangTaiHong
 * Date:     2019-11-11
 * Description: CommonOps.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.core;

import cn.hutool.core.thread.NamedThreadFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * 〈CommonOps.〉
 *
 * @author HuangTaiHong
 * @since 2019-11-11
 */
public class CommonOps {
    private static final int TASK_THREAD_NUM = 3;
    private static final String TASK_THREAD_NAME = "log-appender-task";

    /**
     * Instantiates a new Common ops.
     *
     * @author HuangTaiHong
     * @since 2019.11.11 23:08:46
     */
    private CommonOps() {

    }

    /**
     * 初始化队列逻辑
     *
     * @param builder the builder
     * @return boolean boolean
     * @author HuangTaiHong
     * @since 2019.11.11 23:08:46
     */
    public static boolean initQueue(final LogSender.Builder builder) {
        if (builder.getLogContext().isEnabledBufferQueue()) {
            if (builder.getLogContext().isUsedMemoryQueue()) {
                return activeMemoryQueue(builder);
            } else {
                return activeDiskQueue(builder);
            }
        }
        return true;
    }

    /**
     * Active memory queue.
     *
     * @param builder the builder
     * @return the boolean
     * @author HuangTaiHong
     * @since 2019.11.11 23:08:46
     */
    @SuppressWarnings("PMD")
    private static boolean activeMemoryQueue(final LogSender.Builder builder) {
        final LogContext logContext = builder.getLogContext();
        if (!validateQueueCapacity(logContext, builder.getReporter())) {
            return false;
        }
        builder.tasksExecutor(Executors.newScheduledThreadPool(1, new NamedThreadFactory(TASK_THREAD_NAME, true)))
                .withMemoryQueueBuilder()
                .capacityInBytes(logContext.getMemoryQueueCapacityBytes())
                .logCountLimit(logContext.getMemoryLogCountCapacity())
                .endMemoryQueue();
        return true;
    }

    /**
     * Active disk queue.
     *
     * @param builder the builder
     * @return the boolean
     * @author HuangTaiHong
     * @since 2019.11.11 23:08:46
     */
    @SuppressWarnings("PMD")
    private static boolean activeDiskQueue(final LogSender.Builder builder) {
        final LogContext logContext = builder.getLogContext();
        if (!validateFileSystemFullPercentThreshold(logContext)) {
            return false;
        }
        final SenderStatusReporter reporter = builder.getReporter();
        final File queueDirFile = getQueueDirFile(logContext, reporter);
        if (queueDirFile == null) {
            return false;
        }
        builder.tasksExecutor(Executors.newScheduledThreadPool(TASK_THREAD_NUM, new NamedThreadFactory(TASK_THREAD_NAME, true)))
                .withDiskQueueBuilder()
                .queueDir(queueDirFile)
                .fsPercentThreshold(logContext.getFsPercentThreshold())
                .gcQueueDataIntervalMs(logContext.getGcQueueDataIntervalMs())
                .checkDiskSpaceInterval(logContext.getCheckDiskSpaceIntervalMs())
                .endDiskQueue();
        return true;
    }

    /**
     * Validate queue capacity.
     *
     * @param logContext the log context
     * @param reporter   the reporter
     * @return the boolean
     * @author HuangTaiHong
     * @since 2019.11.11 23:08:46
     */
    private static boolean validateQueueCapacity(final LogContext logContext, final SenderStatusReporter reporter) {
        if (logContext.getMemoryLogCountCapacity() <= 0 && logContext.getMemoryLogCountCapacity() != -1) {
            reporter.error("MemoryLogCountCapacity 只能设置为非0整数或-1值");
            return false;
        }

        if (logContext.getMemoryQueueCapacityBytes() <= 0 && logContext.getMemoryQueueCapacityBytes() != -1) {
            reporter.error("MemoryQueueCapacityBytes 只能设置为非0整数或-1值");
            return false;
        }
        return true;
    }

    /**
     * Validate file system full percent threshold.
     *
     * @param logContext the log context
     * @return the boolean
     * @author HuangTaiHong
     * @since 2019.11.11 23:08:47
     */
    private static boolean validateFileSystemFullPercentThreshold(final LogContext logContext) {
        if (logContext.getFsPercentThreshold() >= 1 && logContext.getFsPercentThreshold() <= 100) {
            return true;
        }
        // 文件系统使用占比配置 不在1%-100%之间; -1:代表不检查
        return logContext.getFsPercentThreshold() == -1;
    }

    /**
     * Gets queue dir file.
     *
     * @param logContext the log context
     * @param reporter   the reporter
     * @return the queue dir file
     * @author HuangTaiHong
     * @since 2019.11.11 23:08:47
     */
    private static File getQueueDirFile(final LogContext logContext, final SenderStatusReporter reporter) {
        final String queueDirPath;
        if (StringUtils.isNotBlank(logContext.getQueueDir())) {
            queueDirPath = logContext.getQueueDir();
            final File queueFile = new File(queueDirPath);
            if (queueFile.exists()) {
                if (!queueFile.canWrite()) {
                    reporter.error("无权限往队列文件写入数据: file=" + queueFile);
                    return null;
                }
            } else {
                if (!queueFile.mkdir()) {
                    reporter.error("无法创建队列文件：" + queueFile);
                    return null;
                }
            }
        } else {
            queueDirPath = System.getProperty("JAVA.io.tmpdir") + File.separator + "log-log4j2-buffer";
        }
        return new File(queueDirPath);
    }
}
