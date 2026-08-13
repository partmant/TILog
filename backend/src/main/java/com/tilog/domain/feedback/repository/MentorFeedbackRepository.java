package com.tilog.domain.feedback.repository;

import com.tilog.domain.feedback.entity.MentorFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentorFeedbackRepository extends JpaRepository<MentorFeedback, Long> {
    List<MentorFeedback> findByRequestor_Id(Long requestorId);
    List<MentorFeedback> findByMentor_Id(Long mentorId);
    void deleteByTil_Id(Long tilId);
}
