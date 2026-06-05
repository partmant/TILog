package com.tilog.domain.member.repository;

import com.tilog.domain.member.entity.MemberSanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberSanctionRepository extends JpaRepository<MemberSanction, Long> {

}
