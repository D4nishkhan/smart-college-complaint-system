package com.smartcollege.backend.controller;

import com.smartcollege.backend.dto.ComplaintResponseDTO;
import com.smartcollege.backend.repository.ComplaintRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/complaints")
@CrossOrigin(origins = "*")
public class ComplaintController {

    private final ComplaintRepository complaintRepository;

    public ComplaintController(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @GetMapping
    public List<ComplaintResponseDTO> getAllComplaints() {
        return complaintRepository.findAllWithStudentAndDepartment()
                .stream()
                .map(ComplaintResponseDTO::new)
                .toList();
    }
}