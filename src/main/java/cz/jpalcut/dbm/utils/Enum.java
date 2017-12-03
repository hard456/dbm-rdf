package cz.jpalcut.dbm.utils;

import org.apache.jena.riot.Lang;

/**
 * Class contains all available enums for this web app
 */
public class Enum {

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

    /**
     * Enum of file extension for upload a file
     */
    public enum RDFFileExt{
        ttl(Lang.TTL),
        owl(Lang.RDFXML),
        rdf(Lang.RDFXML),
        nt(Lang.NTRIPLES)
        ;

        private final Lang langType;

        RDFFileExt(Lang langType) {
            this.langType = langType;
        }

        public Lang getLangType(){
            return langType;
        }

    }

}