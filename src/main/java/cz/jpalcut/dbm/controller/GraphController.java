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
     * Display content of default Turtle file
     *
     * @param fileID
     * @return ModelAndView - default_file (VIEW), fileID, fileContent (default Turtle file content)
     */
    @RequestMapping(value = "/{id}/defaultFile/{modelType}", method = RequestMethod.GET)
    public ModelAndView viewDefaultFile(@PathVariable("id") String fileID, @PathVariable("modelType") String modelType) {

        ModelAndView model = new ModelAndView();

        if (!EnumUtils.isValidEnum(Enum.RDFModelType.class, modelType)) {
            model.addObject("error", 404);
            model.setViewName("error");
            return model;
        }

        String realModelType = Enum.RDFModelType.valueOf(modelType).toString();
        String ttlPath = servletContext.getRealPath("/Public/ttl/");
        FileManager.get().addLocatorClassLoader(FileUploadController.class.getClassLoader());

        String modelContent = null;

        try {
            Model loadModel = FileManager.get().loadModel(ttlPath + fileID + "-default.ttl");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            loadModel.write(os, realModelType);
            modelContent = new String(os.toByteArray(), "UTF-8");
        } catch (Exception e) {
            model.addObject("error", 404);
            model.setViewName("error");
            return model;
        }

        modelContent = modelContent.replaceAll("<", "&#60;").replaceAll(" ", "&nbsp;").replaceAll("\n", "<br>");

        model.addObject("RDFModelType", modelType);
        model.addObject("RDFModelEnum", Enum.RDFModelType.values());
        model.addObject("fileContent", modelContent);
        model.addObject("fileID", fileID);

        model.setViewName("default_file");
        return model;
    }

    /**
     * Display content of aggregated Turtle file
     *
     * @param fileID
     * @return ModelAndView - aggregated_file (VIEW), fileID, fileContent (aggregated Turtle file content)
     */
    @RequestMapping(value = "/{id}/aggregatedFile", method = RequestMethod.GET)
    public ModelAndView viewAggregatedFile(@PathVariable("id") String fileID) {

        ModelAndView model = new ModelAndView();
        BufferedReader reader;
        StringBuffer stringBuffer = new StringBuffer();

        try {
            String filePathToGraphsDir = servletContext.getRealPath("/Public/ttl/");
            reader = new BufferedReader(new FileReader(filePathToGraphsDir + fileID + "-aggregated.ttl"));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("<", "&#60;").replaceAll(" ", "&nbsp;");
                stringBuffer.append(line).append("<br>");
            }
        } catch (Exception e) {
            model.addObject("error", 404);
            model.setViewName("error");
            return model;
        }

        model.addObject("fileContent", stringBuffer);
        model.addObject("fileID", fileID);

        model.setViewName("aggregated_file");
        return model;
    }

    /**
     * Response N-Triples from aggregated file as JSON
     *
     * @param fileID
     * @return String that contain N-Triples in JSON format
     */
    @RequestMapping(value = "/{id}/graph/getNTriples", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody
    String getNTriples(@PathVariable("id") String fileID) {

        String ttlPath = servletContext.getRealPath("/Public/ttl/");
        FileManager.get().addLocatorClassLoader(FileUploadController.class.getClassLoader());
        Model loadModel;

        try {
            loadModel = FileManager.get().loadModel(ttlPath + fileID + "-default.ttl");
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
        int k = 0;
        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            objectNode = mapper.createObjectNode();
            objectNode.put("subject", statement.getSubject().toString());
            objectNode.put("predicate", statement.getPredicate().toString());
            objectNode.put("object", statement.getObject().toString());
            arrayNode.add(objectNode);
            // TODO: Change to aggregation file and delete this
            k++;
            if (k > 100) {
                break;
            }
        }

        try {
            reader.close();
            return mapper.writeValueAsString(arrayNode);
        } catch (IOException e) {
            return "0";
        }

    }


}
