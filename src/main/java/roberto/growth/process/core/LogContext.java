/**
 * FileName: LogContext
 * Author:   HuangTaiHong
 * Date:     2019-11-11
 * Description: 日志上下文.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.core;

import roberto.growth.process.core.utils.InternetUtils;
import roberto.growth.process.core.utils.ProjectUtils;

import java.util.Properties;

import static com.sun.org.apache.xml.internal.serializer.Method.UNKNOWN;

/**
 * 〈日志上下文.〉
 *
 * @author HuangTaiHong
 * @since 2019-11-11
 */
public class LogContext {
    private String projectName;
    private final String ip;
    private final String hostName;

    private String bootstrapServers;
    private String topic;

    /**
     * 是否开启缓存队列
     */
    private boolean enabledBufferQueue;

    /**
     * 是否使用内存队列
     */
    private boolean usedMemoryQueue = false;

    /**
     * 内存队列最大容量
     */
    private long memoryQueueCapacityBytes;

    /**
     * 内存队列最大存储日志条数
     */
    private long memoryLogCountCapacity;

    /**
     * 文件队列存放路径
     */
    private String queueDir;

    /**
     * 文件系统阈值百分百, 超过阈值时直接丢弃日志，防止系统不可用
     */
    private int fsPercentThreshold;

    /**
     * 默认任务持久化间隔，单位MS
     */
    private int gcQueueDataIntervalMs;

    /**
     * 默认的检查磁盘空间时间间隔, 单位MS
     */
    private int checkDiskSpaceIntervalMs;

    public LogContext() {
        this.projectName = ProjectUtils.getProjectName();
        this.ip = InternetUtils.getLocalIpAddress();
        this.hostName = InternetUtils.getHostName();
    }

    public String getIp() {
        return ip;
    }

    public String getHostName() {
        return hostName;
    }

    public String projectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isEnabledBufferQueue() {
        return enabledBufferQueue;
    }

    public void setEnabledBufferQueue(boolean enabledBufferQueue) {
        this.enabledBufferQueue = enabledBufferQueue;
    }

    public boolean isUsedMemoryQueue() {
        return usedMemoryQueue;
    }

    public void setUsedMemoryQueue(boolean usedMemoryQueue) {
        this.usedMemoryQueue = usedMemoryQueue;
    }

    public long getMemoryQueueCapacityBytes() {
        return memoryQueueCapacityBytes;
    }

    public void setMemoryQueueCapacityBytes(long memoryQueueCapacityBytes) {
        this.memoryQueueCapacityBytes = memoryQueueCapacityBytes;
    }

    public long getMemoryLogCountCapacity() {
        return memoryLogCountCapacity;
    }

    public void setMemoryLogCountCapacity(long memoryLogCountCapacity) {
        this.memoryLogCountCapacity = memoryLogCountCapacity;
    }

    public String getQueueDir() {
        return queueDir;
    }

    public void setQueueDir(String queueDir) {
        this.queueDir = queueDir;
    }

    public int getFsPercentThreshold() {
        return fsPercentThreshold;
    }

    public void setFsPercentThreshold(int fsPercentThreshold) {
        this.fsPercentThreshold = fsPercentThreshold;
    }

    public int getGcQueueDataIntervalMs() {
        return gcQueueDataIntervalMs;
    }

    public void setGcQueueDataIntervalMs(int gcQueueDataIntervalMs) {
        this.gcQueueDataIntervalMs = gcQueueDataIntervalMs;
    }

    public int getCheckDiskSpaceIntervalMs() {
        return checkDiskSpaceIntervalMs;
    }

    public void setCheckDiskSpaceIntervalMs(int checkDiskSpaceIntervalMs) {
        this.checkDiskSpaceIntervalMs = checkDiskSpaceIntervalMs;
    }
}
