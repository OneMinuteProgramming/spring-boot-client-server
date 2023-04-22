package com.lc.springbootclientserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Configuration
@Slf4j
public class WebClientConfig {


    @Bean
    public ExchangeFilterFunction authFilter(ReactiveClientRegistrationRepository clientRegistrations) {
        InMemoryReactiveOAuth2AuthorizedClientService clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrations);
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrations, clientService);
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultClientRegistrationId("op");
        return oauth;
    }

    @Bean
    public WebClient resourceWebClient(ExchangeFilterFunction authFilter) {
        return WebClient.builder().baseUrl("http://localhost:8090/")
                .filter(authFilter)
                .filter(requestFilter())
                .filter(responseFilter())
                .build();
    }

    private ExchangeFilterFunction requestFilter() {
        return (clientRequest, nextFilter) -> {
            log.info("Request Header: {}", clientRequest.headers().entrySet());
            return nextFilter.exchange(clientRequest);
        };
    }

    private ExchangeFilterFunction responseFilter() {
        return ExchangeFilterFunction.ofResponseProcessor((response) -> Mono.just(response.mutate()
                .body(db -> db.map(getDataBufferDataBufferFunction(response))).build()));
    }

    private Function<DataBuffer, DataBuffer> getDataBufferDataBufferFunction(ClientResponse response) {
        return db -> {
            log.info("Response body: {}", db.toString(StandardCharsets.UTF_8));
            log.info("Response status: {}", response.statusCode());
            return db;
        };
    }
}
