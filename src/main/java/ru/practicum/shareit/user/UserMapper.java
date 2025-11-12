package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail());
    }

    public static User fromDto(UserDto d) {
        return new User(d.getId(), d.getName(), d.getEmail());
    }
}