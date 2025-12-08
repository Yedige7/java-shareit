package ru.practicum.shareit.exeption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ErrorHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void conflictMappedTo409() throws Exception {
        mockMvc.perform(get("/conflict"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("conflict message")));
    }

    @Test
    void notFoundMappedTo404() throws Exception {
        mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("not found")));
    }

    @Test
    void validationMappedTo400() throws Exception {
        mockMvc.perform(get("/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("validation error")));
    }

    @Test
    void illegalArgMappedTo400() throws Exception {
        mockMvc.perform(get("/illegal-arg"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("illegal arg")));
    }

    @Test
    void otherMappedTo500() throws Exception {
        mockMvc.perform(get("/other"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("other error")));
    }

    @RestController
    static class TestController {

        @GetMapping("/conflict")
        public void conflict() {
            throw new ConflictException("conflict message");
        }

        @GetMapping("/not-found")
        public void notFound() {
            throw new NotFoundException("not found");
        }

        @GetMapping("/validation")
        public void validation() {
            throw new ValidationException("validation error");
        }

        @GetMapping("/illegal-arg")
        public void illegalArg() {
            throw new IllegalArgumentException("illegal arg");
        }

        @GetMapping("/other")
        public void other() {
            throw new RuntimeException("other error");
        }
    }
}
