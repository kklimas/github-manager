package com.github.manager.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Slf4j
@Component
public class AcceptWebFilter implements WebFilter {

    private static final String NOT_ACCEPTABLE_MSG = "Can only accept header with MediaType application/json.";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var acceptedMediaType = exchange.getRequest().getHeaders()
                .getAccept()
                .stream()
                .findFirst();

        if (!isMediaTypeAcceptable(acceptedMediaType)) {
            throw new ResponseStatusException(NOT_ACCEPTABLE, NOT_ACCEPTABLE_MSG);
        }

        return chain.filter(exchange);
    }

    private boolean isMediaTypeAcceptable(Optional<MediaType> optionalMediaType) {
        return optionalMediaType.isPresent() && optionalMediaType.get().equals(MediaType.APPLICATION_JSON);
    }
}
