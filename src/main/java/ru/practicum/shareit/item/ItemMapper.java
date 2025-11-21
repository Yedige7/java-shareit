package ru.practicum.shareit.item;

import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        ItemDto.ItemDtoBuilder builder = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner().getId());

        if (item.getRequest() != null) {
            builder.requestId(item.getRequest().getId());
        }

        return builder.build();
    }

    public static Item toItem(ItemDto dto, User owner, ItemRequest request) {
        return Item.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(owner)
                .request(request)           // ВАЖНО: просто объект, без getId()
                .build();
    }
}
