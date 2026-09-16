package com.openclassrooms.mddapi.seeder;

import com.openclassrooms.mddapi.common.enums.Role;
import com.openclassrooms.mddapi.user.User;
import com.openclassrooms.mddapi.user.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
public class UserSeeder extends AbstractSeeder<User> {

    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository);
        this.passwordEncoder = passwordEncoder;
        log.info("UserSeeder - userRepository: {}", userRepository);
    }


    public List<User> getEntities() {
        log.info("Seeding Users ");

        return List.of(
                generateUser("John", "Doe", true),
                generateUser("Jane", "Doe", true),
                generateUser("John", "Smith", true)
        );
    }

    private User generateUser(String firstName, String lastName, Boolean isAdmin) {

        User user = new User();
        user.setUsername(firstName + "_" + lastName);
        user.setPassword(passwordEncoder.encode("password1234"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(
                user.getFirstName().toLowerCase()
                + "-" +
                user.getLastName().toLowerCase()
                + "@email.com"
        );
        user.setRole(isAdmin ? Role.ADMIN : Role.USER);

        return user;
    }
}
