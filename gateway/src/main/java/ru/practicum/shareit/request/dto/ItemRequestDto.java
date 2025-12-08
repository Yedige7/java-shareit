package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ItemRequestDto {
    private Long id;

    @NotBlank
    private String description;

    private LocalDateTime created;

    private List<ItemRequestItemDto> items;

    @Data
    public static class ItemRequestItemDto {
        private Long id;
        private String name;
        private Long ownerId;
        private Long requestId;
    }


}
