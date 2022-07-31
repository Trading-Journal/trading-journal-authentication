package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.user.service.UserManagementService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
class UserManagementServiceImplPageableIntegratedTest {

    private static Tenancy tenancy;
    @Autowired
    UserManagementService userManagementService;

    @BeforeAll
    public static void setUp(@Autowired UserRepository userRepository, @Autowired TenancyRepository tenancyRepository) {
        tenancy = tenancyRepository.save(Tenancy.builder().name("tenancy").build());
        Stream<String> users = Stream.of(
                "Andy Johnson", "Angel Duncan", "Angelo Wells", "Arthur Lawrence", "Bernard Myers", "Beth Guzman", "Blake Coleman", "Brian Mann", "Cameron Fleming", "Carlton Santos",
                "Carrie Tate", "Catherine Jones", "Cecil Perkins", "Colin Ward", "Conrad Hernandez", "Dolores Williamson", "Doris Parker", "Earl Norris", "Eddie Massey", "Elena Boyd",
                "Elisa Vargas", "Erma Black", "Ernestine Steele", "Ernesto Kim", "Fannie Hines", "Gabriel Dixon", "Gary Logan", "Gerard Webb", "Ida Garza", "Isaac James",
                "Jerome Pratt", "Joel Dunn", "Julie Carson", "Kathy Oliver", "Katrina Hawkins", "Larry Robbins", "Laurie Adams", "Loretta Stanley", "Luke Tyler", "Melinda Fields",
                "Natasha Rivera", "Nora Waters", "Pedro Sullivan", "Phyllis Terry", "Rochelle Graves", "Sabrina Garcia", "Sadie Davis", "Vera Lamb", "Verna Wilkins", "Victoria Luna"
        );
        users.map(user -> {
            String userName = user.replace(" ", "").toLowerCase();
            String email = userName.concat("@email.com");
            String[] names = user.split(" ");
            String firstName = names[0];
            String lastName = names[1];
            return User.builder()
                    .userName(userName)
                    .email(email)
                    .password(UUID.randomUUID().toString())
                    .firstName(firstName)
                    .lastName(lastName)
                    .enabled(true)
                    .verified(true)
                    .createdAt(LocalDateTime.now())
                    .tenancy(tenancy)
                    .build();
        }).forEach(userRepository::save);
    }

    @AfterAll
    public static void shutdown(@Autowired UserRepository userRepository, @Autowired TenancyRepository tenancyRepository) {
        userRepository.deleteAll();
        tenancyRepository.deleteAll();
    }

