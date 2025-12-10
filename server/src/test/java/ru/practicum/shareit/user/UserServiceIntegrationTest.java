package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void create_persistsUserAndReturnsDto() {
        UserDto dto = UserDto.builder()
                .name("John")
                .email("john@test.com")
                .build();

        UserDto created = userService.create(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("John");
        assertThat(created.getEmail()).isEqualTo("john@test.com");

        UserDto fromDb = userService.getById(created.getId());
        assertThat(fromDb.getId()).isEqualTo(created.getId());
        assertThat(fromDb.getName()).isEqualTo("John");
        assertThat(fromDb.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void update_updatesUserFields() {
        UserDto dto = UserDto.builder()
                .name("John")
                .email("john@test.com")
                .build();

        UserDto created = userService.create(dto);

        UserDto patch = UserDto.builder()
                .name("Updated name")
                .email("updated@test.com")
                .build();

        UserDto updated = userService.update(created.getId(), patch);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo("Updated name");
        assertThat(updated.getEmail()).isEqualTo("updated@test.com");

        UserDto fromDb = userService.getById(created.getId());
        assertThat(fromDb.getName()).isEqualTo("Updated name");
        assertThat(fromDb.getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    void getById_returnsExistingUser() {
        UserDto dto = UserDto.builder()
                .name("John")
                .email("john@test.com")
                .build();

        UserDto created = userService.create(dto);

        UserDto fromDb = userService.getById(created.getId());

        assertThat(fromDb.getId()).isEqualTo(created.getId());
        assertThat(fromDb.getName()).isEqualTo("John");
        assertThat(fromDb.getEmail()).isEqualTo("john@test.com");
    }
}
