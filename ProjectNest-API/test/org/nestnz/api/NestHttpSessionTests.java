/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nestnz.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Sam
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NestHttpSessionTests {
    
    // Centralize variables for test environment
    private static final String BASE_URL = AllTests.getTestServerBaseUrl();
    private static String SESSION_TOKEN = "";
    
    public NestHttpSessionTests() {
    }
    
    @Test
    public void AA_SessionPostSucceeds() throws IOException {
        // Login using nest root (deactivated in production)
        URL url = new URL(BASE_URL + "/session");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        String encoded = Base64.getEncoder().encodeToString("nestrootadmin:nestrootadmin".getBytes());
        connection.setRequestProperty("Authorization", "Basic "+encoded);
        
        connection.setRequestMethod("POST");
        connection.connect();
        
        int code = connection.getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
        
        SESSION_TOKEN = connection.getHeaderField("Session-Token");
    }
    
    @Test
    public void AB_SessionDeleteSucceeds() throws IOException {
        // Login using nest root (deactivated in production)
        URL url = new URL(BASE_URL + "/session");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("DELETE");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.connect();
        
        int code = connection.getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
}
