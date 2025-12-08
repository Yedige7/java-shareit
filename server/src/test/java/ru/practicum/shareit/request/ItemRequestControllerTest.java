package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@Import(ErrorHandler.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService service;

    @Test
    void create_callsServiceWithUserIdAndDescription() throws Exception {
        ItemRequestDto body = ItemRequestDto.builder()
                .description("Нужна дрель")
                .build();

        ItemRequestDto response = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .build();

        when(service.create(eq(1L), eq("Нужна дрель")))
                .thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(service).create(1L, "Нужна дрель");
    }

    @Test
    void getOwn_callsServiceWithUserId() throws Exception {
        when(service.getOwn(1L)).thenReturn(List.of());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(service).getOwn(1L);
    }

    @Test
    void getAll_withoutParams_usesDefaultFromAndSize() throws Exception {
        when(service.getAll(1L, 0, 10)).thenReturn(List.of());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(service).getAll(1L, 0, 10);
    }

    @Test
    void getAll_withParams_passesThemToService() throws Exception {
        when(service.getAll(1L, 5, 20)).thenReturn(List.of());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(service).getAll(1L, 5, 20);
    }

    @Test
    void getById_callsServiceWithUserIdAndRequestId() throws Exception {
        ItemRequestDto response = ItemRequestDto.builder()
                .id(100L)
                .description("Описание")
                .build();

        when(service.getById(1L, 100L)).thenReturn(response);

        mockMvc.perform(get("/requests/{requestId}", 100L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(service).getById(1L, 100L);
    }
}
