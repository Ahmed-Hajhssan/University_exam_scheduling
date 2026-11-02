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
    
      // GET /api/top-cours
    @GetMapping("/api/top-cours")
    public List<Map<String, Object>> getTopCours() {
        return rdfService.getTopCours();
    }
        // GET /api/enseignants-charges
    @GetMapping("/api/enseignants-charges")
    public List<Map<String, Object>> getEnseignantsCharges() {
        return rdfService.getEnseignantsCharges();
    }
}


 