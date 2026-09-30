package com.example.integrationdemo.dao;

import com.example.integrationdemo.dao.entity.Favorite;

import com.example.integrationdemo.dto.BookResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "PG_TEST_DB_URL", matches = ".+")
@SpringBootTest(properties = {
        "spring.datasource.url=${PG_TEST_DB_URL}",
        "spring.datasource.username=${PG_TEST_DB_USERNAME:demo_user}",
        "spring.datasource.password=${PG_TEST_DB_PASSWORD}"})
@Transactional
class PostgresPersistenceTests {
    @Autowired FavoriteDao dao;

    @Test
    void databaseRejectsDuplicateIsbn() {
        var book = new BookResponse("0000000000001", "Unique constraint verification", "",
                List.of(), "", "", "");
        dao.insert(new Favorite(book));
        assertThatThrownBy(() -> dao.insert(new Favorite(book)))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void persistsAndReloadsAuthorsOnRealPostgresThenRollsBack() {
        var saved = dao.insert(new Favorite(new BookResponse("0000000000000",
                "PostgreSQL verification", "Rollback after test", List.of("Author A", "Author B"), "", "", "")));
        long id = saved.toResponse().id();
        var restored = dao.findById(id).orElseThrow().toResponse();
        assertThat(restored.authors()).containsExactly("Author A", "Author B");
        assertThat(restored.title()).isEqualTo("PostgreSQL verification");
        assertThat(dao.delete(id)).isTrue();
        assertThat(dao.findById(id)).isEmpty();
    }
}
