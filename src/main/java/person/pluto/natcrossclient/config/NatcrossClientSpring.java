package person.pluto.natcrossclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import person.pluto.natcrossclient.model.CommonClientConfig;

@Configuration
public class NatcrossClientSpring {

    @Bean("commonClientConfig")
    @Primary
    @ConfigurationProperties(prefix = "natcross")
    public CommonClientConfig getCommonClientConfig() {
        return new CommonClientConfig();
    }

}
