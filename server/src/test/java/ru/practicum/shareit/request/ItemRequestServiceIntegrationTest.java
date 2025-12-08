package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
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
        ItemRequestDto created = itemRequestService.create(requesterId, "Need drill");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need drill");
        assertThat(created.getCreated()).isNotNull();
        // сейчас toDtoWithItems отдаёт пустой список при отсутствии вещей
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void getOwn_returnsOnlyRequesterRequests() {
        ItemRequestDto r1 = itemRequestService.create(requesterId, "Need drill");
        ItemRequestDto r2 = itemRequestService.create(requesterId, "Need hammer");
        // чужой запрос
        itemRequestService.create(otherUserId, "Other request");

        List<ItemRequestDto> own = itemRequestService.getOwn(requesterId);

        assertThat(own).hasSize(2);
        assertThat(own)
                .extracting(ItemRequestDto::getDescription)
                .containsExactlyInAnyOrder("Need drill", "Need hammer");
        assertThat(own)
                .extracting(ItemRequestDto::getId)
                .contains(r1.getId(), r2.getId());
    }

    @Test
    void getAll_returnsRequestsOfOthersOnly_andRespectsPagination() {
        ItemRequestDto r1 = itemRequestService.create(requesterId, "Need drill");
        ItemRequestDto r2 = itemRequestService.create(requesterId, "Need hammer");
        ItemRequestDto r3 = itemRequestService.create(requesterId, "Need saw");

        // другой пользователь смотрит все запросы постранично
        List<ItemRequestDto> page1 = itemRequestService.getAll(otherUserId, 0, 2);
        List<ItemRequestDto> page2 = itemRequestService.getAll(otherUserId, 2, 2);

        // размер страниц не больше size
        assertThat(page1).hasSizeLessThanOrEqualTo(2);
        assertThat(page2).hasSizeLessThanOrEqualTo(2);

        // собираем id всех запросов из обеих страниц
        Set<Long> allIds = Stream.concat(page1.stream(), page2.stream())
                .map(ItemRequestDto::getId)
                .collect(Collectors.toSet());

        // три созданных нами запроса присутствуют в совокупности двух страниц
        assertThat(allIds).contains(r1.getId(), r2.getId(), r3.getId());
    }

    @Test
    void getById_returnsRequest() {
        ItemRequestDto created = itemRequestService.create(requesterId, "Need drill");

        ItemRequestDto fromService = itemRequestService.getById(otherUserId, created.getId());

        assertThat(fromService.getId()).isEqualTo(created.getId());
        assertThat(fromService.getDescription()).isEqualTo("Need drill");
        // items по умолчанию пустые, т.к. вещей ещё нет
        assertThat(fromService.getItems()).isEmpty();
    }

    @Test
    void methodsThrowNotFound_whenUserDoesNotExist() {
        Long unknownUserId = 9999L;

        assertThatThrownBy(() -> itemRequestService.create(unknownUserId, "x"))
                .isInstanceOf(NotFoundException.class);

        assertThatThrownBy(() -> itemRequestService.getOwn(unknownUserId))
                .isInstanceOf(NotFoundException.class);

        assertThatThrownBy(() -> itemRequestService.getAll(unknownUserId, 0, 10))
                .isInstanceOf(NotFoundException.class);

        assertThatThrownBy(() -> itemRequestService.getById(unknownUserId, 1L))
                .isInstanceOf(NotFoundException.class);
    }
}

