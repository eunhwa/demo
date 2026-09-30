package com.example.integrationdemo.integration.kakao;

import com.example.integrationdemo.common.ApiException;
import java.net.SocketTimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class KakaoBookClient {
    private final RestClient client;
    private final String apiKey;

    public KakaoBookClient(RestClient kakaoRestClient, @Value("${kakao.api.key}") String apiKey) {
        this.client = kakaoRestClient;
        this.apiKey = apiKey;
    }

    public KakaoBookResponse search(String query, int page, int size, boolean isbnSearch) {
        if (apiKey.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "BOOK_API_NOT_CONFIGURED",
                    "서버에 KAKAO_REST_API_KEY 환경변수를 설정해 주세요.");
        }
        try {
            var result = client.get().uri(builder -> {
                builder.path("/v3/search/book").queryParam("query", "{query}")
                        .queryParam("page", page).queryParam("size", size);
                if (isbnSearch) builder.queryParam("target", "isbn");
                return builder.build(query);
            }).header("Authorization", "KakaoAK " + apiKey)
                    .retrieve().body(KakaoBookResponse.class);
            if (result == null || result.meta() == null || result.documents() == null
                    || result.documents().stream().anyMatch(java.util.Objects::isNull)) {
                throw unavailable();
            }
            return result;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 429) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "BOOK_API_RATE_LIMITED",
                        "도서 검색 서비스의 요청 한도에 도달했습니다. 잠시 후 다시 시도해 주세요.");
            }
            // 외부 응답 본문이나 인증 헤더를 사용자 응답에 포함하지 않는다.
            throw unavailable();
        } catch (ResourceAccessException ex) {
            for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
                if (cause instanceof SocketTimeoutException) {
                    throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "BOOK_API_TIMEOUT",
                            "도서 검색 서비스의 응답 시간이 초과되었습니다.");
                }
            }
            throw unavailable();
        } catch (RestClientException ex) {
            throw unavailable();
        }
    }

    private ApiException unavailable() {
        return new ApiException(HttpStatus.BAD_GATEWAY, "BOOK_API_ERROR",
                "외부 도서 검색 서비스에서 정상 응답을 받지 못했습니다.");
    }
}
