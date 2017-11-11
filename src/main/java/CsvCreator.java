import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.FileManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by cemil on 12.09.2017.
 */
public class CsvCreator {

    public void filterDbpedia(String settingsFileLocation) throws IOException {
        //First, check whether settings file exists or no
        if(!Files.exists(Paths.get(settingsFileLocation))){
            System.out.println(String.format("Settings file '%%' does not exist"));
        }
        else{
            //Read settings file content
            Settings settings = getSettings(settingsFileLocation);

            //Create dbpedia ontology instance using Apache Jena
            OntModel ontologyModel = getDbpediaOntology(settings.getOntologyFileLocation());

            //Filtered types given in settings file can have subclasses. So we need to also add these subclasses to filtered type list
            findAndAddSubclassesOfFilteredTypes(settings,ontologyModel);

            //The resources that will be in filtered data set will be stored here
            Set<String> filteredResources = new HashSet<>();

            //Now, we can read the instance types line by line and filter dataset
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("dbpedia-resources.csv"))){

                writeResourceCsvHeaders(writer);

                //Filter instance types and write to file
                filterInstanceTypesAndWriteToCsvFile(settings.getInstanceTypesFileLocation(),settings.getFilteredType(),writer,filteredResources);

                //Close new file
                writer.flush();
                writer.close();
            }

            //Now, we can read the instance types line by line and filter dataset
            try (BufferedWriter relationWriter = new BufferedWriter(new FileWriter("dbpedia-relations.csv"))){

                writeRelationsCsvHeaders(relationWriter);

                //Filter properties and write to new filtered file
                filterPropertiesAndWriteToFile(settings.getPropertyFileLocation(),relationWriter,filteredResources);

                //Close new file
                relationWriter.flush();
                relationWriter.close();
            }




        }
    }

    /**
     * Reads settings file and fills the Settings instance with their values
     * @param settingsFileLocation Settings file location
     * @return Settings instance
     */
    private Settings getSettings(String settingsFileLocation) throws IOException{
        Settings settings = new Settings();
        try (BufferedReader br = new BufferedReader(new FileReader(settingsFileLocation))) {
            String line = br.readLine();

            //First line only contains the dbpedia ontology and dataset file locations
            String[] firstLineParts = line.split(" ");

            //First line contains dbpedia file locations seperated with blank
            settings.setOntologyFileLocation(firstLineParts[0]);
            settings.setInstanceTypesFileLocation(firstLineParts[1]);
            settings.setPropertyFileLocation(firstLineParts[2]);
            settings.setLabelsFileLocation(firstLineParts[3]);

            settings.setFilteredType(new HashSet<>());
            //Other lines contain filtered dbpedia types
            while ((line = br.readLine()) != null) {
                settings.getFilteredType().add(line);
            }
        }

        return settings;
    }

    /**
     * Loads dbpedia ontology instance as Jena OntModel
     * @param owlFileLocation Owl file location
     * @return OntModel instance
     */
    private OntModel getDbpediaOntology(String owlFileLocation){
        //Create ontology model
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,null);

        //Read owl file into stream
        InputStream owlFileStream = FileManager.get().open(owlFileLocation);

        //Load ontology model using input stream
        model.read(owlFileStream,null);

        //
        return model;
    }

    /**
     * Find subclasses of filtered types recursively and append them to the list
     * @param settings Settings instance
     * @param model Ontology model
     */
    private void findAndAddSubclassesOfFilteredTypes(Settings settings,OntModel model){
        Set<String> subclasses = new HashSet<>();

        ParameterizedSparqlString queryString = new ParameterizedSparqlString();
        queryString.setCommandText("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "SELECT ?subClass WHERE { ?subClass rdfs:subClassOf* ?C. }");

        for (String filteredType: settings.getFilteredType()) {
            //Set parameter value
            queryString.setIri("C",filteredType);

            //Create query
            Query query = QueryFactory.create(queryString.toString());

            //Execute query and subclasses of current class
            try (QueryExecution execution = QueryExecutionFactory.create(query,model)){
                ResultSet resultSet = execution.execSelect();
                while(resultSet.hasNext()){
                    RDFNode next = resultSet.next().get("subClass");
                    subclasses.add(next.isResource() ? next.asResource().getURI() : next.asLiteral().getString());
                }
            }
        }

        //Add subclasses list to filtered types list
        settings.getFilteredType().addAll(subclasses);
    }

    private void writeResourceCsvHeaders(BufferedWriter writer) throws IOException{
        writer.write("resourceId:ID(Resource),url,:LABEL"+ System.lineSeparator());
    }

    private void writeRelationsCsvHeaders(BufferedWriter writer) throws IOException{
        writer.write(":START_ID(Resource),url,:END_ID(Resource),:TYPE"+ System.lineSeparator());
    }

    private void filterInstanceTypesAndWriteToCsvFile(String instanceTypesFile,Set<String> filteredTypes,Writer writer,Set<String> filteredInstances) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(instanceTypesFile))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                //Skip comment lines
                if (!line.startsWith("#")) {
                    //Each line is in form of subject predicate object
                    String[] partsOfLine = line.split(" ");

                    //We need to check type of subject. As this file only contains type of instances, it is sufficient to check third part
                    String subjectUrl = Tools.escapeTagChars(partsOfLine[0]);
                    String objectUrl = Tools.escapeTagChars(partsOfLine[2]);

                    if (filteredTypes.contains(objectUrl)) {
                        //Write this line through writer
                        writer.write(String.format("\"%s\",\"%s\",\"Dbpedia;Resource\"",subjectUrl,subjectUrl)+ System.lineSeparator());

                        //add resource uri to the instance set
                        filteredInstances.add(Tools.escapeTagChars(partsOfLine[0]));
                    }
                }
            }
        }
    }

    private void filterPropertiesAndWriteToFile(String propertiesFile,Writer writer,Set<String> filteredInstances) throws IOException{
        try (BufferedReader br = new BufferedReader(new FileReader(propertiesFile))) {
            String line = null;
            while((line=br.readLine())!=null){
                //Skip comment lines
                if(!line.startsWith("#")) {
                    //Each line is in form of subject predicate object
                    String[] partsOfLine = line.split(" ");

                    String subjectUrl = Tools.escapeTagChars(partsOfLine[0]);
                    String objectUrl = Tools.escapeTagChars(partsOfLine[2]);
                    String predicate = Tools.escapeTagChars(partsOfLine[1]);

                    //We need to check type of subject and object. If both of them are in filtered instance list, then we will append this line to new file
                    if (filteredInstances.contains(Tools.escapeTagChars(partsOfLine[0])) && filteredInstances.contains(Tools.escapeTagChars(partsOfLine[2]))) {
                        //Write this line through writer
                        writer.write(String.format("\"%s\",\"%s\",\"%s\"",subjectUrl, predicate,objectUrl,predicate)+ System.lineSeparator());
                    }
                }
            }
        }
    }



}
