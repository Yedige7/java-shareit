package ru.practicum.shareit.request;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, String description) {
        ensureUserExists(userId);
        ItemRequest entity = ItemRequestMapper.toItemRequest(userId, description);
        return toDtoWithItems(requestRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getOwn(Long userId) {
        ensureUserExists(userId);
        return requestRepository.findByRequestorIdOrderByCreatedDesc(userId)
                .stream()
                .map(this::toDtoWithItems)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAll(Long userId, int from, int size) {
        ensureUserExists(userId);
        Pageable page = PageRequest.of(from / size, size);
        return requestRepository.findByRequestorIdNotOrderByCreatedDesc(userId)
                .stream()
                .skip(from)
                .limit(size)
                .map(this::toDtoWithItems)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto getById(Long userId, Long requestId) {
        ensureUserExists(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("request not found"));
        return toDtoWithItems(request);
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("user not found");
        }
    }

    private ItemRequestDto toDtoWithItems(ItemRequest request) {
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);

        List<ItemDtoShort> items = itemRepository.findByRequestId(request.getId()).stream()
                .map(item -> ItemDtoShort.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .ownerId(item.getOwner().getId())
                        .build())
                .toList();

        dto.setItems(items);
        return dto;
    }

}
