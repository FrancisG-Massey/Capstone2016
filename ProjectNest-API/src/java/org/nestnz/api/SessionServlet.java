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
public class SessionServlet extends HttpServlet {

    private static String propPath = null;
    
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
            
            
            final String sqlQuery = "SELECT session_id, session_user_id, substring(session_token from 1 for 31)||'xxxxx' AS session_token, session_created FROM public.session;";
            //out.println("<p>dbconfig.properties filepath: \"" + propPath + "\"</p>");
            out.println("<p>Querying: \"" + sqlQuery + "\"</p>");
            
            try (
                Connection conn = Common.getNestDS(propPath).getConnection();
                PreparedStatement st = conn.prepareStatement(sqlQuery);
                ResultSet rs = st.executeQuery();
            ) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();
                out.println("<p>" + columnsNumber + " columns found!</p><p>");
                while (rs.next()) {
                    
                    for (int i = 1; i <= columnsNumber; i++) {
                        if (i > 1) out.print(",  ");
                        out.println(rsmd.getColumnName(i) + ": " + rs.getString(i) );
                    }
                    out.println("<br/>");
                }
            } catch (IOException ex) {
                // TODO: Log ex
                out.println("IO Error: " + ex.getMessage());
            } catch (SQLException ex) {
                // TODO: Log ex
                out.println("DB Error: " + ex.getMessage());
            }
            out.println("</p>");
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
        
        // Check for a well-formed basic auth header.
        final String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Basic")) {
            // No basic auth header found, or header is not well-formed
            response.addHeader("WWW-Authenticate", "Basic realm=\"User Visible Realm\"");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Extract the base64credentials from the header ("Authorization: Basic base64credentials")
        final String base64Credentials = auth.substring("Basic".length()).trim();
        
        // Decode the credentials from the base64-encoded "username:password" string
        final String decodedCredentials = new String(Base64.getDecoder().decode(base64Credentials), Charset.forName("UTF-8"));
        final int delimiterIndex = decodedCredentials.indexOf(":");
        if (delimiterIndex < 1) {
            // Header is not well-formed
            response.addHeader("WWW-Authenticate", "Basic realm=\"User Visible Realm\"");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Split the decoded credentials into the actual username and password 
        final String inputUsername = decodedCredentials.substring(0, delimiterIndex);
        final String inputPassword = decodedCredentials.substring(delimiterIndex+1);
        
        // Get the user's id and hashed password from the DB
        long dbUserID; String dbPassword;
        final String sqlQuery = "SELECT user_id, user_password FROM public.users WHERE user_name = ?;";
        try (
            Connection conn = Common.getNestDS(propPath).getConnection();
            PreparedStatement sth = conn.prepareStatement(sqlQuery);
        ) {
            sth.setString(1, inputUsername);
            try (ResultSet rsh = sth.executeQuery();) {
                if (rsh.isBeforeFirst()) {
                    // Pull the hashed password and user's id from the result set.
                    rsh.next();
                    dbPassword = rsh.getString("user_password");
                    dbUserID = rsh.getLong("user_id");
                } else {
                    // No such-named user is registered in the database.
                    // TODO: Log failed login attempt and reason
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        } catch (SQLException | IOException ex) {
            // TODO: Log ex
            //response.setHeader("Error", ex.getMessage());      // YOLO debug
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // Compare the two passwords, returning a fail if they don't match
        if (!BCrypt.checkpw(inputPassword, dbPassword)) {
            // User exists but password attempt is incorrect
            // TODO: Log failed login attempt and reason
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        // TODO: Add transparent password hash strengthening if the old password hash bcrypt cost is too low
        
        // User has been identified and validated by the DB!
        // Create a strong unique session token
        String newSessionToken = java.util.UUID.randomUUID().toString();

        // Log it in the session table in the DB
        String sql = "INSERT INTO public.session (session_user_id, session_token) VALUES (?, ?)";
        try (
            Connection conn = Common.getNestDS(propPath).getConnection();
            PreparedStatement sth = conn.prepareStatement(sql);
        ) {
            sth.setLong(1, dbUserID);
            sth.setString(2, newSessionToken);
            final int rows = sth.executeUpdate();
            sth.close();
            if (rows != 1) {
                throw new SQLException("Failed to create new session.");
            }
         } catch (SQLException | IOException ex) {
            // TODO: Log ex
            //response.setHeader("Error", ex.getMessage());      // YOLO debug
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // Return the new session id to the client
        response.addHeader("Session-Token", newSessionToken);
        response.setStatus(HttpServletResponse.SC_CREATED);
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
        
        // Attempt to delete any sessions matching the session token        
        final String sql = "DELETE FROM public.session WHERE session_token = ?;";
        try (
            Connection conn = Common.getNestDS(propPath).getConnection();
            PreparedStatement sth = conn.prepareStatement(sql);
        ) {
            // Bind parameters and execute the delete query.
            sth.setString(1, sessionToken);
            int rows = sth.executeUpdate();
            sth.close();
            
            // Return a response appropriate to whether we actually logged out or the session did not exist/was expired
            response.setStatus((rows>=1)? HttpServletResponse.SC_NO_CONTENT : HttpServletResponse.SC_NOT_FOUND);
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
