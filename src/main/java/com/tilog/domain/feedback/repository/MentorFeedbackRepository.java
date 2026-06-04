package com.tilog.domain.feedback.repository;

import com.tilog.domain.feedback.entity.MentorFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorFeedbackRepository extends JpaRepository<MentorFeedback, Long> {
}
