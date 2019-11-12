/**
 * FileName: ProjectUtils
 * Author:   HuangTaiHong
 * Date:     2019-11-11
 * Description: 项目工具类.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.core.utils;

/**
 * 〈项目工具类.〉
 *
 * @author HuangTaiHong
 * @since 2019-11-11
 */
public class ProjectUtils {
    public static final String UNKNOWN = "unknown";

    /**
     * 用于获取当前项目名.
     *
     * @return the project name
     * @author HuangTaiHong
     * @since 2019.11.11 17:01:59
     */
    public static String getProjectName() {
        // TODO 伪代码 需自定义扩展
        return "ROBERTO-APPENDER";
    }

    /**
     * 获取项目当前运行环境.
     *
     * @return the env
     * @author HuangTaiHong
     * @since 2019.11.11 19:18:17
     */
    public static String getEnv() {
        return "PROD";
    }
}
