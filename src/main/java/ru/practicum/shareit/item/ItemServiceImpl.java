package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;

    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, ItemDto dto) {
        User u = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user not found"));
        return ItemMapper.toItemDto(repository.save(ItemMapper.toItem(userId, dto)));
    }

    @Override
    public ItemDto getById(Long id) {
        Item i = repository.findById(id).orElseThrow(() -> new NotFoundException("item not found"));
        return ItemMapper.toItemDto(i);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto dto) {
        User u = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user not found"));
        Item i = repository.findById(itemId).orElseThrow(() -> new NotFoundException("item not found"));
        if (dto.getName() != null && !dto.getName().isBlank()) i.setName(dto.getName());
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            i.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            i.setAvailable(dto.getAvailable());
        }
        return ItemMapper.toItemDto(repository.save(ItemMapper.toItem(userId, dto)));
    }

    @Override
    public List<ItemDto> getOwnerItems(Long userId) {
        User u = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user not found"));
        return repository.findByOwnerId(userId).stream().map(ItemMapper::toItemDto).toList();
    }


    @Override
    public List<ItemDto> search(String name) {
        return repository.searchAvailable(name).stream().map(ItemMapper::toItemDto).toList();
    }
}
