package com.careerconnect.repository;

import com.careerconnect.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByLocationAndIndustry(String location, String industry);
}
