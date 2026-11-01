package com.pi.rdfReader.controller;

import com.pi.rdfReader.service.RdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController

public class RdfController {

    @Autowired
    private RdfService rdfService;
    
    @GetMapping("/professors")
    public List<Map<String, String>> getAllProfessors() {
        return rdfService.getAllProfessors();
    }
}


 