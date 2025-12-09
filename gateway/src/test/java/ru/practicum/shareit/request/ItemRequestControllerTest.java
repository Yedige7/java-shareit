package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@Import(ErrorHandler.class)
class ItemRequestControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestClient client;

    @Test
    void create_ok() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("desc")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(client.create(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok(dto));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void getOwn_ok() throws Exception {
        when(client.getOwn(1L))
                .thenReturn(ResponseEntity.ok(List.of()));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ok() throws Exception {
        when(client.getAll(1L, 0, 10))
                .thenReturn(ResponseEntity.ok(List.of()));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_negativeFrom_returns400() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_zeroSize_returns400() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getById_ok() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("desc")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(client.getById(1L, 1L))
                .thenReturn(ResponseEntity.ok(dto));

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}
