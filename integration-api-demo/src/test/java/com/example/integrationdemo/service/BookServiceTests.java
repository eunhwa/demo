package com.example.integrationdemo.service;

import com.example.integrationdemo.dto.BookResponse;

import com.example.integrationdemo.common.exception.ApiException;
import com.example.integrationdemo.service.kakao.KakaoBookClient;
import com.example.integrationdemo.dto.kakao.KakaoBookResponse;
import com.example.integrationdemo.dto.kakao.KakaoBookResponse.Document;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTests {
    private final KakaoBookClient client = mock(KakaoBookClient.class);
    private final BookService service = new BookService(client);

    @Test
    void trimsQueryPrefersIsbn13AndHandlesMissingIsbn() {
        when(client.search("spring", 1, 10, false)).thenReturn(response(
                document("8996991341 9788996991342"), document(""), document("8996991341")));
        var result = service.search("  spring  ", 1, 10);
        assertThat(result.keyword()).isEqualTo("spring");
        assertThat(result.books()).extracting(BookResponse::isbn)
                .containsExactly("9788996991342", null, "8996991341");
        assertThat(result.books().getFirst().authors()).isEmpty();
    }

    @Test
    void detailMatchesExactIsbn10InsteadOfFirstResult() {
        when(client.search("8996991341", 1, 50, true)).thenReturn(response(
                document("9780000000000"), document("8996991341 9788996991342")));
        assertThat(service.findByIsbn("8996991341").isbn()).isEqualTo("9788996991342");
    }

    @Test
    void detailRejectsUnrelatedSearchResult() {
        when(client.search("9788996991342", 1, 50, true)).thenReturn(response(document("9780000000000")));
        assertThatThrownBy(() -> service.findByIsbn("9788996991342"))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.code()).isEqualTo("BOOK_NOT_FOUND"));
    }

    private KakaoBookResponse response(Document... documents) {
        return new KakaoBookResponse(new KakaoBookResponse.Meta(documents.length, true), List.of(documents));
    }

    private Document document(String isbn) {
        return new Document("Example", "Description", isbn, null, "Publisher", "", "");
    }
}
