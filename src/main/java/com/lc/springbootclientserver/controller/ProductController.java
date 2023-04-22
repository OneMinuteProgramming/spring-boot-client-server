package com.lc.springbootclientserver.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@Slf4j
public class ProductController {
    final private WebClient resourceWebClient;

    @RequestMapping("/product")
    public Mono<String> getProduct(){
        return resourceWebClient.get().uri("/product").retrieve().bodyToMono(String.class);
    }

}
