package com.example.integrationdemo.dao;

import com.example.integrationdemo.dao.entity.Favorite;
import com.example.integrationdemo.dao.mapper.FavoriteMapper;
import com.example.integrationdemo.dto.BookResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class FavoriteDaoTests {
    @Autowired FavoriteDao dao;
    @MockitoSpyBean FavoriteMapper mapper;

    @AfterEach
    void cleanup() {
        reset(mapper);
        dao.findPage(0, 50).forEach(favorite -> dao.delete(favorite.getId()));
    }

    @Test
    void pagesBooksBeforeJoiningAuthorsAndPreservesOrderAndDuplicateNames() {
        var older = dao.insert(book("0000000000002", List.of()));
        var newer = dao.insert(book("0000000000003", List.of("Same", "Same", "Last")));
        var firstPage = dao.findPage(0, 1);
        assertThat(firstPage).hasSize(1);
        assertThat(firstPage.getFirst().getId()).isEqualTo(newer.getId());
        assertThat(firstPage.getFirst().getAuthors()).containsExactly("Same", "Same", "Last");
        var secondPage = dao.findPage(1, 1);
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getId()).isEqualTo(older.getId());
        assertThat(secondPage.getFirst().getAuthors()).isEmpty();
        assertThat(dao.findPage(Integer.MAX_VALUE, 50)).isEmpty();
    }

    @Test
    void rollsBackParentWhenAuthorInsertFails() {
        doThrow(new DataIntegrityViolationException("simulated author failure"))
                .when(mapper).insertAuthors(any());
        assertThatThrownBy(() -> dao.insert(book("0000000000004", List.of("Author"))))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(dao.existsByIsbn("0000000000004")).isFalse();
    }

    @Test
    void rollsBackAuthorDeleteWhenParentDeleteFails() {
        var saved = dao.insert(book("0000000000005", List.of("Author")));
        doThrow(new DataIntegrityViolationException("simulated delete failure"))
                .when(mapper).deleteFavorite(saved.getId());
        assertThatThrownBy(() -> dao.delete(saved.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(dao.findById(saved.getId()).orElseThrow().getAuthors()).containsExactly("Author");
    }

    private Favorite book(String isbn, List<String> authors) {
        return new Favorite(new BookResponse(isbn, "DAO test", "", authors, "", "", ""));
    }
}
