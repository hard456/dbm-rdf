package cz.jpalcut.dbm;

import cz.jpalcut.dbm.container.RDFObjectProperties;
import cz.jpalcut.dbm.container.SPOContainer;
import org.apache.jena.rdf.model.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

public class RDFModelAggregator {

    /**
     * Method that aggregate model
     *
     * @return aggregated model
     */
    public Model aggregate(Model model) {

        List<SPOContainer> SPOlist = new ArrayList<SPOContainer>();

        //List of unique subjects
        List<Resource> subjects = new ArrayList<Resource>(getListOfSubjects(model.listStatements()));
        boolean found;

        //Loop list of subjects
        for (Resource subject : subjects) {

            //Define container for predicates and objects properties of actual subject
            SPOContainer spoContainer = new SPOContainer();

            //Set actual subject
            spoContainer.setSubject(subject);

            //Get TreeMap of Property and RDFNode of actual subject
            TreeMap<Property, RDFNode> newProperties = getPredicatesWithObjectsBySubject(model.listStatements(), subject);

            //Set TreeMap<Property, RDFNode> of actual subject
            spoContainer.setMap(newProperties);

            //Merge TreeMap<Property, RDFNode>
            spoContainer = mergeObjectsOfOneInstance(spoContainer);

            found = false;
            int indexOfSPOContainer;

            //Loop SPOContainer
            for (SPOContainer subjectProperties : SPOlist) {

                //Check if SPOContainer contains subject
                if (subjectProperties.getSubject().toString().equals(subject.toString())) {
                    break;
                }

                //Check predicates and class type of two subjects
                if (hasEqualPredicatesAndClassType(subjectProperties.getMap(), newProperties)) {
                    indexOfSPOContainer = SPOlist.indexOf(subjectProperties);

                    //Merge predicates of subjects with same structure
                    subjectProperties.setObjectProperties(mergeObjectsPropertiesOfTwoInstances(
                            subjectProperties.getObjectProperties(), spoContainer.getObjectProperties()));
                    SPOlist.set(indexOfSPOContainer, subjectProperties);
                    found = true;
                    break;
                }

            }

            //Add new unique subject
            if (!found) {
                spoContainer.setSubject(subject);
                spoContainer.setMap(newProperties);
                spoContainer = mergeObjectsOfOneInstance(spoContainer);
                SPOlist.add(spoContainer);
            }
        }

        //New aggregated model
        Model newModel = ModelFactory.createDefaultModel();

        //Add Namespace prefixes to model
        newModel.setNsPrefixes(model.getNsPrefixMap());

        transformObjectsToAggregatedValues(SPOlist, model);

        //Input new aggregated Triples into model
        newModel = inputTriplesIntoModel(SPOlist, newModel);

        return newModel;
    }

    private void transformObjectsToAggregatedValues(List<SPOContainer> SPOlist, Model model) {
        List<RDFObjectProperties> properties;
        List<Property> predicates;
        List<RDFNode> objects;
        TreeMap<Property, RDFNode> newMap;
        String newValue;
        SPOContainer spoContainer;

        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
        sym.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.###");
        df.setDecimalFormatSymbols(sym);

        for (int i = 0; i < SPOlist.size(); i++) {
            newMap = new TreeMap<Property, RDFNode>(new PredicateComparator());
            spoContainer = SPOlist.get(i);
            properties = spoContainer.getObjectProperties();
            objects = new ArrayList<RDFNode>(spoContainer.getMap().values());
            predicates = new ArrayList<Property>(spoContainer.getMap().keySet());
            RDFNode node;
            for (int j = 0; j < properties.size(); j++) {

                if (properties.get(j).isClass()) {
                    node = objects.get(j);
                } else if (properties.get(j).isInteger()) {
                    newValue = "C:" + String.valueOf(properties.get(j).getCount()) + " ";
                    newValue += "H:" + properties.get(j).getMax() + " ";
                    newValue += "L:" + properties.get(j).getMin() + " ";
                    newValue += "A:" + df.format((float)Integer.parseInt(properties.get(j).getSum()) / properties.get(j).getCount());
                    node = model.createLiteral(newValue);
                } else if (properties.get(j).isDouble()) {
                    newValue = "C:" + String.valueOf(properties.get(j).getCount()) + " ";
                    newValue += "H:" + properties.get(j).getMax() + " ";
                    newValue += "L:" + properties.get(j).getMin() + " ";

                    newValue += "A:" + df.format(Double.parseDouble(properties.get(j).getSum()) / properties.get(j).getCount());
                    node = model.createLiteral(newValue);
                } else {
                    newValue = "C:" + String.valueOf(properties.get(j).getCount());
                    node = model.createLiteral(newValue);
                }
                newMap.put(predicates.get(j), node);
            }
            spoContainer.setMap(newMap);
        }
//        SPOlist = newList;
    }

