package person.pluto.natcrossclient;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;
import person.pluto.natcross.clientitem.ClientControlThread;
import person.pluto.tools.HttpUtils;

@Slf4j
@Component
public class NatcrossClientTask {

    @Scheduled(fixedRate = 60000)
    public void go() {
        String doPost = HttpUtils.doPost(NatcrossSpringConstants.HTTP_SERVER + "/natcross/getAllListenServer", null,
                null);
        if (StringUtils.isBlank(doPost)) {
            log.error("向服务器获取监听状态异常");
            return;
        }
        log.debug("向服务器获取监听状态，{}", doPost);

        JSONObject jsonObject = JSONObject.parseObject(doPost);

        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("listenPortList");

        Set<Integer> hasListenPort = new TreeSet<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject listenPortObject = jsonArray.getJSONObject(i);
            if (listenPortObject.get("serverListenThread") != null) {
                Integer listenPort = listenPortObject.getInteger("listenPort");
                String destIp = listenPortObject.getString("destIp");
                Integer destPort = listenPortObject.getInteger("destPort");

                hasListenPort.add(listenPort);

                ClientControlThread createNewClientThread = NatcrossClientControl.createNewClientThread(listenPort,
                        destIp, destPort);
                if (createNewClientThread == null) {
                    log.warn("创建客户端[{} <-> {}:{}]异常", listenPort, destIp, destPort);
                }
            }
        }

        // 将取消监听的进行去除，以免服务端取消，客户端仍然存在的尴尬
        List<ClientControlThread> clientAllList = NatcrossClientControl.getAll();
        for (ClientControlThread model : clientAllList) {
            if (!hasListenPort.contains(model.getListenServerPort())) {
                NatcrossClientControl.remove(model.getListenServerPort());
            }
        }

    }

}
