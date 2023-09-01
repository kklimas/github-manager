package com.github.manager.web.controller;

import com.github.manager.model.UserRepositoryDTO;
import com.github.manager.service.GithubUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class GithubUserController {

    private final GithubUserService githubUserService;

    @GetMapping("users/{username}")
    public Flux<UserRepositoryDTO> findUserDetails(@PathVariable String username) {
        return githubUserService.findUserDetails(username);
    }

}
