package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient client;

    @Test
    void createUser_ok() throws Exception {
        UserDto dto = new UserDto(null, "Test", "test@mail.com");

        when(client.create(dto)).thenReturn(ResponseEntity.ok(dto));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(client).create(dto);
    }

    @Test
    void updateUser_ok() throws Exception {
        UserDto dto = new UserDto(1L, "User", "user@mail.com");

        when(client.update(eq(1L), any())).thenReturn(ResponseEntity.ok(dto));

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_invalidEmail_returns400() throws Exception {
        UserDto dto = new UserDto(null, "Bad", "invalid-email");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(client, never()).create(any());
    }
}
