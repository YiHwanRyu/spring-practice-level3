package com.example.blog.service;

import com.example.blog.dto.CommentRequestDto;
import com.example.blog.dto.CommentResponseDto;
import com.example.blog.dto.MessageResponseDto;
import com.example.blog.dto.PostRequestDto;
import com.example.blog.entity.Comment;
import com.example.blog.exception.ApiException;
import com.example.blog.jwt.JwtUtil;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    private CommentRepository commentRepository;
    private JwtUtil jwtUtil;

    public CommentService(CommentRepository commentRepository, JwtUtil jwtUtil) {
        this.commentRepository = commentRepository;
        this.jwtUtil = jwtUtil;
    }

    // 댓글 생성
    public CommentResponseDto createComment(String tokenValue, CommentRequestDto requestDto) {
        String token = jwtUtil.substringToken(tokenValue);

        if(!jwtUtil.validateToken(token)){
            throw new ApiException("토큰이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 토큰에서 사용자 정보 가져오기
        Claims info = jwtUtil.getUserInfoFromToken(token);
        // username
        String username = info.getSubject();

        Comment comment = new Comment(requestDto, username);
        Comment saveComment = commentRepository.save(comment);
        CommentResponseDto commentRequestDto = new CommentResponseDto(saveComment);
        return commentRequestDto;
    }

    @Transactional
    public CommentResponseDto updateComment(String tokenValue, Long id, CommentRequestDto requestDto) {
        Comment comment = findComment(id);

        // JWT 토큰 substring
        String token = jwtUtil.substringToken(tokenValue);

        // 토큰 검증
        if(!jwtUtil.validateToken(token)) {
            throw new ApiException("토큰이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 토큰에서 사용자 정보 가져오기
        Claims info = jwtUtil.getUserInfoFromToken(token);

        // username
        String username = info.getSubject();

        if(!username.equals(comment.getUsername())){
            throw new ApiException("작성자만 수정할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        comment.update(requestDto);

        return new CommentResponseDto(comment);
    }


    public ResponseEntity<MessageResponseDto> deleteComment(String tokenValue, Long id) {
        Comment comment = findComment(id);

        // JWT 토큰 substring
        String token = jwtUtil.substringToken(tokenValue);

        // 토큰 검증
        if(!jwtUtil.validateToken(token)) {
            throw new ApiException("토큰이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 토큰에서 사용자 정보 가져오기
        Claims info = jwtUtil.getUserInfoFromToken(token);
        // username
        String username = info.getSubject();

        if(!username.equals(comment.getUsername())){
            throw new ApiException("작성자만 삭제할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        commentRepository.delete(comment);

        return new ResponseEntity<>(new MessageResponseDto("댓글 삭제 성공", "200"), HttpStatus.OK);
    }

    private Comment findComment(Long id){
        return commentRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("선택하신 댓글은 존재하지 않습니다.")
        );
    }

}
