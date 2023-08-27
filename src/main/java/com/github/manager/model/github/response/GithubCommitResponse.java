package com.github.manager.model.github.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubCommitResponse {
    private String sha;
    private String url;
}