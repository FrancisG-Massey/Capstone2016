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
public class NestHttpDeleteTests extends NestHttpTests {

    // Keep a list and map so we can delete these afterwards, 
    // and also use these for creation of dependent objects
    private static List<String> entityInsertOrder = new ArrayList<>();
    private static Map<String, Long> entityMap = new HashMap<>();
    
    private static long userId = 0;
    private static long baitId = 0;
    private static long catchTypeId = 0;
    private static long trapTypeId = 0;
    private static long regionId = 0;
    private static long traplineId = 0;
    private static long trapId = 0;
    private static long traplineuserId = 0;
    private static long catchId = 0;
    
    
    public NestHttpDeleteTests() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException, SQLException {
        NestHttpTests.NestAdminLogin();
        
        // Create all of the entities with POST requests here
        // because the delete order should be in reverse.
        
        // Also ideally later we would have tests which check records have been
        // set to inactive if they have dependent entities.
        
        HttpURLConnection connection = null;
        String newResource = null;
        JsonObject jsonObject = null;
        
        // Buid the Json user object
        jsonObject = new JsonObject();
        jsonObject.addProperty("username", "test");
        jsonObject.addProperty("password", "test");
        jsonObject.addProperty("fullname", "Testy Tester");
        jsonObject.addProperty("phone", "012345678");
        jsonObject.addProperty("email", "testy@testers.com");
        jsonObject.addProperty("admin", "true");
        connection = NestHttpTests.nestHttpPostRequest("/user", true, jsonObject.toString());
        newResource = connection.getHeaderField("Location");
        entityInsertOrder.add("users");
        userId = Long.parseLong(newResource.substring(newResource.lastIndexOf('/') + 1));
        entityMap.put("users", userId);
        
        
        // Buid the Json bait object
        jsonObject = new JsonObject();
        jsonObject.addProperty("name", "test bait");
        jsonObject.addProperty("img_filename", "a_giant_piece_of_test.png");
        jsonObject.addProperty("note", "Everyone uses this all the time.");
        connection = NestHttpTests.nestHttpPostRequest("/bait", true, jsonObject.toString());
        newResource = connection.getHeaderField("Location");
        entityInsertOrder.add("bait");
        baitId = Long.parseLong(newResource.substring(newResource.lastIndexOf('/') + 1));
        entityMap.put("bait", baitId);
        
        
        // Buid the Json catchtype object
        jsonObject = new JsonObject();
        jsonObject.addProperty("name", "a flesh eating centipede!");
        jsonObject.addProperty("img_filename", "wtf_is_this.pcx");
        connection = NestHttpTests.nestHttpPostRequest("/catch-type", true, jsonObject.toString());
        newResource = connection.getHeaderField("Location");
        entityInsertOrder.add("catchtype");
        catchTypeId = Long.parseLong(newResource.substring(newResource.lastIndexOf('/') + 1));
        entityMap.put("catchtype", catchTypeId);
        
        
        // Buid the Json traptype object
        jsonObject = new JsonObject();
        jsonObject.addProperty("name", "new test trap design");
        jsonObject.addProperty("model", "test-0123-abc");
        jsonObject.addProperty("note", "The new prototype trap for ages to come!");
        connection = NestHttpTests.nestHttpPostRequest("/trap-type", true, jsonObject.toString());
        newResource = connection.getHeaderField("Location");
        entityInsertOrder.add("traptype");
        trapTypeId = Long.parseLong(newResource.substring(newResource.lastIndexOf('/') + 1));
        entityMap.put("traptype", trapTypeId);
        
        
        // Build the Json region object
        jsonObject = new JsonObject();
        jsonObject.addProperty("name", "new test region");
        connection = NestHttpTests.nestHttpPostRequest("/region", true, jsonObject.toString());
        newResource = connection.getHeaderField("Location");
        entityInsertOrder.add("region");
        regionId = Long.parseLong(newResource.substring(newResource.lastIndexOf('/') + 1));
        entityMap.put("region", regionId);
        
        
        // Build the Json trapline object
        jsonObject = new JsonObject();
        jsonObject.addProperty("name", "new test trapline");
        jsonObject.addProperty("region_id", entityMap.get("region"));
        jsonObject.addProperty("start_tag", "East gate");
        jsonObject.addProperty("end_tag", "West stream");
        jsonObject.addProperty("img_filename", "pretty_mountain.png");
        jsonObject.addProperty("default_bait_id", entityMap.get("bait"));
        jsonObject.addProperty("default_traptype_id", entityMap.get("traptype"));
        connection = NestHttpTests.nestHttpPostRequest("/trapline", true, jsonObject.toString());
        newResource = connection.getHeaderField("Location");
        entityInsertOrder.add("trapline");
        traplineId = Long.parseLong(newResource.substring(newResource.lastIndexOf('/') + 1));
        entityMap.put("trapline", traplineId);
        
        
        // Build the Json trap object
        jsonObject = new JsonObject();
        jsonObject.addProperty("trapline_id", entityMap.get("trapline"));
        jsonObject.addProperty("number", 1);
        jsonObject.addProperty("coord_long", 1.23456789);
        jsonObject.addProperty("coord_lat", -1.23456789);
        // Rest of the properties can be defaults
        connection = NestHttpTests.nestHttpPostRequest("/trap", true, jsonObject.toString());
        newResource = connection.getHeaderField("Location");
        entityInsertOrder.add("trap");
        trapId = Long.parseLong(newResource.substring(newResource.lastIndexOf('/') + 1));
        entityMap.put("trap", trapId);
        
        
        // Build the Json traplineuser object
        jsonObject = new JsonObject();
        jsonObject.addProperty("trapline_id", entityMap.get("trapline"));
        // Important that we create the traplineuser record as the nestrootadmin
        // instead of the newly created user, so that we can successfully log 
        // a catch later without relogging as the new user
        jsonObject.addProperty("user_id", 1);
        jsonObject.addProperty("admin", true);
        connection = NestHttpTests.nestHttpPostRequest("/trapline-user", true, jsonObject.toString());
        newResource = connection.getHeaderField("Location");
        entityInsertOrder.add("traplineuser");
        traplineuserId = Long.parseLong(newResource.substring(newResource.lastIndexOf('/') + 1));
        entityMap.put("traplineuser", traplineuserId);
        

        // Build the Json catch object
        jsonObject = new JsonObject();
        jsonObject.addProperty("trap_id", entityMap.get("trap"));
        jsonObject.addProperty("catchtype_id", entityMap.get("catchtype"));
        jsonObject.addProperty("note", "a hundred legs");
        jsonObject.addProperty("img_filename", "verygross.png");
        // Rest of the properties can be defaults
        connection = NestHttpTests.nestHttpPostRequest("/catch", true, jsonObject.toString());
        // We need to remove the catch manually otherwise everything will be set inactive instead of removed.
        newResource = connection.getHeaderField("Location");
        entityInsertOrder.add("catch");
        catchId = Long.parseLong(newResource.substring(newResource.lastIndexOf('/') + 1));
        entityMap.put("catch", catchId);
        
        
    }
    
