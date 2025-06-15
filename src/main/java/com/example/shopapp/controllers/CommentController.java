package com.example.shopapp.controllers;

import com.example.shopapp.dtos.request.CommentDTO;
import com.example.shopapp.dtos.responses.CommentResponse;
import com.example.shopapp.models.User;
import com.example.shopapp.security.CustomUserDetails;
import com.example.shopapp.service.impl.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("${api.prefix}/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {
    private final CommentService commentService;

    @GetMapping("")
    public ResponseEntity<List<CommentResponse>> getAllComments(
            @RequestParam(value = "user_id", required = false) Integer userId,
            @RequestParam("product_id") Integer productId
    ) {
        List<CommentResponse> commentResponses;
        if (userId == null) {
            commentResponses = commentService.getCommentsByProduct(productId);
        } else {
            commentResponses = commentService.getCommentsByUserAndProduct(userId, productId);
        }
        return ResponseEntity.ok(commentResponses);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<Object> updateComment(
            @PathVariable("id") Long commentId,
            @Valid @RequestBody CommentDTO commentDTO
    ) {
        try {
            User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!Objects.equals(loginUser.getId(), commentDTO.getUserId())) {
                return ResponseEntity.badRequest().body("You cannot update another user's comment");
            }
            commentService.updateComment(commentId, commentDTO);
            return ResponseEntity.ok(Map.of("message", "Update comment successfully"));
        } catch (Exception e) {
            // Handle and log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred during comment update."));

        }
    }


    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<Object> insertComment(@Valid @RequestBody CommentDTO commentDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authenticated user: {}", auth.getName());
        auth.getAuthorities().forEach(a -> log.info("Role is: {}", a.getAuthority()));

        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User loginUser = userDetails.getUser();

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!Objects.equals(loginUser.getId(), commentDTO.getUserId()) && !isAdmin) {
                return ResponseEntity.badRequest().body(Map.of("error", "You cannot comment as another user"));
            }

            commentService.insertComment(commentDTO);
            return ResponseEntity.ok(Map.of("message", "Insert comment successfully"));

        } catch (Exception e) {
            log.error("Comment insert error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "An error occurred during comment insertion."));
        }
    }



}
