package com.example.integrationdemo.book;

import com.example.integrationdemo.common.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookControllerTests {
    private final BookService service = mock(BookService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new BookController(service))
            .setControllerAdvice(new ApiExceptionHandler()).build();

    @Test
    void rejectsInvalidInputBeforeCallingService() throws Exception {
        for (String query : new String[]{"", "?keyword=", "?keyword=spring&page=0",
                "?keyword=spring&size=51", "?keyword=spring&page=abc"}) {
            mvc.perform(get("/api/books/search" + query)).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }
        mvc.perform(get("/api/books/search").param("keyword", "   ")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/books/invalid-isbn")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
