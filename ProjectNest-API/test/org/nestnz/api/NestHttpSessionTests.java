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
