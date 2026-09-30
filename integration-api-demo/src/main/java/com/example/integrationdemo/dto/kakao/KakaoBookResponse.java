package com.example.integrationdemo.dto.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoBookResponse(Meta meta, List<Document> documents) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(@JsonProperty("total_count") int totalCount,
                       @JsonProperty("is_end") boolean end) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(String title, String contents, String isbn, List<String> authors,
                           String publisher, String thumbnail, String url) {}
}
