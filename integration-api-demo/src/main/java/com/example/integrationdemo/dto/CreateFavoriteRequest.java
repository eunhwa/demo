package com.example.integrationdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateFavoriteRequest(
        @NotBlank @Pattern(regexp = "(?:[0-9]{13}|[0-9]{9}[0-9X])")
        @Schema(example = "9788996991342", description = "하이픈 없는 ISBN10 또는 ISBN13") String isbn) {}
