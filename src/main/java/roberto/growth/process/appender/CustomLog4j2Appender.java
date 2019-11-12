/**
 * FileName: CustomLog4j2Appender
 * Author:   HuangTaiHong
 * Date:     2019-11-11
 * Description: 自定义Log4j2 Appender的实现.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.appender;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import roberto.growth.process.core.CommonOps;
import roberto.growth.process.core.LogContext;
import roberto.growth.process.core.LogMessage;
import roberto.growth.process.core.LogSender;
import roberto.growth.process.core.SenderStatusReporter;
import roberto.growth.process.core.enums.LogTypeEnum;
import roberto.growth.process.core.exception.LogParameterErrorException;
import roberto.growth.process.core.utils.ProjectUtils;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 〈自定义Log4j2 Appender的实现.〉
 *
 * @author HuangTaiHong
 * @since 2019-11-11
 */
@Plugin(name = "CustomLog4j2Appender", category = Node.CATEGORY, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class CustomLog4j2Appender extends AbstractAppender {
    private static final String DEFAULT_FULL_MESSAGE_PATTERN = "%date{yyyy-MM-dd HH:mm:ss} [%-5level] [%thread] %class{1.}.%method(%file:%line) - %msg%n";

    private final LogContext logContext;
    private LogSender logSender;
    private static final ThreadLocal<Boolean> RECURSIVE_CHECK = new ThreadLocal<>();
    private static final String IGNORE_PACKAGE_NAME = "roberto.growth.process.mq";

    public CustomLog4j2Appender(final String name, final Layout<? extends Serializable> layout, final Filter filter, final boolean ignoreExceptions, final LogContext logContext) {
        super(name, filter, layout, ignoreExceptions);
        this.logContext = logContext;
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void start() {
        if (logContext == null) {
            LOGGER.warn("CustomLog4j2Appender未启动成功");
            return;
        }
        final SenderStatusReporter reporter = new LogStatusReporter(LOGGER);
        final LogSender.Builder builder = LogSender.builder().logContext(logContext).reporter(reporter);
        if (!CommonOps.initQueue(builder)) {
            return;
        }
        try {
            logSender = builder.build();
        } catch (LogParameterErrorException e) {
            LOGGER.error("不能初始化LogSender实例", e);
            return;
        }
        logSender.start();
        super.start();
    }


    @Override
    public void append(final LogEvent event) {
        if (!super.isStarted() || logContext == null) {
            LOGGER.warn("CustomLog4j2Appender未启动成功");
            return;
        }
        if (isRecursive(event)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Recursive logging in appender {}: logger={}, level={}, message={}, exception={}", this.getName(), event.getLoggerName(), event.getLevel(), event.getMessage(), ExceptionUtils.getStackTrace(event.getThrown()), new RuntimeException("here is recursive call stack; Please Ignore It !!!"));
            }
        } else {
            this.formatMessage(event);
        }
    }

    @Override
    public boolean stop(final long timeout, final TimeUnit timeUnit) {
        LOGGER.info("开始Stop CustomLog4j2Appender");
        this.setStopping();
        final boolean stopped = super.stop(timeout, timeUnit, false);
        if (logSender != null) {
            logSender.stop();
        }
        this.setStopped();
        LOGGER.info("成功Stop CustomLog4j2Appender");
        return stopped;
    }

    private boolean isRecursive(final LogEvent event) {
        if (RECURSIVE_CHECK.get() != null && RECURSIVE_CHECK.get() == true) {
            return true;
        }
        return event.getLoggerName() != null && (event.getLoggerName().startsWith(IGNORE_PACKAGE_NAME));
    }

    private void formatMessage(final LogEvent logEvent) {
        final LogMessage logMessage = new LogMessage();
        final ReadOnlyStringMap mdcProperties = logEvent.getContextData();
        if (mdcProperties != null) {
            final String zeusMessageId = mdcProperties.getValue("zeusMessageId");
            final String zeusRootMessageId = mdcProperties.getValue("zeusRootMessageId");
            if (StringUtils.isNotEmpty(zeusMessageId)) {
                logMessage.setLogId(zeusMessageId);
            }
            if (StringUtils.isNotEmpty(zeusRootMessageId)) {
                logMessage.setTraceId(zeusRootMessageId);
            }
        }
        logMessage.setTimestamp(logEvent.getTimeMillis());
        logMessage.setLevel(logEvent.getLevel().toString().toLowerCase(Locale.getDefault()));
        logMessage.setMessage(logEvent.getMessage().getFormattedMessage());
        logMessage.setFullMessage(super.getLayout().toSerializable(logEvent).toString());
        logMessage.setLogger(logEvent.getLoggerName());
        logMessage.setThreadName(logEvent.getThreadName());
        logMessage.setIp(logContext.getIp());
        logMessage.setHostname(logContext.getHostName());
        logMessage.setLogType(LogTypeEnum.JAVA);
        logMessage.setEnv(ProjectUtils.getEnv());
        logMessage.setProjectName(logContext.projectName());
        if (logEvent.getSource() != null) {
            logMessage.setFileName(logEvent.getSource().getFileName());
            logMessage.setLineNum(logEvent.getSource().getLineNumber());
        }
        try {
            RECURSIVE_CHECK.set(Boolean.TRUE);
            logSender.writeQueue(logMessage);
        } finally {
            RECURSIVE_CHECK.remove();
        }
    }

    @Override
    public String toString() {
        return "CustomLog4j2Appender{" + "name=" + this.getName() + ", state=" + this.getState() + '}';
    }

    public static class Builder<B extends CustomLog4j2Appender.Builder<B>> extends AbstractAppender.Builder<B> implements org.apache.logging.log4j.core.util.Builder<CustomLog4j2Appender> {
        @PluginBuilderAttribute
        private String projectName;

        /**
         * 启用 / 禁用 缓冲队列
         * <p>
         * 如果启用那么根据 {@usedMemoryQueue} 参数决定是使用文件映射队列还是JVM堆内存队列，默认禁用日志会直接发送到MQ
         */
        @PluginBuilderAttribute
        private boolean enabledBufferQueue = false;

        /**
         * 默认使用文件内存映射, 来降低对目标应用的GC影响
         */
        @PluginBuilderAttribute
        private boolean usedMemoryQueue = false;

        /**
         * 默认JVM内存队列容量大小:100MB
         */
        @PluginBuilderAttribute
        private long memoryQueueCapacityBytes = 100 * 1024 * 1024;

        /**
         * 默认JVM内存队列日志条数容量大小，-1表示不限制条数
         */
        @PluginBuilderAttribute
        private long memoryLogCountCapacity = -1;

        /**
         * 文件队列存放路径
         */
        @PluginBuilderAttribute
        private String queueDir;

        /**
         * 文件系统阈值百分百, 超过阈值时直接丢弃日志, 防止系统不可用
         */
        @PluginBuilderAttribute
        private int fsPercentThreshold = 85;

        /**
         * 默认任务持久化间隔，单位MS
         */
        @PluginBuilderAttribute
        private int gcQueueDataIntervalMs = 3000;

        /**
         * 默认的检查磁盘空间时间间隔, 单位MS
         */
        @PluginBuilderAttribute
        private int checkDiskSpaceIntervalMs = 1000;

        @Override
        public CustomLog4j2Appender build() {
            Layout<? extends Serializable> layout = this.getLayout();
            if (layout == null) {
                AbstractLifeCycle.LOGGER.warn("No layout provided for CustomLog4j2Appender; Used Default Layout");
                layout = initPatternLayout();
            }
            final LogContext logContext = new LogContext();
            logContext.setProjectName(projectName);
            logContext.setUsedMemoryQueue(usedMemoryQueue);
            logContext.setGcQueueDataIntervalMs(gcQueueDataIntervalMs);
            logContext.setMemoryQueueCapacityBytes(memoryQueueCapacityBytes);
            logContext.setMemoryLogCountCapacity(memoryLogCountCapacity);

            logContext.setQueueDir(queueDir);
            logContext.setFsPercentThreshold(fsPercentThreshold);
            logContext.setCheckDiskSpaceIntervalMs(checkDiskSpaceIntervalMs);
            if (ProjectUtils.UNKNOWN.equals(logContext.projectName())) {
                LOGGER.warn("Can`t find project name. Please config it;");
                return new CustomLog4j2Appender(this.getName(), layout, this.getFilter(), this.isIgnoreExceptions(), null);
            }

            logContext.setBootstrapServers("127.0.0.1:9092");
            logContext.setTopic("TEST-TOPIC");
            logContext.setEnabledBufferQueue(enabledBufferQueue);
            return new CustomLog4j2Appender(this.getName(), layout, this.getFilter(), this.isIgnoreExceptions(), logContext);
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public void setEnabledBufferQueue(boolean enabledBufferQueue) {
            this.enabledBufferQueue = enabledBufferQueue;
        }

        public void setUsedMemoryQueue(boolean usedMemoryQueue) {
            this.usedMemoryQueue = usedMemoryQueue;
        }

        public void setMemoryQueueCapacityBytes(long memoryQueueCapacityBytes) {
            this.memoryQueueCapacityBytes = memoryQueueCapacityBytes;
        }

        public void setMemoryLogCountCapacity(long memoryLogCountCapacity) {
            this.memoryLogCountCapacity = memoryLogCountCapacity;
        }

        public void setQueueDir(String queueDir) {
            this.queueDir = queueDir;
        }

        public void setFsPercentThreshold(int fsPercentThreshold) {
            this.fsPercentThreshold = fsPercentThreshold;
        }

        public void setGcQueueDataIntervalMs(int gcQueueDataIntervalMs) {
            this.gcQueueDataIntervalMs = gcQueueDataIntervalMs;
        }

        public void setCheckDiskSpaceIntervalMs(int checkDiskSpaceIntervalMs) {
            this.checkDiskSpaceIntervalMs = checkDiskSpaceIntervalMs;
        }

        public static PatternLayout initPatternLayout() {
            return PatternLayout.newBuilder().withPattern(CustomLog4j2Appender.DEFAULT_FULL_MESSAGE_PATTERN).build();
        }
    }

    private static class LogStatusReporter implements SenderStatusReporter {
        private final Logger logger;

        LogStatusReporter(final Logger logger) {
            this.logger = logger;
        }

        @Override
        public void info(final String msg) {
            this.logger.info(msg);
        }

        @Override
        public void info(final String msg, final Throwable throwable) {
            this.logger.info(msg, throwable);
        }

        @Override
        public void warn(final String msg) {
            this.logger.warn(msg);
        }

        @Override
        public void warn(final String msg, final Throwable throwable) {
            this.logger.warn(msg, throwable);
        }

        @Override
        public void error(final String msg) {
            this.logger.error(msg);
        }

        @Override
        public void error(final String msg, final Throwable throwable) {
            this.logger.error(msg, throwable);
        }
    }
}
