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

    // GET /api/etudiants-par-programme
    @GetMapping("/api/etudiants-par-programme")
    public List<Map<String, Object>> getEtudiantsParProgramme() {
        return rdfService.getEtudiantsParProgramme();
    }

    // GET /api/conflits-horaire
    @GetMapping("/api/conflits-horaire")
    public List<Map<String, Object>> getConflitsHoraire() {
        return rdfService.getConflitsHoraire();
    }

    // GET /api/taux-remplissage
    @GetMapping("/api/taux-remplissage")
    public List<Map<String, Object>> getTauxRemplissage() {
        return rdfService.getTauxRemplissage();
    }

    // GET /api/inference/personnes
    @GetMapping("/api/inference/personnes")
    public Map<String, Object> getPersonnesInference() {
        return rdfService.getPersonnesInference();
    }

    // GET /api/inference/departements
    @GetMapping("/api/inference/departements")
    public Map<String, Object> getDepartementsInference() {
        return rdfService.getDepartementsInference();
    }
}
