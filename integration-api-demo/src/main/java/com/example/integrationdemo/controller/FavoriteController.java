package com.example.integrationdemo.controller;

import com.example.integrationdemo.dto.CreateFavoriteRequest;
import com.example.integrationdemo.dto.FavoritePage;
import com.example.integrationdemo.dto.FavoriteResponse;
import com.example.integrationdemo.service.FavoriteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "02. 즐겨찾기", description = "외부 API로 조회한 도서 정보를 PostgreSQL에 저장합니다. 인증 전 공용 목록입니다.")
public class FavoriteController {
    private final FavoriteService service;

    public FavoriteController(FavoriteService service) { this.service = service; }

    @PostMapping
    @ApiResponses({@ApiResponse(responseCode = "201", description = "등록 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 ISBN 또는 JSON"),
            @ApiResponse(responseCode = "404", description = "도서 없음"),
            @ApiResponse(responseCode = "409", description = "이미 등록한 도서"),
            @ApiResponse(responseCode = "502", description = "외부 API 오류"),
            @ApiResponse(responseCode = "503", description = "키 미설정 또는 요청 한도 초과"),
            @ApiResponse(responseCode = "504", description = "외부 API 시간 초과")})
    @Operation(summary = "즐겨찾기 등록", description = "ISBN으로 외부 API를 조회한 뒤 저장합니다. 같은 도서 중복은 409를 반환합니다.")
    public ResponseEntity<FavoriteResponse> add(@Valid @RequestBody CreateFavoriteRequest request) {
        return ResponseEntity.status(201).body(service.add(request.isbn()));
    }

    @GetMapping
    @Operation(summary = "즐겨찾기 목록", description = "최신 등록순이며 DB에서만 조회합니다. page는 0부터 시작합니다.")
    public FavoritePage list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return service.list(page, size);
    }

    @DeleteMapping("/{id}")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "삭제 완료"),
            @ApiResponse(responseCode = "404", description = "즐겨찾기 없음")})
    @Operation(summary = "즐겨찾기 삭제", description = "삭제 성공은 204, 없는 ID는 404를 반환합니다.")
    public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
