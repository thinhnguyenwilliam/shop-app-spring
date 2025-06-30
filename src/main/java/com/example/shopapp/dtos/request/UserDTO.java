package com.example.shopapp.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDTO {
    @JsonProperty("fullname")
    String fullName;

    @JsonProperty("phone_number")
    String phoneNumber;

    String address;
    String email;

    @NotBlank(message = "Password must not be blank")
    String password;

    @JsonProperty("date_of_birth")
    Date dateOfBirth;

    @JsonProperty("facebook_account_id")
    String facebookAccountId;

    @JsonProperty("google_account_id")
    String googleAccountId;

    @JsonProperty("role_id")
    Integer roleId;

    @JsonProperty("retype_password")
    String retypePassword;
}
