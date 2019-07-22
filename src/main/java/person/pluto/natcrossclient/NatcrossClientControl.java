package person.pluto.natcrossclient;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;
import person.pluto.natcross.clientitem.ClientControlThread;
import person.pluto.natcross.common.NatcrossConstants;

/**
 * <p>
 * 客户端控制类
 * </p>
 *
 * @author wangmin1994@qq.com
 * @since 2019-07-05 11:25:44
 */
@Slf4j
public class NatcrossClientControl {

    private static Map<Integer, ClientControlThread> serverListenMap = new TreeMap<>();

    /**
     * 加入新的客户端线程
     *
     * @author Pluto
     * @since 2019-07-22 16:42:13
     * @param clientControl
     * @return
     */
    public static boolean add(ClientControlThread clientControl) {
        if (clientControl == null) {
            return false;
        }

        Integer listenPort = clientControl.getListenServerPort();
        ClientControlThread clientControlThread = serverListenMap.get(listenPort);
        if (clientControlThread != null) {
            // 必须要先remove掉才能add
            return false;
        }

        serverListenMap.put(listenPort, clientControl);
        return true;
    }

    /**
     * 除去指定的客户端端口
     *
     * @author Pluto
     * @since 2019-07-22 16:50:10
     * @param listenPort
     * @return
     */
    public static boolean remove(Integer listenPort) {
        ClientControlThread clientControlThread = serverListenMap.get(listenPort);
        if (clientControlThread == null) {
            return true;
        }

        clientControlThread.cancell();
        serverListenMap.remove(listenPort);

        return true;
    }

    /**
     * 根据端口获取客户端线程
     *
     * @author Pluto
     * @since 2019-07-18 18:36:52
     * @param listenPort
     * @return
     */
    public static ClientControlThread get(Integer listenPort) {
        return serverListenMap.get(listenPort);
    }

    /**
     * 获取全部监听服务
     *
     * @author Pluto
     * @since 2019-07-19 15:31:55
     * @return
     */
    public static List<ClientControlThread> getAll() {
        List<ClientControlThread> list = new LinkedList<>();
        serverListenMap.forEach((key, value) -> {
            list.add(value);
        });
        return list;
    }

    /**
     * 关闭所有监听服务
     *
     * @author Pluto
     * @since 2019-07-18 19:00:54
     */
    public static void closeAll() {
        Set<Integer> keySet = serverListenMap.keySet();
        Integer[] array = keySet.toArray(new Integer[keySet.size()]);
        for (Integer key : array) {
            remove(key);
        }
    }

    /**
     * 创建新的客户端进程
     *
     * @author Pluto
     * @since 2019-07-22 16:55:57
     * @param port
     * @param destIp
     * @param destPort
     * @return
     */
    public static ClientControlThread createNewClientThread(Integer port, String destIp, Integer destPort) {

        // 如果之前存在，并且还活着，则使用以前的，遵从以前连接不清理的原则，只更新dest便可很友好完成变更目标的目的
        ClientControlThread clientControlThread = NatcrossClientControl.get(port);
        if (clientControlThread != null && clientControlThread.isAlive()) {
            clientControlThread.setDestIpPort(destIp, destPort);
            return clientControlThread;
        }

        // 不管是不存在，还是不在活跃状态，都符合去除设定，即时下面创建失败，也应该除去这些无用的线程
        NatcrossClientControl.remove(port);

        try {
            clientControlThread = new ClientControlThread(NatcrossConstants.CLIENT_SERVER_IP,
                    NatcrossConstants.CLIENT_SERVER_PORT, port, destIp, destPort);
        } catch (IOException e) {
            log.warn("create client thread [{} <-> {}:{}] faild", port, destIp, destPort);
            return null;
        }
        try {
            boolean createControl = clientControlThread.createControl();
            if (!createControl) {
                clientControlThread.cancell();
                return null;
            }
        } catch (IOException e) {
            clientControlThread.cancell();
            return null;
        }
        NatcrossClientControl.add(clientControlThread);
        return clientControlThread;
    }

}
