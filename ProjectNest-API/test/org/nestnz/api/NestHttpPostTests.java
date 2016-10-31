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


import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Sam
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NestHttpPostTests extends NestHttpTests {

    // Keep a list and map so we can delete these afterwards, 
    // and also use these for creation of dependent objects
    private static List<String> entityInsertOrder = new ArrayList<>();
    private static Map<String, Long> entityMap = new HashMap<>();
    
    
    public NestHttpPostTests() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException, SQLException {
        NestHttpTests.NestAdminLogin();
    }
    
    @AfterClass
    public static void tearDownClass() throws IOException, SQLException {
        // Then delete everything else in reverse order direct via the db
        NestHttpTests.dbDeleteEntities(entityInsertOrder, entityMap);
        
        // Finally log out of the session.
        NestHttpTests.NestAdminLogout();
    }
    
    @Test
    public void AA_PostUserSucceeds() throws IOException {
        // Buid the Json user object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", "test");
        jsonObject.addProperty("password", "test");
        jsonObject.addProperty("fullname", "Testy Tester");
        jsonObject.addProperty("phone", "012345678");
        jsonObject.addProperty("email", "testy@testers.com");
        jsonObject.addProperty("admin", "true");
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/user", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("users");
        entityMap.put("users", Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1)));
    }
    
    @Test
    public void AB_PostBaitSucceeds() throws IOException {
        // Buid the Json bait object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "test bait");
        jsonObject.addProperty("img_filename", "a_giant_piece_of_test.png");
        jsonObject.addProperty("note", "Everyone uses this all the time.");
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/bait", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("bait");
        entityMap.put("bait", Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1)));
    }
    
    @Test
    public void AC_PostCatchTypeSucceeds() throws IOException {
        // Buid the Json catchtype object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "a flesh eating centipede!");
        jsonObject.addProperty("img_filename", "wtf_is_this.pcx");
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/catch-type", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("catchtype");
        entityMap.put("catchtype", Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1)));
    }
    
    @Test
    public void AD_PostTrapTypeSucceeds() throws IOException {
        // Buid the Json traptype object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "new test trap design");
        jsonObject.addProperty("model", "test-0123-abc");
        jsonObject.addProperty("note", "The new prototype trap for ages to come!");
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/trap-type", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("traptype");
        entityMap.put("traptype", Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1)));
    }
    
    @Test
    public void AE_PostRegionSucceeds() throws IOException {
        // Build the Json region object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "new test region");
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/region", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("region");
        entityMap.put("region", Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1)));
    }
    
    @Test
    public void AF_PostTraplineSucceeds() throws IOException {
        // Build the Json trapline object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "new test trapline");
        jsonObject.addProperty("region_id", entityMap.get("region"));
        jsonObject.addProperty("start_tag", "East gate");
        jsonObject.addProperty("end_tag", "West stream");
        jsonObject.addProperty("img_filename", "pretty_mountain.png");
        jsonObject.addProperty("default_bait_id", entityMap.get("bait"));
        jsonObject.addProperty("default_traptype_id", entityMap.get("traptype"));
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/trapline", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("trapline");
        entityMap.put("trapline", Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1)));
    }
    
    @Test
    public void AG_PostTrapSucceeds() throws IOException {
        // Build the Json trap object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("trapline_id", entityMap.get("trapline"));
        jsonObject.addProperty("number", 1);
        jsonObject.addProperty("coord_long", 1.23456789);
        jsonObject.addProperty("coord_lat", -1.23456789);
        // Rest of the properties can be defaults
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/trap", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("trap");
        entityMap.put("trap", Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1)));
    }
       
    @Test
    public void AH_PostTraplineUserSucceeds() throws IOException {
        // Build the Json traplineuser object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("trapline_id", entityMap.get("trapline"));
        
        // Important that we create the traplineuser record as the nestrootadmin
        // instead of the newly created user, so that we can successfully log 
        // a catch later without relogging as the new user
        
        jsonObject.addProperty("user_id", 1);
        jsonObject.addProperty("admin", true);
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/trapline-user", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("traplineuser");
        entityMap.put("traplineuser", Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1)));
    }
    
    @Test
    public void AI_PostCatchSucceeds() throws IOException {
        // The user is now registered to the trapline, so we can reattempt to log the catch
        
        // Build the Json trap object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("trap_id", entityMap.get("trap"));
        jsonObject.addProperty("catchtype_id", entityMap.get("catchtype"));
        jsonObject.addProperty("note", "a hundred legs");
        jsonObject.addProperty("img_filename", "verygross.png");
        // Rest of the properties can be defaults
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/catch", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code), code == 201);
        
        // We need to remove the catch manually otherwise everything will be set inactive instead of removed.
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("catch");
        entityMap.put("catch", Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1)));
    }
}
