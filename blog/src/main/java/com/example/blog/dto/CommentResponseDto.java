package com.example.blog.dto;

import com.example.blog.entity.Comment;
import lombok.Getter;

@Getter
public class CommentResponseDto {
    private Long id;
    private String username;
    private String content;

    public CommentResponseDto(Comment comment){
        this.id = comment.getId();
        this.username = comment.getUsername();
        this.content = comment.getContent();
    }
}
