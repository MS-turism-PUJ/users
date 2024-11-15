package com.turism.users.models;

import java.util.List;

import com.turism.users.dtos.UserMessageDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String description;

    @Column
    private String photo;

    @Column
    private String photoExtension;

    @Column
    private Long phone;

    @Column
    private String webPage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    @OneToMany(mappedBy = "user")
    private List<SocialMedia> socialMedia;

    public User(String username, String name, Integer age, String email, Long phone, String description, String photo, String photoExtension,
            String webPage, UserType userType, List<SocialMedia> socialMedia) {
        this.username = username;
        this.name = name;
        this.age = age;
        this.email = email;
        this.phone = phone;
        this.description = description;
        this.photo = photo;
        this.photoExtension = photoExtension;
        this.webPage = webPage;
        this.userType = userType;
        this.socialMedia = socialMedia;
    }

    public UserMessageDTO toUserMessageDTO() {
        return new UserMessageDTO(userId, username, name, email);
    }
}
