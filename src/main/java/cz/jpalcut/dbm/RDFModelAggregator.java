package cz.jpalcut.dbm;

import cz.jpalcut.dbm.container.ObjectProperties;
import cz.jpalcut.dbm.container.SPOContainer;
import org.apache.jena.rdf.model.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class RDFModelAggregator {

    /**
     * Method that aggregate model
     *
     * @return aggregated model
     */
    public Model aggregate(Model model) {

        List<SPOContainer> SPOList = new ArrayList<SPOContainer>();

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
            spoContainer = mergeObjectsOfOneInstance(spoContainer, model);

            found = false;
            int indexOfSPOContainer;

            //Loop SPOContainer
            for (SPOContainer subjectProperties : SPOList) {

                //Check if SPOList contains subject
                if (subjectProperties.getSubject().toString().equals(subject.toString())) {
                    break;
                }

                //Check predicates and class type of two subjects
                if (hasEqualPredicatesAndClassType(subjectProperties.getMap(), newProperties)) {
                    indexOfSPOContainer = SPOList.indexOf(subjectProperties);

                    //Merge predicates of subjects with same structure
                    subjectProperties.setObjectProperties(mergeObjectsPropertiesOfTwoInstances(
                            subjectProperties.getObjectProperties(), spoContainer.getObjectProperties()));
                    SPOList.set(indexOfSPOContainer, subjectProperties);
                    found = true;
                    break;
                }

            }

            //Add new unique subject
            if (!found) {
                spoContainer.setSubject(subject);
                spoContainer.setMap(newProperties);
                spoContainer = mergeObjectsOfOneInstance(spoContainer, model);
                SPOList.add(spoContainer);
            }
        }

        //New aggregated model
        Model newModel = ModelFactory.createDefaultModel();

        //Add Namespace prefixes to model
        newModel.setNsPrefixes(model.getNsPrefixMap());

        //Merge instances with different predicates
        SPOList = mergeSameInstancesWithDiffPredicates(SPOList);

        //Transform values of objects
        SPOList = transformObjectsToAggregatedValues(SPOList, model);

        //Input new aggregated Triples into model
        newModel = inputTriplesIntoModel(SPOList, newModel);

        return newModel;
    }


    /**
     * Method that merge instances with same class
     * @param SPOList
     * @return merged SPOList
     */
    private List<SPOContainer> mergeSameInstancesWithDiffPredicates(List<SPOContainer> SPOList){

        List<String> classesFirst;
        List<String> classesSecond;

        for (int i = 0; i < SPOList.size(); i++){
            classesFirst = getClassesOfSubject(SPOList.get(i));
            for (int j = i+1; j < SPOList.size(); j++){
                classesSecond = getClassesOfSubject(SPOList.get(j));
                if(Arrays.equals(classesFirst.toArray(), classesSecond.toArray())){
                   SPOList.set(i,mergePredicates(SPOList.get(i),SPOList.get(j)));
                   SPOList.remove(j);
                   j--;
                }
            }
        }

        return SPOList;
    }

    /**
     * Merge predicates of two subjects to one subject
     * @param first container of subject
     * @param second cotainer of subject
     * @return merge container of subject
     */
    private SPOContainer mergePredicates(SPOContainer first, SPOContainer second){

        List<Property> keysFirst = new ArrayList<Property>(first.getMap().keySet());
        List<Property> keysSecond = new ArrayList<Property>(second.getMap().keySet());
        List<RDFNode> valuesSecond = new ArrayList<RDFNode>(second.getMap().values());
        List<ObjectProperties> propertiesFirst = first.getObjectProperties();
        TreeMap<Property, RDFNode> newMap = first.getMap();

        //Merge same predicates
        for (int i = 0; i < keysFirst.size(); i++){
            for (int j = 0; j<keysSecond.size(); j++){
                ObjectProperties propFirst = propertiesFirst.get(i);
                if(keysFirst.get(i).toString().equals(keysSecond.get(j).toString())){

                    ObjectProperties propSecond = second.getObjectProperties().get(j);
                    if(keysFirst.get(i).toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")){
                        break;
                    }
                    else if(first.getObjectProperties().get(i).isInteger() && second.getObjectProperties().get(j).isInteger()){
                        propFirst = mergePredicatePropertiesAsInteger(propFirst,propSecond);
                    }
                    else if(first.getObjectProperties().get(i).isDouble() && second.getObjectProperties().get(j).isDouble()){
                        propFirst = mergePredicatePropertiesAsDouble(propFirst,propSecond);
                    }
                    else if(first.getObjectProperties().get(i).isLink() && second.getObjectProperties().get(j).isLink()){
                        first.getObjectProperties().get(i).setCount(propFirst.getCount() + propSecond.getCount());
                        if (!propFirst.getLinkValue().equals(propSecond.getLinkValue())) {
                            propFirst.setLinkValue("different");
                        }
                    }
                    else{
                        propFirst.setCount(propFirst.getCount()+propSecond.getCount());
                    }
                    propertiesFirst.set(i,propFirst);
                    break;
                }
            }
        }

        List<String> predicatesStrings = convertPropertyListToStringList(keysFirst);

        //Add new predicates
        for (int i = 0; i<keysSecond.size(); i++){
            if(!predicatesStrings.contains(keysSecond.get(i).toString())){

                newMap.put(keysSecond.get(i),valuesSecond.get(i));

                //Add Properties of object to the position in list
                int indexOfNewPredicate = 0;
                for (Map.Entry<Property, RDFNode> entry : newMap.entrySet()) {
                    if(entry.getKey().toString().equals(keysSecond.get(i).toString())){
                        if(indexOfNewPredicate == propertiesFirst.size()-1){
                            ObjectProperties newProp = second.getObjectProperties().get(i);
                            propertiesFirst.add(newProp);
                        }
                        else{
                            propertiesFirst.add(indexOfNewPredicate,second.getObjectProperties().get(i));
                        }
                        break;
                    }
                    indexOfNewPredicate++;
                }

            }

        }

        first.setMap(newMap);
        first.setObjectProperties(propertiesFirst);

        return first;
    }

    /**
     * Convert List of Property to List of String
     * @param predicates List of Property
     * @return List of String
     */
    private List<String> convertPropertyListToStringList(List<Property> predicates) {
        List<String> strings = new ArrayList<String>(predicates.size());
        for (Property property : predicates) {
            strings.add(property.toString());
        }
        return strings;
    }

    /**
     * To get classes of subject
     * @param container
     * @return List of String contains classes of subject
     */
    private List<String> getClassesOfSubject(SPOContainer container){
        List<Property> predicates = new ArrayList<Property>(container.getMap().keySet());
        List<RDFNode> objects = new ArrayList<RDFNode>(container.getMap().values());
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < predicates.size(); i++){
            if(predicates.get(i).toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")){
                list.add(objects.get(i).toString());
            }
        }

        return list;
    }

    /**
     * Method that transform values to objects to aggregated values
     *
     * @param SPOList container of subject, predicates, objects a properties of objects
     * @param model
     */
    private List<SPOContainer> transformObjectsToAggregatedValues(List<SPOContainer> SPOList, Model model) {

        List<ObjectProperties> properties;
        List<Property> predicates;
        List<RDFNode> objects;
        TreeMap<Property, RDFNode> newMap;
        String newValue;
        SPOContainer spoContainer;

        //For decimal format output
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
        sym.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.###");
        df.setDecimalFormatSymbols(sym);

        //Loop subjects
        for (int i = 0; i < SPOList.size(); i++) {
            newMap = new TreeMap<Property, RDFNode>(new PredicateComparator());
            spoContainer = SPOList.get(i);
            properties = spoContainer.getObjectProperties();
            objects = new ArrayList<RDFNode>(spoContainer.getMap().values());
            predicates = new ArrayList<Property>(spoContainer.getMap().keySet());
            RDFNode node;

            //Loop properties of objects
            for (int j = 0; j < properties.size(); j++) {

                //Set new value for object
                if (properties.get(j).getCount() == 1) {
                    if(properties.get(j).isClass()){
                        node = objects.get(j);
                    }
                    else if (properties.get(j).isLink()) {
                        node = model.createLiteral("N:" + getMapKeyByValue(model.getNsPrefixMap(), properties.get(j).getLinkValue()));
                    } else {
                        if (objects.get(j).isResource()) {
                            newValue = objects.get(j).asResource().toString();
                            int indexOfLastSlash = newValue.lastIndexOf("/");
                            newValue = newValue.subSequence(0, indexOfLastSlash + 1).toString();
                            node = model.createLiteral(newValue);
                        } else {
                            node = objects.get(j);
                        }
                    }
                } else {
                    //Check properties of object to set new value
                    if (properties.get(j).isInteger()) {
                        newValue = "C:" + String.valueOf(properties.get(j).getCount()) + " ";
                        newValue += "H:" + properties.get(j).getMax() + " ";
                        newValue += "L:" + properties.get(j).getMin() + " ";
                        newValue += "A:" + df.format((float) Integer.parseInt(properties.get(j).getSum()) / properties.get(j).getCount());
                        node = model.createLiteral(newValue);
                    } else if (properties.get(j).isDouble()) {
                        newValue = "C:" + String.valueOf(properties.get(j).getCount()) + " ";
                        newValue += "H:" + properties.get(j).getMax() + " ";
                        newValue += "L:" + properties.get(j).getMin() + " ";
                        newValue += "A:" + df.format(Double.parseDouble(properties.get(j).getSum()) / properties.get(j).getCount());
                        node = model.createLiteral(newValue);
                    } else if (properties.get(j).isLink()) {
                        String pom = getMapKeyByValue(model.getNsPrefixMap(), properties.get(j).getLinkValue());
                        if (pom == null) {
                            pom = "different";
                        }
                        newValue = "C:" + String.valueOf(properties.get(j).getCount()) + " ";
                        newValue += "N:" + pom;
                        node = model.createLiteral(newValue);
                    } else {
                        if (objects.get(j).isResource()) {
                            newValue = objects.get(j).asResource().toString();
                            int indexOfLastSlash = newValue.lastIndexOf("/");
                            newValue = newValue.subSequence(0, indexOfLastSlash + 1).toString();
                            newValue = "C:" + String.valueOf(properties.get(j).getCount() + " N:" + newValue);
                            node = model.createLiteral(newValue);
                        } else {
                            newValue = "C:" + String.valueOf(properties.get(j).getCount());
                            node = model.createLiteral(newValue);
                        }
                    }
                }
                //Add predicate and object to TreeMap
                newMap.put(predicates.get(j), node);
            }
            //Set new map of predicates and objects
            spoContainer.setMap(newMap);
        }
        //return new aggregated TreeMap
        return SPOList;
    }

    /**
     * To merge properties about objects of two different instances (instances of same type)
     *
     * @param first  properties of first list
     * @param second properties of second list
     * @return merged list of properties about objects
     */
    private List<ObjectProperties> mergeObjectsPropertiesOfTwoInstances(List<ObjectProperties> first, List<ObjectProperties> second) {
        List<ObjectProperties> list = new ArrayList<ObjectProperties>();
        //Loop properties
        for (int i = 0; i < first.size(); i++) {
            //Set properties
            if (first.get(i).isClass()) {
                list.add(first.get(i));
            } else if (first.get(i).isInteger() && second.get(i).isInteger()) {
                list.add(mergePredicatePropertiesAsInteger(first.get(i), second.get(i)));
            } else if (first.get(i).isDouble() && second.get(i).isDouble()) {
                list.add(mergePredicatePropertiesAsDouble(first.get(i), second.get(i)));
            } else if (first.get(i).isLink() && second.get(i).isLink()) {
                first.get(i).setCount(first.get(i).getCount() + second.get(i).getCount());
                if (!first.get(i).getLinkValue().equals(second.get(i).getLinkValue())) {
                    first.get(i).setLinkValue("different");
                }
                list.add(first.get(i));
            } else {
                ObjectProperties objectProperties = new ObjectProperties();
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
     * @return model with added N-Triples
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

        TreeMap<Property, RDFNode> sortedMap = new TreeMap<Property, RDFNode>(new PredicateComparator());

        //Loop triples
        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            if (statement.getSubject().toString().equals(subject.toString())) {
                sortedMap.put(statement.getPredicate(), statement.getObject());
            }
        }

        return sortedMap;
    }

    /**
     * To merge predicates of same instance and set properties of objects
     *
     * @param spoContainer contains predicates, objects and object properties of one subject
     * @return merged spoContainer
     */
    public SPOContainer mergeObjectsOfOneInstance(SPOContainer spoContainer, Model model) {
        boolean checkNext;
        int k, index;
        String namespace;
        List<ObjectProperties> listOfPropPredicates = new ArrayList<ObjectProperties>();
        TreeMap<Property, RDFNode> newPropertiesMap = new TreeMap<Property, RDFNode>(new PredicateComparator());
        ObjectProperties predicateProperties;
        TreeMap<Property, RDFNode> properties = spoContainer.getMap();
        List<Property> keys = new ArrayList<Property>(properties.keySet());
        List<RDFNode> objects = new ArrayList<RDFNode>(properties.values());

        //Loop properties
        for (int i = 0; i < keys.size(); i++) {

            //Set property of objects
            if (objects.get(i).toString().contains("^^")) {
                index = objects.get(i).toString().indexOf("^^");
                objects.set(i, model.createLiteral((String) objects.get(i).toString().subSequence(0, index)));
            }

            if (keys.get(i).toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                predicateProperties = new ObjectProperties();
                predicateProperties.setClass(true);
            } else if (isInteger(objects.get(i))) {
                predicateProperties = definePredicateAsInteger(objects.get(i).toString());
            } else if (isDouble(objects.get(i))) {
                predicateProperties = definePredicateAsDouble(objects.get(i).toString());
            } else if ((namespace = getNamespace(objects.get(i), model)) != null) {
                predicateProperties = definePredicateAsLink(namespace);
            } else {
                predicateProperties = definePredicateAsString();
            }

            checkNext = true;
            k = 1;

            //To merge same properties
            while (checkNext && i + k < keys.size()) {
                if (!keys.get(i).toString().equals(keys.get(i + k).toString())) {
                    checkNext = false;
                } else {

                    //Set properties of object for same predicates
                    if (objects.get(i + k).toString().contains("^^")) {
                        index = objects.get(i + k).toString().indexOf("^^");
                        objects.set(i + k, model.createLiteral((String) objects.get(i + k).toString().subSequence(0, index)));
                    }

                    if (predicateProperties.isClass()) {
                        break;
                    }
                    if (predicateProperties.isInteger() && isInteger(objects.get(i + k))) {
                        predicateProperties = mergePredicatesAsInteger(predicateProperties, objects.get(i + k).toString());
                    } else if (predicateProperties.isDouble() && isDouble(objects.get(i + k))) {
                        predicateProperties = mergePredicatesAsDouble(predicateProperties, objects.get(i + k).toString());
                    } else if (getNamespace(objects.get(i), model) != null && getNamespace(objects.get(i + k), model) != null) {
                        namespace = predicateProperties.getLinkValue();
                        String namespaceSecond = getNamespace(objects.get(i + k), model);
                        predicateProperties.setCount(predicateProperties.getCount() + 1);
                        if (namespace.equals(namespaceSecond)) {
                            predicateProperties.setLinkValue(namespace);
                            predicateProperties.setLink(true);
                        } else {
                            predicateProperties.setLinkValue("different");
                            predicateProperties.setLink(true);
                        }
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
     * To get namespace of object
     *
     * @param object
     * @param model
     * @return null - not contain namespace, else namespace as string
     */
    private String getNamespace(RDFNode object, Model model) {
        List<String> namespaces = new ArrayList<String>(model.getNsPrefixMap().values());
        for (String prefix : namespaces) {
            if (object.toString().contains(prefix)) {
                return prefix;
            }
        }
        return null;
    }


    /**
     * To define predicate as Integer
     *
     * @param value String value of object
     * @return
     */
    private ObjectProperties definePredicateAsInteger(String value) {
        ObjectProperties properties = new ObjectProperties();
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
    private ObjectProperties definePredicateAsDouble(String value) {
        ObjectProperties properties = new ObjectProperties();
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
    private ObjectProperties definePredicateAsString() {
        ObjectProperties properties = new ObjectProperties();
        properties.setString(true);
        properties.setCount(1);
        return properties;
    }

    /**
     * To define predicate as link
     *
     * @param namespace
     * @return
     */
    private ObjectProperties definePredicateAsLink(String namespace) {
        ObjectProperties properties = new ObjectProperties();
        properties.setLink(true);
        properties.setLinkValue(namespace);
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
    private ObjectProperties mergePredicatesAsInteger(ObjectProperties predicateProperties, String value) {
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
    private ObjectProperties mergePredicatesAsDouble(ObjectProperties predicateProperties, String value) {
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

    /**
     * To merge object properties as Integer of same predicates
     *
     * @param first  properties of first object
     * @param second properties of second object
     * @return merged properties
     */
    private ObjectProperties mergePredicatePropertiesAsInteger(ObjectProperties first, ObjectProperties second) {
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

    /**
     * To merge object properties as Double of same predicates
     *
     * @param first  properties of first object
     * @param second properties of second object
     * @return merged properties
     */
    private ObjectProperties mergePredicatePropertiesAsDouble(ObjectProperties first, ObjectProperties second) {
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
     * Merge object properties as String with already defined predicate as String
     *
     * @param predicateProperties predicate properties
     * @return
     */
    private ObjectProperties mergePredicatesAsString(ObjectProperties predicateProperties) {
        predicateProperties.setString(true);
        predicateProperties.setCount(predicateProperties.getCount() + 1);
        return predicateProperties;
    }

    /**
     * To get key of map by value
     *
     * @param map
     * @param value
     * @return null - value not found in map, else return key as string
     */
    private String getMapKeyByValue(Map<String, String> map, String value) {
        for (String key : map.keySet()) {
            if (value.equals(map.get(key))) {
                return key;
            }
        }
        return null;
    }

    /**
     * Comparator for sorting TreeMap (alphabetical ordering by value of predicate)
     */
    private class PredicateComparator implements Comparator<Property> {
        @Override
        public int compare(Property p1, Property p2) {
            if (p2.toString().compareTo(p1.toString()) > 0) {
                return 1;
            }
            return -1;
        }
    }

}
