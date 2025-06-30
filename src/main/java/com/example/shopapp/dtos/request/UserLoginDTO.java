package com.example.shopapp.dtos.request;


import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserLoginDTO {

    @JsonProperty("phone_number")
    String phoneNumber;

    @JsonProperty("email")
    String email;

    @NotBlank(message = "Password must not be blank")
    String password;

    @JsonProperty("fullname")
    String fullName;

    @JsonProperty("profile_image")
    String profileImage;

    @Min(value = 2, message = "You must enter role's Id")
    @JsonProperty("role_id")
    Integer roleId;

    public boolean isPasswordBlank() {
        return password == null || password.trim().isEmpty();
    }

    @JsonProperty("facebook_account_id")
    String facebookAccountId;

    @JsonProperty("google_account_id")
    String googleAccountId;

    public boolean isFacebookAccountIdValid() {
        return facebookAccountId != null && !facebookAccountId.isEmpty();
    }

    public boolean isGoogleAccountIdValid() {
        return googleAccountId != null && !googleAccountId.isEmpty();
    }

    public boolean isSocialLogin() {
        return isFacebookAccountIdValid() || isGoogleAccountIdValid();
    }
}

