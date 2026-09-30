package com.example.integrationdemo.dao.entity;

import com.example.integrationdemo.dto.BookResponse;
import com.example.integrationdemo.dto.FavoriteResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// MyBatis 결과 매핑용 데이터 객체. JPA Entity가 아니다.
public class Favorite {
    private Long id;
    private String isbn;
    private String title;
    private String description;
    private List<FavoriteAuthor> authorRows = new ArrayList<>();
    private String publisher;
    private String thumbnail;
    private String url;
    private Instant createdAt;

    public Favorite() {}

    public Favorite(BookResponse book) {
        isbn = book.isbn();
        title = book.title();
        description = book.description();
        setAuthors(book.authors());
        publisher = book.publisher();
        thumbnail = book.thumbnail();
        url = book.url();
        createdAt = Instant.now();
    }

    public FavoriteResponse toResponse() {
        return new FavoriteResponse(id, isbn, title, description, getAuthors(),
                publisher, thumbnail, url, createdAt);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getAuthors() { return authorRows.stream().map(FavoriteAuthor::getAuthor).toList(); }
    public void setAuthors(List<String> authors) {
        this.authorRows = new ArrayList<>();
        for (int i = 0; i < authors.size(); i++) {
            this.authorRows.add(new FavoriteAuthor(i, authors.get(i)));
        }
    }
    public List<FavoriteAuthor> getAuthorRows() { return authorRows; }
    public void setAuthorRows(List<FavoriteAuthor> authorRows) { this.authorRows = authorRows; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
