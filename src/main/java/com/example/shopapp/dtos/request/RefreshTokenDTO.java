package com.example.shopapp.dtos.request;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshTokenDTO {
    @NotBlank
    private String refreshToken;
}
