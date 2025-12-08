package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void serializeBookingDto() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 12, 31, 10, 15, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 1, 10, 15, 0);

        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setItemId(2L);
        dto.setBookerId(3L);
        dto.setStart(start);
        dto.setEnd(end);
        dto.setStatus(BookingStatus.APPROVED);

        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }

    @Test
    void deserializeBookingDto() throws JsonProcessingException {
        String body = """
        {
                  "id": 1,
                  "itemId": 2,
                  "bookerId": 3,
                  "start": "2025-12-31T10:15:00",
                  "end": "2026-01-01T10:15:00",
                  "status": "APPROVED"
        }
        """;

        BookingDto dto = mapper.readValue(body, BookingDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getItemId()).isEqualTo(2L);
        assertThat(dto.getBookerId()).isEqualTo(3L);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }
}
