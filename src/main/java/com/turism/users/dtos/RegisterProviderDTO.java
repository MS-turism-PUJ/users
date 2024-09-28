package com.turism.users.dtos;

import com.turism.users.models.SocialMedia;
import com.turism.users.models.User;
import com.turism.users.models.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterProviderDTO {
    private String username;
    private String name;
    private Integer age;
    private String description = null;
    private String photo = null;
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
                photo,
                webPage,
                UserType.PROVIDER,
                socialMedia
        );
    }
}
