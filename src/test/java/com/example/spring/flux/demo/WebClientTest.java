package com.example.spring.flux.demo;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebClientTest {

    @Test
    public void test() throws InterruptedException {
        WebClient webClient = WebClient.create("https://www.baidu.com");
        Mono<String> stringMono = webClient.get().retrieve().bodyToMono(String.class);
        System.err.println(Thread.currentThread().getName() + " start..");
        stringMono.subscribe(s-> System.err.println(Thread.currentThread().getName() + ">>>" + s));
        System.err.println(Thread.currentThread().getName() + " end..");
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void test1(){
        WebClient.Builder builder = WebClient.builder();
        builder.uriBuilderFactory(null);
        builder.defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        builder.defaultCookie("session-id", "123");
        builder.defaultRequest(r->{});


        WebClient webClient = builder.build();
    }
}
