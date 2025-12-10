package ru.practicum.shareit.comment;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void toDto_returnsCorrectDto() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 10, 0);

        User author = new User();
        author.setId(10L);
        author.setName("Alex");
        author.setEmail("a@a.com");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("text");
        comment.setAuthor(author);
        comment.setCreated(now);

        CommentDto dto = CommentMapper.toDto(comment);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("text");
        assertThat(dto.getAuthorName()).isEqualTo("Alex");
        assertThat(dto.getCreated()).isEqualTo(now);
    }
}
