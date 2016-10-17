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
public class NestHttpGetTests extends NestHttpTests {
    
    public NestHttpGetTests() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        NestHttpTests.NestAdminLogin();
    }
    
    @AfterClass
    public static void tearDownClass() throws IOException {
        NestHttpTests.NestAdminLogout();
    }

    // Check that all of the URLs we expect to succeed, do.
    
    @Test
    public void AAA_GetUserSucceeds() throws IOException {
        int code = nestHttpGetRequest("/user", true, "application/json").getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    // Next check that successful GET reponses are formatted as we expect
    // We'll use the user table as we've logged in and thus can be sure that at least some data exists
        
    @Test
    public void AAB_GetUserAsJsonIsJson() throws IOException {
        HttpURLConnection connection = nestHttpGetRequest("/user", true, "application/json");
        
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
        HttpURLConnection connection = nestHttpGetRequest("/user", true, "text/csv");
        
        // Make sure the request succeeds
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
        
        // Make sure the request has a correct mime-type declaration
        String acceptHeader = connection.getContentType();
        assertTrue("Error, no Content-Type header found", acceptHeader != null);
        assertTrue("Error, Content-Type is not valid for CSV:" + acceptHeader, 
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
        int code = nestHttpGetRequest("/bait", true, "application/json").getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }

    @Test
    public void AC_GetCatchTypeSucceeds() throws IOException {
        int code = nestHttpGetRequest("/catch-type", true, "application/json").getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AD_GetTrapTypeSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trap-type", true, "application/json").getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AE_GetRegionSucceeds() throws IOException {
        int code = nestHttpGetRequest("/region", true, "application/json").getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }

    @Test
    public void AF_GetCatchSucceeds() throws IOException {
        int code = nestHttpGetRequest("/catch", true, "application/json").getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AG_GetTraplineSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trapline", true, "application/json").getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AH_GetTraplineUserSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trapline-user", true, "application/json").getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AI_GetTrapSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trap", true, "application/json").getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    @Test
    public void AJ_GetCatchReportSimpleSucceeds() throws IOException {
        int code = nestHttpGetRequest("/catch-report-simple", false, "application/json").getResponseCode();
        
        assertTrue("Error, non-success response code: " + Integer.toString(code), code >= 200);
        assertTrue("Error, non-success response code: " + Integer.toString(code), code < 300);
    }
    
    // Next check that undefined Urls fail with 404 as we expect
    
    @Test
    public void AK_GetUnknownUrlFails() throws IOException {
        int code = nestHttpGetRequest("/some-undefined-entity", true, "application/json").getResponseCode();
        
        assertEquals(code, 404);
    }
}
