package com.turism.users.dtos;

import com.turism.users.models.SocialMedia;
import com.turism.users.models.User;
import com.turism.users.models.UserType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterProviderDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age must be less than 150")
    private Integer age;

    @Min(value = 1000000000, message = "Phone number must have 10 digits")
    private Long phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Web page is required")
    private String webPage;

    private String description = null;

    private MultipartFile photo = null;

    private List<SocialMedia> socialMedia = new ArrayList<>();

    public User toUser() {
        return new User(
                username,
                name,
                age,
                email,
                phone,
                description,
                photo != null ? username : null,
                photo != null ? FilenameUtils.getExtension(photo.getOriginalFilename()) : null,
                webPage,
                UserType.PROVIDER,
                socialMedia);
    }
}
