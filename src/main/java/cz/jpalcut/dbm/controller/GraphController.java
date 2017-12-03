package cz.jpalcut.dbm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.jpalcut.dbm.utils.Enum;
import org.apache.commons.lang3.EnumUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import java.io.*;

@Controller
public class GraphController {

    @Autowired
    ServletContext servletContext;

    /**
     * Display VIEW of aggregated graph
     *
     * @param fileID
     * @return ModelAndView - aggregated graph (VIEW), fileID
     */
    @RequestMapping(value = "/{id}/graph", method = RequestMethod.GET)
    public ModelAndView viewGraph(@PathVariable("id") String fileID) {

        ModelAndView model = new ModelAndView();

        model.addObject("fileID", fileID);
        model.setViewName("graph");
        return model;
    }

    /**
     * Display content of default/aggregated file in different formats
     * @param fileID
     * @param fileStatus ("aggregatedFile","defaultFile")
     * @param modelType (value from Enum.RDFModelType)
     * @return ModelAndView - fileStatus("aggregatedFile" or "defaultFile"), RDFModelType(value from Enum.RDFModelType),
     *                        RDFModelEnum(list of Enum.RDFModelType values), fileID,
     *                        fileContent(data from Model in chosen format),
     */
    @RequestMapping(value = "/{id}/{fileStatus}/{modelType}", method = RequestMethod.GET)
    public ModelAndView viewFileContent(@PathVariable("id") String fileID,
                                        @PathVariable("fileStatus") String fileStatus,
                                        @PathVariable("modelType") String modelType) {

        ModelAndView model = new ModelAndView();

        if (!EnumUtils.isValidEnum(Enum.RDFModelType.class, modelType)) {
            model.addObject("error", 404);
            model.setViewName("error");
            return model;
        }

        if(!fileStatus.equals("defaultFile") && !fileStatus.equals("aggregatedFile")){
            model.addObject("error", 404);
            model.setViewName("error");
            return model;
        }

        String realModelType = Enum.RDFModelType.valueOf(modelType).toString();
        String ttlPath = servletContext.getRealPath("/Public/ttl/");
        FileManager.get().addLocatorClassLoader(FileUploadController.class.getClassLoader());

        String modelContent;

        //To get Model in chosen format from TTL file
        try {
            Model loadModel;
            if(fileStatus.equals("defaultFile")){
                loadModel = FileManager.get().loadModel(ttlPath + fileID + "-default.ttl");
            }
            else {
                loadModel = FileManager.get().loadModel(ttlPath + fileID + "-aggregated.ttl");
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            loadModel.write(os, realModelType);
            modelContent = new String(os.toByteArray(), "UTF-8");
        } catch (Exception e) {
            model.addObject("error", 404);
            model.setViewName("error");
            return model;
        }

        //replace characters
        modelContent = modelContent.replaceAll("<", "&#60;").replaceAll(" ", "&nbsp;").replaceAll("\n", "<br>");

        model.addObject("fileStatus", fileStatus);
        model.addObject("RDFModelType", modelType);
        model.addObject("RDFModelEnum", Enum.RDFModelType.values());
        model.addObject("fileContent", modelContent);
        model.addObject("fileID", fileID);

        model.setViewName("file_content");
        return model;
    }

    /**
     * Response N-Triples from aggregated file as JSON
     *
     * @param fileID
     * @return String that contain N-Triples in JSON format
     */
    @RequestMapping(value = "/{id}/graph/getNTriples", method = RequestMethod.POST,
            produces = "text/plain;charset=UTF-8")
    public @ResponseBody
    String getNTriples(@PathVariable("id") String fileID) {

        String ttlPath = servletContext.getRealPath("/Public/ttl/");
        FileManager.get().addLocatorClassLoader(FileUploadController.class.getClassLoader());
        Model loadModel;

        try {
            loadModel = FileManager.get().loadModel(ttlPath + fileID + "-aggregated.ttl");
        }
        catch (Exception e){
            return "1";
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        loadModel.write(os, "N-Triples");

        BufferedReader reader = new BufferedReader(new StringReader(os.toString()));

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode objectNode = mapper.createObjectNode();

        StmtIterator iterator = loadModel.listStatements();

        //Triples to JSON
        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            objectNode = mapper.createObjectNode();
            objectNode.put("subject", statement.getSubject().toString());
            objectNode.put("predicate", statement.getPredicate().toString());
            objectNode.put("object", statement.getObject().toString());
            arrayNode.add(objectNode);
        }

        try {
            reader.close();
            return mapper.writeValueAsString(arrayNode);
        } catch (IOException e) {
            return "0";
        }

    }


}
