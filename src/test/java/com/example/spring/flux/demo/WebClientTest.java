package com.example.spring.flux.demo;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
}
