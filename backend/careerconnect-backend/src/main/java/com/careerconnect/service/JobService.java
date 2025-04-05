package com.careerconnect.service;

import com.careerconnect.model.Job;
import com.careerconnect.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {
    @Autowired
    private JobRepository jobRepo;

    public List<Job> listAll() {
        return jobRepo.findAll();
    }

    public Job post(Job job) {
        return jobRepo.save(job);
    }

    public List<Job> search(String location, String industry) {
        return jobRepo.findByLocationAndIndustry(location, industry);
    }
}
