package com.example.integrationdemo.book;

import java.util.List;

public record BookSearchResponse(String keyword, int page, int size, int totalCount,
                                 boolean end, List<BookResponse> books) {}
