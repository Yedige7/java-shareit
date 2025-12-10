package ru.practicum.shareit.booking;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        ownerId = userRepository.save(owner).getId();

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@test.com");
        bookerId = userRepository.save(booker).getId();

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Simple drill");
        item.setAvailable(true);
        item.setOwner(owner);
        itemId = itemRepository.save(item).getId();
    }

    @Test
    void createBooking_persistsWithWaitingStatus() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDto createDto = new BookingDto();
        createDto.setItemId(itemId);
        createDto.setStart(start);
        createDto.setEnd(end);

        BookingResponseDto created = bookingService.create(bookerId, createDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getItem().getId()).isEqualTo(itemId);
        assertThat(created.getBooker().getId()).isEqualTo(bookerId);
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void approveBooking_changesStatusToApproved() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDto createDto = new BookingDto();
        createDto.setItemId(itemId);
        createDto.setStart(start);
        createDto.setEnd(end);

        BookingResponseDto created = bookingService.create(bookerId, createDto);

        BookingResponseDto approved = bookingService.approve(ownerId, created.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getByBookerAndOwner_returnCorrectBookings() {
        LocalDateTime start1 = LocalDateTime.now().plusDays(1);
        LocalDateTime end1 = LocalDateTime.now().plusDays(2);

        BookingDto dto1 = new BookingDto();
        dto1.setItemId(itemId);
        dto1.setStart(start1);
        dto1.setEnd(end1);

        BookingResponseDto booking1 = bookingService.create(bookerId, dto1);
        bookingService.approve(ownerId, booking1.getId(), true);

        List<BookingResponseDto> byBooker = bookingService.getBookingsForUser(bookerId, String.valueOf(BookingState.ALL), 0, 10);
        assertThat(byBooker)
                .extracting(BookingResponseDto::getId)
                .contains(booking1.getId());

        List<BookingResponseDto> byOwner = bookingService.getBookingsForOwner(ownerId, String.valueOf(BookingState.ALL), 0, 10);
        assertThat(byOwner)
                .extracting(BookingResponseDto::getId)
                .contains(booking1.getId());
    }
}
