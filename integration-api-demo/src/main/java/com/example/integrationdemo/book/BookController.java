package com.example.integrationdemo.book;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@Tag(name = "01. 도서 검색", description = "카카오 API 연동 → 필요한 필드와 ISBN 정리 → REST 응답")
public class BookController {
    private final BookService service;

    public BookController(BookService service) { this.service = service; }

    @GetMapping("/search")
    @Operation(summary = "키워드로 도서 검색", description = "외부 API 검색 결과를 변환합니다. DB에는 저장하지 않습니다. 페이지와 크기는 1~50입니다.")
    public BookSearchResponse search(
            @Parameter(example = "spring") @RequestParam @NotBlank @Size(max = 100) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) @Max(50) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return service.search(keyword, page, size);
    }

    @GetMapping("/{isbn}")
    @Operation(summary = "ISBN으로 도서 상세 조회", description = "하이픈 없는 ISBN10 또는 ISBN13으로 검색한 후 정확히 일치하는 도서를 반환합니다. ISBN 체크섬은 검증하지 않습니다.")
    public BookResponse detail(@Parameter(example = "9788996991342")
            @PathVariable @Pattern(regexp = "(?:[0-9]{13}|[0-9]{9}[0-9X])") String isbn) {
        return service.findByIsbn(isbn);
    }
}
