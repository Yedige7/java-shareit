package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void create_whenEmailNotUsed_savesUser() {
        UserDto dto = new UserDto();
        dto.setName("John");
        dto.setEmail("john@example.com");

        when(repository.existsByEmail("john@example.com")).thenReturn(false);

        User saved = new User();
        saved.setId(1L);
        saved.setName("John");
        saved.setEmail("john@example.com");

        when(repository.save(any(User.class))).thenReturn(saved);

        UserDto result = service.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void create_whenEmailAlreadyUsed_throwsConflict() {
        UserDto dto = new UserDto();
        dto.setName("John");
        dto.setEmail("john@example.com");

        when(repository.existsByEmail("john@example.com")).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.create(dto));

        assertThat(ex.getMessage()).isEqualTo("email already used");
        verify(repository, never()).save(any());
    }

    @Test
    void update_whenUserNotFound_throwsNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        UserDto patch = new UserDto();
        patch.setName("New name");

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.update(1L, patch));

        assertThat(ex.getMessage()).isEqualTo("user not found");
    }

    @Test
    void update_whenEmailAlreadyUsed_throwsConflict() {
        User user = new User();
        user.setId(1L);
        user.setName("Old");
        user.setEmail("old@example.com");

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByEmail("new@example.com")).thenReturn(true);

        UserDto patch = new UserDto();
        patch.setEmail("new@example.com");

        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.update(1L, patch));

        assertThat(ex.getMessage()).isEqualTo("email already used");
        verify(repository, never()).save(any());
    }

    @Test
    void update_changesOnlyNonBlankFields() {
        User user = new User();
        user.setId(1L);
        user.setName("Old");
        user.setEmail("old@example.com");

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByEmail("new@example.com")).thenReturn(false);

        User saved = new User();
        saved.setId(1L);
        saved.setName("New name");
        saved.setEmail("new@example.com");
        when(repository.save(any(User.class))).thenReturn(saved);

        UserDto patch = new UserDto();
        patch.setName("New name");
        patch.setEmail("new@example.com");

        UserDto result = service.update(1L, patch);

        assertThat(result.getName()).isEqualTo("New name");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void getById_whenUserExists_returnsDto() {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getById_whenUserNotFound_throwsNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getById(1L));

        assertThat(ex.getMessage()).isEqualTo("user not found");
    }

    @Test
    void delete_whenUserExists_deletes() {
        User user = new User();
        user.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        service.delete(1L);

        verify(repository).delete(user);
    }

    @Test
    void delete_whenUserNotFound_throwsNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.delete(1L));

        assertThat(ex.getMessage()).isEqualTo("user not found");
        verify(repository, never()).delete(any());
    }
}
