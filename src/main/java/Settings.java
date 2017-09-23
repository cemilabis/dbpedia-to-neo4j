import java.util.Set;

/**
 * Created by cemil on 31.08.2017.
 */
public class Settings {

    //region Fields

    private String ontologyFileLocation;

    private String instanceTypesFileLocation;

    private String propertyFileLocation;

    private String labelsFileLocation;

    private Set<String> filteredType;

    //endregion

    //region Getters/Setters

    public String getOntologyFileLocation() {
        return ontologyFileLocation;
    }

    public void setOntologyFileLocation(String ontologyFileLocation) {
        this.ontologyFileLocation = ontologyFileLocation;
    }

    public String getInstanceTypesFileLocation() {
        return instanceTypesFileLocation;
    }

    public void setInstanceTypesFileLocation(String instanceTypesFileLocation) {
        this.instanceTypesFileLocation = instanceTypesFileLocation;
    }

    public String getPropertyFileLocation() {
        return propertyFileLocation;
    }

    public void setPropertyFileLocation(String propertyFileLocation) {
        this.propertyFileLocation = propertyFileLocation;
    }

    public String getLabelsFileLocation() {
        return labelsFileLocation;
    }

    public void setLabelsFileLocation(String labelsFileLocation) {
        this.labelsFileLocation = labelsFileLocation;
    }

    public Set<String> getFilteredType() {
        return filteredType;
    }

    public void setFilteredType(Set<String> filteredType) {
        this.filteredType = filteredType;
    }

    //endregion
}
