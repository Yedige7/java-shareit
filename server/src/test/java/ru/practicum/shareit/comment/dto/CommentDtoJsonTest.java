package ru.practicum.shareit.comment.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);

        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Nice item")
                .authorName("John")
                .created(now)
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Nice item");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("John");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2025-01-01T12:00:00");
    }

    @Test
    void testDeserialize() throws Exception {
        String jsonBody = """
                {
                  "id": 5,
                  "text": "Comment text",
                  "authorName": "Mike",
                  "created": "2025-02-10T13:12:00"
                }
                """;

        var dto = json.parseObject(jsonBody);

        assertThat(dto.getId()).isEqualTo(5);
        assertThat(dto.getText()).isEqualTo("Comment text");
        assertThat(dto.getAuthorName()).isEqualTo("Mike");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 2, 10, 13, 12));
    }
}
