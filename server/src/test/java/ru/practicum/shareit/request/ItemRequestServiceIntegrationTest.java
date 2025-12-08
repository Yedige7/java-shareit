package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    private Long requesterId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        User requester = new User();
        requester.setName("Requester");
        requester.setEmail("requester@test.com");
        requesterId = userRepository.save(requester).getId();

        User other = new User();
        other.setName("Other");
        other.setEmail("other@test.com");
        otherUserId = userRepository.save(other).getId();
    }

    @Test
    void create_persistsRequestAndReturnsDto() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need drill")
                .build();

        ItemRequestDto created = itemRequestService.create(requesterId, dto.getDescription());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need drill");
        assertThat(created.getId()).isEqualTo(requesterId);
        assertThat(created.getCreated()).isNotNull();
    }

    @Test
    void getOwn_returnsOnlyRequesterRequests_sortedByCreatedDesc() throws InterruptedException {
        ItemRequestDto dto1 = ItemRequestDto.builder()
                .description("Need drill")
                .build();
        ItemRequestDto dto2 = ItemRequestDto.builder()
                .description("Need hammer")
                .build();

        ItemRequestDto r1 = itemRequestService.create(requesterId, dto1.getDescription());
        // небольшая задержка, если created = now()
        Thread.sleep(5);
        ItemRequestDto r2 = itemRequestService.create(requesterId, dto2.getDescription());

        List<ItemRequestDto> own = itemRequestService.getOwn(requesterId);

        assertThat(own).hasSize(2);
        assertThat(own.get(0).getId()).isEqualTo(r2.getId());
        assertThat(own.get(1).getId()).isEqualTo(r1.getId());
    }

    @Test
    void getAll_returnsRequestsOfOthersOnly() {
        ItemRequestDto dto1 = ItemRequestDto.builder()
                .description("Need drill")
                .build();
        ItemRequestDto dto2 = ItemRequestDto.builder()
                .description("Need hammer")
                .build();

        ItemRequestDto r1 = itemRequestService.create(requesterId, dto1.getDescription());
        ItemRequestDto r2 = itemRequestService.create(requesterId, dto2.getDescription());

        // другой пользователь смотрит все запросы
        List<ItemRequestDto> all = itemRequestService.getAll(otherUserId, 0, 10);

        assertThat(all)
                .extracting(ItemRequestDto::getId)
                .containsExactlyInAnyOrder(r1.getId(), r2.getId());
    }

    @Test
    void getById_returnsRequestWithAnswers() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need drill")
                .build();

        ItemRequestDto created = itemRequestService.create(requesterId, dto.getDescription());

        ItemRequestDto fromService = itemRequestService.getById(otherUserId, created.getId());

        assertThat(fromService.getId()).isEqualTo(created.getId());
        assertThat(fromService.getDescription()).isEqualTo("Need drill");
        assertThat(fromService.getId()).isEqualTo(requesterId);
        // здесь можно ещё проверить список ответов (items), если он у тебя есть в DTO
    }
}
