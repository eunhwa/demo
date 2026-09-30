package com.example.integrationdemo.service;

import com.example.integrationdemo.dto.BookResponse;
import com.example.integrationdemo.dto.BookSearchResponse;
import com.example.integrationdemo.dto.kakao.KakaoBookResponse;

import com.example.integrationdemo.common.exception.ApiException;
import com.example.integrationdemo.service.kakao.KakaoBookClient;
import com.example.integrationdemo.dto.kakao.KakaoBookResponse.Document;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final KakaoBookClient client;

    public BookService(KakaoBookClient client) { this.client = client; }

    public BookSearchResponse search(String keyword, int page, int size) {
        String query = keyword.strip();
        var result = client.search(query, page, size, false);
        return new BookSearchResponse(query, page, size, result.meta().totalCount(),
                result.meta().end(), result.documents().stream().map(this::toBook).toList());
    }

    public BookResponse findByIsbn(String isbn) {
        var result = client.search(isbn, 1, 50, true);
        return result.documents().stream()
                .filter(document -> isbns(document.isbn()).contains(isbn))
                .findFirst().map(this::toBook)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND",
                        "해당 ISBN의 도서를 찾을 수 없습니다."));
    }

    private BookResponse toBook(Document document) {
        var identifiers = isbns(document.isbn());
        String isbn = identifiers.stream().filter(value -> value.length() == 13).findFirst()
                .orElse(identifiers.isEmpty() ? null : identifiers.getFirst());
        return new BookResponse(isbn, document.title(), document.contents(),
                document.authors() == null ? List.of() : document.authors(),
                document.publisher(), document.thumbnail(), document.url());
    }

    private List<String> isbns(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.strip().split("\\s+"))
                .filter(value -> value.matches("(?:[0-9]{13}|[0-9]{9}[0-9X])")).toList();
    }
}
