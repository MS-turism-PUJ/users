package com.turism.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageDTO implements Serializable {
    private String userId;
    private String username;
    private String name;
    private String email;
    private String photo;
}

