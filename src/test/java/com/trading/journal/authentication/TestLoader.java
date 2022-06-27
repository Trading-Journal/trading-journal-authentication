package com.trading.journal.authentication;

import com.trading.journal.authentication.authority.AuthoritiesHelper;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.service.AuthorityService;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

public class TestLoader {

    public static void load50Users(ApplicationUserRepository applicationUserRepository, UserAuthorityRepository userAuthorityRepository, AuthorityService authorityService) {
        applicationUserRepository.deleteAll();
        userAuthorityRepository.deleteAll();

        Stream<String> users = Stream.of(
                "Andy Johnson", "Angel Duncan", "Angelo Wells", "Arthur Lawrence", "Bernard Myers", "Beth Guzman", "Blake Coleman", "Brian Mann", "Cameron Fleming", "Carlton Santos",
                "Carrie Tate", "Catherine Jones", "Cecil Perkins", "Colin Ward", "Conrad Hernandez", "Dolores Williamson", "Doris Parker", "Earl Norris", "Eddie Massey", "Elena Boyd",
                "Elisa Vargas", "Erma Black", "Ernestine Steele", "Ernesto Kim", "Fannie Hines", "Gabriel Dixon", "Gary Logan", "Gerard Webb", "Ida Garza", "Isaac James",
                "Jerome Pratt", "Joel Dunn", "Julie Carson", "Kathy Oliver", "Katrina Hawkins", "Larry Robbins", "Laurie Adams", "Loretta Stanley", "Luke Tyler", "Melinda Fields",
                "Natasha Rivera", "Nora Waters", "Pedro Sullivan", "Phyllis Terry", "Rochelle Graves", "Sabrina Garcia", "Sadie Davis", "Vera Lamb", "Verna Wilkins", "Victoria Luna"
        );

        Authority authority = authorityService.getByName(AuthoritiesHelper.ROLE_USER.getLabel()).get();

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
                .map(applicationUser -> new UserAuthority(applicationUser.getId(), AuthoritiesHelper.ROLE_USER.getLabel(), authority.getId()))
                .forEach(userAuthorityRepository::save);

    }
}
