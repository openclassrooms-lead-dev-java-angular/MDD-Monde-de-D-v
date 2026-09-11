package com.openclassrooms.mddapi.user;

import com.openclassrooms.mddapi.common.entity.BaseEntity;
import com.openclassrooms.mddapi.common.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true
    )
    @NotBlank
    @Size(max = 50)
    private String username;

    @Column(
            nullable = false,
            unique = true
    )
    @Email
    @Size(max = 50)
    @NotBlank
    private String email;

    @Column(
            nullable = false,
            length = 255
    )
    @NotBlank
    @Size(max = 255)
    private String password;

    @Column(
            name = "firstname",
            nullable = false,
            length = 50
    )
    @NotBlank
    @Size(max = 50)
    private String firstName;

    @Column(
            name = "lastname",
            nullable = false,
            length = 50
    )
    @NotBlank
    @Size(max = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 10
    )
    private Role role;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
