package com.example.integrationdemo.dto;

import java.util.List;

public record FavoritePage(List<FavoriteResponse> favorites, int page, int size,
                           long totalElements, int totalPages) {}
