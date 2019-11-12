/**
 * FileName: SenderStatusReporter
 * Author:   HuangTaiHong
 * Date:     2019-11-11
 * Description: Sender状态上报器.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.core;

/**
 * 〈Sender状态上报器.〉
 *
 * @author HuangTaiHong
 * @since 2019-11-11
 */
public interface SenderStatusReporter {
    /**
     * info级别日志
     *
     * @param msg the msg
     * @author HuangTaiHong
     * @since 2019.11.11 19:24:11
     */
    void info(String msg);

    /**
     * info级别日志
     *
     * @param msg       the msg
     * @param throwable the throwable
     * @author HuangTaiHong
     * @since 2019.11.11 19:24:11
     */
    void info(String msg, Throwable throwable);

    /**
     * 警告级别日志
     *
     * @param msg the msg
     * @author HuangTaiHong
     * @since 2019.11.11 19:24:11
     */
    void warn(String msg);

    /**
     * 警告级别日志
     *
     * @param msg       the msg
     * @param throwable the throwable
     * @author HuangTaiHong
     * @since 2019.11.11 19:24:11
     */
    void warn(String msg, Throwable throwable);

    /**
     * 错误级别日志
     *
     * @param msg the msg
     * @author HuangTaiHong
     * @since 2019.11.11 19:24:11
     */
    void error(String msg);

    /**
     * 错误级别日志
     *
     * @param msg       the msg
     * @param throwable the throwable
     * @author HuangTaiHong
     * @since 2019.11.11 19:24:11
     */
    void error(String msg, Throwable throwable);
}
