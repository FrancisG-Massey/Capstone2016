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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
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
     * @param httpMethod
     * @throws ServletException
     * @throws IOException 
     */
    protected void doRequest(HttpServletRequest request, HttpServletResponse response, String httpMethod)
            throws ServletException, IOException {
        String dirtySQL_before, dirtySQL_main, dirtySQL_after;
        
        // Parse out the requested entity type and id from the request URL
        // Seems the java regex matcher isn't fully PCRE compliant? We'll have to strip slashes manually

        Matcher m = Pattern.compile(Common.URLENTITY_REGEX).matcher(request.getPathInfo().toLowerCase());
        String requestEntity = m.find() ? m.group().substring(1) : "";
        String requestEntityID = m.find() ? m.group().substring(1) : "";
        
        // Strip the data type extension off the path if there is one
        final String[] requestEntityFull = requestEntity.split("[.]");
        System.out.println(requestEntity.length());
        requestEntity = (requestEntityFull.length > 1) ? requestEntityFull[0] : requestEntity;
        final String requestExt = (requestEntityFull.length > 1) ? requestEntityFull[1] : "";
        System.out.println(requestEntityFull);
        System.out.println(requestEntity);
        System.out.println(requestExt);
        
        LOG.log(Level.INFO, "Received incoming {0} request for {1} Entity\n",
                new Object[]{httpMethod, requestEntity});
        
        if (httpMethod.equals("POST") && !requestEntityID.equals("")) {
            LOG.log(Level.INFO, "Bad request syntax: Entity id supplied in POST request URL: {0}", requestEntityID);        
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (httpMethod.equals("PUT") && requestEntityID.equals("")) {
            LOG.log(Level.INFO, "Bad request syntax: No entity id supplied in PUT request URL");        
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
        if ((sessionToken != null) && !sessionToken.matches(Common.UUID_REGEX)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            LOG.log(Level.WARNING, "Session token is not a valid token format!\nReturning 400...\n{0}", response.toString());
            return;
        }

        // Build a map of typed parameters which appear in the retrieved query i.e. regex matches for #string:session-token# etc.
        // ParamOrder maintains insert positions as we dynamically bind our parameters later from the unordered map. 
        // This isn't great having 3 copies, but ideally we'll clean up later when we refactor the datasets out to a class.
        Map<String, String> datasetParams = new HashMap<>();
        List<String> datasetParamOrder_before = new ArrayList<>();
        List<String> datasetParamOrder_main = new ArrayList<>();
        List<String> datasetParamOrder_after = new ArrayList<>();
        Common.parseDatasetParameters(dirtySQL_before, datasetParams, datasetParamOrder_before);
        Common.parseDatasetParameters(dirtySQL_main, datasetParams, datasetParamOrder_main);
        Common.parseDatasetParameters(dirtySQL_after, datasetParams, datasetParamOrder_after);
        //LOG.log(Level.INFO, "Dataset accepts the following parameters:\n{0}\n{1}\n{2}", 
        //        new Object[]{datasetParams_before.toString(), datasetParams_main.toString(), datasetParams_after.toString()});

        
        
        // Populate the paramter maps
        // If this is a get request, parameters will be in the URL query string
        // If this is a post or put, parameters will be in the JSON content body
        
        if (httpMethod.equals("GET")) {
            // Fill the datasetParams map with values if they are provided in the request
            // Note if a query string parameter has multiple mappings, its undefined behaviour as to which one will be used.
            Enumeration<String> parameterNames = request.getParameterNames();
            //LOG.log(Level.INFO, "Request supplies the following parameters:\n{0}", parameterNames.toString());
            while (parameterNames.hasMoreElements()) {
                final String paramName = (String) parameterNames.nextElement();
                if (datasetParams.containsKey(paramName)) {
                    datasetParams.put(paramName, request.getParameter(paramName));
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
                //LOG.log(Level.INFO, "Request supplies the following parameters:\n{0}", requestObjectParams.toString());
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
        }
        // Add the session token as a parameter (we've already validated its format above)
        datasetParams.put("session-token", sessionToken);
        // Pass the config session expiry into the first dataset to extend the session etc.
        datasetParams.put("session-timeout", String.valueOf(Common.SESSION_TIMEOUT) + " minutes");
        
        // If a subroute was provided specifying the entity id, use it with preference to an id in the body
        if (!requestEntityID.isEmpty()) {
            datasetParams.put("id", requestEntityID);
        }

        // Replace the placeholders in the retrieved SQL with the values supplied by the request
        final String cleanSQL_before = (dirtySQL_before != null) ? dirtySQL_before.replaceAll(Common.DATASETPARAM_REGEX, "?") : null;
        final String cleanSQL_main = (dirtySQL_main != null) ? dirtySQL_main.replaceAll(Common.DATASETPARAM_REGEX, "?") : null;
        final String cleanSQL_after = (dirtySQL_after != null) ? dirtySQL_after.replaceAll(Common.DATASETPARAM_REGEX, "?") : null;

        // Execute the pre-request
        try (
            Connection conn = Common.getNestDS(propPath).getConnection();
        ) {
            
            // Execute the pre-request dataset entry for session handling if one exists.
            if (cleanSQL_before != null) {
                try (
                    PreparedStatement st = conn.prepareStatement(cleanSQL_before, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ) {
                    Common.bindDynamicParameters(st, datasetParams, datasetParamOrder_before);
                    LOG.log(Level.INFO, "Executing query:\n{0}", st.toString());

                    boolean hasResults = st.execute();
                    if (hasResults) {
                        ResultSet rsh = st.getResultSet();
                        if (rsh.isBeforeFirst()) {
                            response.addHeader("Session-Token", sessionToken);
                            
                            // TODO: We can take this further and instead of pulling out specifically named variables,
                            // we can dynamically create variables using the result set meta data column names.
                            boolean hasRow = rsh.next();
                            datasetParams.put("logged-user-id", (hasRow) ? String.valueOf(rsh.getLong("user_id")) : null);
                            datasetParams.put("logged-user-isadmin", (hasRow) ? String.valueOf(rsh.getBoolean("user_isadmin")) : null);
                            datasetParams.put("session-expires", (hasRow) ? rsh.getString("session_expirestimestamp") : null);
                            
                            // Instead of getting the exact time from the db we can compensate for latency somewhat by calculating it here
                            response.addDateHeader("Expires", System.currentTimeMillis() + Common.SESSION_TIMEOUT * 60000L);
                            rsh.last(); LOG.log(Level.INFO, "{0} rows retrieved from database.", rsh.getRow());
                        } else {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN);
                            LOG.log(Level.INFO, "Unable to find session");
                            return;
                        }
                    }
                    st.close();
                }
            }
            
            // Execute the main request dataset entry if one exists.
            String responseBody = null;
            boolean formatCSV = false;
            LOG.log(Level.INFO, "Sending request with the following parameters:\n1: {0}", datasetParams);
            if (cleanSQL_main != null) {
                try (
                    PreparedStatement st = conn.prepareStatement(cleanSQL_main, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ) {
                    Common.bindDynamicParameters(st, datasetParams, datasetParamOrder_main);
                    LOG.log(Level.INFO, "Executing query:\n{0}", st.toString());

                    boolean hasResults = st.execute();
                    if (hasResults) {
                        ResultSet rsh = st.getResultSet();
                        switch (httpMethod) {
                            case "GET":
                                if (rsh.isBeforeFirst()) {
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    if (requestExt.toLowerCase().equals("csv")) {
                                        String timeid = String.valueOf(Calendar.getInstance().getTimeInMillis());
                                        final String filename = "\"" + requestEntity + "_" + timeid + ".csv\"";
                                        response.setHeader("Content-Description","File Transfer");
                                        response.setHeader("Content-Type","application/octet-stream");
                                        response.setHeader("Content-disposition","attachment; filename=" + filename);
                                        response.setHeader("Content-Transfer-Encoding","binary");
                                        responseBody = Common.resultSetAsCSV(rsh);
                                    } else {
                                        response.setContentType("application/json");
                                        responseBody = Common.resultSetAsJSON(rsh);
                                    }
                                    rsh.last(); LOG.log(Level.INFO, "{0} rows retrieved from database.", rsh.getRow());
                                } else {
                                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                    responseBody = (formatCSV) ? "" : "[]";
                                    LOG.log(Level.INFO, "Empty ResultSet received from database");
                                }

                                response.setContentLength(responseBody.length());
                                try (PrintWriter out = response.getWriter()) {
                                    out.print(responseBody);
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
                                if (rsh.isBeforeFirst()) {
                                    // Success. Generate a location header for the client to find the (possibly moved) resource
                                    rsh.next();
                                    final String newEntity = "/" + requestEntity + "/" + rsh.getString(1);
                                    LOG.log(Level.INFO, "Entity modified on server: {0}", newEntity);
                                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                    response.addHeader("Location", newEntity);
                                } else {
                                    LOG.log(Level.INFO, "Unable to modify specified {0} on server.", requestEntity);
                                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                                }
                                break;

                            case "DELETE":
                                if (rsh.isBeforeFirst()) {
                                    rsh.next();
                                    final String newEntity = "/" + requestEntity + "/" + rsh.getString(1);
                                    LOG.log(Level.INFO, "Entity removed from server: {0}", newEntity);
                                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                } else {
                                    LOG.log(Level.INFO, "Unable to delete specified {0} on server.", requestEntity);
                                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                                }
                                break;
                        }
                    }
                    st.close();
                }
            }
            
            // Execute the post-request dataset entry if one exists
            if (cleanSQL_after != null) {
                try (
                    PreparedStatement st = conn.prepareStatement(cleanSQL_after, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ) {
                    Common.bindDynamicParameters(st, datasetParams, datasetParamOrder_after);
                    LOG.log(Level.INFO, "Executing query:\n{0}", st.toString());
                    st.execute();
                    st.close();
                }
            }
            
        } catch (DateTimeParseException | ParseException | NumberFormatException ex){
            // Error is written to log lower down the stack so parameter values can be logged.
            LOG.log(Level.INFO, "Parse error while casting expected types:\n{0}\nReturning 400...\n{1}", 
                    new Object[]{ex.getMessage(), response.toString()});
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (SQLException ex) {
            LOG.log(Level.INFO, "SQL error while executing queries:\n{0}\n{1}Returning 400...", 
                    new Object[]{ex.getMessage(), response.toString()});
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (IOException ex) {
            LOG.log(Level.INFO, "IO error while executing queries:\n{0}\n{1}\nReturning 500...", 
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
