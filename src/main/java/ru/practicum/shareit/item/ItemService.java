package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

interface ItemService {

    ItemDto create(Long userId, ItemDto dto);

    ItemDto getById(Long id);

    ItemDto update(Long userId, Long itemId, ItemDto dto);

    List<ItemDto> getOwnerItems(Long userId);

    List<ItemDto> search(String name);

    CommentDto addComment(Long userId, Long itemId, CommentDto dto);
}
