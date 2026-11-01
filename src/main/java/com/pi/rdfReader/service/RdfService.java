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

@Service

public class RdfService {
    private final Model model;

    public RdfService() {
        model = FileManager.get().loadModel("professeur1.rdf");
    }

    public List<Map<String, String>> getAllProfessors() {

        String sparql = """
                    PREFIX ex: <http://example.org/uni#>

                    SELECT ?nom ?date ?url
                    WHERE {
                      ?p a ex:Professeur ;
                         ex:nom ?nom ;
                         ex:enseigneDepuis ?date ;
                         ex:articlesURL ?url .
                    }
                """;

        Query query = QueryFactory.create(sparql);

        QueryExecution qexec = null;

        try {
            qexec = QueryExecutionFactory.create(query, model);

            ResultSet results = qexec.execSelect();

            List<Map<String, String>> resultList = new ArrayList<>();

            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();

                Map<String, String> row = new HashMap<>();
                row.put("nom", sol.getLiteral("nom").getString());
                row.put("date", sol.getLiteral("date").getString());
                row.put("url", sol.getLiteral("url").getString());

                resultList.add(row);
            }

            return resultList;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;

        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }

    }
}
