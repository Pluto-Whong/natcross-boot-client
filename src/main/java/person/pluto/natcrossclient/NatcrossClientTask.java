package person.pluto.natcrossclient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;
import person.pluto.natcross2.clientside.ClientControlThread;
import person.pluto.natcross2.clientside.config.SecretInteractiveClientConfig;
import person.pluto.natcrossclient.model.CommonClientConfig;
import person.pluto.tools.HttpUtils;
import person.pluto.tools.SignTools;

@Slf4j
@Component
public class NatcrossClientTask {

    @Autowired
    private CommonClientConfig commonClientConfig;

    @Scheduled(fixedRate = 60000)
    public void go() {
        Map<String, String> param = new HashMap<>();
        SignTools.fullSignature(param, commonClientConfig.getHttpProjectSign());
        String doPost = HttpUtils
                .doPost(commonClientConfig.getHttpServer() + "/natcross/projectSign/getAllListenServer", param, null);
        if (StringUtils.isBlank(doPost)) {
            log.error("向服务器获取监听状态异常");
            return;
        }
        log.debug("向服务器获取监听状态，{}", doPost);

        JSONObject jsonObject = JSONObject.parseObject(doPost);

        JSONArray listenPortList = jsonObject.getJSONObject("data").getJSONArray("listenPortList");

        Set<Integer> hasListenPort = new TreeSet<>();

        for (int i = 0; i < listenPortList.size(); i++) {
            JSONObject listenPortObject = listenPortList.getJSONObject(i);

            JSONObject listenStatus = listenPortObject.getJSONObject("listenStatus");

            Integer statusCode = listenStatus.getInteger("code");
            // 若不是停止状态
            if (!statusCode.equals(1)) {
                Integer listenPort = listenPortObject.getInteger("listenPort");
                String destIp = listenPortObject.getString("destIp");
                Integer destPort = listenPortObject.getInteger("destPort");

                hasListenPort.add(listenPort);

                SecretInteractiveClientConfig config = new SecretInteractiveClientConfig();
                config.setListenServerPort(listenPort);
                config.setClientServiceIp(commonClientConfig.getClientServerIp());
                config.setClientServicePort(commonClientConfig.getClientServerPort());
                config.setDestIp(destIp);
                config.setDestPort(destPort);
                config.setBaseAesKey(commonClientConfig.getAeskey());
                config.setTokenKey(commonClientConfig.getTokenKey());

                ClientControlThread createNewClientThread = NatcrossClientControl.createNewClientThread(config);
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
