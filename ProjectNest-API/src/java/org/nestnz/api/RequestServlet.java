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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        ServletContext sc = this.getServletContext();
        propPath = sc.getRealPath("/WEB-INF/dbconfig.properties");
        datasetsPath = sc.getRealPath("/WEB-INF/datasets.properties");
        
        // TODO: Merge these into a single config file
        
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
            LOG.log(Level.SEVERE, "Unable to load dataset mappings", ex);
            response.setHeader("Error", ex.getMessage());            
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // Check that the request target is mapped and valid
        if (dirtySQL == null) {
            LOG.log(Level.INFO, "Unable to locate requested dataset: {0}", requestEntity);
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
        
        // Build a map of typed parameters which appear in the retrieved query i.e. regex matches for #string:session-token# etc.
        // ParamOrder maintains insert positions as we dynamically bind our parameters later from the unordered map. 
        Map<String, String> datasetParams = new HashMap<>();
        List<String> datasetParamOrder = new ArrayList<>();
        // Find all parameters including their datatypes
        String paramRegex = "#([a-z]+:[a-z-_][\\w-]*)#";
        m = Pattern.compile(paramRegex).matcher(dirtySQL.toLowerCase());
        while (m.find()) {
            final String param = m.group();
            // Discard the datatype in the parameter value map but not in the order list
            // This means we support casting the same value to different types in different places in the dataset if required
            datasetParamOrder.add(param);
            datasetParams.put(param.substring(param.indexOf(":")+1, param.length()-1), null);
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
        final String cleanSQL = dirtySQL.replaceAll(paramRegex, "?");
        
        // Get the DB response and convert it to JSON
        String jsonArray;
        try (
            Connection conn = Common.getNestDS(propPath).getConnection();
            PreparedStatement st = conn.prepareStatement(cleanSQL);
        ) {
            // Bind all of the parameters to their placeholders
            String nextParam = null;
            String nextParamValue = null;

            SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

            try {
                for (int i=1; i<=datasetParamOrder.size(); i++) {
                    // Depending on the specified type, cast and bind
                    nextParam = datasetParamOrder.get(i-1);
                    final String paramType = nextParam.substring(1, nextParam.indexOf(":"));
                    final String paramName = nextParam.substring(nextParam.indexOf(":") + 1, nextParam.length()-1);
                    nextParamValue = datasetParams.get(paramName);

                    // Use this to test that parameters are parsed correctly
                    //response.setHeader("Param-" + i, nextParam + ", " + nextParamValue + ", " + paramType + ", " + paramName);

                    switch (paramType) {
                        case "bigint":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.BIGINT);
                                continue;
                            }
                            st.setLong(i, Long.parseLong(nextParamValue));
                            break;
                        case "bit":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.BIT);
                                continue;
                            }
                        case "boolean":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.BOOLEAN);
                                continue;
                            }
                            st.setBoolean(i, Boolean.parseBoolean(nextParamValue));
                            break;
                        case "decimal":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.DECIMAL);
                                continue;
                            }
                        case "numeric":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.NUMERIC);
                                continue;
                            }
                            st.setBigDecimal(i, new BigDecimal(nextParamValue));
                            break;
                        case "double":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.DOUBLE);
                                continue;
                            }
                            st.setDouble(1, Double.parseDouble(nextParamValue));
                            break;
                        case "float":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.FLOAT);
                                continue;
                            }
                            st.setFloat(i, Float.parseFloat(nextParamValue));
                            break;
                        case "integer":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.INTEGER);
                                continue;
                            }
                            st.setInt(i, Integer.parseInt(nextParamValue));
                            break;
                        case "nvarchar":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.NVARCHAR);
                                continue;
                            }
                            st.setNString(i, nextParamValue);
                            break;
                        case "date":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.DATE);
                                continue;
                            }
                            java.util.Date dt1 = ISO8601DATEFORMAT.parse(nextParamValue.trim().replaceAll(" ", "T"));
                            st.setDate(i, new java.sql.Date(dt1.getTime()));
                            break;
                        case "timestamp":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.TIMESTAMP);
                                continue;
                            }
                            java.util.Date dt2 = ISO8601DATEFORMAT.parse(nextParamValue.trim().replaceAll(" ", "T"));
                            st.setTimestamp(i, new java.sql.Timestamp(dt2.getTime()));
                            break;
                        //case "string":
                        //case "varchar":
                        default:
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.VARCHAR);
                                continue;
                            }
                            st.setString(i, nextParamValue);
                            break;
                    }
                }
            } catch (
                ParseException ex
            ) {
                // Write log within inner catch block so we still have params
                LOG.log(Level.INFO, "Supplied request parameter '" + nextParamValue + 
                        "' does not match expected parameter '" + nextParam + "'" , ex);
                throw ex;
            }

            try (ResultSet rsh = st.executeQuery();) {
                if (rsh.isBeforeFirst()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    jsonArray = Common.resultSetAsJSON(rsh);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonArray = "[]";
                }
            }

        } catch (ParseException ex){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (IOException | SQLException ex) {
            LOG.log(Level.SEVERE, "Unable to execute \"" + requestEntity + "\" dataset GET query", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
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
