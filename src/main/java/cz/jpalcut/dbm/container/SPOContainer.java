package cz.jpalcut.dbm.container;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.TreeMap;

/**
 * Container for subjects, predicates, objects and object properties
 */
public class SPOContainer {

    private Resource subject;

    //TreeMap contains predicates as keys and objects as values
    private TreeMap<Property, RDFNode> map;

    //List of properties about objects
    private List<ObjectProperties> objectProperties;

    public Resource getSubject() {
        return subject;
    }

    public void setSubject(Resource subject) {
        this.subject = subject;
    }

    public TreeMap<Property, RDFNode> getMap() {
        return map;
    }

    public void setMap(TreeMap<Property, RDFNode> map) {
        this.map = map;
    }

    public List<ObjectProperties> getObjectProperties() {
        return objectProperties;
    }

    public void setObjectProperties(List<ObjectProperties> objectProperties) {
        this.objectProperties = objectProperties;
    }

}
