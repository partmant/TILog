package com.tilog.domain.member.repository;

import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    List<Member> findByRole(MemberRole role);   // MemberRole 일치하는 회원 찾기
}
