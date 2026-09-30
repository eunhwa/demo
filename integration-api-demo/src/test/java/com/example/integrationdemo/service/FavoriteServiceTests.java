package com.example.integrationdemo.service;

import com.example.integrationdemo.dao.FavoriteDao;

import com.example.integrationdemo.dto.BookResponse;
import com.example.integrationdemo.service.BookService;
import com.example.integrationdemo.common.exception.ApiException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class FavoriteServiceTests {
    @Autowired FavoriteService service;
    @Autowired FavoriteDao dao;
    @MockitoBean BookService books;

    @AfterEach
    void cleanup() { dao.findPage(0, 50).forEach(favorite -> dao.delete(favorite.getId())); }

    @Test
    void persistsListsAndDeletesWithoutCallingProviderForReads() {
        when(books.findByIsbn("9788996991342")).thenReturn(book());
        var created = service.add("9788996991342");
        assertThat(created.id()).isPositive();
        assertThat(created.createdAt()).isNotNull();
        clearInvocations(books);
        var page = service.list(0, 20);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.favorites().getFirst().authors()).containsExactly("Author A", "Author B");
        service.delete(created.id());
        assertThat(service.list(0, 20).favorites()).isEmpty();
        verifyNoInteractions(books);
    }

    @Test
    void treatsIsbn10AndIsbn13OfSameBookAsDuplicate() {
        when(books.findByIsbn(anyString())).thenReturn(book());
        service.add("8996991341");
        assertThatThrownBy(() -> service.add("9788996991342"))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.code()).isEqualTo("FAVORITE_ALREADY_EXISTS"));
        assertThat(dao.count()).isEqualTo(1);
    }

    @Test
    void missingDeleteReturnsNotFound() {
        assertThatThrownBy(() -> service.delete(Long.MAX_VALUE))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.code()).isEqualTo("FAVORITE_NOT_FOUND"));
    }

    private BookResponse book() {
        return new BookResponse("9788996991342", "Test Book", "Description",
                List.of("Author A", "Author B"), "Publisher", "", "");
    }
}
