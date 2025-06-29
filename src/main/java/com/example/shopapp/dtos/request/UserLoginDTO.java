package com.example.shopapp.dtos.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
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
    String phoneNumber;

    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Password must not be blank")
    String password;

    @Min(value = 2, message = "You must enter role's Id")
    @JsonProperty("role_id")
    Integer roleId;
}
