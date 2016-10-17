/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, Sept 2016
 **********************************************************/
package org.nestnz.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.regex.Pattern;
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

    /**
     * Accepts a REST entity subroute, e.g. "/region" and returns a response code from a GET request to the URL
     * @param entitySubroute
     * @return
     * @throws MalformedURLException
     * @throws IOException 
     */
    private static int nestHttpGetRequest(String entitySubroute, boolean addSessionToken) throws IOException {
        URL url = new URL(BASE_URL + entitySubroute);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        if (addSessionToken) {
            connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        }
        connection.connect();
        return connection.getResponseCode();
    }
    
    // Check that all of the URLs we expect to succeed, do.
    
    @Test
    public void AAA_GetUserSucceeds() throws IOException {
        int code = nestHttpGetRequest("/user", true);
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    // Next check that successful GET reponses are formatted as we expect
    // We'll use the user table as we've logged in and thus can be sure that at least some data exists
        
    @Test
    public void AAB_GetUserAsJsonIsJson() throws IOException {
        URL url = new URL(BASE_URL + "/user");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.addRequestProperty("Accept", "application/json");
        connection.connect();
        
        // Make sure the request succeeds
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
        
        // Make sure the request has the right JSON mime-type declaration
        String acceptHeader = connection.getContentType();
        assertTrue("Error, no Content-Type header found", acceptHeader != null);
        assertTrue("Error, Content-Type is not application/json", Pattern.compile("application/json").matcher(acceptHeader.toLowerCase()).find());
        
        // Make sure that the response body is parsable Json
        
        assertTrue("Error, response body is length zero", connection.getContentLength() > 0);
        
        // Java hackery to get the response body into a string
        String responseBody = null;
        try (java.util.Scanner s = new java.util.Scanner(connection.getInputStream())) {
            responseBody = s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }
        assertTrue("Error, unable to read response body from inputstream", responseBody != null);
        
        // This will throw an exception if response is not a parsable Json array.
        JsonArray responseArray = (new JsonParser()).parse(responseBody).getAsJsonArray();
    }
    
    @Test
    public void AAC_GetUserAsCsvIsCsv() throws IOException {
        URL url = new URL(BASE_URL + "/user");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Session-Token", SESSION_TOKEN);
        connection.addRequestProperty("Accept", "text/csv");
        connection.connect();
        
        // Make sure the request succeeds
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
        
        // Make sure the request has a correct mime-type declaration
        String acceptHeader = connection.getContentType();
        assertTrue("Error, no Content-Type header found", acceptHeader != null);
        assertTrue("Error, Content-Type is not valid for CSV", 
                Pattern.compile("text/csv").matcher(acceptHeader.toLowerCase()).find() ||
                Pattern.compile("application/octet-stream").matcher(acceptHeader.toLowerCase()).find()
        );
        
        assertTrue("Error, response body is length zero", connection.getContentLength() > 0);
        
        // Java hackery to get the response body into a string
        String responseBody = null;
        try (java.util.Scanner s = new java.util.Scanner(connection.getInputStream())) {
            responseBody = s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }
        assertTrue("Error, unable to read response body from inputstream", responseBody != null);
        
        // Make sure that the response body is CSV formatted with regex
        String csvRegex = "(?:\\s*\"[^\"]*\"\\s*)(?:,\\s*\"[^\"]*\"\\s*)*";
        assertTrue("Error: response content does not match CSV regex", 
                Pattern.compile(csvRegex).matcher(responseBody).find());
    }
    
    @Test
    public void AB_GetBaitSucceeds() throws IOException {
        int code = nestHttpGetRequest("/bait", true);
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }

    @Test
    public void AC_GetCatchTypeSucceeds() throws IOException {
        int code = nestHttpGetRequest("/catch-type", true);
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AD_GetTrapTypeSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trap-type", true);
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AE_GetRegionSucceeds() throws IOException {
        int code = nestHttpGetRequest("/region", true);
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }

    @Test
    public void AF_GetCatchSucceeds() throws IOException {
        int code = nestHttpGetRequest("/catch", true);
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AG_GetTraplineSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trapline", true);
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AH_GetTraplineUserSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trapline-user", true);
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AI_GetTrapSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trap", true);
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AJ_GetCatchReportSimpleSucceeds() throws IOException {
        int code = nestHttpGetRequest("/catch-report-simple", false);
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    // Next check that undefined Urls fail with 404 as we expect
    
    @Test
    public void AK_GetUnknownUrlFails() throws IOException {
        int code = nestHttpGetRequest("/some-undefined-entity", true);
        
        assertEquals(code, 404);
    }
}
