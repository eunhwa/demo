package com.example.integrationdemo.controller;

import com.example.integrationdemo.dto.FavoriteResponse;
import com.example.integrationdemo.service.FavoriteService;

import com.example.integrationdemo.common.exception.ApiException;
import com.example.integrationdemo.common.exception.ApiExceptionHandler;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FavoriteControllerTests {
    private final FavoriteService service = mock(FavoriteService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new FavoriteController(service))
            .setControllerAdvice(new ApiExceptionHandler()).build();

    @Test
    void rejectsInvalidJsonAndIsbnBeforeService() throws Exception {
        for (String body : new String[]{"{}", "{\"isbn\":null}", "{\"isbn\":\"wrong\"}", "broken"}) {
            mvc.perform(post("/api/favorites").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }
        mvc.perform(delete("/api/favorites/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/favorites?size=51")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void returnsCreatedAndNoContent() throws Exception {
        when(service.add("9788996991342")).thenReturn(new FavoriteResponse(1L, "9788996991342",
                "Book", "", List.of(), "", "", "", Instant.now()));
        mvc.perform(post("/api/favorites").contentType(MediaType.APPLICATION_JSON)
                .content("{\"isbn\":\"9788996991342\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
        mvc.perform(delete("/api/favorites/1")).andExpect(status().isNoContent());
    }

    @Test
    void mapsDuplicateToConflict() throws Exception {
        when(service.add(anyString())).thenThrow(new ApiException(HttpStatus.CONFLICT,
                "FAVORITE_ALREADY_EXISTS", "이미 등록한 도서입니다."));
        mvc.perform(post("/api/favorites").contentType(MediaType.APPLICATION_JSON)
                .content("{\"isbn\":\"9788996991342\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("FAVORITE_ALREADY_EXISTS"));
    }
}
