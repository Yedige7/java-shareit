package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;
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

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

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
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void getOwn_returnsOnlyRequesterRequests() {
        ItemRequestDto r1 = itemRequestService.create(requesterId, "Need drill");
        ItemRequestDto r2 = itemRequestService.create(requesterId, "Need hammer");
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

        List<ItemRequestDto> page1 = itemRequestService.getAll(otherUserId, 0, 2);
        List<ItemRequestDto> page2 = itemRequestService.getAll(otherUserId, 2, 2);

        assertThat(page1).hasSizeLessThanOrEqualTo(2);
        assertThat(page2).hasSizeLessThanOrEqualTo(2);

        Set<Long> allIds = Stream.concat(page1.stream(), page2.stream())
                .map(ItemRequestDto::getId)
                .collect(Collectors.toSet());

        assertThat(allIds).contains(r1.getId(), r2.getId(), r3.getId());
    }

    @Test
    void getById_returnsRequest() {
        ItemRequestDto created = itemRequestService.create(requesterId, "Need drill");

        ItemRequestDto fromService = itemRequestService.getById(otherUserId, created.getId());

        assertThat(fromService.getId()).isEqualTo(created.getId());
        assertThat(fromService.getDescription()).isEqualTo("Need drill");
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

    @Test
    void getOwn_populatesItemsForRequests() {

        ItemRequestDto requestDto = itemRequestService.create(requesterId, "Need drill");


        ItemRequest requestEntity = itemRequestRepository.findById(requestDto.getId())
                .orElseThrow(() -> new NotFoundException("request not found"));


        User owner = userRepository.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("user not found"));

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(requestEntity);
        itemRepository.save(item);


        List<ItemRequestDto> own = itemRequestService.getOwn(requesterId);

        assertThat(own).hasSize(1);
        ItemRequestDto fromService = own.get(0);
        assertThat(fromService.getId()).isEqualTo(requestDto.getId());
        assertThat(fromService.getItems()).hasSize(1);
        assertThat(fromService.getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getById_populatesItemsForRequest() {

        ItemRequestDto created = itemRequestService.create(requesterId, "Need drill");


        ItemRequest requestEntity = itemRequestRepository.findById(created.getId())
                .orElseThrow(() -> new NotFoundException("request not found"));


        User owner = userRepository.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("owner not found"));

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(requestEntity);
        itemRepository.save(item);


        ItemRequestDto fromService = itemRequestService.getById(otherUserId, created.getId());

        assertThat(fromService.getId()).isEqualTo(created.getId());
        assertThat(fromService.getDescription()).isEqualTo("Need drill");
        assertThat(fromService.getItems())
                .hasSize(1)
                .extracting("name")
                .containsExactly("Drill");
    }

    @Test
    void getAll_populatesItemsForRequestsOfOthers() {

        ItemRequestDto r1 = itemRequestService.create(requesterId, "Need drill");
        ItemRequestDto r2 = itemRequestService.create(requesterId, "Need hammer");
        ItemRequestDto r3 = itemRequestService.create(requesterId, "Need saw");


        ItemRequest req1 = itemRequestRepository.findById(r1.getId())
                .orElseThrow(() -> new NotFoundException("request1 not found"));
        ItemRequest req2 = itemRequestRepository.findById(r2.getId())
                .orElseThrow(() -> new NotFoundException("request2 not found"));
        ItemRequest req3 = itemRequestRepository.findById(r3.getId())
                .orElseThrow(() -> new NotFoundException("request3 not found"));


        User owner = userRepository.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("owner not found"));

        Item i1 = new Item();
        i1.setName("Drill");
        i1.setDescription("Powerful drill");
        i1.setAvailable(true);
        i1.setOwner(owner);          // подставь, если у тебя ownerId вместо owner
        i1.setRequest(req1);
        itemRepository.save(i1);

        Item i2 = new Item();
        i2.setName("Hammer");
        i2.setDescription("Heavy hammer");
        i2.setAvailable(true);
        i2.setOwner(owner);
        i2.setRequest(req2);
        itemRepository.save(i2);

        Item i3 = new Item();
        i3.setName("Saw");
        i3.setDescription("Sharp saw");
        i3.setAvailable(true);
        i3.setOwner(owner);
        i3.setRequest(req3);
        itemRepository.save(i3);


        List<ItemRequestDto> page = itemRequestService.getAll(otherUserId, 0, 10);


        assertThat(page).extracting(ItemRequestDto::getId)
                .contains(r1.getId(), r2.getId(), r3.getId());


        Map<Long, ItemRequestDto> byId = page.stream()
                .collect(Collectors.toMap(ItemRequestDto::getId, dto -> dto));

        assertThat(byId.get(r1.getId()).getItems())
                .hasSize(1)
                .extracting("name")
                .containsExactly("Drill");

        assertThat(byId.get(r2.getId()).getItems())
                .hasSize(1)
                .extracting("name")
                .containsExactly("Hammer");

        assertThat(byId.get(r3.getId()).getItems())
                .hasSize(1)
                .extracting("name")
                .containsExactly("Saw");
    }
}

