package com.example.shopapp.dtos.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserLoginDTO {
    @NotBlank(message = "Phone number must not be blank")
    String phoneNumber;

    @NotBlank(message = "Password must not be blank")
    String password;

    @JsonProperty("role_id")
    Integer roleId;
}
