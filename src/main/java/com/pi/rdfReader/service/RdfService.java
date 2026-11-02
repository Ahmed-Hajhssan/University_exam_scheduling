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
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.rdf.model.RDFNode;
import java.util.Iterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service

public class RdfService {
    private final Model examModel;
    private final InfModel infModel;

    public RdfService() {
        examModel = FileManager.get().loadModel("exam_schedule.rdf");
        // Raisonneur RDFS simple : subClassOf, subPropertyOf, domain, range
        infModel = ModelFactory.createInfModel(ReasonerRegistry.getRDFSSimpleReasoner(), examModel);
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

    // Q3 : nombre d'étudiants par programme (top 5)
    public List<Map<String, Object>> getEtudiantsParProgramme() {

        String sparql = """
                    PREFIX ex: <http://example.org/exam#>

                    SELECT ?programme (COUNT(?etu) AS ?nbEtudiants)
                    WHERE {
                      ?etu a ex:Etudiant ;
                           ex:programme ?programme .
                    }
                    GROUP BY ?programme
                    ORDER BY DESC(?nbEtudiants)
                    LIMIT 5
                """;

        Query query = QueryFactory.create(sparql);

        try (QueryExecution qexec = QueryExecutionFactory.create(query, examModel)) {

            ResultSet results = qexec.execSelect();
            List<Map<String, Object>> resultList = new ArrayList<>();

            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("programme", sol.getLiteral("programme").getString());
                row.put("nbEtudiants", sol.getLiteral("nbEtudiants").getInt());

                resultList.add(row);
            }

            return resultList;
        }
    }

    // Q4 : conflits d'horaire (étudiants avec plusieurs examens au même créneau)
    public List<Map<String, Object>> getConflitsHoraire() {

        String sparql = """
                    PREFIX ex:   <http://example.org/exam#>
                    PREFIX foaf: <http://xmlns.com/foaf/0.1/>

                    SELECT ?nom ?jour ?heure (COUNT(?s) AS ?nbExamens)
                    WHERE {
                      ?s   ex:pourEtudiant ?etu ;
                           ex:auCreneau    ?creneau .
                      ?etu foaf:name ?nom .
                      ?creneau ex:jour ?jour ;
                               ex:heureDebut ?heure .
                    }
                    GROUP BY ?etu ?nom ?creneau ?jour ?heure
                    HAVING (COUNT(?s) > 1)
                    ORDER BY DESC(?nbExamens)
                    LIMIT 10
                """;

        Query query = QueryFactory.create(sparql);

        try (QueryExecution qexec = QueryExecutionFactory.create(query, examModel)) {

            ResultSet results = qexec.execSelect();
            List<Map<String, Object>> resultList = new ArrayList<>();

            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("nom", sol.getLiteral("nom").getString());
                row.put("jour", sol.getLiteral("jour").getString());
                row.put("heure", sol.getLiteral("heure").getString());
                row.put("nbExamens", sol.getLiteral("nbExamens").getInt());

                resultList.add(row);
            }

            return resultList;
        }
    }

    // Q5 : taux de remplissage des salles par créneau (top 5)
    public List<Map<String, Object>> getTauxRemplissage() {

        String sparql = """
                    PREFIX ex: <http://example.org/exam#>

                    SELECT ?batiment ?numero ?jour ?heure ?capacite ?nbEtudiants
                           (ROUND(100 * ?nbEtudiants / ?capacite) AS ?tauxPct)
                    WHERE {
                      {
                        SELECT ?salle ?creneau (COUNT(?s) AS ?nbEtudiants)
                        WHERE {
                          ?s ex:sePasseDans ?salle ;
                             ex:auCreneau   ?creneau .
                        }
                        GROUP BY ?salle ?creneau
                      }
                      ?salle ex:batiment ?batiment ;
                             ex:numeroSalle ?numero ;
                             ex:capacite ?capacite .
                      ?creneau ex:jour ?jour ;
                               ex:heureDebut ?heure .
                    }
                    ORDER BY DESC(?nbEtudiants / ?capacite)
                    LIMIT 5
                """;

        Query query = QueryFactory.create(sparql);

        try (QueryExecution qexec = QueryExecutionFactory.create(query, examModel)) {

            ResultSet results = qexec.execSelect();
            List<Map<String, Object>> resultList = new ArrayList<>();

            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("batiment", sol.getLiteral("batiment").getString());
                row.put("numero", sol.getLiteral("numero").getInt());
                row.put("jour", sol.getLiteral("jour").getString());
                row.put("heure", sol.getLiteral("heure").getString());
                row.put("capacite", sol.getLiteral("capacite").getInt());
                row.put("nbEtudiants", sol.getLiteral("nbEtudiants").getInt());
                row.put("tauxPct", (int) Math.round(sol.getLiteral("tauxPct").getDouble()));

                resultList.add(row);
            }

            return resultList;
        }
    }

    // ---------- INFERENCE ----------

    // Exécute une requête sur un modèle donné et renvoie les lignes sous forme de
    // Map
    private List<Map<String, Object>> executer(String sparql, Model model) {
        Query query = QueryFactory.create(sparql);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            List<Map<String, Object>> list = new ArrayList<>();
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();
                Map<String, Object> row = new LinkedHashMap<>();
                Iterator<String> vars = sol.varNames();
                while (vars.hasNext()) {
                    String v = vars.next();
                    RDFNode node = sol.get(v);
                    row.put(v, node.isLiteral() ? node.asLiteral().getValue() : node.toString());
                }
                list.add(row);
            }
            return list;
        }
    }

    // Compare une même requête sans / avec inférence
    private Map<String, Object> comparer(String sparql) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("sansInference", executer(sparql, examModel));
        res.put("avecInference", executer(sparql, infModel));
        return res;
    }

    // Règle 1 : rdfs:subClassOf (Etudiant, Enseignant => foaf:Person)
    public Map<String, Object> getPersonnesInference() {
        String sparql = """
                PREFIX foaf: <http://xmlns.com/foaf/0.1/>
                SELECT (COUNT(DISTINCT ?p) AS ?nbPersonnes)
                WHERE { ?p a foaf:Person . }
                """;
        return comparer(sparql);
    }

    // Règle 2 : rdfs:subPropertyOf (departementCours, departementEnseignant =>
    // departement)
    public Map<String, Object> getDepartementsInference() {
        String sparql = """
                PREFIX ex: <http://example.org/exam#>
                SELECT ?departement (COUNT(DISTINCT ?x) AS ?nb)
                WHERE { ?x ex:departement ?departement . }
                GROUP BY ?departement
                ORDER BY DESC(?nb)
                LIMIT 10
                """;
        return comparer(sparql);
    }
}
