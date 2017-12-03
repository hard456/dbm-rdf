package cz.jpalcut.dbm.container;

import cz.jpalcut.dbm.RDFModelAggregator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.TreeMap;

public class SPOContainer {

    private Resource subject;

    private TreeMap<Property, RDFNode> map;

    private List<RDFObjectProperties> objectProperties;

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

    public List<RDFObjectProperties> getObjectProperties() {
        return objectProperties;
    }

    public void setObjectProperties(List<RDFObjectProperties> objectProperties) {
        this.objectProperties = objectProperties;
    }

}
