package com.example.integrationdemo.dto;

import java.time.Instant;
import java.util.List;

public record FavoriteResponse(Long id, String isbn, String title, String description,
        List<String> authors, String publisher, String thumbnail, String url, Instant createdAt) {}
