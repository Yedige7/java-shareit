package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl service;

    // ---------- create ----------

    @Test
    void create_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BookingDto dto = new BookingDto();

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.create(1L, dto));

        assertThat(ex.getMessage()).isEqualTo("Пользователь не найден: id=1");
        verifyNoInteractions(itemRepository, bookingRepository);
    }

    @Test
    void create_whenItemIdIsNull_throwsValidation() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BookingDto dto = new BookingDto();
        dto.setItemId(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(1L, dto));

        assertThat(ex.getMessage()).isEqualTo("Не указана вещь для бронирования");
        verifyNoInteractions(itemRepository, bookingRepository);
    }

    @Test
    void create_whenItemNotFound_throwsNotFound() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        BookingDto dto = new BookingDto();
        dto.setItemId(10L);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.create(1L, dto));

        assertThat(ex.getMessage()).isEqualTo("Вещь не найдена: id=10");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void create_whenItemNotAvailable_throwsValidation() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);
        item.setAvailable(false);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        BookingDto dto = new BookingDto();
        dto.setItemId(10L);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(1L, dto));

        assertThat(ex.getMessage()).isEqualTo("Вещь недоступна для бронирования");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void create_whenOwnerTriesToBookOwnItem_throwsNotFound() {
        User owner = new User();
        owner.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);
        item.setAvailable(true);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        BookingDto dto = new BookingDto();
        dto.setItemId(10L);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.create(1L, dto));

        assertThat(ex.getMessage()).isEqualTo("Владелец не может бронировать свою вещь");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void create_whenDatesInvalid_throwsValidation() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);
        item.setAvailable(true);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        BookingDto dto = new BookingDto();
        dto.setItemId(10L);
        LocalDateTime now = LocalDateTime.now();
        dto.setStart(now.plusDays(1));
        dto.setEnd(now); // start !before end

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(1L, dto));

        assertThat(ex.getMessage()).isEqualTo("Некорректные даты бронирования");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void create_whenValid_savesBookingWithWaitingStatus() {
        User booker = new User();
        booker.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);
        item.setAvailable(true);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        BookingDto dto = new BookingDto();
        dto.setItemId(10L);
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0, Booking.class);
            b.setId(100L);
            return b;
        });

        BookingResponseDto result = service.create(1L, dto);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);

        verify(bookingRepository).save(any(Booking.class));
    }

    // ---------- approve ----------

    @Test
    void approve_whenBookingNotFound_throwsNotFound() {
        when(bookingRepository.findById(5L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.approve(1L, 5L, true));

        assertThat(ex.getMessage()).isEqualTo("Бронирование не найдено: id=5");
    }

    @Test
    void approve_whenUserNotOwner_throwsValidation() {
        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(5L);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.approve(1L, 5L, true));

        assertThat(ex.getMessage()).isEqualTo("Пользователь не является владельцем вещи");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approve_whenStatusNotWaiting_throwsValidation() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(5L);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.approve(1L, 5L, true));

        assertThat(ex.getMessage()).isEqualTo("Статус бронирования уже изменён");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approve_whenApproved_setsApprovedStatus() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);
        User booker = new User();
        booker.setId(3L);
        Booking booking = new Booking();
        booking.setId(5L);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(booker); // <<< обязательно

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDto result = service.approve(1L, 5L, true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).save(any(Booking.class));
    }

    // ---------- getById ----------

    @Test
    void getById_whenUserIsBooker_returnsDto() {
        User booker = new User();
        booker.setId(1L);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(5L);
        booking.setBooker(booker);
        booking.setItem(item);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = service.getById(1L, 5L);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void getById_whenUserHasNoRights_throwsNotFound() {
        User booker = new User();
        booker.setId(1L);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(5L);
        booking.setBooker(booker);
        booking.setItem(item);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getById(3L, 5L));

        assertThat(ex.getMessage()).isEqualTo("Доступ к бронированию запрещён");
    }

    // ---------- getBookingsForUser ----------

    @Test
    void getBookingsForUser_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getBookingsForUser(1L, "ALL", 0, 10));

        assertThat(ex.getMessage()).isEqualTo("Пользователь не найден: id=1");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void getBookingsForUser_whenStateAll_usesFindByBooker() {

        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);
        item.setAvailable(true);

        User booker = new User();
        booker.setId(1L);

        Booking booking = new Booking();
        booking.setId(5L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(1));

        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(1L), any(Pageable.class)))
                .thenReturn(Collections.singletonList(booking));

        List<BookingResponseDto> result =
                service.getBookingsForUser(1L, "ALL", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(5L);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.APPROVED);

        verify(bookingRepository).findByBookerIdOrderByStartDesc(eq(1L), any(Pageable.class));
    }

    // ---------- getBookingsForOwner ----------

    @Test
    void getBookingsForOwner_whenStateWaiting_usesWaitingRepositoryMethod() {
        User owner = new User();
        owner.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        User booker = new User();
        booker.setId(3L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);
        item.setAvailable(true);

        Booking booking = new Booking();
        booking.setId(7L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                eq(2L), eq(BookingStatus.WAITING), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result =
                service.getBookingsForOwner(2L, "WAITING", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(7L);

        verify(bookingRepository).findByItemOwnerIdAndStatusOrderByStartDesc(
                eq(2L), eq(BookingStatus.WAITING), any(Pageable.class));
    }
}
