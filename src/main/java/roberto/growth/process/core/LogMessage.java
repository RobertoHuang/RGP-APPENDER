/**
 * FileName: LogMessage
 * Author:   HuangTaiHong
 * Date:     2019-11-11
 * Description: 日志消息.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.core;

import roberto.growth.process.core.enums.LogTypeEnum;

import java.util.List;

/**
 * 〈日志消息.〉
 *
 * @author HuangTaiHong
 * @since 2019-11-11
 */
public class LogMessage {private long timestamp;
    /**
     * 环境
     */
    private String env;


    /**
     * 配置的 logger name
     */
    private String logger;

    /**
     * 日志级别
     */
    private String level;

    /**
     * 线程名称
     */
    private String threadName;

    /**
     * 消息体
     */
    private String message;

    /**
     * 完整日志
     */
    private String fullMessage;

    /**
     * 全局调用链ID, 通过它可以实现查询微服务整体调用链路日志
     */
    private String traceId;

    /**
     * 进程内线程执行链路id, 通过它可以实现查询某个请求或调用的完整链路日志
     */
    private String logId;

    /**
     * 服务器ip
     */
    private String ip;

    /**
     * 服务器主机名
     */
    private String hostname;

    /**
     * 日志类型: java日志、nginx日志、JVM指标, 机器指标
     */
    private LogTypeEnum logType;

    /**
     * 当前项目的名称
     */
    private String projectName;

    /**
     * 文件名, 如java类名字, nginx日志文件名字
     */
    private String fileName;

    /**
     * 日志行号
     */
    private int lineNum;

    /**
     * 标签
     */
    private List<String> tags;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public LogTypeEnum getLogType() {
        return logType;
    }

    public void setLogType(LogTypeEnum logType) {
        this.logType = logType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
