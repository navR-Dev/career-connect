package com.careerconnect.controller;

import com.careerconnect.model.Job;
import com.careerconnect.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping
    public ResponseEntity<Job> post(@RequestBody Job job) {
        return ResponseEntity.ok(jobService.post(job));
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAll() {
        return ResponseEntity.ok(jobService.listAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Job>> search(@RequestParam String location,
            @RequestParam String industry) {
        return ResponseEntity.ok(jobService.search(location, industry));
    }
}
