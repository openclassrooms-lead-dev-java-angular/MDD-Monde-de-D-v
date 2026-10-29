package com.openclassrooms.mddapi.topic.entity;

import com.openclassrooms.mddapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "topics")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Topic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 50
    )
    private String name;

    @Column()
    private String description;

    @Column(
            name = "public_id",
            nullable = false,
            unique = true,
            updatable = false,
            length = 36
    )
    private String publicId = UUID.randomUUID().toString();
}
