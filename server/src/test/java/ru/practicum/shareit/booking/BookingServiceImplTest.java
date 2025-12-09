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
        dto.setEnd(now);

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
        booking.setBooker(booker);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDto result = service.approve(1L, 5L, true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).save(any(Booking.class));
    }

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

    @Test
    void getBookingsForUser_withUnknownState_throwsValidation() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.getBookingsForUser(1L, "UNKNOWN", 0, 10));

        // Сообщение подгони под своё, главное — попадаем в ветку "неизвестное состояние"
        assertThat(ex.getMessage()).contains("UNKNOWN");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void getBookingsForOwner_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getBookingsForOwner(2L, "ALL", 0, 10));

        assertThat(ex.getMessage()).isEqualTo("Пользователь не найден: id=2");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void getBookingsForOwner_withUnknownState_throwsValidation() {
        User owner = new User();
        owner.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.getBookingsForOwner(2L, "UNKNOWN", 0, 10));

        assertThat(ex.getMessage()).contains("UNKNOWN");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void approve_whenRejected_setsRejectedStatus() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        User booker = new User();
        booker.setId(3L);

        Booking booking = new Booking();
        booking.setId(5L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDto result = service.approve(1L, 5L, false);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void getBookingsForUser_whenStateCurrent_usesCurrentRepositoryMethod() {
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

        when(bookingRepository.findCurrentByBooker(eq(1L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result =
                service.getBookingsForUser(1L, "CURRENT", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(5L);

        verify(bookingRepository).findCurrentByBooker(eq(1L), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getBookingsForUser_whenStatePast_usesPastRepositoryMethod() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        User booker = new User();
        booker.setId(1L);

        Booking booking = new Booking();
        booking.setId(6L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));

        when(bookingRepository.findPastByBooker(eq(1L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result =
                service.getBookingsForUser(1L, "PAST", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(6L);

        verify(bookingRepository).findPastByBooker(eq(1L), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getBookingsForUser_whenStateFuture_usesFutureRepositoryMethod() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        User booker = new User();
        booker.setId(1L);

        Booking booking = new Booking();
        booking.setId(7L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingRepository.findFutureByBooker(eq(1L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result =
                service.getBookingsForUser(1L, "FUTURE", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(7L);

        verify(bookingRepository).findFutureByBooker(eq(1L), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getBookingsForUser_whenStateWaiting_usesWaitingRepositoryMethod() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        User booker = new User();
        booker.setId(1L);

        Booking booking = new Booking();
        booking.setId(8L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                eq(1L), eq(BookingStatus.WAITING), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result =
                service.getBookingsForUser(1L, "WAITING", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(8L);

        verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(
                eq(1L), eq(BookingStatus.WAITING), any(Pageable.class));
    }

    @Test
    void getBookingsForUser_whenStateRejected_usesRejectedRepositoryMethod() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        User booker = new User();
        booker.setId(1L);

        Booking booking = new Booking();
        booking.setId(9L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.REJECTED);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                eq(1L), eq(BookingStatus.REJECTED), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result =
                service.getBookingsForUser(1L, "REJECTED", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(9L);

        verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(
                eq(1L), eq(BookingStatus.REJECTED), any(Pageable.class));
    }

    @Test
    void getBookingsForOwner_whenStateAll_usesFindByOwnerId() {
        User owner = new User();
        owner.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        User booker = new User();
        booker.setId(3L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(11L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingRepository.findByOwnerId(eq(2L), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result =
                service.getBookingsForOwner(2L, "ALL", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(11L);

        verify(bookingRepository).findByOwnerId(eq(2L), any(Pageable.class));
    }

    @Test
    void getBookingsForOwner_whenStateCurrent_usesCurrentRepositoryMethod() {
        User owner = new User();
        owner.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        User booker = new User();
        booker.setId(3L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(12L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(1));

        when(bookingRepository.findCurrentByOwner(eq(2L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result =
                service.getBookingsForOwner(2L, "CURRENT", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(12L);

        verify(bookingRepository).findCurrentByOwner(eq(2L), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getBookingsForOwner_whenStatePast_usesPastRepositoryMethod() {
        User owner = new User();
        owner.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        User booker = new User();
        booker.setId(3L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(13L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));

        when(bookingRepository.findPastByOwner(eq(2L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result =
                service.getBookingsForOwner(2L, "PAST", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(13L);

        verify(bookingRepository).findPastByOwner(eq(2L), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getBookingsForOwner_whenStateFuture_usesFutureRepositoryMethod() {
        User owner = new User();
        owner.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        User booker = new User();
        booker.setId(3L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(14L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingRepository.findFutureByOwner(eq(2L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result =
                service.getBookingsForOwner(2L, "FUTURE", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(14L);

        verify(bookingRepository).findFutureByOwner(eq(2L), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getBookingsForOwner_whenStateRejected_usesRejectedRepositoryMethod() {
        User owner = new User();
        owner.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        User booker = new User();
        booker.setId(3L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(15L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.REJECTED);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                eq(2L), eq(BookingStatus.REJECTED), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result =
                service.getBookingsForOwner(2L, "REJECTED", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(15L);

        verify(bookingRepository).findByItemOwnerIdAndStatusOrderByStartDesc(
                eq(2L), eq(BookingStatus.REJECTED), any(Pageable.class));
    }
}
