package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static ru.practicum.shareit.user.UserMapper.fromDto;
import static ru.practicum.shareit.user.UserMapper.toDto;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepository repository;


//    private static UserDto toDto(User u) {
//        UserDto d = new UserDto();
//        d.setId(u.getId());
//        d.setName(u.getName());
//        d.setEmail(u.getEmail());
//        return d;
//    }
//    private static User fromDto(UserDto d) {
//        return new User(d.getId(), d.getName(), d.getEmail());
//    }

    @Override
    public UserDto create(UserDto dto) {
        if (repository.existsByEmail(dto.getEmail())) throw new ValidationException("email already used");
        return toDto(repository.save(fromDto(dto)));
    }

    @Override
    public UserDto update(Long id, UserDto patch) {
        User u = repository.findById(id).orElseThrow(() -> new NotFoundException("user not found"));
        if (patch.getName() != null && !patch.getName().isBlank()) u.setName(patch.getName());
        if (patch.getEmail() != null && !patch.getEmail().isBlank()) {
            if (repository.existsByEmail(patch.getEmail())) throw new ValidationException("email already used");
            u.setEmail(patch.getEmail());
        }
        return toDto(repository.update(u));

    }

    @Override
    public UserDto getById(Long id) {
        User u = repository.findById(id).orElseThrow(() -> new NotFoundException("user not found"));
        return toDto(u);
    }

    @Override
    public List<UserDto> findAll() {
        return List.of();
    }

    @Override
    public void delete(Long id) {
        User u = repository.findById(id).orElseThrow(() -> new NotFoundException("user not found"));
        repository.delete(id);
    }
}
