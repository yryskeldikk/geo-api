package com.yrys.weather;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.Executor;


@Configuration
@EnableAutoConfiguration
public class AppConfig {
    @Value("${proxy.use:false}")
    private boolean proxyUse;
    @Value("${proxy.http:}")
    private String proxyHttp;
    @Value("${proxy.http.port:0}")
    private String proxyHttpPort;

    @Bean (name = "taskExecutor")
    public Executor taskExecutor(
            @Value("${app.process.thread.min-count}") int minThread,
            @Value("${app.process.thread.max-count}") int maxThread,
            @Value("${app.process.queue-size}") int queueSize
    ) {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize( minThread );
        executor.setMaxPoolSize( maxThread );
        executor.setThreadNamePrefix("geo-thread-");
        executor.setQueueCapacity( queueSize );
        executor.initialize();
        return executor;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();

        RestTemplate restTemplate = builder.setConnectTimeout(Duration.ofMillis(3000))
                .setReadTimeout(Duration.ofMillis(3000)).additionalMessageConverters(
                        new Jaxb2RootElementHttpMessageConverter(), new MappingJackson2HttpMessageConverter(), new StringHttpMessageConverter())
                .build();

        if (proxyUse) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHttp, Integer.parseInt(proxyHttpPort)));
            SimpleClientHttpRequestFactory proxyRequestFactory = new SimpleClientHttpRequestFactory();
            proxyRequestFactory.setProxy(proxy);
            restTemplate.setRequestFactory(proxyRequestFactory);
        } else {
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setHttpClient(httpClient);
            restTemplate.setRequestFactory(factory);
        }

        return restTemplate;
    }
}

