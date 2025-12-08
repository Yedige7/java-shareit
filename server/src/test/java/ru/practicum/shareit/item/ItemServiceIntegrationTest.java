package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    private Long ownerId;
    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        ownerId = userRepository.save(owner).getId();

        ItemDto item1 = ItemDto.builder()
                .name("Drill")
                .description("Simple drill")
                .available(true)
                .build();
        itemService.create(ownerId, item1);

        ItemDto item2 = ItemDto.builder()
                .name("Hammer")
                .description("Good hammer")
                .available(true)
                .build();
        itemService.create(ownerId, item2);
    }

    @Test
    void createAndGetById_basicFlow() {
        UserDto owner = new UserDto();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner = userService.create(owner);

        ItemDto toCreate = ItemDto.builder()
                .name("Drill")
                .description("Good drill")
                .available(true)
                .build();

        ItemDto created = itemService.create(owner.getId(), toCreate);

        ItemDto found = itemService.getById(owner.getId(), created.getId());

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("Drill");
        assertThat(found.getDescription()).isEqualTo("Good drill");
        assertThat(found.getAvailable()).isTrue();
    }

    @Test
    void search_findsByNameOrDescription_ignoringCase() {
        UserDto owner = new UserDto();
        owner.setName("Owner2");
        owner.setEmail("owner2@example.com");
        owner = userService.create(owner);


        itemService.create(owner.getId(), ItemDto.builder()
                .name("Power drill")
                .description("For concrete")
                .available(true)
                .build());

        itemService.create(owner.getId(), ItemDto.builder()
                .name("Hammer")
                .description("Steel hammer")
                .available(true)
                .build());


        List<ItemDto> result = itemService.search("drill");

        var names = result.stream()
                .map(ItemDto::getName)
                .toList();

        // важное
        assertThat(names).contains("Power drill");
        assertThat(names).doesNotContain("Hammer");
    }

    @Test
    void getOwnerItems_returnsItemsForOwner() {
        List<ItemDto> items = itemService.getOwnerItems(ownerId);

        assertThat(items).hasSize(2);
        assertThat(items)
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Drill", "Hammer");
    }

    @Test
    void search_returnsOnlyAvailableItemsContainingText() {
        List<ItemDto> result = itemService.search("drill");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Drill");
    }
}
