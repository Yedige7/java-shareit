package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

public class BookingMapper {
    public static Booking toBooking(BookingDto dto) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        return booking;
    }

    public static BookingResponseDto toResponse(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus().name());

        BookingResponseDto.BookingItemDto itemDto = new BookingResponseDto.BookingItemDto();
        itemDto.setId(booking.getItem().getId());
        itemDto.setName(booking.getItem().getName());
        dto.setItem(itemDto);

        BookingResponseDto.BookingBookerDto bookerDto = new BookingResponseDto.BookingBookerDto();
        bookerDto.setId(booking.getBooker().getId());
        dto.setBooker(bookerDto);

        return dto;
    }
}
