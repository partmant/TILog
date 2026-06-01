package com.tilog.repository;

import com.tilog.entity.MemberSanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberSanctionRepository extends JpaRepository<MemberSanction, Long> {

}
