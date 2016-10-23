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
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	private static final long serialVersionUID = 1191162081764524199L;
	private static final Logger LOG = Logger.getLogger(SessionServlet.class.getName());

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
        
        LOG.log(Level.INFO, "Initializing RequestServlet @{0}", sc.getContextPath());
        
        // Attempt the initial connection to the database.
        try {
            Common.getNestDS(propPath);
            LOG.log(Level.INFO, "Connection successfully established to the database");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to initialise database connection", ex);
        }
    }
    
    /**
     *  Attempt to close the db connection when the session manager terminates
     */
    @Override
    public void destroy() {
        try {
            Common.closeNestDS();
            LOG.log(Level.INFO, "Connection to datasource successfully closed");
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "Unable to close datasource object", ex);
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
        
        LOG.log(Level.INFO, "Received request:\n{0}", request.toString());
        
        // Check for a well-formed basic auth header.
        final String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Basic ")) {
            // No basic auth header found, or header is not well-formed
            LOG.log(Level.INFO, "No basic auth header, or header is poorly formed. Returning 401...\n{0}", response.toString());
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
            LOG.log(Level.INFO, "Basic auth header is not well formed. Returning 401...\n{0}", response.toString());
            response.addHeader("WWW-Authenticate", "Basic realm=\"User Visible Realm\"");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Split the decoded credentials into the actual username and password 
        final String inputUsername = decodedCredentials.substring(0, delimiterIndex);
        final String inputPassword = decodedCredentials.substring(delimiterIndex+1);
        
        // Get the user's id and hashed password from the DB
        long dbUserID; String dbPassword;
        final String sqlQuery = "SELECT user_id, user_password FROM public.users WHERE ((user_name = ?) OR (user_contactemail = ?)) AND user_isinactive = false;";
        try (
            Connection conn = Common.getNestDS(propPath).getConnection();
            PreparedStatement sth = conn.prepareStatement(sqlQuery);
        ) {
            sth.setString(1, inputUsername);
            sth.setString(2, inputUsername);
            try (ResultSet rsh = sth.executeQuery();) {
                if (rsh.isBeforeFirst()) {
                    // Pull the hashed password and user's id from the result set.
                    rsh.next();
                    dbPassword = rsh.getString("user_password");
                    dbUserID = rsh.getLong("user_id");
                    LOG.log(Level.INFO, "User '{0}' with id '{1}' has successfully logged in!", new Object[]{inputUsername, Long.toString(dbUserID)});
                } else {
                    // No such-named active user is registered in the database.
                    LOG.log(Level.INFO, "Failed login attempt from {0} with unrecognised username: \"{1}\" and password: \"{2}\"\nReturning 403...\n{3}", 
                            new Object[]{request.getRemoteAddr(), inputUsername, inputPassword, response.toString()});
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        } catch (SQLException | IOException ex) {
        	LOG.log(Level.SEVERE, "Problem executing login query: \n{0}\nReturning 500...\n{1}", 
                        new Object[]{ex.getMessage(), response.toString()});
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // Compare the two passwords, returning a fail if they don't match
        if (!BCrypt.checkpw(inputPassword, dbPassword)) {
            // User exists but password attempt is incorrect
            LOG.log(Level.INFO, "Failed login attempt from {0} with recognised username: \"{1}\" and password: \"{2}\"\nReturning 403...\n{3}", 
                    new Object[]{request.getRemoteAddr(), inputUsername, inputPassword, response.toString()});
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        // TODO: Add transparent password hash strengthening if the old password hash bcrypt cost is too low
        
        // User has been identified and validated by the DB!
        // Create a strong unique session token
        String newSessionToken = java.util.UUID.randomUUID().toString();

        // Log it in the session table in the DB
        String sql = "INSERT INTO public.session (session_userid, session_token) VALUES (?, ?)";
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
         	LOG.log(Level.SEVERE, "Unable to execute new session query:\n{0}\nReturning 500...\n{1}", 
                        new Object[]{ex.getMessage(), response.toString()});
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // Return the new session id to the client
        LOG.log(Level.INFO, "Session created for user: '{0}', token: {1}\nReturning 200...\n{2}", 
                new Object[]{inputUsername, newSessionToken, response.toString()});
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
        LOG.log(Level.INFO, "Received request:\n{0}\n", request.toString());
        
        // Check for a "Session-Token" header with regex validation
        final String sessionToken = request.getHeader("Session-Token");
        final String uuidRegex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
        if (sessionToken == null || !sessionToken.matches(uuidRegex)) {
            LOG.log(Level.WARNING, "No session token supplied, or session token is not a valid token format!\nReturning 400...\n{0}",
                    response.toString());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Attempt to delete any sessions matching the session token plus any expired sessions
        final String sql = "DELETE FROM public.session WHERE (session_token = ?) OR (session_expirestimestamp < now()::timestamp);";
        try (
            Connection conn = Common.getNestDS(propPath).getConnection();
            PreparedStatement sth = conn.prepareStatement(sql);
        ) {
            // Bind parameters and execute the delete query.
            sth.setString(1, sessionToken);
            int rows = sth.executeUpdate();
            sth.close();
            
            // Return a response appropriate to whether we actually logged out or the session did not exist/was expired
            if (rows >= 1) {
                LOG.log(Level.INFO, "Session {0} deleted successfully!\nReturning 203...\n{1}", new Object[]{sessionToken, response.toString()});
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                LOG.log(Level.INFO, "Session gone/not found!\nReturning 404...\n{1}", new Object[]{sessionToken, response.toString()});
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
        	LOG.log(Level.SEVERE, "Unable to execute query to delete session record!\n{0}Returning 500...\n{1}", 
                        new Object[]{ex.getMessage(), response.toString()});
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
