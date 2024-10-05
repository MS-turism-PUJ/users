package com.turism.users.dtos;

import com.turism.users.models.SocialMedia;
import com.turism.users.models.User;
import com.turism.users.models.UserType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterClientDTO {
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Name is required")
    private String name;
    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age must be less than 150")
    private Integer age;
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
    private String description = null;
    private MultipartFile photo = null;
    private List<SocialMedia> socialMedia = new ArrayList<>();

    public User toUser() {
        return new User(
                username,
                name,
                age,
                email,
                null,
                description,
                username,
                null,
                UserType.CLIENT,
                socialMedia
        );
    }
}
