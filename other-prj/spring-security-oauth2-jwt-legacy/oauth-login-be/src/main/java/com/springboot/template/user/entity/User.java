package com.springboot.template.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springboot.template.auth.entity.ProviderType;
import com.springboot.template.auth.entity.RoleType;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {
    @JsonIgnore
    @Id
    @Column(name = "user_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userNo;
    @Column(name = "user_id", length = 64, unique = true)
    @NotNull
    @Size(max = 64)
    private String userId;
    @JsonIgnore
    @Column(name = "user_password", length = 128)
    @NotNull
    @Size(max = 128)
    private String userPassword;
    @Column(name = "user_name", length = 100)
    @NotNull
    @Size(max = 100)
    private String userName;
    @Column(name = "user_email", length = 512, unique = true)
    @NotNull
    @Size(max = 512)
    private String userEmail;
    @Column(name = "user_phone", length = 50, unique = true)
    @NotNull
    @Size(max = 50)
    private String userPhone;
    @Column(name = "user_birthdate", length = 8)
    @NotNull
    @Size(max = 8)
    private String userBirthDate;
    @Column(name = "user_gender", length = 1)
    @NotNull
    @Size(max = 1)
    private String userGender;
    @Column(name = "user_terms")
    @NotNull
    private boolean userTerms;
    @Column(name = "user_provide_type", length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private ProviderType providerType;
    @Column(name = "user_role_type", length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private RoleType roleType;
    @Column(name = "user_created_time")
    @NotNull
    private LocalDateTime createdAt;
    @Column(name = "user_active")
    @NotNull
    private boolean userActive;

}
