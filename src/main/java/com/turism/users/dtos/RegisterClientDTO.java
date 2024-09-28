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
public class RegisterClientDTO {
    private String name;
    private Integer age;
    private String description = null;
    private String photo = null;
    private String email;
    private String password;

    public User toUser() {
        return new User(
                name,
                age,
                email,
                null,
                description,
                photo,
                null,
                UserType.PROVIDER,
                new ArrayList<>()
        );
    }
}