    @DisplayName("Given default PageableRequest return first 10 items")
    @Test
    void plainPageable() {
        PageableRequest pageableRequest = new PageableRequest(0, 10, null, null);
        PageResponse<UserInfo> usersPage = userManagementService.getAll(tenancy.getId(), pageableRequest);
        assertThat(usersPage.items()).hasSize(10);
        assertThat(usersPage.currentPage()).isEqualTo(0);
        assertThat(usersPage.totalPages()).isEqualTo(5);
        assertThat(usersPage.totalItems()).isEqualTo(50L);
        assertThat(usersPage.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                .containsExactly("Andy Johnson", "Angel Duncan", "Angelo Wells", "Arthur Lawrence", "Bernard Myers", "Beth Guzman", "Blake Coleman", "Brian Mann", "Cameron Fleming", "Carlton Santos");
    }

    @DisplayName("Get items in the middle pages")
    @Test
    void middlePage() {
        PageableRequest pageableRequest = new PageableRequest(3, 10, null, null);
        PageResponse<UserInfo> usersPage = userManagementService.getAll(tenancy.getId(), pageableRequest);
        assertThat(usersPage.items()).hasSize(10);
        assertThat(usersPage.currentPage()).isEqualTo(3);
        assertThat(usersPage.totalPages()).isEqualTo(5);
        assertThat(usersPage.totalItems()).isEqualTo(50L);
        assertThat(usersPage.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                .containsExactly("Jerome Pratt", "Joel Dunn", "Julie Carson", "Kathy Oliver", "Katrina Hawkins", "Larry Robbins", "Laurie Adams", "Loretta Stanley", "Luke Tyler", "Melinda Fields");
    }


    @DisplayName("Page out of range return empty results")
    @Test
    void outOfRange() {
        PageableRequest pageableRequest = new PageableRequest(5, 10, null, null);
        PageResponse<UserInfo> usersPage = userManagementService.getAll(tenancy.getId(), pageableRequest);
        assertThat(usersPage.items()).hasSize(0);
        assertThat(usersPage.currentPage()).isEqualTo(5);
        assertThat(usersPage.totalPages()).isEqualTo(5);
        assertThat(usersPage.totalItems()).isEqualTo(50L);
        assertThat(usersPage.items()).isEmpty();
    }

    @DisplayName("Simple sort without paging")
    @Test
    void plainSort() {
        String[] sort = new String[]{"firstName", "desc"};
        PageableRequest pageableRequest = new PageableRequest(0, 10, sort, null);
        PageResponse<UserInfo> usersPage = userManagementService.getAll(tenancy.getId(), pageableRequest);
        assertThat(usersPage.items()).hasSize(10);
        assertThat(usersPage.currentPage()).isEqualTo(0);
        assertThat(usersPage.totalPages()).isEqualTo(5);
        assertThat(usersPage.totalItems()).isEqualTo(50L);
        assertThat(usersPage.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                .containsExactly("Victoria Luna", "Verna Wilkins", "Vera Lamb", "Sadie Davis", "Sabrina Garcia", "Rochelle Graves", "Phyllis Terry", "Pedro Sullivan", "Nora Waters", "Natasha Rivera");
    }


    @DisplayName("Sort for two columns")
    @Test
    void sortTwoColumns() {
        String[] sort = new String[]{"firstName", "desc", "lastName", "asc"};
        PageableRequest pageableRequest = new PageableRequest(0, 10, sort, null);
        PageResponse<UserInfo> usersPage = userManagementService.getAll(tenancy.getId(), pageableRequest);
        assertThat(usersPage.items()).hasSize(10);
        assertThat(usersPage.currentPage()).isEqualTo(0);
        assertThat(usersPage.totalPages()).isEqualTo(5);
        assertThat(usersPage.totalItems()).isEqualTo(50L);
        assertThat(usersPage.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                .containsExactly("Victoria Luna", "Verna Wilkins", "Vera Lamb", "Sadie Davis", "Sabrina Garcia", "Rochelle Graves", "Phyllis Terry", "Pedro Sullivan", "Nora Waters", "Natasha Rivera");
    }

    @DisplayName("Filter users returning only one page")
    @Test
    void filterFirstPage() {
        String filter = "son";
        PageableRequest pageableRequest = new PageableRequest(0, 10, null, filter);
        PageResponse<UserInfo> usersPage = userManagementService.getAll(tenancy.getId(), pageableRequest);
        assertThat(usersPage.items()).hasSize(3);
        assertThat(usersPage.currentPage()).isEqualTo(0);
        assertThat(usersPage.totalPages()).isEqualTo(1);
        assertThat(usersPage.totalItems()).isEqualTo(3L);
        assertThat(usersPage.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                .containsExactly("Andy Johnson", "Dolores Williamson", "Julie Carson");
    }

    @DisplayName("Filter users returning two pages page")
    @Test
    void filterTwoPages() {
        String filter = "la";
        PageableRequest pageableRequest = new PageableRequest(0, 4, null, filter);
        PageResponse<UserInfo> usersPage = userManagementService.getAll(tenancy.getId(), pageableRequest);
        assertThat(usersPage.items()).hasSize(4);
        assertThat(usersPage.currentPage()).isEqualTo(0);
        assertThat(usersPage.totalPages()).isEqualTo(2);
        assertThat(usersPage.totalItems()).isEqualTo(6L);
        assertThat(usersPage.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                .containsExactly("Arthur Lawrence", "Blake Coleman", "Erma Black", "Larry Robbins");

        pageableRequest = new PageableRequest(1, 4, null, filter);
        usersPage = userManagementService.getAll(tenancy.getId(), pageableRequest);
        assertThat(usersPage.items()).hasSize(2);
        assertThat(usersPage.currentPage()).isEqualTo(1);
        assertThat(usersPage.totalPages()).isEqualTo(2);
        assertThat(usersPage.totalItems()).isEqualTo(6L);
        assertThat(usersPage.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                .containsExactly("Laurie Adams", "Vera Lamb");
    }

    @DisplayName("Filter users returning no results")
    @Test
    void filterEmpty() {
        String filter = "www";
        PageableRequest pageableRequest = new PageableRequest(0, 4, null, filter);
        PageResponse<UserInfo> usersPage = userManagementService.getAll(tenancy.getId(), pageableRequest);
        assertThat(usersPage.items()).hasSize(0);
        assertThat(usersPage.currentPage()).isEqualTo(0);
        assertThat(usersPage.totalPages()).isEqualTo(0);
        assertThat(usersPage.totalItems()).isEqualTo(0L);
        assertThat(usersPage.items()).isEmpty();
    }

    @DisplayName("Filter users returning two pages page but sorting by last name")
    @Test
    void filterAndSort() {
        String filter = "la";
        String[] sort = new String[]{"lastName", "desc"};
        PageableRequest pageableRequest = new PageableRequest(0, 4, sort, filter);
        PageResponse<UserInfo> usersPage = userManagementService.getAll(tenancy.getId(), pageableRequest);
        assertThat(usersPage.items()).hasSize(4);
        assertThat(usersPage.currentPage()).isEqualTo(0);
        assertThat(usersPage.totalPages()).isEqualTo(2);
        assertThat(usersPage.totalItems()).isEqualTo(6L);
        assertThat(usersPage.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                .containsExactly("Larry Robbins", "Arthur Lawrence", "Vera Lamb", "Blake Coleman");

        pageableRequest = new PageableRequest(1, 4, sort, filter);
        usersPage = userManagementService.getAll(tenancy.getId(), pageableRequest);
        assertThat(usersPage.items()).hasSize(2);
        assertThat(usersPage.currentPage()).isEqualTo(1);
        assertThat(usersPage.totalPages()).isEqualTo(2);
        assertThat(usersPage.totalItems()).isEqualTo(6L);
        assertThat(usersPage.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                .containsExactly("Erma Black", "Laurie Adams");
    }
}