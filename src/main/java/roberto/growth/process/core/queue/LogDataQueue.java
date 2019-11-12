/**
 * FileName: LogDataQueue
 * Author:   HuangTaiHong
 * Date:     2019-11-11
 * Description: 日志数据队列.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.core.queue;

import java.io.Closeable;

/**
 * 〈日志数据队列.〉
 *
 * @author HuangTaiHong
 * @since 2019-11-11
 */
public interface LogDataQueue extends Closeable {
    /**
     * 写入队列
     *
     * @param data the data
     * @author HuangTaiHong
     * @since 2019.11.11 19:21:51
     */
    void enqueue(byte[] data);

    /**
     * 弹出队列
     *
     * @return 一条记录 byte [ ]
     * @author HuangTaiHong
     * @since 2019.11.11 19:21:51
     */
    byte[] dequeue();

    /**
     * 队列是否为空
     *
     * @return true :是;  false: 否
     * @author HuangTaiHong
     * @since 2019.11.11 19:21:51
     */
    boolean isEmpty();
}
