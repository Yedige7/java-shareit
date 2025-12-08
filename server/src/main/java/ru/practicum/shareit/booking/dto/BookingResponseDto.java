package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Data
public class BookingResponseDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingItemDto item;
    private BookingBookerDto booker;
    private BookingStatus status;

    @Data
    public static class BookingItemDto {
        private Long id;
        private String name;
    }

    @Data
    public static class BookingBookerDto {
        private Long id;
    }
}
