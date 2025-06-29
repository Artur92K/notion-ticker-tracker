package dev.pato;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient integrationWebClient() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Notion-Version", "2022-06-28")
                .build();
    }
    public static HttpHeaders getHeaders(String apikey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apikey);
        headers.set("Notion-Version", "2022-06-28");
        headers.set("Content-Type", "application/json");
        return headers;
    }

}