    private List<RDFObjectProperties> mergeObjectsPropertiesOfTwoInstances(List<RDFObjectProperties> first, List<RDFObjectProperties> second) {
        List<RDFObjectProperties> list = new ArrayList<RDFObjectProperties>();
        for (int i = 0; i < first.size(); i++) {
            if (first.get(i).isClass()) {
                list.add(first.get(i));
            } else if (first.get(i).isInteger() && second.get(i).isInteger()) {
                list.add(mergePredicatePropertiesAsInteger(first.get(i), second.get(i)));
            } else if (first.get(i).isDouble() && second.get(i).isDouble()) {
                list.add(mergePredicatePropertiesAsDouble(first.get(i), second.get(i)));
            } else {
                RDFObjectProperties objectProperties = new RDFObjectProperties();
                objectProperties.setString(true);
                objectProperties.setCount(first.get(i).getCount() + second.get(i).getCount());
                list.add(objectProperties);
            }
        }
        return list;
    }

    /**
     * To input new aggregated triples into model
     *
     * @param SPOlist aggregated container
     * @param model
     * @return
     */
    public Model inputTriplesIntoModel(List<SPOContainer> SPOlist, Model model) {
        for (SPOContainer spoContainer : SPOlist) {
            for (Map.Entry<Property, RDFNode> entry : spoContainer.getMap().entrySet()) {
                model.add(model.createStatement(spoContainer.getSubject(), entry.getKey(), entry.getValue()));
            }
        }
        return model;
    }

