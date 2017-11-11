import java.io.*;
import java.util.*;

/**
 * Created by cemil on 11.11.2017.
 */
public class TboxCsvCreator {

    private List<String> excludedRelations = Arrays.asList(
    "http://www.w3.org/2000/01/rdf-schema#label", "http://www.w3.org/2000/01/rdf-schema#comment",
    "http://www.w3.org/ns/prov#wasDerivedFrom");

    public void filterDbpedia(String dbpediaOntologyLocation) throws IOException {

        Set<String> resources = new HashSet<>();
        List<String> relations = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(dbpediaOntologyLocation))) {
            String line = null;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                String subject = Tools.escapeTagChars(parts[0]);
                String predicate = Tools.escapeTagChars(parts[1]);
                String object = Tools.escapeTagChars(parts[2]);

                if (!excludedRelations.contains(predicate)) {
                    resources.add(subject);
                    resources.add(object);

                    relations.add(String.format("%s %s %s",subject,predicate,object));
                }
            }
        }


        try (BufferedWriter resourcesWriter = new BufferedWriter(new FileWriter("dbpedia-ontology-resources.csv"))) {
            writeHeadersToResourceCsvFile(resourcesWriter);

            for(String entity: resources){
                resourcesWriter.write(String.format("%s,%s,%s",entity,entity,"Dbpedia;Abox")+System.lineSeparator());
            }

        }

        try (BufferedWriter relationsWriter = new BufferedWriter(new FileWriter("dbpedia-ontology-relations.csv"))) {
            writeHeadersToRelationsCsvFile(relationsWriter);

            for(String relation: relations){
                String[] parts = relation.split(" ");
                relationsWriter.write(String.format("%s,%s,%s,%s",parts[0],parts[1],parts[2],parts[1])+System.lineSeparator());
            }
        }

    }

    private void writeHeadersToResourceCsvFile(BufferedWriter resourceWriter) throws IOException {
        resourceWriter.write("classId:ID(Class),url,:LABEL"+System.lineSeparator());
    }

    private void writeHeadersToRelationsCsvFile(BufferedWriter resourceWriter) throws IOException {
        resourceWriter.write(":START_ID(Class),url,:END_ID(Class),:TYPE"+System.lineSeparator());
    }
}
