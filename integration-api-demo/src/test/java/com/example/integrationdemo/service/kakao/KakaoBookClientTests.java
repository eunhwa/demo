package com.example.integrationdemo.service.kakao;

import com.example.integrationdemo.common.exception.ApiException;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class KakaoBookClientTests {
    private MockRestServiceServer server;
    private KakaoBookClient client;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://dapi.kakao.com");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new KakaoBookClient(builder.build(), "test-key");
    }

    @Test
    void encodesQueryAndMapsKakaoJson() {
        server.expect(requestTo("https://dapi.kakao.com/v3/search/book?query=C%2B%2B%20%26%20spring&page=1&size=10"))
                .andExpect(header("Authorization", "KakaoAK test-key"))
                .andRespond(withSuccess("""
                    {"meta":{"total_count":1,"is_end":true,"pageable_count":1},
                     "documents":[{"title":"Test Book","isbn":"8996991341 9788996991342",
                     "authors":["Author"],"sale_price":100}]}
                    """, MediaType.APPLICATION_JSON));
        var result = client.search("C++ & spring", 1, 10, false);
        assertThat(result.meta().totalCount()).isEqualTo(1);
        assertThat(result.meta().end()).isTrue();
        assertThat(result.documents().getFirst().title()).isEqualTo("Test Book");
        server.verify();
    }

    @Test
    void addsIsbnTarget() {
        server.expect(queryParam("target", "isbn"))
                .andRespond(withSuccess("{\"meta\":{\"total_count\":0,\"is_end\":true},\"documents\":[]}", MediaType.APPLICATION_JSON));
        assertThat(client.search("9788996991342", 1, 50, true).documents()).isEmpty();
        server.verify();
    }

    @Test
    void hidesProviderErrorBody() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("private-provider-details"));
        assertThatThrownBy(() -> client.search("spring", 1, 10, false))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.status()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(ex.getMessage()).doesNotContain("private-provider-details");
                });
    }

    @Test
    void mapsQuotaLimit() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertThatThrownBy(() -> client.search("spring", 1, 10, false))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.code()).isEqualTo("BOOK_API_RATE_LIMITED"));
    }

    @Test
    void mapsTimeout() {
        server.expect(anything()).andRespond(withException(new SocketTimeoutException("timed out")));
        assertThatThrownBy(() -> client.search("spring", 1, 10, false))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.status()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT));
    }

    @Test
    void rejectsInvalidResponseStructure() {
        server.expect(anything()).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> client.search("spring", 1, 10, false))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.code()).isEqualTo("BOOK_API_ERROR"));
    }

    @Test
    void missingKeyDoesNotCallProvider() {
        var unconfigured = new KakaoBookClient(RestClient.create(), "");
        assertThatThrownBy(() -> unconfigured.search("spring", 1, 10, false))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.code()).isEqualTo("BOOK_API_NOT_CONFIGURED"));
        server.verify();
    }
}
