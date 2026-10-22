package com.openclassrooms.mddapi.comment.repository;

import com.openclassrooms.mddapi.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
