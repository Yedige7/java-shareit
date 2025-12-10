package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

public class ItemRequestMapper {

    private ItemRequestMapper() {
    }

    public static ItemRequest toItemRequest(User user, String description) {
        return ItemRequest.builder()
                .description(description)
                .created(LocalDateTime.now())
                .requestor(user)
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(Collections.emptyList())
                .build();
    }
}
