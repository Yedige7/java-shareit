package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static ru.practicum.shareit.user.UserMapper.fromDto;
import static ru.practicum.shareit.user.UserMapper.toDto;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public UserDto create(UserDto dto) {
        if (repository.existsByEmail(dto.getEmail())) throw new ConflictException("email already used");
        return toDto(repository.save(fromDto(dto)));
    }

    @Override
    public UserDto update(Long id, UserDto patch) {
        User user = repository.findById(id).orElseThrow(() -> new NotFoundException("user not found"));
        if (patch.getName() != null && !patch.getName().isBlank()) user.setName(patch.getName());
        if (patch.getEmail() != null && !patch.getEmail().isBlank()) {
            if (repository.existsByEmail(patch.getEmail())) throw new ConflictException("email already used");
            user.setEmail(patch.getEmail());
        }
        return toDto(repository.save(user));

    }

    @Override
    public UserDto getById(Long id) {
        User user = repository.findById(id).orElseThrow(() -> new NotFoundException("user not found"));
        return toDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        return List.of();
    }

    @Override
    public void delete(Long id) {
        User user = repository.findById(id).orElseThrow(() -> new NotFoundException("user not found"));
        repository.delete(user);
    }
}
