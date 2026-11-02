package com.pi.rdfReader.service;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service

public class RdfService {
    private final Model examModel;

    public RdfService() {
        examModel = FileManager.get().loadModel("exam_schedule.rdf");
    }

        // Q1 : les 5 cours les plus demandés (nombre de séances d'examen par cours)
    public List<Map<String, Object>> getTopCours() {

        String sparql = """
                    PREFIX ex:      <http://example.org/exam#>
                    PREFIX dcterms: <http://purl.org/dc/terms/>

                    SELECT ?titre (COUNT(?s) AS ?nbExamens)
                    WHERE {
                      ?s a ex:Seance ;
                         ex:pourCours ?cours .
                      ?cours dcterms:title ?titre .
                    }
                    GROUP BY ?titre
                    ORDER BY DESC(?nbExamens)
                    LIMIT 5
                """;

        Query query = QueryFactory.create(sparql);

        try (QueryExecution qexec = QueryExecutionFactory.create(query, examModel)) {

            ResultSet results = qexec.execSelect();
            List<Map<String, Object>> resultList = new ArrayList<>();

            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("titre", sol.getLiteral("titre").getString());
                row.put("nbExamens", sol.getLiteral("nbExamens").getInt());

                resultList.add(row);
            }

            return resultList;
        }
    }
        // Q2 : enseignants les plus chargés (plus de 200 séances)
    public List<Map<String, Object>> getEnseignantsCharges() {

        String sparql = """
                    PREFIX ex:   <http://example.org/exam#>
                    PREFIX foaf: <http://xmlns.com/foaf/0.1/>

                    SELECT ?nom ?departement
                           (COUNT(?s) AS ?nbSeances)
                           (COUNT(DISTINCT ?cours) AS ?nbCours)
                    WHERE {
                      ?s   ex:assureePar ?ens ;
                           ex:pourCours  ?cours .
                      ?ens foaf:name ?nom ;
                           ex:departementEnseignant ?departement .
                    }
                    GROUP BY ?nom ?departement
                    HAVING (COUNT(?s) > 200)
                    ORDER BY DESC(?nbSeances)
                """;

        Query query = QueryFactory.create(sparql);

        try (QueryExecution qexec = QueryExecutionFactory.create(query, examModel)) {

            ResultSet results = qexec.execSelect();
            List<Map<String, Object>> resultList = new ArrayList<>();

            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("nom", sol.getLiteral("nom").getString());
                row.put("departement", sol.getLiteral("departement").getString());
                row.put("nbSeances", sol.getLiteral("nbSeances").getInt());
                row.put("nbCours", sol.getLiteral("nbCours").getInt());

                resultList.add(row);
            }

            return resultList;
        }
    }
}
