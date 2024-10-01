package com.turism.users.dtos;

import com.turism.users.models.SocialMedia;
import com.turism.users.models.User;
import com.turism.users.models.UserType;
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
    private String username;
    private String name;
    private Integer age;
    private String description = null;
    private MultipartFile photo = null;
    private String email;
    private String password;
    private List<SocialMedia> socialMedia = new ArrayList<>();

    public Boolean valid() {
        return username != null && name != null && age != null && email != null && password != null;
    }

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
