package com.example.integrationdemo.book;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record BookResponse(
        @Schema(description = "ISBN13 우선, 없으면 ISBN10. 둘 다 없으면 null", example = "9788996991342") String isbn,
        @Schema(example = "미움받을 용기") String title,
        String description, List<String> authors, String publisher, String thumbnail, String url) {}
