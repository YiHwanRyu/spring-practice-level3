package com.example.blog.service;

import com.example.blog.dto.CommentRequestDto;
import com.example.blog.dto.CommentResponseDto;
import com.example.blog.dto.MessageResponseDto;
import com.example.blog.entity.Comment;
import com.example.blog.entity.UserRoleEnum;
import com.example.blog.jwt.JwtUtil;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private CommentRepository commentRepository;
    private UserRepository userRepository;
    private JwtUtil jwtUtil;

    public CommentService(CommentRepository commentRepository, JwtUtil jwtUtil, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.jwtUtil = jwtUtil;
    }

    // 댓글 생성
    public CommentResponseDto createComment(String tokenValue, CommentRequestDto requestDto) {
        String token = jwtUtil.substringToken(tokenValue);

        if(!jwtUtil.validateToken(token)){
            throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
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
            throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
        }

        // 토큰에서 사용자 정보 가져오기
        Claims info = jwtUtil.getUserInfoFromToken(token);

        // username
        String username = info.getSubject();

        // role
        String role = info.get(JwtUtil.AUTHORIZATION_KEY, String.class);

        if(!(role.equals(UserRoleEnum.ADMIN.toString()) || username.equals(comment.getUsername()))){
            throw new RuntimeException("작성자만 수정할 수 있습니다.");
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
            throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
        }

        // 토큰에서 사용자 정보 가져오기
        Claims info = jwtUtil.getUserInfoFromToken(token);

        // username
        String username = info.getSubject();

        // role
        String role = info.get(JwtUtil.AUTHORIZATION_KEY, String.class);

        if(!(role.equals(UserRoleEnum.ADMIN.toString()) || username.equals(comment.getUsername()))){
            throw new RuntimeException("작성자만 삭제할 수 있습니다.");
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
