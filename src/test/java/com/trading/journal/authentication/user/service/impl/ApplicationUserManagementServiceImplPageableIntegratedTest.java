package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.authority.AuthoritiesHelper;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.service.ApplicationUserManagementService;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
class ApplicationUserManagementServiceImplPageableIntegratedTest {

    @Autowired
    ApplicationUserRepository applicationUserRepository;

    @Autowired
    UserAuthorityRepository userAuthorityRepository;

    @Autowired
    ApplicationUserManagementService applicationUserManagementService;

    @BeforeEach
    public void setUp() {
        applicationUserRepository.deleteAll();
        userAuthorityRepository.deleteAll();
        load50Users();
    }

    @DisplayName("Given default PageableRequest return first 10 items")
    @Test
    void plainPageable() {
        PageableRequest pageableRequest = new PageableRequest(0, 10, null, null);
        PageResponse<UserInfo> usersPage = applicationUserManagementService.getAll(pageableRequest);
        assertThat(usersPage.items()).hasSize(10);
        assertThat(usersPage.currentPage()).isEqualTo(0);
        assertThat(usersPage.totalPages()).isEqualTo(5);
        assertThat(usersPage.totalItems()).isEqualTo(50L);
        assertThat(usersPage.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                .containsExactly("Andy Johnson", "Angel Duncan", "Angelo Wells", "Arthur Lawrence", "Bernard Myers", "Beth Guzman", "Blake Coleman", "Brian Mann", "Cameron Fleming", "Carlton Santos");

//        verify(applicationUserRepository, never()).findByFirstNameContainingOrLastNameContainingOrUserNameContainingOrEmailContaining(anyString(), any());
    }

//    @DisplayName("ODD items")
//    @Test
//    void plainPageable() {
//
//    }
//
//    @DisplayName("MIDDLE ITEMS")
//    @Test
//    void plainPageable() {
//
//    }
//
//    @DisplayName("OUT OF RANGE PAGE")
//    @Test
//    void plainPageable() {
//
//    }
//
//    @DisplayName("SORT NO PAGE")
//    @Test
//    void plainPageable() {
//
//    }
//
//    @DisplayName("SORT LAST PAGE")
//    @Test
//    void plainPageable() {
//
//    }
//
//    @DisplayName("SORT IN THE MIDDLE")
//    @Test
//    void plainPageable() {
//
//    }
//
//    @DisplayName("FILTER FIRST PAGE")
//    @Test
//    void plainPageable() {
//
//    }
//
//    @DisplayName("FILTER LAST PAGE")
//    @Test
//    void plainPageable() {
//
//    }
//
//    @DisplayName("FILTER IN THE MIDDLE")
//    @Test
//    void plainPageable() {
//
//    }
//
//    @DisplayName("FILTER FOR NO RESULTS")
//    @Test
//    void plainPageable() {
//
//    }

    private void load50Users() {
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

                    return ApplicationUser.builder()
                            .userName(userName)
                            .email(email)
                            .password(UUID.randomUUID().toString())
                            .firstName(firstName)
                            .lastName(lastName)
                            .enabled(true)
                            .verified(true)
                            .createdAt(LocalDateTime.now())
                            .build();
                }).map(applicationUserRepository::save)
                .map(applicationUser -> new UserAuthority(applicationUser.getId(), AuthoritiesHelper.ROLE_USER.getLabel(), null))
                .forEach(userAuthorityRepository::save);

    }
}