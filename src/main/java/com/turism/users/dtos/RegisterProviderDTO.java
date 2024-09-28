package com.turism.users.dtos;

import com.turism.users.models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterDTO {
    private String name;
    private Integer age;
    private String description = "";
    private String photo = "";
    private Long phone;
    private String email;
    private String password;
    private String webPage = "";

    public User toUser() {
        return new User();
    }
}
