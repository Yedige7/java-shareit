package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> storage = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) item.setId(seq.incrementAndGet());
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Item> searchAvailable(String name) {
        if (name == null || name.isBlank()) return List.of();
        final String q = name.toLowerCase();
        return storage.values().stream()
                .filter(Objects::nonNull)
                .filter(i -> Boolean.TRUE.equals(i.getAvailable())) // безопасно к null
                .filter(i -> {
                    final String n = i.getName();
                    final String d = i.getDescription();
                    return (n != null && n.toLowerCase(Locale.ROOT).contains(q))
                            || (d != null && d.toLowerCase(Locale.ROOT).contains(q));
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return storage.values().stream().filter(i -> Objects.equals(i.getOwnerId(), ownerId)).collect(Collectors.toList());
    }
}
