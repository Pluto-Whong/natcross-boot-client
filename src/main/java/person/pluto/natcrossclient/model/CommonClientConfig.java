package person.pluto.natcrossclient.model;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

/**
 * <p>
 * 公共配置模型
 * </p>
 *
 * @author Pluto
 * @since 2020-01-10 16:16:59
 */
@Data
public class CommonClientConfig {

    /**
     * 服务端端口配置地址
     */
    private String httpServer;

    /**
     * 服务端请求签名密钥
     */
    private String httpProjectSign;

    /**
     * 客户端服务IP
     */
    private String clientServerIp;
    /**
     * 客户端服务端口
     */
    private Integer clientServerPort;

    /**
     * base64格式的AES密钥
     */
    private String aeskey;

    /**
     * 交互签名密钥
     */
    private String tokenKey;

    /**
     * 判断是否启用加密模式
     * 
     * @author Pluto
     * @since 2020-01-10 09:57:55
     * @return
     */
    public boolean isValid() {
        return StringUtils.isNoneBlank(this.getAeskey(), this.getTokenKey());
    }

}
