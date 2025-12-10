package ru.practicum.shareit.user;

import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor
public final class UserMapper {
    public static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail());
    }

    public static User fromDto(UserDto d) {
        return new User(d.getId(), d.getName(), d.getEmail());
    }
}