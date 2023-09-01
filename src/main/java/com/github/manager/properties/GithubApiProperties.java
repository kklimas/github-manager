package com.github.manager.properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@NoArgsConstructor
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "webclient.github")
public class GithubApiProperties {
    private int port;
    private String url;
    private String apiVersion;
}
