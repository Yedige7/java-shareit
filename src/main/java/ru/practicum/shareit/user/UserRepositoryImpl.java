package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class UserRepositoryImpl implements UserRepository {
    private final List<User> users = new ArrayList<>();

    @Override
    public List<User> findAll() {
        return users;
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(getId());
        }
        users.add(user);
        return user;
    }

    @Override
    public Optional<User> getUser(Long id) {
        if (id == null) return Optional.empty();
        return users.stream()
                .filter(u -> Objects.equals(u.getId(), id))
                .findFirst();
    }

    @Override
    public User update(User newUser) {
        Optional<User> oldUser = users.stream()
                .filter(u -> Objects.equals(u.getId(), newUser.getId()))
                .findFirst();
        if (oldUser.isEmpty()) {
            log.warn("Попытка ообновить несуществующего пользователя с id={} из InMemory хранилища", newUser.getId());
            return null;
        }
        delete(oldUser.get().getId());
        users.add(newUser);

        return newUser;

    }

    @Override
    public void delete(Long id) {
        Optional<User> user = getUser(id);
        if (user.isEmpty()) {
            log.warn("Попытка удалить несуществующего пользователя с id={} из InMemory хранилища", id);
        } else {
            users.remove(user);
            log.info("Пользователь с id={} удален из InMemory хранилища", id);
        }
    }

    private long getId() {
        long lastId = users.stream()
                .mapToLong(User::getId)
                .max()
                .orElse(1);
        return lastId + 1;
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return true;
        }
        Optional<User> isUser = users.stream()
                .filter(u -> Objects.equals(u.getEmail(), email))
                .findFirst();
        return isUser.isPresent();
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null || id < 0) {
            log.info("Id должен быть указан ");
            throw new NotFoundException("Id должен быть указан");
        }
        return getUser(id);
    }
}
