package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService service;

    @Test
    void create_validItem_returns201_andCallsService() throws Exception {
        ItemDto body = ItemDto.builder()
                .name("Drill")
                .description("Good drill")
                .available(true)
                .build();

        when(service.create(eq(1L), ArgumentMatchers.any(ItemDto.class)))
                .thenReturn(body);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        verify(service).create(eq(1L), ArgumentMatchers.any(ItemDto.class));
    }

    @Test
    void update_callsServiceWithUserIdAndItemId() throws Exception {
        ItemDto patch = ItemDto.builder()
                .name("New name")
                .description("New description")
                .available(false)
                .build();

        when(service.update(eq(1L), eq(42L), ArgumentMatchers.any(ItemDto.class)))
                .thenReturn(patch);

        mockMvc.perform(patch("/items/{itemId}", 42L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk());

        verify(service).update(eq(1L), eq(42L), ArgumentMatchers.any(ItemDto.class));
    }

    @Test
    void getById_callsServiceWithUserIdAndItemId() throws Exception {
        ItemDto response = ItemDto.builder()
                .id(42L)
                .name("Drill")
                .description("Good drill")
                .available(true)
                .build();

        when(service.getById(1L, 42L)).thenReturn(response);

        mockMvc.perform(get("/items/{itemId}", 42L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(service).getById(1L, 42L);
    }

    @Test
    void ownerItems_callsServiceWithUserId() throws Exception {
        when(service.getOwnerItems(1L)).thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(service).getOwnerItems(1L);
    }

    @Test
    void search_callsServiceWithText() throws Exception {
        when(service.search("дрель")).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(service).search("дрель");
    }

    @Test
    void addComment_callsServiceWithUserIdItemIdAndBody() throws Exception {
        CommentDto body = CommentDto.builder()
                .text("Отличная вещь")
                .build();

        CommentDto response = CommentDto.builder()
                .id(5L)
                .text("Отличная вещь")
                .build();

        when(service.addComment(eq(1L), eq(42L), ArgumentMatchers.any(CommentDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/items/{itemId}/comment", 42L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(service).addComment(eq(1L), eq(42L), ArgumentMatchers.any(CommentDto.class));
    }
}
