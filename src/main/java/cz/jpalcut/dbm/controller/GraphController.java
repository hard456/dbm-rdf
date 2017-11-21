package cz.jpalcut.dbm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.jena.rdf.model.Model;
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
     * @param fileID
     * @return ModelAndView - aggregated graph (VIEW), fileID
     */
    @RequestMapping(value = "/{id}/graph", method = RequestMethod.GET)
    public ModelAndView viewGraph(@PathVariable("id") String fileID) {

        ModelAndView model = new ModelAndView();

        model.addObject("fileID",fileID);
        model.setViewName("graph");
        return model;
    }

    /**
     * Display content of default Turtle file
     * @param fileID
     * @return ModelAndView - default_file (VIEW), fileID, fileContent (default Turtle file content)
     */
    @RequestMapping(value = "/{id}/defaultFile", method = RequestMethod.GET)
    public ModelAndView viewDefaultFile(@PathVariable("id") String fileID) {

        ModelAndView model = new ModelAndView();
        BufferedReader reader;
        StringBuffer stringBuffer = new StringBuffer();

//        TODO: escaping < and maybe ""

        try {
            String filePathToGraphsDir = servletContext.getRealPath("/Public/ttl/");
            reader = new BufferedReader(new FileReader(filePathToGraphsDir + fileID + "-default.ttl"));

            String line;
            while((line =reader.readLine())!=null){
                line = line.replaceAll("<","&#60;").replaceAll(" ","&nbsp;");
                stringBuffer.append(line).append("<br>");
            }
        } catch (Exception e) {
            model.addObject("error",404);
            model.setViewName("error");
            return model;
        }

        model.addObject("fileContent",stringBuffer);
        model.addObject("fileID",fileID);

        model.setViewName("default_file");
        return model;
    }

    /**
     * Display content of aggregated Turtle file
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
            while((line =reader.readLine())!=null){
                stringBuffer.append(line).append("<br>");
            }
        } catch (Exception e) {
            model.addObject("error",404);
            model.setViewName("error");
            return model;
        }

        model.addObject("fileContent",stringBuffer);
        model.addObject("fileID",fileID);

        model.setViewName("aggregated_file");
        return model;
    }

    @RequestMapping(value = "/{id}/graph/getNTriples", method = RequestMethod.POST)
    public @ResponseBody
    String getNTriples(@PathVariable("id") String fileID) {

        String ttlPath = servletContext.getRealPath("/Public/ttl/");
        FileManager.get().addLocatorClassLoader(FileUploadController.class.getClassLoader());
        Model loadModel = FileManager.get().loadModel(ttlPath + fileID +"-default.ttl");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        loadModel.write(os,"N-Triples");

        BufferedReader reader = new BufferedReader(new StringReader(os.toString()));

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode objectNode = mapper.createObjectNode();

        String line;

        int k = 0;
        try {
            while ((line = reader.readLine()) != null){
                String[] splited = line.split("\\s+");
                objectNode = mapper.createObjectNode();
                objectNode.put("subject",splited[0]);
                objectNode.put("predicate",splited[1]);
                objectNode.put("object",splited[2]);

                // TODO: Change to aggregation file and delete this
                k++;
                if(k<100){
                    arrayNode.add(objectNode);
                }
            }

        } catch (IOException e) {
            return "0";
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
        } catch (JsonProcessingException e) {
            return "0";
        }
    }

}
