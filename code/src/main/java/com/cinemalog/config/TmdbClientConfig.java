
package com.cinemalog.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TmdbClientConfig {

    @Bean
    public RestClient tmdbRestClient(TmdbProperties properties) {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        // ระยะเวลารอเชื่อมต่อ TMDB สูงสุด 5 วินาที
        factory.setConnectTimeout(Duration.ofSeconds(5));

        // ระยะเวลารออ่านข้อมูลจาก TMDB สูงสุด 7 วินาที
        factory.setReadTimeout(Duration.ofSeconds(7));

        RestClient.Builder b = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory);

        if (properties.usesBearerToken()) {
            b.defaultHeader(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + properties.apiKey()
            );
        }

        return b.build();
    }
}
