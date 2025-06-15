package com.example.shopapp.service;

import com.example.shopapp.dtos.request.CommentDTO;
import com.example.shopapp.dtos.responses.CommentResponse;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Comment;

import java.util.List;

public interface ICommentService {
    Comment insertComment(CommentDTO comment);

    void deleteComment(Long commentId);
    void updateComment(Long id, CommentDTO commentDTO) throws DataNotFoundException;

    List<CommentResponse> getCommentsByUserAndProduct(Integer userId, Integer productId);
    List<CommentResponse> getCommentsByProduct(Integer productId);
}

