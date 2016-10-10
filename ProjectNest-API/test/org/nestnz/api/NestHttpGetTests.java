/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, Sept 2016
 **********************************************************/
package org.nestnz.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Sam
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NestHttpGetTests {
    
    private static final String BASE_URL = AllTests.getTestServerBaseUrl();
    private static String SESSION_TOKEN = "";
    
    public NestHttpGetTests() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        
        // Login using nest root (deactivated in production)
        URL url = new URL(BASE_URL + "/session");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        String encoded = Base64.getEncoder().encodeToString("nestrootadmin:nestrootadmin".getBytes());
        connection.setRequestProperty("Authorization", "Basic "+encoded);
        
        connection.setRequestMethod("POST");
        connection.connect();
        
        SESSION_TOKEN = connection.getHeaderField("Session-Token");
    }
    
    @AfterClass
    public static void tearDownClass() throws IOException {
        // Logout of nest root (deactivated in production)
        URL url = new URL(BASE_URL + "/session");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("DELETE");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        
        connection.connect();
    }

    @Test
    public void AA_GetUserSucceeds() throws IOException {
        URL url = new URL(BASE_URL + "/user");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.connect();
        
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AB_GetBaitSucceeds() throws IOException {
        URL url = new URL(BASE_URL + "/bait");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.connect();
        
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }

    @Test
    public void AC_GetCatchTypeSucceeds() throws IOException {
        URL url = new URL(BASE_URL + "/catch-type");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.connect();
        
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AD_GetTrapTypeSucceeds() throws IOException {
        URL url = new URL(BASE_URL + "/trap-type");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.connect();
        
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AE_GetRegionSucceeds() throws IOException {
        URL url = new URL(BASE_URL + "/region");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.connect();
        
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }

    @Test
    public void AF_GetCatchSucceeds() throws IOException {
        URL url = new URL(BASE_URL + "/catch");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.connect();
        
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AG_GetTraplineSucceeds() throws IOException {
        URL url = new URL(BASE_URL + "/trapline");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.connect();
        
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AH_GetTraplineUserSucceeds() throws IOException {
        URL url = new URL(BASE_URL + "/trapline-user");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.connect();
        
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AI_GetTrapSucceeds() throws IOException {
        URL url = new URL(BASE_URL + "/trap");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.connect();
        
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
}
