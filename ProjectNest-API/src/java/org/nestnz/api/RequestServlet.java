/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, August 2016
 **********************************************************/
package org.nestnz.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import java.io.FileInputStream;
import java.sql.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(RequestServlet.class.getName());
    
    /**
     * Attempt to connect to the database on servlet creation
     * @param config for super
     * @throws ServletException 
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        // Get the servlet context so we can get the file-path of the db config file.
        // TODO: Merge these into a single config file
        ServletContext sc = this.getServletContext();
        propPath = sc.getRealPath("/WEB-INF/dbconfig.properties");
        datasetsPath = sc.getRealPath("/WEB-INF/datasets.properties");

        LOG.log(Level.INFO, "Initializing RequestServlet @{0}", sc.getContextPath());
        
        // Attempt the initial connection to the database.
        try {
            Common.getNestDS(propPath);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Unable to get a datasource db connection", ex);
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
            LOG.log(Level.WARNING, "Unable to close datasource object", ex);
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
        
        LOG.log(Level.INFO, "Received incoming request:\n{0}\n", request.toString());
        String dirtySQL;
        
        // Parse out the requested entity type and id from the request URL
        // Seems the java regex matcher isn't fully PCRE compliant? We'll have to strip slashes manually

        Matcher m = Pattern.compile(Common.URLENTITY_REGEX).matcher(request.getPathInfo().toLowerCase());
        String requestEntity = m.find() ? m.group().substring(1) : "";
        String requestEntityID = m.find() ? m.group().substring(1) : "";

        // Retrieve the SQL query string mapped to the requested entity's GET method
        try {
            dirtySQL = getSQLQuery(requestEntity, "GET");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Unable to load dataset mappings!\n{0}\nReturning 500...\n{1}", 
                    new Object[] {ex.toString(), response.toString()});
            response.setHeader("Error", ex.getMessage());            
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Check that the request target is mapped and valid
        if (dirtySQL == null) {
            LOG.log(Level.SEVERE, "Unable to locate requested dataset!\n{0}\nReturning 400...\n{1}", 
                    new Object[] {requestEntity, response.toString()});
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
       
        // Check for a "Session-Token" header with regex validation
        final String sessionToken = request.getHeader("Session-Token");
        if (sessionToken == null) {
            LOG.log(Level.WARNING, "No session token supplied!\nReturning 403...\n{0}", response.toString());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } else if (!sessionToken.matches(Common.UUID_REGEX)) {
            LOG.log(Level.WARNING, "Session token is not a valid token format!\nReturning 400...\n{0}", response.toString());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Build a map of typed parameters which appear in the retrieved query i.e. regex matches for #string:session-token# etc.
        // ParamOrder maintains insert positions as we dynamically bind our parameters later from the unordered map. 
        Map<String, String> datasetParams = new HashMap<>();
        List<String> datasetParamOrder = new ArrayList<>();
        Common.parseDatasetParameters(dirtySQL, datasetParams, datasetParamOrder);
        LOG.log(Level.INFO, "Dataset accepts the following parameters:\n{0}", datasetParams.toString());

        // Fill the datasetParams map with values if they are provided in the request
        // Note if a query string parameter has multiple mappings, its undefined behaviour as to which one will be used.
        Enumeration<String> parameterNames = request.getParameterNames();
        LOG.log(Level.INFO, "Request supplies the following parameters:\n{0}", parameterNames.toString());
        while (parameterNames.hasMoreElements()) {
            final String paramName = (String) parameterNames.nextElement();
            if (datasetParams.containsKey(paramName)) {
                datasetParams.put(paramName, request.getParameter(paramName));
            }
        }
        // Add the session token as a parameter (we've already validated it above)
        LOG.log(Level.INFO, "Using session token from header: {0}", sessionToken);
        datasetParams.put("session-token", sessionToken);
        // If a subroute was provided specifying the entity id, use it
        if (!requestEntityID.isEmpty()) {
            LOG.log(Level.INFO, "Using entity id from URL path: {0}", requestEntityID);
            datasetParams.put("id", requestEntityID);
        }

        // Replace the placeholders in the retrieved SQL with the values supplied by the request
        final String cleanSQL = dirtySQL.replaceAll(Common.DATASETPARAM_REGEX, "?");
        LOG.log(Level.INFO, "Using SQL template from dataset '{0}':\n{1}", new Object[] {requestEntity, cleanSQL});

        // Get the DB response and convert it to JSON
        String jsonArray;
        try (
            Connection conn = Common.getNestDS(propPath).getConnection();
            PreparedStatement st = conn.prepareStatement(cleanSQL);
        ) {
            // Bind all of the parameters to their placeholders
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            try (ResultSet rsh = st.executeQuery();) {
                if (rsh.isBeforeFirst()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    jsonArray = Common.resultSetAsJSON(rsh);
                    LOG.log(Level.INFO, "ResultSet retrieved from database!");
                } else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    jsonArray = "[]";
                    LOG.log(Level.INFO, "Empty ResultSet received from database");
                }
            }

        } catch (ParseException | NumberFormatException ex){
            // Error is written to log lower down the stack so parameter values can be logged.
            LOG.log(Level.INFO, "Parse error while casting expected types:\n{0}\nReturning 400...\n{1}", 
                    new Object[]{ex.getMessage(), response.toString()});
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (IOException | SQLException ex) {
            LOG.log(Level.INFO, "IO/SQL error while casting to types expected by dataset:\n{0}\n{1}\nReturning 500...", 
                    new Object[]{ex.getMessage(), response.toString()});
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Return the JSON array to the user
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonArray);
        }
        LOG.log(Level.INFO, "Request completed successfully! Returning 20X...\n{0}", response.toString());
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
        LOG.log(Level.INFO, "Received incoming request:\n{0}\n", request.toString());
        String dirtySQL;

        // Parse out the requested entity type and id from the request URL
        // Seems the java regex matcher isn't fully PCRE compliant? We'll have to strip slashes manually

        Matcher m = Pattern.compile(Common.URLENTITY_REGEX).matcher(request.getPathInfo().toLowerCase());
        String requestEntity = m.find() ? m.group().substring(1) : "";
        String requestEntityID = m.find() ? m.group().substring(1) : "";

        // Check that an entity id has not been supplied
        if (!requestEntityID.equals("")) {
            LOG.log(Level.INFO, "Bad request syntax: Entity id supplied to POST request: {0}", requestEntityID);        
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Retrieve the SQL query string mapped to the requested entity's GET method
        try {
            dirtySQL = getSQLQuery(requestEntity, "POST");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Unable to load dataset mappings!\n{0}\nReturning 500...\n{1}", 
                    new Object[] {ex.toString(), response.toString()});
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Check that the request target is mapped and valid
        if (dirtySQL == null) {
            LOG.log(Level.SEVERE, "Unable to locate requested dataset!\n{0}\nReturning 400...\n{1}", 
                    new Object[] {requestEntity, response.toString()});
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Check for a "Session-Token" header with regex validation
        final String sessionToken = request.getHeader("Session-Token");
        if (sessionToken == null) {
            LOG.log(Level.WARNING, "No session token supplied!\nReturning 403...\n{0}", response.toString());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } else if (!sessionToken.matches(Common.UUID_REGEX)) {
            LOG.log(Level.WARNING, "Session token is not a valid token format!\nReturning 400...\n{0}", response.toString());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Build a map of typed parameters which appear in the retrieved query i.e. regex matches for #string:session-token# etc.
        // ParamOrder maintains insert positions as we dynamically bind our parameters later from the unordered map. 
        Map<String, String> datasetParams = new HashMap<>();
        List<String> datasetParamOrder = new ArrayList<>();
        Common.parseDatasetParameters(dirtySQL, datasetParams, datasetParamOrder);
        LOG.log(Level.INFO, "Dataset accepts the following parameters:\n{0}", datasetParams.toString());

        // If the json object provided by the request is unparsable, a 400 bad request is returned.
        String requestJSON = null;
        Map<String,String> requestObjectParams;
        try {
            // TODO: uncomment this and change to pattern.find()
//            if (!request.getContentType().matches("application/json")) {
//                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
//                return;
//            }
            // Parse the json object with gson
            requestJSON = Common.BufferedReaderToString(request.getReader());
            Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
            requestObjectParams = new Gson().fromJson(requestJSON, stringStringMap);
            LOG.log(Level.INFO, "Request supplies the following parameters:\n{0}", requestObjectParams.toString());
        } 
        catch (JsonSyntaxException | MalformedJsonException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            LOG.log(Level.WARNING, "Malformed JSON object received:\n{0}\n{1}\nReturning 400...\n{2}", 
                    new Object[]{ex.getMessage(),requestJSON, response.toString()});
            return;
        }

        // Fill the datasetParams map with values if they are provided in the request
        for (Map.Entry<String, String> requestParam : requestObjectParams.entrySet()) {
            final String paramName = requestParam.getKey();
            if (datasetParams.containsKey(paramName)) {
                datasetParams.put(paramName, requestParam.getValue());
            }
        }

        // Add the session token as a parameter (we've already validated it above)
        LOG.log(Level.INFO, "Using session token from header: {0}", sessionToken);
        datasetParams.put("session-token", sessionToken);

        // Replace the placeholders in the retrieved SQL with the values supplied by the request
        final String cleanSQL = dirtySQL.replaceAll(Common.DATASETPARAM_REGEX, "?");
        LOG.log(Level.INFO, "Using SQL template from dataset '{0}':\n{1}", new Object[] {requestEntity, cleanSQL});

        // Get the DB response (new record id) and convert it to a location header to output
        String jsonArray;
        try (
            Connection conn = Common.getNestDS(propPath).getConnection();
            PreparedStatement st = conn.prepareStatement(cleanSQL);
        ) {
            // Bind all of the parameters to their placeholders
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);
            LOG.log(Level.INFO, "Sending SQL query to db server:\n{0}", new Object[] {st.toString()});

            try (ResultSet rsh = st.executeQuery();) {
                if (rsh.isBeforeFirst()) {
                    rsh.next();
                    final String newEntity = "/" + requestEntity + "/" + rsh.getString(1);
                    LOG.log(Level.INFO, "New entity created on server: {0}\nReturning 201...", newEntity);
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.addHeader("Location", newEntity);
                } else {
                    LOG.log(Level.INFO, "Unable to create new {0} on server\nReturning 400...", requestEntity);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
        catch (ParseException | NumberFormatException ex){
            // Error is written to log lower down the stack so parameter values can be logged.
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (IOException | SQLException ex) {
            LOG.log(Level.INFO, "IO/SQL error while casting to types expected by dataset:\n{0}\n{1}\nReturning 500...", 
                    new Object[]{ex.getMessage(), response.toString()});
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        LOG.log(Level.INFO, "Request completed successfully!\n{0}", response.toString());
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
        LOG.log(Level.INFO, "Received incoming request:\n{0}\n", request.toString());

        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        LOG.log(Level.INFO, "Request to not-implemented method PUT\nReturning 501...{0}", response.toString());
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
        LOG.log(Level.INFO, "Received incoming request:\n{0}\n", request.toString());
        
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        LOG.log(Level.INFO, "Request to not-implemented method PUT\nReturning 501...{0}", response.toString());
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
        // Remove the returned quotes as the SQL is a string.
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
