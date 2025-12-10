package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void serialize_itemRequestDto_ok() throws Exception {
        LocalDateTime created = LocalDateTime.of(2023, 12, 1, 10, 15, 30);

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need drill")
                .created(created)
                .items(List.of())
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need drill");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2023-12-01T10:15:30");
    }

    @Test
    void deserialize_itemRequestDto_ok() throws Exception {
        String content = "{\n" +
                "  \"id\": 1,\n" +
                "  \"description\": \"Need drill\",\n" +
                "  \"created\": \"2023-12-01T10:15:30\"\n" +
                "}";


        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need drill");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2023, 12, 1, 10, 15, 30));
    }
}
