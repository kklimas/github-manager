package com.github.manager.configuration;

import com.github.manager.properties.GithubApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.WebClient;

@EnableWebFlux
@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {
    private static final String GITHUB_API_HEADER = "X-GitHub-Api-Version";

    private final GithubApiProperties clientProperties;

    @Bean
    public WebClient githubWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(clientProperties.getUrl())
                .defaultHeader(GITHUB_API_HEADER, clientProperties.getApiVersion())
                .build();
    }
}
