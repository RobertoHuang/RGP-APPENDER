/**
 * FileName: LogParameterErrorException
 * Author:   HuangTaiHong
 * Date:     2019-11-11
 * Description: LogParameterErrorException.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.core.exception;

/**
 * 〈LogParameterErrorException.〉
 *
 * @author HuangTaiHong
 * @since 2019-11-11
 */
public class LogParameterErrorException extends Exception {
    private static final long serialVersionUID = 2565583286106522662L;

    public LogParameterErrorException(final String message) {
        super(message);
    }

    public LogParameterErrorException(final Throwable cause) {
        super(cause);
    }

    public LogParameterErrorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LogParameterErrorException(final String property, final String explanation) {
        super(String.format("存在有问题的参数： %s : %s", property, explanation));
    }
}
