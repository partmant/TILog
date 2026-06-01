package com.tilog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 태그 정보 저장 엔티티

@Entity
@Table(name = "tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    // 태그 이름
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // 태그 생성 메서드
    public static Tag create(String name) {
        Tag tag = new Tag();
        tag.name = name;

        return tag;
    }
}