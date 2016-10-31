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
public class NestHttpPutTests extends NestHttpTests {

    // Keep a list and map so we can delete these afterwards, 
    // and also use these for creation of dependent objects
    private static List<String> entityInsertOrder = new ArrayList<>();
    private static Map<String, Long> entityMap = new HashMap<>();
    
    
    public NestHttpPutTests() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException, SQLException {
        NestHttpTests.NestAdminLogin();
        
        // We could send all the POST requests here and only do PUT in the tests, 
        // however PUT will send an UPDATE to the db even if the object already matches exactly in the db.
        // Thus we can test if the PUT works, using the same object we POSTed.
        // This also means we don't have the extra worry of a PUT conflicting with other 
        // records as this would be caught at the POST stage.
        // We also have less code duplication this way as we don't need to define JSON objects twice,
        // both here in the beforeclass, as well as in the tests themselves.
    }
    
    @AfterClass
    public static void tearDownClass() throws IOException, SQLException {
        // Then delete everything else in reverse order direct via the db
        NestHttpTests.dbDeleteEntities(entityInsertOrder, entityMap);
        
        // Finally log out of the session.
        NestHttpTests.NestAdminLogout();
    }
    
    @Test
    public void AA_PutUserSucceeds() throws IOException {
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
        assertTrue("Error, pre-PUT POST request fails with " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("users");
        long userId = Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1));
        entityMap.put("users", userId);
        
        HttpURLConnection connection2 = NestHttpTests.nestHttpPutRequest("/user/"+userId, true, jsonObject.toString());
        int code2 = connection2.getResponseCode();
        assertTrue("Error, non-success response code:" + Integer.toString(code2), code2 == 204);
    }
    
    @Test
    public void AB_PutBaitSucceeds() throws IOException {
        // Buid the Json bait object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "test bait");
        jsonObject.addProperty("img_filename", "a_giant_piece_of_test.png");
        jsonObject.addProperty("note", "Everyone uses this all the time.");
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/bait", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, pre-PUT POST request fails with " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("bait");
        long baitId = Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1));
        entityMap.put("bait", baitId);
        
        HttpURLConnection connection2 = NestHttpTests.nestHttpPutRequest("/bait/"+baitId, true, jsonObject.toString());
        int code2 = connection2.getResponseCode();
        assertTrue("Error, non-success response code:" + Integer.toString(code2), code2 == 204);
    }
    
    @Test
    public void AC_PutCatchTypeSucceeds() throws IOException {
        // Buid the Json catchtype object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "a flesh eating centipede!");
        jsonObject.addProperty("img_filename", "wtf_is_this.pcx");
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/catch-type", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, pre-PUT POST request fails with " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("catchtype");
        long catchTypeId = Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1));
        entityMap.put("catchtype", catchTypeId);
        
        HttpURLConnection connection2 = NestHttpTests.nestHttpPutRequest("/catch-type/"+catchTypeId, true, jsonObject.toString());
        int code2 = connection2.getResponseCode();
        assertTrue("Error, non-success response code:" + Integer.toString(code2), code2 == 204);
    }
    
    @Test
    public void AD_PutTrapTypeSucceeds() throws IOException {
        // Buid the Json traptype object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "new test trap design");
        jsonObject.addProperty("model", "test-0123-abc");
        jsonObject.addProperty("note", "The new prototype trap for ages to come!");
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/trap-type", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, pre-PUT POST request fails with " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("traptype");
        long trapTypeId = Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1));
        entityMap.put("traptype", trapTypeId);
        
        HttpURLConnection connection2 = NestHttpTests.nestHttpPutRequest("/trap-type/"+trapTypeId, true, jsonObject.toString());
        int code2 = connection2.getResponseCode();
        assertTrue("Error, non-success response code:" + Integer.toString(code2), code2 == 204);
    }
    
    @Test
    public void AE_PutRegionSucceeds() throws IOException {
        // Build the Json region object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "new test region");
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/region", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, pre-PUT POST request fails with " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("region");
        long regionId = Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1));
        entityMap.put("region", regionId);
        
        HttpURLConnection connection2 = NestHttpTests.nestHttpPutRequest("/region/"+regionId, true, jsonObject.toString());
        int code2 = connection2.getResponseCode();
        assertTrue("Error, non-success response code:" + Integer.toString(code2), code2 == 204);
    }
    
    @Test
    public void AF_PutTraplineSucceeds() throws IOException {
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
        assertTrue("Error, pre-PUT POST request fails with " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("trapline");
        long traplineId = Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1));
        entityMap.put("trapline", traplineId);
        
        HttpURLConnection connection2 = NestHttpTests.nestHttpPutRequest("/trapline/"+traplineId, true, jsonObject.toString());
        int code2 = connection2.getResponseCode();
        assertTrue("Error, non-success response code:" + Integer.toString(code2), code2 == 204);
    }
    
    @Test
    public void AG_PutTrapSucceeds() throws IOException {
        // Build the Json trap object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("trapline_id", entityMap.get("trapline"));
        jsonObject.addProperty("number", 1);
        jsonObject.addProperty("coord_long", 1.23456789);
        jsonObject.addProperty("coord_lat", -1.23456789);
        // Rest of the properties can be defaults
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/trap", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, pre-PUT POST request fails with " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("trap");
        long trapId = Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1));
        entityMap.put("trap", trapId);
        
        HttpURLConnection connection2 = NestHttpTests.nestHttpPutRequest("/trap/"+trapId, true, jsonObject.toString());
        int code2 = connection2.getResponseCode();
        assertTrue("Error, non-success response code:" + Integer.toString(code2), code2 == 204);
    }
        
    @Test
    public void AH_PutTraplineUserSucceeds() throws IOException {
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
        assertTrue("Error, pre-PUT POST request fails with " + Integer.toString(code), code == 201);
        
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("traplineuser");
        long traplineuserId = Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1));
        entityMap.put("traplineuser", traplineuserId);
        
        HttpURLConnection connection2 = NestHttpTests.nestHttpPutRequest("/trapline-user/"+traplineuserId, true, jsonObject.toString());
        int code2 = connection2.getResponseCode();
        assertTrue("Error, non-success response code:" + Integer.toString(code2), code2 == 204);
    }
    
    @Test
    public void AI_PutCatchFails() throws IOException {
        // The user is now registered to the trapline, so we can reattempt to log the catch
        
        // Build the Json catch object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("trap_id", entityMap.get("trap"));
        jsonObject.addProperty("catchtype_id", entityMap.get("catchtype"));
        jsonObject.addProperty("note", "a hundred legs");
        jsonObject.addProperty("img_filename", "verygross.png");
        // Rest of the properties can be defaults
        
        HttpURLConnection connection = NestHttpTests.nestHttpPostRequest("/catch", true, jsonObject.toString());
        int code = connection.getResponseCode();
        assertTrue("Error, pre-PUT POST request fails with " + Integer.toString(code), code == 201);
        
        // We need to remove the catch manually otherwise everything will be set inactive instead of removed.
        final String newRes = connection.getHeaderField("Location");
        entityInsertOrder.add("catch");
        long catchId = Long.parseLong(newRes.substring(newRes.lastIndexOf('/') + 1));
        entityMap.put("catch", catchId);
        
        HttpURLConnection connection2 = NestHttpTests.nestHttpPutRequest("/catch/"+catchId, true, jsonObject.toString());
        int code2 = connection2.getResponseCode();
        assertTrue("Error, non-success response code: " + Integer.toString(code2), !((code2 >= 200) && (code2 < 300)));
    }
}
