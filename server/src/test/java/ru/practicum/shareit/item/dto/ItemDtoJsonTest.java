package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serialize_itemDto_ok() throws Exception {
        ItemDto.BookingShortDto lastBooking = new ItemDto.BookingShortDto();
        lastBooking.setId(5L);
        lastBooking.setBookerId(10L);

        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Good drill")
                .available(true)
                .ownerId(2L)
                .requestId(3L)
                .lastBooking(lastBooking)
                .nextBooking(null)
                .comments(List.of())
                .build();

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Good drill");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(3);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(5);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(10);
    }

    @Test
    void deserialize_itemDto_ok() throws Exception {
        String content = """
        {
                  "id": 1,
                  "name": "Drill",
                  "description": "Good drill",
                  "available": true,
                  "ownerId": 2,
                  "requestId": 3
        }
        """;

        ItemDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Drill");
        assertThat(dto.getDescription()).isEqualTo("Good drill");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getOwnerId()).isEqualTo(2L);
        assertThat(dto.getRequestId()).isEqualTo(3L);
    }
}
