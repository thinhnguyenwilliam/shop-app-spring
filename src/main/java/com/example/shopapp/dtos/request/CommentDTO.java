package com.example.shopapp.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("content")
    private String content;
}
