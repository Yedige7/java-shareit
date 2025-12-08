package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;


interface UserService {
    UserDto create(UserDto dto);

    UserDto update(Long id, UserDto patch);

    UserDto getById(Long id);

    List<UserDto> findAll();

    void delete(Long id);
}
