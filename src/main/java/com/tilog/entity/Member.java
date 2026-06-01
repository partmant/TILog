package com.tilog.entity;

import com.tilog.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_member_nickname", columnNames = "nickname")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    @Column(name = "is_banned", nullable = false)
    private boolean banned;

    @Column(name = "banned_until")
    private LocalDateTime bannedUntil;

    @Builder
    private Member(String email, String password, String nickname, MemberRole role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : MemberRole.USER;
        this.banned = false;
        this.bannedUntil = null;
    }

   // 회원가입 시 신규 회원 생성
    public static Member create(String email, String encodedPassword, String nickname) {
        return Member.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(MemberRole.USER)
                .build();
    }

  // 회원 밴
    public void ban(LocalDateTime until) {
        this.banned = true;
        this.bannedUntil = until;
    }

   // 회원 정지상태 반환
    public boolean isCurrentlyBanned() {
        if (!this.banned) {
            return false;
        }
        if (this.bannedUntil == null) {
            // 영구 정지
            return true;
        }
        return this.bannedUntil.isAfter(LocalDateTime.now());
    }
}
