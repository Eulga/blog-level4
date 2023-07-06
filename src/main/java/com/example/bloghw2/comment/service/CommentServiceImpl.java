package com.example.bloghw2.comment.service;

import com.example.bloghw2.comment.dto.CommentRequestDTO;
import com.example.bloghw2.comment.dto.CommentResponseDTO;
import com.example.bloghw2.comment.entity.Comment;
import com.example.bloghw2.comment.exception.CommentNotFoundException;
import com.example.bloghw2.comment.exception.CommentPermissionException;
import com.example.bloghw2.comment.repository.CommentRepository;
import com.example.bloghw2.post.entity.Post;
import com.example.bloghw2.post.exception.PostNotFoundException;
import com.example.bloghw2.post.repository.PostRepository;
import com.example.bloghw2.user.entity.User;
import com.example.bloghw2.user.exception.UserNotFoundException;
import com.example.bloghw2.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "CommentService")
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<CommentResponseDTO> getCommentsByPostId(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException("Not Found Post")
        );
        List<Comment> commentList = commentRepository.findAllByPostOrderByCreatedDateDesc(post);

        return commentList.stream()
                .map(CommentResponseDTO::new)
                .toList();
    }

    @Transactional
    @Override
    public CommentResponseDTO createComment(CommentRequestDTO crqd, String username) {
        log.info(username + " 댓글 작성 post_id: " + crqd.getPostId());
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("Not Found User")
        );
        Post post = postRepository.findById(crqd.getPostId()).orElseThrow(
                () -> new PostNotFoundException("Not Found Post")
        );
        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content(crqd.getContent())
                .build();

        return new CommentResponseDTO(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public CommentResponseDTO modifyComment(Long commentId, CommentRequestDTO commentRequestDTO, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("Not Found User")
        );
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new CommentNotFoundException("Not Found Comment")
        );

        if(!user.getUsername().equals(comment.getUser().getUsername())) {
            throw new CommentPermissionException("Not The User's Comment");
        }

        comment.modifyComment(commentRequestDTO.getContent());

        return new CommentResponseDTO(comment);
    }

    @Transactional
    @Override
    public Map<String, String> deleteComment(Long commentId, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("Not Found User")
        );
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new CommentNotFoundException("Not Found Comment")
        );

        if(!user.getUsername().equals(comment.getUser().getUsername())) {
            throw new CommentPermissionException("Not The User's Comment");
        }

        commentRepository.delete(comment);

        return new LinkedHashMap<>() {{
            put("success","true");
            put("status","200");
        }};
    }
}
