/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, August 2016
 **********************************************************/
package org.nestnz.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileInputStream;
import java.sql.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sam
 */
public class RequestServlet extends HttpServlet {
    private String propPath = null, datasetsPath = null;
    
    /**
     * Attempt to connect to the database on servlet creation
     * @param config for super
     * @throws ServletException 
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        // Get the servlet context so we can get the file-path of the db config file.
        ServletContext sc = this.getServletContext();
        propPath = sc.getRealPath("/WEB-INF/dbconfig.properties");
        datasetsPath = sc.getRealPath("/WEB-INF/datasets.properties");
        
        // TODO: Merge these into a single config file
        
        // Attempt the initial connection to the database.
        try {
            Common.getNestDS(propPath);
        } catch (IOException ex) {
            // TODO: Log ex
        }
    }
    
    /**
     *  Attempt to close the db connection when the session manager terminates
     */
    @Override
    public void destroy() {
        try {
            Common.closeNestDS();
        } catch (SQLException ex) {
            // TODO: Log ex
        }
    }
    
    /**
     * Processes requests for HTTP GET methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dirtySQL;
        
        // Parse out the requested entity type and id from the request URL
        // Seems the java regex matcher isn't fully PCRE compliant? We'll have to strip slashes manually
        
        //Matcher m = Pattern.compile("/^\\/(?>([a-z][a-z-_]*))(?>\\/(\\d+))?/i").matcher(request.getPathInfo());
        Matcher m = Pattern.compile("\\/([\\w-]*)").matcher(request.getPathInfo().toLowerCase());
        String requestEntity = m.find() ? m.group().substring(1) : "";
        String requestEntityID = m.find() ? m.group().substring(1) : "";
        
        // Retrieve the SQL query string mapped to the requested entity's GET method
        try {
            dirtySQL = getSQLQuery(requestEntity, "GET");
        } catch (IOException ex) {
            // TODO: Log ex
            response.setHeader("Error", ex.getMessage());            
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // Check that the request target is mapped and valid
        if (dirtySQL == null) {
            // TODO: Log ex
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
       
        // Check for a "Session-Token" header with regex validation
        final String sessionToken = request.getHeader("Session-Token");
        final String uuidRegex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
        if (sessionToken == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } else if (!sessionToken.matches(uuidRegex)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Build a map of parameters which appear in the retrieved query i.e. regex matches for #session-token# etc.
        // ParamOrder will be used when we dynamically bind our parameters later. 
        // FYI, Its not going to be pretty using java's static typing...
        List<String> datasetParamOrder = new ArrayList<>();
        Map<String, String> datasetParams = new HashMap<>();
        m = Pattern.compile("#([a-z-_][\\w-]*)#").matcher(dirtySQL.toLowerCase());
        while (m.find()) {
            datasetParamOrder.add(m.group());
            datasetParams.put(m.group(), null);
        }
        
        // Fill the datasetParams map with values if they are provided in the request
        // Note if a query string parameter has multiple mappings, its undefined behaviour as to which one will be used.
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String paramName = (String) parameterNames.nextElement();
            if (datasetParams.containsKey(paramName)) {
                datasetParams.put(paramName, request.getParameter(paramName));
            }
        }
        // Add the session token as a parameter (we've already validated it above)
        datasetParams.put("session-token", sessionToken);
        // If a subroute was provided specifying the entity id, use it
        if (!requestEntityID.isEmpty()) {
            datasetParams.put("id", requestEntityID);
        }
        
        // Replace the placeholders in the retrieved SQL with the values supplied by the request
        // Temporary measure until we get binding going on.
        for (Map.Entry<String, String> entry : datasetParams.entrySet()) {
            dirtySQL = dirtySQL.replaceAll("#" + entry.getKey() + "#", "'" + entry.getValue() + "'");
        }
        String cleanSQL = dirtySQL;
        
        // Get the DB response and convert it to JSON
        String jsonArray = null;
        try (
            Connection conn = Common.getNestDS(propPath).getConnection();
            PreparedStatement st = conn.prepareStatement(cleanSQL);
            ResultSet rsh = st.executeQuery();
        ) {
            if (rsh.isBeforeFirst()) {
                response.setStatus(HttpServletResponse.SC_OK);
                jsonArray = Common.resultSetAsJSON(rsh);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonArray = "[]";
            }
        } catch (IOException | SQLException ex) {
            // TODO: Log ex
            response.setHeader("Error", ex.getMessage());            
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
                
        // Return the JSON array to the user
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonArray);
        }
    }

    /**
     * Handles the HTTP POST method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    /**
     * Handles the HTTP PUT method.
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
    
    /**
     * Handles the HTTP DELETE method.
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
    
    /**
     * Gets the SQL query for a named method in a named dataset.
     * @param dataset The name of the dataset in the config file
     * @param method The name of the method in the named dataset JSON file
     * @return the sql query string (with markers for named parameter injection)
     * @throws IOException 
     */
    private String getSQLQuery(String dataset, String method) throws IOException{

        // Load the list of dataset mappings from the config file 
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(datasetsPath)) {
            prop.load(input);
        }
        
        // Return null if no such dataset exists
        if (prop.getProperty(dataset) == null) {
            return null;
        }

        // Get the dataset json string from file
        final String datasetPath = this.getServletContext().getRealPath(prop.getProperty(dataset));
        byte[] encoded = Files.readAllBytes(Paths.get(datasetPath));
        final String datasetJSON = new String(encoded, StandardCharsets.UTF_8);
        
        // Decode from JSON clean enclosing quotes and newlines then return
        JsonObject jObject = new JsonParser().parse(datasetJSON).getAsJsonObject();
        String dirtyJSON = jObject.get(method).toString();
        dirtyJSON = dirtyJSON.substring(1, dirtyJSON.length()-1);
        final String cleanJSON = dirtyJSON.replace("\\r\\n", "\n").replace("\\n", "\n");
        return cleanJSON;
    }
    
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Dynamic request handler servlet for the Nest API";
    }// </editor-fold>

}
