package com.openclassrooms.mddapi.article.entity;

import com.openclassrooms.mddapi.common.entity.BaseEntity;
import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "articles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@SuperBuilder
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "author_id",
            nullable = false
    )
    private User author;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "topic_id",
            nullable = false
    )
    private Topic topic;

    @Column(
            nullable = false,
            length = 100
    )
    private String title;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 255)
    private String media;
}
