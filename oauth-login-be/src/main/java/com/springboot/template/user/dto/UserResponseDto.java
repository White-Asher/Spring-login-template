package com.springboot.template.user.dto;

import com.springboot.template.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "회원 정보 반환 DTO")
public class UserResponseDto {
    @Schema(description = "회원 식별번호")
    private Integer userNo;
    @Schema(description = "회원 아이디")
    private String userId;
    @Schema(description = "회원 이름")
    private String userName;
    @Schema(description = "회원 이메일", example = "testEmail@email.com")
    private String userEmail;
    @Schema(description = "회원 휴대전화번호", example = "01012345678")
    private String userPhone;
    @Schema(description = "회원 성별", example = "M")
    private String userGender;
    @Schema(description = "회원 이용약관 동의")
    private boolean userTerms;
    @Schema(description = "회원 가입 일시")
    private LocalDateTime userCreateTime;
    @Schema(description = "회원 인증 방식")
    private String userAuthType;
    @Schema(description = "회원 활성화 여부")
    private boolean userActive;
    @Builder
    public UserResponseDto(User user) {
        this.userNo = user.getUserNo();
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.userEmail = user.getUserEmail();
        this.userPhone = user.getUserPhone();
        this.userGender = user.getUserGender();
        this.userTerms = user.isUserTerms();
        this.userCreateTime = user.getCreatedAt();
        this.userAuthType = "이메일 인증";
        this.userActive = user.isUserActive();
    }
}
