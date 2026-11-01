package com.openclassrooms.mddapi.user.repository;

import com.openclassrooms.mddapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM User u
            WHERE u.username = :username
              AND u.id <> :userId
            """)
    boolean existsByUsernameForAnotherUser(
            @Param("username") String username,
            @Param("userId") Long userId
    );

    boolean existsByUsername(String username);
}
