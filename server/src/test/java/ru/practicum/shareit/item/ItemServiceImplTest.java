package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void create_whenUserExistsAndNoRequest_savesItem() {
        User owner = new User();
        owner.setId(1L);
        owner.setName("Owner");

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        ItemDto dto = new ItemDto();
        dto.setName("Drill");
        dto.setDescription("Good drill");
        dto.setAvailable(true);

        Item saved = new Item();
        saved.setId(10L);
        saved.setName("Drill");
        saved.setDescription("Good drill");
        saved.setAvailable(true);
        saved.setOwner(owner);

        when(itemRepository.save(any(Item.class))).thenReturn(saved);

        ItemDto result = itemService.create(1L, dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Drill");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void create_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ItemDto dto = new ItemDto();
        dto.setName("Drill");
        dto.setAvailable(true);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.create(1L, dto));

        assertThat(ex.getMessage()).isEqualTo("user not found");
        verify(itemRepository, never()).save(any());
    }

    @Test
    void create_whenRequestIdSet_andRequestNotFound_throwsNotFound() {
        User owner = new User();
        owner.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(100L)).thenReturn(Optional.empty());

        ItemDto dto = new ItemDto();
        dto.setName("Drill");
        dto.setAvailable(true);
        dto.setRequestId(100L);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.create(1L, dto));

        assertThat(ex.getMessage()).isEqualTo("request not found");
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getById_whenItemExists_setsCommentsAndBookingsForOwner() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(10L);
        item.setName("Drill");
        item.setDescription("Good");
        item.setAvailable(true);
        item.setOwner(owner);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        Comment comment = new Comment();
        comment.setId(5L);
        comment.setText("Nice");
        comment.setItem(item);
        comment.setAuthor(owner);
        comment.setCreated(LocalDateTime.now().minusDays(1));

        when(commentRepository.findByItemIdOrderByCreatedDesc(10L))
                .thenReturn(List.of(comment));

        Booking lastBooking = new Booking();
        lastBooking.setId(100L);
        lastBooking.setItem(item);
        lastBooking.setBooker(owner);
        lastBooking.setStatus(BookingStatus.APPROVED);
        lastBooking.setStart(LocalDateTime.now().minusDays(2));

        when(bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.of(lastBooking));

        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());

        ItemDto result = itemService.getById(1L, 10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getText()).isEqualTo("Nice");
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getLastBooking().getId()).isEqualTo(100L);
    }

    @Test
    void getById_whenItemNotFound_throwsNotFound() {
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.getById(1L, 10L));

        assertThat(ex.getMessage()).isEqualTo("item not found");
    }

    @Test
    void update_updatesOnlyNonNullFields() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(10L);
        item.setName("Old name");
        item.setDescription("Old desc");
        item.setAvailable(false);
        item.setOwner(owner);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        Item saved = new Item();
        saved.setId(10L);
        saved.setName("New name");
        saved.setDescription("New desc");
        saved.setAvailable(true);
        saved.setOwner(owner);

        when(itemRepository.save(any(Item.class))).thenReturn(saved);

        ItemDto patch = new ItemDto();
        patch.setName("New name");
        patch.setDescription("New desc");
        patch.setAvailable(true);

        ItemDto result = itemService.update(1L, 10L, patch);

        assertThat(result.getName()).isEqualTo("New name");
        assertThat(result.getDescription()).isEqualTo("New desc");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void update_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ItemDto patch = new ItemDto();
        patch.setName("New");

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.update(1L, 10L, patch));

        assertThat(ex.getMessage()).isEqualTo("user not found");
    }

    @Test
    void update_whenItemNotFound_throwsNotFound() {
        User owner = new User();
        owner.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        ItemDto patch = new ItemDto();
        patch.setName("New");

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.update(1L, 10L, patch));

        assertThat(ex.getMessage()).isEqualTo("item not found");
    }

    @Test
    void search_whenTextBlank_returnsEmptyListAndDoesNotCallRepo() {
        List<ItemDto> result1 = itemService.search(null);
        List<ItemDto> result2 = itemService.search("");
        List<ItemDto> result3 = itemService.search("   ");

        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
        assertThat(result3).isEmpty();

        verify(itemRepository, never()).searchAvailable(anyString());
    }

    @Test
    void addComment_whenNoPastApprovedBookings_throwsValidation() {
        User user = new User();
        user.setId(1L);

        Item item = new Item();
        item.setId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.findPastApprovedBookingForComment(eq(1L), eq(10L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        CommentDto dto = new CommentDto();
        dto.setText("Some text");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> itemService.addComment(1L, 10L, dto));

        assertThat(ex.getMessage()).isEqualTo("User has not completed booking for this item");
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_whenPastApprovedBookingExists_savesComment() {
        User user = new User();
        user.setId(1L);

        Item item = new Item();
        item.setId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findPastApprovedBookingForComment(eq(1L), eq(10L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        Comment saved = new Comment();
        saved.setId(5L);
        saved.setText("Nice item");
        saved.setItem(item);
        saved.setAuthor(user);
        saved.setCreated(LocalDateTime.now());

        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentDto dto = new CommentDto();
        dto.setText("Nice item");

        CommentDto result = itemService.addComment(1L, 10L, dto);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getText()).isEqualTo("Nice item");

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getText()).isEqualTo("Nice item");
        assertThat(captor.getValue().getItem().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getAuthor().getId()).isEqualTo(1L);
    }
}