    /**
     * To get unique subjects from model
     *
     * @param iterator contains list of statements
     * @return
     */
    public Set<Resource> getListOfSubjects(StmtIterator iterator) {
        Set<Resource> subjects = new HashSet<Resource>();
        //Loop triples
        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            subjects.add(statement.getSubject());
        }
        return subjects;
    }

    /**
     * To get TreeMap of predicates and objects by subject
     *
     * @param iterator list of statements
     * @param subject
     * @return
     */
    public TreeMap<Property, RDFNode> getPredicatesWithObjectsBySubject(StmtIterator iterator, Resource subject) {
        //Tree map with sorting predicates
        TreeMap<Property, RDFNode> sortedMap = new TreeMap<Property, RDFNode>(new PredicateComparator());

        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            if (statement.getSubject().toString().equals(subject.toString())) {
                sortedMap.put(statement.getPredicate(), statement.getObject());
            }
        }

        return sortedMap;
    }

    /**
     * To merge objects of same predicates
     *
     * @param spoContainer contains predicates, objects and object properties of one subject
     * @return merged spoContainer
     */
    public SPOContainer mergeObjectsOfOneInstance(SPOContainer spoContainer) {
        boolean checkNext;
        int k;
        List<RDFObjectProperties> listOfPropPredicates = new ArrayList<RDFObjectProperties>();
        TreeMap<Property, RDFNode> newPropertiesMap = new TreeMap<Property, RDFNode>(new PredicateComparator());
        RDFObjectProperties predicateProperties;
        TreeMap<Property, RDFNode> properties = spoContainer.getMap();
        List<Property> keys = new ArrayList<Property>(properties.keySet());
        List<RDFNode> objects = new ArrayList<RDFNode>(properties.values());
        for (int i = 0; i < keys.size(); i++) {

            if (keys.get(i).toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                predicateProperties = new RDFObjectProperties();
                predicateProperties.setClass(true);
            } else if (isInteger(objects.get(i))) {
                predicateProperties = definePredicateAsInteger(objects.get(i).toString());
            } else if (isDouble(objects.get(i))) {
                predicateProperties = definePredicateAsDouble(objects.get(i).toString());
            } else {
                predicateProperties = definePredicateAsString();
            }

            checkNext = true;
            k = 1;

            while (checkNext && i + k < keys.size()) {
                if (!keys.get(i).toString().equals(keys.get(i + k).toString())) {
                    checkNext = false;
                } else {
                    if (predicateProperties.isInteger() && isInteger(objects.get(i + k))) {
                        predicateProperties = mergePredicatesAsInteger(predicateProperties, objects.get(i + k).toString());
                    } else if (predicateProperties.isDouble() && isDouble(objects.get(i + k))) {
                        predicateProperties = mergePredicatesAsDouble(predicateProperties, objects.get(i + k).toString());
                    } else {
                        predicateProperties = mergePredicatesAsString(predicateProperties);
                    }
                    keys.remove(i + k);
                    objects.remove(i + k);
                }
            }
            newPropertiesMap.put(keys.get(i), objects.get(i));
            listOfPropPredicates.add(predicateProperties);
        }
        spoContainer.setMap(newPropertiesMap);
        spoContainer.setObjectProperties(listOfPropPredicates);
        return spoContainer;
    }

    /**
     * To check same Predicates include same Class type
     *
     * @param first  TreeMap with Predicates and Objects of first instance
     * @param second TreeMap with Predicates and Objects of second instance
     * @return true - same instances type, false - different instance type
     */
    public boolean hasEqualPredicatesAndClassType(TreeMap<Property, RDFNode> first, TreeMap<Property, RDFNode> second) {
        if (first.size() != second.size()) {
            return false;
        }

        //Lists of keys
        List<Property> firstKeys = new ArrayList<Property>(first.keySet());
        List<Property> secondKeys = new ArrayList<Property>(second.keySet());

        //Lists of values
        List<RDFNode> firstObjects = new ArrayList<RDFNode>(first.values());
        List<RDFNode> secondObjects = new ArrayList<RDFNode>(second.values());

        //Iteration to check same properties and same Namespace type
        for (int i = 0; i < firstKeys.size(); i++) {

            //To check same Class type
            if (firstKeys.get(i).toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                if (!firstObjects.get(i).toString().equals(secondObjects.get(i).toString())) {
                    return false;
                }
            }

            //To check same properties
            if (!firstKeys.get(i).toString().equals(secondKeys.get(i).toString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * To check if object is Integer
     *
     * @param object
     * @return true - is Integer, false - not Integer
     */
    private boolean isInteger(RDFNode object) {
        try {
            Integer.parseInt(object.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * To check if object is Double
     *
     * @param object
     * @return true - is Double, false - not Double
     */
    private boolean isDouble(RDFNode object) {
        try {
            Double.parseDouble(object.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * To define predicate as Integer
     *
     * @param value String value of object
     * @return
     */
    private RDFObjectProperties definePredicateAsInteger(String value) {
        RDFObjectProperties properties = new RDFObjectProperties();
        properties.setInteger(true);
        properties.setMin(value);
        properties.setMax(value);
        properties.setSum(value);
        properties.setCount(1);
        return properties;
    }

    /**
     * To define predicate as Double
     *
     * @param value String value of object
     * @return
     */
    private RDFObjectProperties definePredicateAsDouble(String value) {
        RDFObjectProperties properties = new RDFObjectProperties();
        properties.setDouble(true);
        properties.setMin(value);
        properties.setMax(value);
        properties.setSum(value);
        properties.setCount(1);
        return properties;
    }

    /**
     * To define predicate as String
     *
     * @return
     */
    private RDFObjectProperties definePredicateAsString() {
        RDFObjectProperties properties = new RDFObjectProperties();
        properties.setString(true);
        properties.setCount(1);
        return properties;
    }

    /**
     * Merge predicate as Integer with already defined predicate as Integer
     *
     * @param predicateProperties predicate properties
     * @param value               string value of object
     * @return
     */
    private RDFObjectProperties mergePredicatesAsInteger(RDFObjectProperties predicateProperties, String value) {
        int number = Integer.parseInt(value);
        int sum = Integer.parseInt(predicateProperties.getSum()) + number;

        if (number > Integer.parseInt(predicateProperties.getMax())) {
            predicateProperties.setMax(String.valueOf(number));
        }
        if (number < Integer.parseInt(predicateProperties.getMin())) {
            predicateProperties.setMin(String.valueOf(number));
        }

        predicateProperties.setCount(predicateProperties.getCount() + 1);
        predicateProperties.setSum(String.valueOf(sum));

        return predicateProperties;
    }

    /**
     * Merge predicate as Double with already defined predicate as Double
     *
     * @param predicateProperties predicate properties
     * @param value               string value of object
     * @return
     */
    private RDFObjectProperties mergePredicatesAsDouble(RDFObjectProperties predicateProperties, String value) {
        double number = Double.parseDouble(value);
        double sum = Double.parseDouble(predicateProperties.getSum()) + number;

        if (number > Integer.parseInt(predicateProperties.getMax())) {
            predicateProperties.setMax(String.valueOf(number));
        }
        if (number < Integer.parseInt(predicateProperties.getMin())) {
            predicateProperties.setMin(String.valueOf(number));
        }

        predicateProperties.setCount(predicateProperties.getCount() + 1);
        predicateProperties.setSum(String.valueOf(sum));

        return predicateProperties;
    }


    private RDFObjectProperties mergePredicatePropertiesAsInteger(RDFObjectProperties first, RDFObjectProperties second) {
        int sum = Integer.parseInt(first.getSum()) + Integer.parseInt(second.getSum());

        if (Integer.parseInt(first.getMax()) > Integer.parseInt(second.getMax())) {
            first.setMax(first.getMax());
        } else {
            first.setMax(second.getMax());
        }

        if (Integer.parseInt(first.getMin()) < Integer.parseInt(second.getMin())) {
            first.setMin(first.getMin());
        } else {
            first.setMin(second.getMin());
        }

        first.setCount(first.getCount() + second.getCount());
        first.setSum(String.valueOf(sum));
        return first;
    }

    private RDFObjectProperties mergePredicatePropertiesAsDouble(RDFObjectProperties first, RDFObjectProperties second) {
        double sum = Double.parseDouble(first.getSum()) + Double.parseDouble(second.getSum());

        if (Double.parseDouble(first.getMax()) > Double.parseDouble(second.getMax())) {
            first.setMax(first.getMax());
        } else {
            first.setMax(second.getMax());
        }

        if (Double.parseDouble(first.getMin()) < Double.parseDouble(second.getMin())) {
            first.setMin(first.getMin());
        } else {
            first.setMin(second.getMin());
        }

        first.setCount(first.getCount() + second.getCount());
        first.setSum(String.valueOf(sum));
        return first;
    }

    /**
     * Merge predicate as String with already defined predicate as String
     *
     * @param predicateProperties predicate properties
     * @return
     */
    private RDFObjectProperties mergePredicatesAsString(RDFObjectProperties predicateProperties) {
        predicateProperties.setString(true);
        predicateProperties.setCount(predicateProperties.getCount() + 1);
        return predicateProperties;
    }

    private class PredicateComparator implements Comparator<Property>{
        @Override
        public int compare(Property p1, Property p2) {
            if (p2.toString().compareTo(p1.toString()) > 0) {
                return 1;
            }
            return -1;
        }
    }

}
