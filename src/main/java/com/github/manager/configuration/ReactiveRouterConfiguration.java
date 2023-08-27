package com.github.manager.configuration;

import com.github.manager.properties.GithubApiProperties;
import com.github.manager.service.GithubUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class ReactiveRouterConfiguration {

    private final GithubApiProperties githubClientProperties;

    @Bean
    public RouterFunction<ServerResponse> routes(GithubUserService userService) {
        return route(
                GET(githubClientProperties.getUserDetailsPath())
                        .and(accept(MediaType.APPLICATION_JSON)), userService::findUserDetails).andRoute(
                GET(githubClientProperties.getUserDetailsPath())
                        .and(accept(MediaType.ALL)), userService::handleNotAllowedMediaType);
    }

}
