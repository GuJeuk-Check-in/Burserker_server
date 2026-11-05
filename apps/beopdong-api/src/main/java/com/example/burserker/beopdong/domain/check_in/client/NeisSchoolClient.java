package com.example.burserker.beopdong.domain.check_in.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import org.springframework.web.client.RestClientException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class NeisSchoolClient {
    private final RestClient restClient;
    private final String apiKey;

    public NeisSchoolClient(@Value("${neis.api-key}") String apiKey) {
        this.apiKey = apiKey;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl("https://open.neis.go.kr")
                .requestFactory(requestFactory)
                .build();
    }

    public JsonNode search(String name, int page) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/hub/schoolInfo")
                            .queryParam("KEY", apiKey)
                            .queryParam("Type", "json")
                            .queryParam("pIndex", page)
                            .queryParam("pSize", 100)
                            .queryParam("SCHUL_NM", "{name}")
                            .build(name))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new BeopdongException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}