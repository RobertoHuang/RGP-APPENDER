/**
 * FileName: DiskQueue
 * Author:   HuangTaiHong
 * Date:     2019-11-11
 * Description: 磁盘Queue.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.core.queue.impl;

import com.bluejeans.bigqueue.BigQueue;
import roberto.growth.process.core.LogSender;
import roberto.growth.process.core.SenderStatusReporter;
import roberto.growth.process.core.exception.LogParameterErrorException;
import roberto.growth.process.core.queue.LogDataQueue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 〈磁盘Queue.〉
 *
 * @author HuangTaiHong
 * @since 2019-11-11
 */
public class DiskQueue implements LogDataQueue {
    public static DiskQueue.Builder builder(final LogSender.Builder context, final ScheduledExecutorService diskSpaceTasks) {
        return new DiskQueue.Builder(context, diskSpaceTasks);
    }

    private final BigQueue logDataQueue;

    private final File queueDir;
    private final int fsPercentThreshold;
    private volatile boolean isEnoughSpace;
    private final boolean dontCheckEnoughDiskSpace;
    private final SenderStatusReporter senderStatusReporter;

    public DiskQueue(final File queueDir, final boolean dontCheckEnoughDiskSpace, final int fsPercentThreshold, final int gcQueueDataIntervalMs, final SenderStatusReporter senderStatusReporter, final int checkDiskSpaceInterval, final ScheduledExecutorService diskSpaceTasks) throws LogParameterErrorException {
        this.queueDir = queueDir;
        this.senderStatusReporter = senderStatusReporter;
        this.validateParameters();
        final String dir = queueDir.getAbsoluteFile().getParent();
        final String queueDirName = queueDir.getName();
        if (dir != null && !queueDirName.isEmpty()) {
            this.logDataQueue = new BigQueue(dir, queueDirName);
            this.dontCheckEnoughDiskSpace = dontCheckEnoughDiskSpace;
            this.fsPercentThreshold = fsPercentThreshold;
            this.isEnoughSpace = true;
            diskSpaceTasks.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    DiskQueue.this.gcBigQueue();
                }
            }, 0L, gcQueueDataIntervalMs, TimeUnit.SECONDS);

            diskSpaceTasks.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    DiskQueue.this.validateEnoughSpace();
                }
            }, 0L, checkDiskSpaceInterval, TimeUnit.MILLISECONDS);
        } else {
            throw new LogParameterErrorException(String.format("queueDir value is empty: %s", queueDir.getAbsolutePath()));
        }
    }

    void gcBigQueue() {
        try {
            this.logDataQueue.gc();
        } catch (Exception e) {
            this.senderStatusReporter.error("Uncaught error from BigQueue.gc()", e);
        }
    }

    void validateEnoughSpace() {
        try {
            if (this.dontCheckEnoughDiskSpace) {
                return;
            }
            // 得到当前磁盘使用率
            final int actualUsedFsPercent = 100 - (int) ((double) this.queueDir.getUsableSpace() / (double) this.queueDir.getTotalSpace() * 100.00);
            // 是否超过预设阈值
            if (actualUsedFsPercent >= this.fsPercentThreshold) {
                if (this.isEnoughSpace) {
                    this.senderStatusReporter.warn(String.format("磁盘空间紧张,路径:%s, 已经使用:%d 百分百, 当前设置的阈值: %d 百分百", this.queueDir.getAbsolutePath(), actualUsedFsPercent, this.fsPercentThreshold));
                }
                this.isEnoughSpace = false;
            } else {
                this.isEnoughSpace = true;
            }
        } catch (Exception e) {
            this.senderStatusReporter.error("Uncaught error from validateEnoughSpace()", e);
        }
    }

    private void validateParameters() throws LogParameterErrorException {
        if (this.queueDir == null) {
            throw new LogParameterErrorException("queueDir不能为空");
        } else if (this.senderStatusReporter == null) {
            throw new LogParameterErrorException("reporter不能为空");
        }
    }

    @Override
    public boolean isEmpty() {
        return logDataQueue.isEmpty();
    }

    @Override
    public void enqueue(final byte[] data) {
        if (this.isEnoughSpace) {
            this.logDataQueue.enqueue(data);
        }
    }

    @Override
    public byte[] dequeue() {
        return this.logDataQueue.dequeue();
    }

    @Override
    public void close() throws IOException {
        this.gcBigQueue();
        this.logDataQueue.close();
    }

    public static class Builder {
        private boolean dontCheckEnoughDiskSpace = false;
        private int fsPercentThreshold;
        private int gcQueueDataIntervalMs;
        private int checkDiskSpaceInterval;
        private File queueDir;
        private SenderStatusReporter reporter;
        private ScheduledExecutorService diskSpaceTasks;
        private final LogSender.Builder context;

        Builder(final LogSender.Builder context, final ScheduledExecutorService diskSpaceTasks) {
            this.diskSpaceTasks = diskSpaceTasks;
            this.context = context;
        }

        public Builder fsPercentThreshold(final int fsPercentThreshold) {
            this.fsPercentThreshold = fsPercentThreshold;
            // -1 不检查
            if (fsPercentThreshold == -1) {
                this.dontCheckEnoughDiskSpace = true;
            }
            return this;
        }

        public Builder gcQueueDataIntervalMs(final int gcQueueDataIntervalMs) {
            this.gcQueueDataIntervalMs = gcQueueDataIntervalMs;
            return this;
        }

        public Builder checkDiskSpaceInterval(final int checkDiskSpaceInterval) {
            this.checkDiskSpaceInterval = checkDiskSpaceInterval;
            return this;
        }

        public Builder queueDir(final File queueDir) {
            this.queueDir = queueDir;
            return this;
        }

        public Builder reporter(final SenderStatusReporter reporter) {
            this.reporter = reporter;
            return this;
        }

        public Builder diskSpaceTasks(final ScheduledExecutorService diskSpaceTasks) {
            this.diskSpaceTasks = diskSpaceTasks;
            return this;
        }

        public LogSender.Builder endDiskQueue() {
            this.context.setDiskQueueBuilder(this);
            return this.context;
        }

        public DiskQueue build() throws LogParameterErrorException {
            return new DiskQueue(this.queueDir, this.dontCheckEnoughDiskSpace, this.fsPercentThreshold, this.gcQueueDataIntervalMs, this.reporter, this.checkDiskSpaceInterval, this.diskSpaceTasks);
        }
    }
}
