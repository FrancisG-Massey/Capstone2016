/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, August 2016
 **********************************************************/
package org.nestnz.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Session manager for the Nest API. The following interface is supported:
 * 
 * POST /session
 *      login to the API, creating a session.
 * 
 * DELETE /session
 *      logout of the API, deleting the session.
 * 
 * @author Sam Hunt 14216618
 * @version 1.0
 */
public class NestSessionServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>POST</code> and <code>DELETE</code> 
     * methods.
     *
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
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet NestSessionServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet NestSessionServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // Parse out the Basic Auth header
        // Authorization: Basic base64credentials
        final String base64Credentials = auth.substring("Basic".length()).trim();
        // credentials = username:password
        final String credentials[] = new String(Base64.getDecoder().decode(base64Credentials),
                Charset.forName("UTF-8")).split(":",2);
        final String username = credentials[0];
        final String password = credentials[1];
        
        
        // Get the hashed password from the DB
        // Hash the supplied password with the salt from the DB
        // Compare the two passwords

        // If they don't match:
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);        
        
        // If they do match:
        // Create a strong unique session token
        // Log it in the session table in the DB
        // Return it to the client
        response.addHeader("SessionToken", "aaaaa-bbbbb-ccccc");
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
    }


    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>DELETE</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check for a "SessionToken" header, if there is none:
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        
        // Check it is a valid session in the DB, if not:
        response.setStatus(HttpServletResponse.SC_GONE);
        
        // If it is a valid session, delete it, then return:
        response.setStatus(HttpServletResponse.SC_OK);
    }    
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Session manager for the Nest API";
    }// </editor-fold>

}
