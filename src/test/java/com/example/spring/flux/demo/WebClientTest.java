package com.example.spring.flux.demo;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

//@SpringBootTest
//@ComponentScan("com.example")
//@ActiveProfiles("constants")
public class WebClientTest {


    private static String blog = "https://blog.csdn.net/weixin_43364172";
    private static String weibo = "http://www.weibo.com";

    private WebClient webClient;

    {
        webClient =  webClient();
    }

    @Test
    public void testSingle() {
        single(blog);
    }

    @Test
    public void tests() {
        concurrency(3, weibo);
        concurrency(10, blog);
        concurrency(2, blog);
        concurrency(2, weibo);
        concurrency(3, weibo);
        concurrency(2, blog);
    }

    private void single(String url) {
        Mono<ClientResponse> mono = webClient.get().uri(url)
                .accept(MediaType.TEXT_HTML)
                .exchange();

        ClientResponse block = mono.block();
        assert block != null;
        Mono<String> stringMono = block.bodyToMono(String.class);
        //stringMono.subscribe(System.out::println);
    }

    private void concurrency(int num, String url) {
        ExecutorService es = Executors.newFixedThreadPool(num);
        for (int i = 0; i < num; i++) {
            es.execute(() -> single(url));
        }
        es.shutdown();
        try {
            es.awaitTermination(10, TimeUnit.SECONDS);
            System.out.println(num + " completes");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Spring 5.1 开始，WebFlux 的 WebClient 支持连接池功能。
     * 默认情况下，WebClient 使用 global Reactor Netty 资源，也可以不使用全局资源：
     */
    //@Bean
    ReactorResourceFactory resourceFactory() {

        ReactorResourceFactory factory = new ReactorResourceFactory();
        factory.setUseGlobalResources(false);

        factory.setConnectionProvider(ConnectionProvider.builder("http-web-client")
                .maxConnections(1000)
                .maxIdleTime(Duration.ofSeconds(5))
                .maxLifeTime(Duration.ofMinutes(1))
                .pendingAcquireMaxCount(2000)
                .pendingAcquireTimeout(Duration.ofSeconds(30))
                .build());

        factory.setLoopResources(LoopResources.create("httpClient", 5, true));

        return factory;
    }

    //@Bean
    WebClient webClient() {
        Function<HttpClient, HttpClient> mapper = client ->
                client.tcpConfiguration(c ->
                        c.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                                .option(ChannelOption.TCP_NODELAY, true)
                                .option(ChannelOption.SO_KEEPALIVE, true) // 设置后链接不会马上关闭
                                .doOnConnected(conn -> {
                                    conn.markPersistent(true);// 设置后链接不会马上关闭
                                    conn.addHandlerLast(new ReadTimeoutHandler(10000));
                                    conn.addHandlerLast(new WriteTimeoutHandler(10000));
                                }));

        ClientHttpConnector connector =
                new ReactorClientHttpConnector(resourceFactory(), mapper);

        return WebClient.builder().clientConnector(connector).build();
    }



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

        builder.exchangeStrategies(ExchangeStrategies.builder()
                .build());

        WebClient webClient = builder.build();
    }
}
