package com.openclassrooms.mddapi.seeder;

import com.openclassrooms.mddapi.common.enums.Role;
import com.openclassrooms.mddapi.user.User;
import com.openclassrooms.mddapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Log4j2
@RequiredArgsConstructor
@Component
public class UserSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void seed(Boolean clear) {
        log.info("Seeding Users ");

        if(clear) {
            log.info("Clearing Users ");
            userRepository.deleteAll();
        }

        User user1 = generateUser("John", "Doe", true);
        User user2 = generateUser("Jane", "Doe", false);

        userRepository.save(user1);
        userRepository.save(user2);
    }

    private User generateUser(String firstName, String lastName, Boolean isAdmin) {

        User user = new User();
        user.setUsername(firstName + "_" + lastName);
        user.setPassword(passwordEncoder.encode("password1234"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(user.getFirstName() + "@email.com");
        user.setRole(isAdmin ? Role.ADMIN : Role.USER);

        return user;
    }
}
