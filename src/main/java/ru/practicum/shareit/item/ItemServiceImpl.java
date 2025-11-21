package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final CommentRepository commentRepository;

    private final BookingRepository bookingRepository;



    @Override
    public ItemDto create(Long userId, ItemDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user not found"));
        // пока игнорируем dto.getRequestId()
        return ItemMapper.toItemDto(itemRepository.save(ItemMapper.toItem(dto, user, null)));
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("item not found"));
        ItemDto dto = ItemMapper.toItemDto(item);
        dto.setComments(
                commentRepository.findByItemIdOrderByCreatedDesc(id)
                        .stream()
                        .map(CommentMapper::toDto)
                        .collect(Collectors.toList())
        );
        //setBookingsForItem(dto, user);
        return dto;
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user not found"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("item not found"));
        if (dto.getName() != null && !dto.getName().isBlank()) item.setName(dto.getName());
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            item.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public List<ItemDto> getOwnerItems(Long userId) {
        User u = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user not found"));
        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(userId);
        List<ItemDto> result = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        for (ItemDto dto : result) {

            dto.setComments(
                    commentRepository.findByItemIdOrderByCreatedDesc(dto.getId())
                            .stream()
                            .map(CommentMapper::toDto)
                            .collect(Collectors.toList())
            );
            setBookingsForItem(dto, userId);
        }
        return result;
    }

    private void setBookingsForItem(ItemDto itemDto, Long requesterId) {
        Item item = itemRepository.findById(itemDto.getId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getOwner().getId().equals(requesterId)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        bookingRepository
                .findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                        item.getId(), now, BookingStatus.APPROVED)
                .ifPresent(b -> {
                    ItemDto.BookingShortDto last = new ItemDto.BookingShortDto();
                    last.setId(b.getId());
                    last.setBookerId(b.getBooker().getId());
                    itemDto.setLastBooking(last);
                });

        bookingRepository
                .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                        item.getId(), now, BookingStatus.APPROVED)
                .ifPresent(b -> {
                    ItemDto.BookingShortDto next = new ItemDto.BookingShortDto();
                    next.setId(b.getId());
                    next.setBookerId(b.getBooker().getId());
                    itemDto.setNextBooking(next);
                });
    }


    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<Item> items = itemRepository.searchAvailable(text);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto dto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings =
                bookingRepository.findPastApprovedBookingForComment(userId, itemId, now);
        if (bookings.isEmpty()) {
            throw new ValidationException("User has not completed booking for this item");
        }

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }
}
