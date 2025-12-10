package ru.practicum.shareit.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(ErrorHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_returns200_andCallsService() throws Exception {
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userService).findAll();
    }

    @Test
    void saveNewUser_valid_returns201_andCallsService() throws Exception {
        UserDto body = new UserDto();
        body.setName("User");
        body.setEmail("user@example.com");

        when(userService.create(ArgumentMatchers.any(UserDto.class)))
                .thenReturn(body);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        verify(userService).create(ArgumentMatchers.any(UserDto.class));
    }

    @Test
    void getUserById_returns200_andCallsService() throws Exception {
        UserDto response = new UserDto();
        response.setId(1L);
        response.setName("User");
        response.setEmail("user@example.com");

        when(userService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk());

        verify(userService).getById(1L);
    }

    @Test
    void update_valid_returns200_andCallsService() throws Exception {
        UserDto patchBody = new UserDto();
        patchBody.setName("Updated");
        patchBody.setEmail("updated@example.com");

        when(userService.update(eq(1L), ArgumentMatchers.any(UserDto.class)))
                .thenReturn(patchBody);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patchBody)))
                .andExpect(status().isOk());

        verify(userService).update(eq(1L), ArgumentMatchers.any(UserDto.class));
    }

    @Test
    void delete_returns204_andCallsService() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }
}