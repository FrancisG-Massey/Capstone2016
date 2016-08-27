/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, August 2016
 **********************************************************/
package org.nestnz.api;

import com.berry.BCrypt;
import java.sql.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Base64;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Session manager for the Nest API. The following interface is supported:
 * 
 * POST /session
 *      login to the API, creating a session.
 *      requires a basic authentication header.
 *      returns our custom "Session-Token" header.
 * 
 * DELETE /session
 *      logout of the API, deleting the session.
 *      requires our custom "Session-Token" header.
 * 
 * @author Sam Hunt
 * @version 1.0
 */
public class NestSessionServlet extends HttpServlet {

    private String propPath = null;
    private Connection dbh = null;
    
    /**
     * Attempt to connect to the database on servlet creation
     * @throws ServletException 
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        // Get the servlet context so we can get the file-path of the db config file.
        ServletContext sc = this.getServletContext();
        propPath = sc.getRealPath("/WEB-INF/dbconfig.properties");
        
        // Attempt the initial connection to the database.
        try {
            dbh = NestDBHandler.getDbConnection(propPath);
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            // TODO: Log ex
        }
    }
    
    /**
     *  Attempt to close the db connection when the session manager terminates
     */
    @Override
    public void destroy() {
        try {
            dbh.close();
        } catch (SQLException ex) {
            // TODO: Log ex
        }
    }
    
    /**
     * Ping the DB and if the connection is timed-out, try to reconnect once.
     */
    private void reconnectIfStale() {
        try {
            if (!NestDBHandler.ping()) {
                dbh = NestDBHandler.getDbConnection(propPath);
            }
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            // TODO: Log ex
        }
    }
    
    /**
     * Display session debug info in a get for now
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            /* TODO remove this method in production lol. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet NestSessionServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet NestSessionServlet at " + request.getContextPath() + "</h1>");
            
            // Ping the DB and if the connection is timed-out, try to reconnect once.
            reconnectIfStale();
            
            try {
                String sqlQuery = "SELECT * FROM public.session;";
                out.println("<p>Querying: \"" + sqlQuery + "\"</p>");
                Statement st = dbh.createStatement();
                ResultSet rs = st.executeQuery(sqlQuery);
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();
                out.println("<p>" + columnsNumber + " columns found!</p>");

                while (rs.next()) {
                    out.println("<p>");
                    for (int i = 1; i <= columnsNumber; i++) {
                        if (i > 1) out.println(",  ");
                        out.println(rsmd.getColumnName(i) + ": " + rs.getString(i) );
                    }
                    out.println("</p>");
                }
                rs.close();
                st.close();
                
            } catch (SQLException ex) {
                // TODO: Log ex
                out.println("DB Error: " + ex.getMessage());
            }
            out.println("</body>");
            out.println("</html>");
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
        
        // Check for a basic auth header, if there is none:
        final String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Basic")) {
            response.addHeader("WWW-Authenticate", "Basic realm=\"User Visible Realm\"");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // Parse out the Basic Auth header ("Authorization: Basic base64credentials")
        final String base64Credentials = auth.substring("Basic".length()).trim();
        final String decodedCredentials = new String(Base64.getDecoder().decode(base64Credentials),
                Charset.forName("UTF-8"));
        
        // Check credentials are in the form: "username:password"
        // TODO: Enforce in DB that usernames cannot contain colon character (:)
        final int delimiterIndex = decodedCredentials.indexOf(":");
        if (delimiterIndex < 1) {
            response.addHeader("WWW-Authenticate", "Basic realm=\"User Visible Realm\"");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        final String inputUsername = decodedCredentials.substring(0, delimiterIndex);
        final String inputPassword = decodedCredentials.substring(delimiterIndex+1);
        
        // Ping the DB and if the connection is timed-out, try to reconnect once.
        reconnectIfStale();
        
        // Declare here so these don't go out of scope with try
        String dbPassword = null;
        long dbUserID = 0; 
        
        PreparedStatement st; ResultSet rs; int responseCode = 0;
                
        // Get the hashed password from the DB
        try {
            // Use prepared statement and bind username parameter to protect against SQL injection
            String sql = "SELECT user_id, user_password FROM public.users WHERE user_name = ?;";
            st = dbh.prepareStatement(sql);
            st.setString(1, inputUsername);
            rs = st.executeQuery();
            // TODO: Make sure the DB has a unique constraint on users.user_name
            if (!rs.isBeforeFirst()) {
                // No such-named user is registered in the database.
                responseCode = HttpServletResponse.SC_FORBIDDEN;  
            } else {
                rs.next();
                dbPassword = rs.getString("user_password");
                dbUserID = rs.getLong("user_id");
            }
            rs.close();
            st.close();
        } catch (SQLException ex) {
            // TODO: Log ex
            //response.setHeader("Error", ex.getMessage());      // YOLO debug
            responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        if (responseCode != 0) {
            response.setStatus(responseCode);
            return;
        }
        // Compare the two passwords, returning a fail if they don't match
        if (!BCrypt.checkpw(inputPassword, dbPassword)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        // TODO: Add transparent password hash strengthening if the old password hash bcrypt cost is too low
        
        // User has been identified and validated by the DB!
        // Create a strong unique session token
        String newSessionToken = java.util.UUID.randomUUID().toString();

        // Log it in the session table in the DB
        try {
            String newSessionSQL = "INSERT INTO public.session (session_user_id, session_token) VALUES (?, ?)";
            PreparedStatement st1 = dbh.prepareStatement(newSessionSQL);
            st1.setLong(1, dbUserID);
            st1.setString(2, newSessionToken);
            int newRows = st1.executeUpdate();
            st1.close();
            if (newRows != 1) {   
                throw new SQLException("Failed to create new record in session table.");
            }
        } catch (SQLException ex) {
            // TODO: Log ex
            //response.setHeader("Error", ex.getMessage());      // YOLO debug
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // Return the new session id to the client
        response.addHeader("Session-Token", newSessionToken);
        response.setStatus(HttpServletResponse.SC_OK);
    }


    /**
     * Handles the HTTP DELETE method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check for a "Session-Token" header with regex validation
        final String sessionToken = request.getHeader("Session-Token");
        final String uuidRegex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
        if (sessionToken == null || !sessionToken.matches(uuidRegex)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Ping the DB and if the connection is timed-out, try to reconnect once.
        reconnectIfStale();
        
        try {
            // Attempt to delete any sessions matching the session token
            String newSessionSQL = "DELETE FROM public.session WHERE session_token = ?;";
            PreparedStatement st = dbh.prepareStatement(newSessionSQL);
            st.setString(1, sessionToken);
            int sessionsDeleted = st.executeUpdate();
            st.close();
            
            // Return a response appropriate to whether we actually logged out or the session did not exist/was expired
            response.setStatus((sessionsDeleted==0)? HttpServletResponse.SC_GONE : HttpServletResponse.SC_OK);
        } catch (SQLException ex) {
            // TODO: Log ex
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }    
    
    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Session manager for the Nest API";
    }
}
