/*******************************************************************************
 * Copyright (C) 2016, Nest NZ
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.nestnz.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.tomcat.jdbc.pool.DataSource;

/**
 * Base class for defining HTTP helper functions
 * @author Sam
 */
abstract public class NestHttpTests {
    
    protected static final String BASE_URL = AllTests.getTestServerBaseUrl();
    protected static String SESSION_TOKEN = "";
    
    private static final String DBCREATESCRIPT_PATH = 
            (new File(AllTests.getServletContextPath())).getParent() 
            + "/db/schema_full.sql".replace("/", File.separator);
    
    private static final String DBCONFIGPATH_TEST = 
            AllTests.getServletContextPath() + "/web/WEB-INF/dbconfig.properties.dev".replace("/", File.separator);
    
    /**
     * For wiping the test database before running tests, so we know they are atomic
     * @throws SQLException
     * @throws IOException 
     */
    protected static void NestWipeDB() throws SQLException, IOException {
        // Wipe the DB so we know the tests are atomic.
        
        // First read the repeatable SQL DB schema into memory.
        String dbSchema = null;
        try (Scanner scanner = new Scanner(new File(DBCREATESCRIPT_PATH));) {
            dbSchema = scanner.useDelimiter("\\A").next();
        }
        if (dbSchema == null) {
            throw new IOException("Unable to load repeatable database schema");
        }
        
        // Next wipe the db
        DataSource dsTest = Common.getNestDS(DBCONFIGPATH_TEST);
        try (
            Connection conn = dsTest.getConnection();
            Statement st = conn.createStatement();        
        ) {
            boolean hasResults = st.execute(dbSchema);
        }
        dsTest.close();
    }
    
    protected static void NestAdminLogin() throws IOException {
        // Login using nest root (deactivated in production)
        URL url = new URL(BASE_URL + "/session");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        String encoded = Base64.getEncoder().encodeToString("nestrootadmin:nestrootadmin".getBytes());
        connection.setRequestProperty("Authorization", "Basic "+encoded);
        
        connection.setRequestMethod("POST");
        connection.connect();
        
        SESSION_TOKEN = connection.getHeaderField("Session-Token");
    }
    
    protected static void NestAdminLogout() throws IOException {
        // Logout of nest root (deactivated in production)
        URL url = new URL(BASE_URL + "/session");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("DELETE");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        
        connection.connect();
    }
    
    /**
     * GETs a REST entity, e.g. "/region" and returns the response object
     * @param entitySubroute
     * @param addSessionToken
     * @param acceptHeader
     * @return 
     * @throws IOException 
     */
    protected static HttpURLConnection nestHttpGetRequest(String entitySubroute, boolean addSessionToken, String acceptHeader) throws IOException {
        URL url = new URL(BASE_URL + entitySubroute);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Accept", (acceptHeader != null) ? acceptHeader : "*/*");
        if (addSessionToken) {
            connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        }
        connection.connect();
        return connection;
    }
    
    /**
     * POSTs to a REST entity, e.g. "/region" and returns the response object
     * @param entitySubroute
     * @param addSessionToken
     * @param requestBody
     * @return
     * @throws IOException 
     */
    protected static HttpURLConnection nestHttpPostRequest(String entitySubroute, Boolean addSessionToken, String requestBody) throws IOException {
        URL url = new URL(BASE_URL + entitySubroute);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        if (addSessionToken) {
            connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        }
        connection.setDoInput(true);
        connection.setDoOutput(true);
        
        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        wr.write(requestBody);
        wr.flush();

        connection.connect();
        return connection;
    }
    
    /**
     * PUTs to a REST entity, e.g. "/region/42" and returns the response object
     * (very similar to POST actually...)
     * @param entitySubroute
     * @param addSessionToken
     * @param requestBody
     * @return
     * @throws IOException 
     */
    protected static HttpURLConnection nestHttpPutRequest(String entitySubroute, Boolean addSessionToken, String requestBody) throws IOException {
        URL url = new URL(BASE_URL + entitySubroute);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        if (addSessionToken) {
            connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        }
        connection.setDoInput(true);
        connection.setDoOutput(true);
        
        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        wr.write(requestBody);
        wr.flush();

        connection.connect();
        return connection;
    }
    
    /**
     * DELETEs a REST entity, e.g. "/region/42" and returns the response object
     * @param entitySubroute
     * @param addSessionToken
     * @return 
     * @throws IOException 
     */
    protected static HttpURLConnection nestHttpDeleteRequest(String entitySubroute, boolean addSessionToken) throws IOException {
        URL url = new URL(BASE_URL + entitySubroute);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        if (addSessionToken) {
            connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        }
        connection.connect();
        return connection;
    }
    
    /**
     * Remove the created entities directly via the db so we don't worry about bugs in DELETE.
     * @param entitiesList These should be names of tables in the database!
     * @param ids This should have keys for each value in entitiesList!
     * @return
     * @throws SQLException
     * @throws IOException 
     */
    protected static boolean dbDeleteEntities(List<String> entitiesList, Map<String,Long> ids) throws SQLException, IOException {
        DataSource dsTest = Common.getNestDS(DBCONFIGPATH_TEST);
        
        for(int i = entitiesList.size()-1; i >= 0; i--) {
            
            // Get the entities to delete
            final String tablename = entitiesList.get(i);
            Long id = ids.get(tablename);
            // It's late, okay!?
            final String columnname = (!tablename.equals("users"))? tablename+"_id" : "user_id";
            
            try (
                Connection conn = dsTest.getConnection();
                Statement st = conn.createStatement();        
            ) {
                boolean hasResults = st.execute("DELETE FROM "+tablename+" WHERE "+columnname+" = "+id+";");
            }
        }
        dsTest.close();
        return true;
    }
}
