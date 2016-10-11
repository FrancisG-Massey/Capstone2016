/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, August 2016
 **********************************************************/
package org.nestnz.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
     * Handles HTTP Requests by routing to through datasets and the db.
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    protected void doRequest(HttpServletRequest request, HttpServletResponse response, String httpMethod)
            throws ServletException, IOException {
        LOG.log(Level.INFO, "Received incoming request:\n{0}\n", request.toString());

        String dirtySQL_before, dirtySQL_main, dirtySQL_after;
        
        // Parse out the requested entity type and id from the request URL
        // Seems the java regex matcher isn't fully PCRE compliant? We'll have to strip slashes manually

        Matcher m = Pattern.compile(Common.URLENTITY_REGEX).matcher(request.getPathInfo().toLowerCase());
        String requestEntity = m.find() ? m.group().substring(1) : "";
        String requestEntityID = m.find() ? m.group().substring(1) : "";
        
        if (httpMethod.equals("POST") && !requestEntityID.equals("")) {
            LOG.log(Level.INFO, "Bad request syntax: Entity id supplied to POST request: {0}", requestEntityID);        
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Retrieve the SQL query string mapped to the requested entity's procedure for the method.
        try {
            dirtySQL_before = getSQLQuery(requestEntity, "before");
            dirtySQL_main = getSQLQuery(requestEntity, httpMethod);
            dirtySQL_after = getSQLQuery(requestEntity, "after");
        } catch (IOException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LOG.log(Level.SEVERE, "Unable to load dataset mappings!\n{0}\nReturning 500...\n{1}", 
                    new Object[] {ex.toString(), response.toString()});
            return;
        }

        // Check that the request target is mapped and valid
        if (dirtySQL_main == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            LOG.log(Level.SEVERE, "Unable to locate requested dataset!\n{0}\nReturning 400...\n{1}", 
                    new Object[] {requestEntity, response.toString()});
            return;
        }

        // Check for a "Session-Token" header with regex validation
        final String sessionToken = request.getHeader("Session-Token");
        if (sessionToken == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            LOG.log(Level.WARNING, "No session token supplied!\nReturning 403...\n{0}", response.toString());
            return;
        } else if (!sessionToken.matches(Common.UUID_REGEX)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            LOG.log(Level.WARNING, "Session token is not a valid token format!\nReturning 400...\n{0}", response.toString());
            return;
        }

        // Build a map of typed parameters which appear in the retrieved query i.e. regex matches for #string:session-token# etc.
        // ParamOrder maintains insert positions as we dynamically bind our parameters later from the unordered map. 
        // This isn't great having 3 copies, but ideally we'll clean up later when we refactor the datasets out to a class.
        Map<String, String> datasetParams_before = new HashMap<>();
        Map<String, String> datasetParams_main = new HashMap<>();
        Map<String, String> datasetParams_after = new HashMap<>();
        List<String> datasetParamOrder_before = new ArrayList<>();
        List<String> datasetParamOrder_main = new ArrayList<>();
        List<String> datasetParamOrder_after = new ArrayList<>();
        Common.parseDatasetParameters(dirtySQL_before, datasetParams_before, datasetParamOrder_before);
        Common.parseDatasetParameters(dirtySQL_main, datasetParams_main, datasetParamOrder_main);
        Common.parseDatasetParameters(dirtySQL_after, datasetParams_after, datasetParamOrder_after);
        LOG.log(Level.INFO, "Dataset accepts the following parameters:\n{0}\n{1}\n{2}", 
                new Object[]{datasetParams_before.toString(), datasetParams_main.toString(), datasetParams_after.toString()});

        
        
        // Populate the paramter maps
        // If this is a get request, parameters will be in the URL query string
        // If this is a post or put, parameters will be in the JSON content body
        
        if (httpMethod.equals("GET")) {
            // Fill the datasetParams map with values if they are provided in the request
            // Note if a query string parameter has multiple mappings, its undefined behaviour as to which one will be used.
            Enumeration<String> parameterNames = request.getParameterNames();
            LOG.log(Level.INFO, "Request supplies the following parameters:\n{0}", parameterNames.toString());
            while (parameterNames.hasMoreElements()) {
                final String paramName = (String) parameterNames.nextElement();
                if (datasetParams_before.containsKey(paramName)) {
                    datasetParams_before.put(paramName, request.getParameter(paramName));
                }
                if (datasetParams_main.containsKey(paramName)) {
                    datasetParams_main.put(paramName, request.getParameter(paramName));
                }
                if (datasetParams_after.containsKey(paramName)) {
                    datasetParams_after.put(paramName, request.getParameter(paramName));
                }
            }
        }
        else if (httpMethod.equals("POST") || httpMethod.equals("PUT")) {
            String requestJSON = null;
            Map<String, String> requestObjectParams;
            try {
                // TODO: check for Content-Type: application/json with regex
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
                if (datasetParams_before.containsKey(paramName)) {
                    datasetParams_before.put(paramName, requestParam.getValue());
                }
                if (datasetParams_main.containsKey(paramName)) {
                    datasetParams_main.put(paramName, requestParam.getValue());
                }
                if (datasetParams_after.containsKey(paramName)) {
                    datasetParams_after.put(paramName, requestParam.getValue());
                }
            }
        }
        // Add the session token as a parameter (we've already validated its format above)
        LOG.log(Level.INFO, "Using session token from header: {0}", sessionToken);
        datasetParams_before.put("session-token", sessionToken);
        datasetParams_main.put("session-token", sessionToken);
        datasetParams_after.put("session-token", sessionToken);
        
        // If a subroute was provided specifying the entity id, use it with preference to an id in the body
        if (!requestEntityID.isEmpty()) {
            LOG.log(Level.INFO, "Using entity id from URL path: {0}", requestEntityID);
            datasetParams_before.put("id", requestEntityID);
            datasetParams_main.put("id", requestEntityID);
            datasetParams_after.put("id", requestEntityID);
        }

        // Replace the placeholders in the retrieved SQL with the values supplied by the request
        final String cleanSQL_before = (dirtySQL_before != null) ? dirtySQL_before.replaceAll(Common.DATASETPARAM_REGEX, "?") : null;
        final String cleanSQL_main = (dirtySQL_main != null) ? dirtySQL_main.replaceAll(Common.DATASETPARAM_REGEX, "?") : null;
        final String cleanSQL_after = (dirtySQL_after != null) ? dirtySQL_after.replaceAll(Common.DATASETPARAM_REGEX, "?") : null;

        // Get the DB response and convert it to JSON
        String jsonArray = null;
        try (
            Connection conn = Common.getNestDS(propPath).getConnection();
        ) {
            
            // Execute the pre-request dataset entry for session handling if one exists
            if (cleanSQL_before != null) {
                try (
                    PreparedStatement st = conn.prepareStatement(cleanSQL_before);
                ) {
                    // Pass the config session expiry into the dataset in case it wants to extend a session etc.
                    datasetParams_before.put("session-timeout", String.valueOf(Common.SESSION_TIMEOUT) + " minutes");
                    Common.bindDynamicParameters(st, datasetParams_before, datasetParamOrder_before);

                    try (ResultSet rsh = st.executeQuery();) {
                        if (rsh.isBeforeFirst()) {
                            response.addHeader("Session-Token", sessionToken);
                            // Instead of getting the exact time from the db we can compensate for latency somewhat by calculating it here
                            response.addDateHeader("Expires", System.currentTimeMillis() + Common.SESSION_TIMEOUT * 60000L);
                            LOG.log(Level.INFO, "ResultSet retrieved from database!");
                        } else {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN);
                            LOG.log(Level.INFO, "Unable to find session");
                            return;
                        }
                    }
                    st.close();
                }
            }
            
            // Execute the main request
            if (cleanSQL_main != null) {
                try (
                    PreparedStatement st = conn.prepareStatement(cleanSQL_main);
                ) {
                    Common.bindDynamicParameters(st, datasetParams_main, datasetParamOrder_main);

                    try (ResultSet rsh = st.executeQuery();) {
                        switch (httpMethod) {
                            case "GET":
                                if (rsh.isBeforeFirst()) {
                                   response.setStatus(HttpServletResponse.SC_OK);
                                    jsonArray = Common.resultSetAsJSON(rsh);
                                    LOG.log(Level.INFO, "ResultSet retrieved from database!");
                                } else {
                                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                    jsonArray = "[]";
                                    LOG.log(Level.INFO, "Empty ResultSet received from database");
                                }
                                response.setContentType("application/json");
                                response.setContentLength(jsonArray.length());
                                try (PrintWriter out = response.getWriter()) {
                                    out.print(jsonArray);
                                }
                                break;
                                
                            case "POST":
                                if (rsh.isBeforeFirst()) {
                                    // Success. Generate a location header for the client to find the new resource
                                    rsh.next();
                                    final String newEntity = "/" + requestEntity + "/" + rsh.getString(1);
                                    LOG.log(Level.INFO, "New entity created on server: {0}", newEntity);
                                    response.setStatus(HttpServletResponse.SC_CREATED);
                                    response.addHeader("Location", newEntity);
                                } else {
                                    LOG.log(Level.INFO, "Unable to create new {0} on server.", requestEntity);
                                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                                }
                                break;
                                
                            case "PUT":
                                break;
                                
                            case "DELETE":
                                break;
                        }
                    }
                    st.close();
                }
            }
            
            // Execute the post-request dataset entry if one exists
            if (cleanSQL_after != null) {
                try (
                    PreparedStatement st = conn.prepareStatement(cleanSQL_after);
                ) {
                    Common.bindDynamicParameters(st, datasetParams_after, datasetParamOrder_after);
                    st.execute();
                    st.close();
                }
            }
            
        } catch (ParseException | NumberFormatException ex){
            // Error is written to log lower down the stack so parameter values can be logged.
            LOG.log(Level.INFO, "Parse error while casting expected types:\n{0}\nReturning 400...\n{1}", 
                    new Object[]{ex.getMessage(), response.toString()});
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (IOException | SQLException ex) {
            LOG.log(Level.INFO, "IO/SQL error while executing queries:\n{0}\n{1}\nReturning 500...", 
                    new Object[]{ex.getMessage(), response.toString()});
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        LOG.log(Level.INFO, "Request completed successfully! Returning...\n{0}", response.toString());
    }
    
    
    
    /**
     * Processes requests for HTTP GET methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        doRequest(request, response, "GET");
    }

    /**
     * Handles the HTTP POST method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doRequest(request, response, "POST");
    }

    /**
     * Handles the HTTP PUT method.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        doRequest(request, response, "PUT");
    }
    
    /**
     * Handles the HTTP DELETE method.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        doRequest(request, response, "DELETE");
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
        JsonElement jsonSqlString = jObject.get(method);
        if (jsonSqlString == null){
            return null;
        }
        String dirtySQL = jsonSqlString.toString();
        // Remove the returned quotes as the SQL is a string.
        dirtySQL = dirtySQL.substring(1, dirtySQL.length()-1);
        final String cleanSQL = dirtySQL.replace("\\r\\n", "\n").replace("\\n", "\n");
        return cleanSQL;
    }


    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Dynamic request handler servlet for the Nest API";
    }// </editor-fold>
}
