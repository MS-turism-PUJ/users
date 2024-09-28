package com.turism.users.dtos;

import com.turism.users.models.User;
import com.turism.users.models.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterProviderDTO {
    private String name;
    private Integer age;
    private String description = null;
    private String photo = null;
    private Long phone;
    private String email;
    private String password;
    private String webPage;

    public User toUser() {
        return new User(
                name,
                age,
                email,
                phone,
                description,
                photo,
                webPage,
                UserType.PROVIDER,
                new ArrayList<>()
        );
    }
}