    @AfterClass
    public static void tearDownClass() throws IOException, SQLException {
        // While the tests will obviously try to delete everything created
        // We need to make sure that everything gets cleaned up either way
        
        // Then delete everything else in reverse order direct via the db
        NestHttpTests.dbDeleteEntities(entityInsertOrder, entityMap);
        
        // Finally log out of the session.
        NestHttpTests.NestAdminLogout();
    }
    
    @Test
    public void AA_DeleteTraplineUserSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trapline-user/"+traplineuserId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code), code == 200);
        
        int code2 = NestHttpTests.nestHttpDeleteRequest("/trapline-user/"+traplineuserId, true).getResponseCode();
        assertTrue("Error, non-success response code on DELETE:" + Integer.toString(code2), code2 == 204);
        
        int code3 = nestHttpGetRequest("/trapline-user/"+traplineuserId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code3), code3 == 204);
    }
    
    @Test
    public void AB_DeleteCatchFails() throws IOException {
        int code = nestHttpGetRequest("/catch/"+catchId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code), code == 200);
        
        int code2 = NestHttpTests.nestHttpDeleteRequest("/catch/"+catchId, true).getResponseCode();
        assertTrue("Error, success response code on DELETE:" + Integer.toString(code2), !((code2 >= 200) && (code2 < 300)));
        
        int code3 = nestHttpGetRequest("/catch/"+catchId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find expected record! GET response: " + Integer.toString(code3), code3 == 200);
    }
    
    @Test
    public void AC_DeleteTrapSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trap/"+trapId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code), code == 200);
        
        int code2 = NestHttpTests.nestHttpDeleteRequest("/trap/"+trapId, true).getResponseCode();
        assertTrue("Error, non-success response code on DELETE:" + Integer.toString(code2), code2 == 204);
        
        int code3 = nestHttpGetRequest("/trap/"+trapId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code3), code3 == 204);
    }
    
    @Test
    public void AD_DeleteTraplineSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trapline/"+traplineId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code), code == 200);
        
        int code2 = NestHttpTests.nestHttpDeleteRequest("/trapline/"+traplineId, true).getResponseCode();
        assertTrue("Error, non-success response code on DELETE:" + Integer.toString(code2), code2 == 204);
        
        int code3 = nestHttpGetRequest("/trapline/"+traplineId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code3), code3 == 204);
    }
    
    @Test
    public void AE_DeleteRegionSucceeds() throws IOException {
        int code = nestHttpGetRequest("/region/"+regionId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code), code == 200);
        
        int code2 = NestHttpTests.nestHttpDeleteRequest("/region/"+regionId, true).getResponseCode();
        assertTrue("Error, non-success response code on DELETE:" + Integer.toString(code2), code2 == 204);
        
        int code3 = nestHttpGetRequest("/region/"+regionId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code3), code3 == 204);
    }
    
    @Test
    public void AF_DeleteBaitSucceeds() throws IOException {
        int code = nestHttpGetRequest("/bait/"+baitId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code), code == 200);
        
        int code2 = NestHttpTests.nestHttpDeleteRequest("/bait/"+baitId, true).getResponseCode();
        assertTrue("Error, non-success response code on DELETE:" + Integer.toString(code2), code2 == 204);
        
        int code3 = nestHttpGetRequest("/bait/"+baitId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code3), code3 == 204);
    }
    
    @Test
    public void AG_DeleteCatchTypeSucceeds() throws IOException {
        int code = nestHttpGetRequest("/catch-type/"+catchTypeId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code), code == 200);
        
        int code2 = NestHttpTests.nestHttpDeleteRequest("/catch-type/"+catchTypeId, true).getResponseCode();
        assertTrue("Error, non-success response code on DELETE:" + Integer.toString(code2), code2 == 204);
        
        int code3 = nestHttpGetRequest("/catch-type/"+catchTypeId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code3), code3 == 204);
    }
    
    @Test
    public void AH_DeleteTrapTypeSucceeds() throws IOException {
        int code = nestHttpGetRequest("/trap-type/"+trapTypeId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code), code == 200);
        
        int code2 = NestHttpTests.nestHttpDeleteRequest("/trap-type/"+trapTypeId, true).getResponseCode();
        assertTrue("Error, non-success response code on DELETE:" + Integer.toString(code2), code2 == 204);
        
        int code3 = nestHttpGetRequest("/trap-type/"+trapTypeId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code3), code3 == 204);
    }
    
    @Test
    public void AI_DeleteUserSucceeds() throws IOException {
        int code = nestHttpGetRequest("/user/"+userId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code), code == 200);
        
        int code2 = NestHttpTests.nestHttpDeleteRequest("/user/"+userId, true).getResponseCode();
        assertTrue("Error, non-success response code on DELETE:" + Integer.toString(code2), code2 == 204);
        
        int code3 = nestHttpGetRequest("/user/"+userId, true, "application/json").getResponseCode();
        assertTrue("Error, unable to find record to delete! GET response: " + Integer.toString(code3), code3 == 204);
    }
    
    
    
   
    
    
    
    
        
    
    
    
}
