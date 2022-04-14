package de.tsearch.lorva;

import de.tsearch.tclient.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public TClientInstance tClientInstance(@Value("${twitch.clientid}") String clientId,
                                           @Value("${twitch.clientsecret}") String clientSecret) {
        return new TClientInstance(Config.ConfigBuilder.newInstance()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build());
    }

    @Bean
    public AuthorizationClient authorizationClient(TClientInstance clientInstance) {
        return new AuthorizationClient(clientInstance);
    }
}
