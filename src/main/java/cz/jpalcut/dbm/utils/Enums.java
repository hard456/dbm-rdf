package cz.jpalcut.dbm.utils;

/**
 * Class contains all available enums for this web app
 */
public class Enums {

    /**
     * Enum of RDF Model types
     */
    public enum RDFModelType {
        Turtle("Turtle"),
        NTriples("N-Triples"),
        JSON_LD("JSON-LD"),
        RDF_XML("RDF/XML"),
        RDF_JSON("RDF/JSON"),
        TriX("Trix");

        private final String modelType;

        RDFModelType(final String modelType) {
            this.modelType = modelType;
        }

        @Override
        public String toString() {
            return modelType;
        }

    }

}