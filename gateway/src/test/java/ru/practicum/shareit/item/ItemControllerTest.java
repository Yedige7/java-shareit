package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void create_validItem_callsClientAndReturns2xx() throws Exception {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Good drill")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().is2xxSuccessful());

        verify(itemClient).create(1L, dto);
    }


    @Test
    void search_emptyText_returnsEmptyListWithoutClientCall() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", " "))
                .andExpect(status().isOk());

        verifyNoInteractions(itemClient);
    }
}

