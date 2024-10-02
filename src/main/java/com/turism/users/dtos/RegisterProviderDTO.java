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
public class RegisterProviderDTO {
    private String username;
    private String name;
    private Integer age;
    private String description = null;
    private MultipartFile photo = null;
    private Long phone;
    private String email;
    private String password;
    private String webPage;
    private List<SocialMedia> socialMedia = new ArrayList<>();

    public Boolean valid() {
        return username != null && name != null && age != null && email != null && password != null && phone != null && webPage != null;
    }

    public User toUser() {
        return new User(
                username,
                name,
                age,
                email,
                phone,
                description,
                username,
                webPage,
                UserType.PROVIDER,
                socialMedia
        );
    }
}
