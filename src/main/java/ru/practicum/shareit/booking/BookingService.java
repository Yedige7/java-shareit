package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(Long userId, BookingDto dto);
    BookingResponseDto approve(Long ownerId, Long bookingId, boolean approved);
    BookingResponseDto getById(Long userId, Long bookingId);
    List<BookingResponseDto> getBookingsForUser(Long userId, String state, int from, int size);
    List<BookingResponseDto> getBookingsForOwner(Long ownerId, String state, int from, int size);
}
