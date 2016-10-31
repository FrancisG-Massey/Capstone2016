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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Sam
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfigFileTests {

    // Store the paths of config files to test
    private static final String SEP = File.separator;
    private static final String SERVLET_CONTEXT_PATH = AllTests.getServletContextPath();
    private static final String DBCONFIG_PATH = SERVLET_CONTEXT_PATH + "/web/WEB-INF/dbconfig.properties".replace("/", SEP);
    private static final String DATASETS_PATH = SERVLET_CONTEXT_PATH + "/web/WEB-INF/datasets.properties".replace("/", SEP);
    private static final String DBCREATESCRIPT_PATH = (new File(SERVLET_CONTEXT_PATH)).getParent() + "/db/schema_full.sql".replace("/", SEP);

    public ConfigFileTests() {
    }

    /**
     * Test that a servlet container can be set up successfully.
     */
    @Test
    public void ApiDirExists() {
        // Return null if unable to find the API /build directory in the 
        // project root directory. By default the tests are executed from: 
        //      projectroot/build/test/classes
        File f = new File(SERVLET_CONTEXT_PATH + "/web".replace("/", SEP));
        assertTrue(f.exists() && f.isDirectory());
    }

    /**
     * Test that the datasets.properties file exists in the expected location
     */
    @Test
    public void DatasetMapExists() {
        File f = new File(DATASETS_PATH);
        assertTrue(f.exists() && !f.isDirectory());
    }

    /**
     * Test that the dbconfig.properties file exists in the expected location
     */
    @Test
    public void DbConfigExists() {
        File f = new File(DBCONFIG_PATH);
        assertTrue(f.exists() && !f.isDirectory());
    }

    /**
     * Test that the schema_full.sql file exists in the expected location 
     * (this is only used for black box unit testing, not by the actual handler)
     */
    @Test
    public void DbSchemaExists() {
        File f = new File(DBCREATESCRIPT_PATH);
        assertTrue(f.exists() && !f.isDirectory());
    }

    /**
     * Test that the dbconfig.properties contains required entries for connecting
     * @throws IOException
     */
    @Test
    public void DBConfigHasRequiredProperties() throws IOException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(DBCONFIG_PATH)) {
            prop.load(input);
        }
        assertTrue(prop.getProperty("driver") != null);
        assertTrue(prop.getProperty("url") != null);
        assertTrue(prop.getProperty("user") != null);
        assertTrue(prop.getProperty("password") != null);
    }

    /**
     * Test that all datasets with mappings in the datasets.properties config 
     * file actually exist in the datasets directory
     * @throws java.io.IOException
     */
    @Test
    public void MappedDatasetsExist() throws IOException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(DATASETS_PATH)) {
            prop.load(input);
        }
        for(Entry<Object, Object> e : prop.entrySet()) {
            File f = new File((SERVLET_CONTEXT_PATH + "/web" + e.getValue().toString().replace("/", SEP)));
            assertTrue(f.exists());
        }
    }

    /**
     * Test that any mapped dataset files are parsable JSON strings.
     * @throws IOException if the dataset mappings, or any datasets are unaccessible
     * @throws MalformedJsonException if a dataset cannot be parsed into JSON
     */
    @Test
    public void MappedDatasetsAreJSON() throws IOException, MalformedJsonException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(DATASETS_PATH)) {
            prop.load(input);
        }
        for(Entry<Object, Object> e : prop.entrySet()) {
            File f = new File((SERVLET_CONTEXT_PATH + "/web" + e.getValue().toString().replace("/", SEP)));
            assertTrue(f.exists());

            // Get the dataset json string from file
            final String datasetPath = f.toString();
            byte[] encoded = Files.readAllBytes(Paths.get(datasetPath));
            final String datasetJSON = new String(encoded, StandardCharsets.UTF_8);

            // An exception will be thrown here if the dataset cannot be parsed as JSON.
            JsonObject jObject = new JsonParser().parse(datasetJSON).getAsJsonObject();
        }
        // All mapped datasets have been resolved by the JSON parser.
        assertTrue(true);
    }
    
    /**
     * Test that any mapped dataset files have parsable parameter types.
     * @throws IOException if the dataset mappings, or any datasets are unaccessible
     * @throws MalformedJsonException if a dataset cannot be parsed into JSON
     * @throws java.sql.SQLException
     * @throws java.text.ParseException
     * @throws java.lang.TypeNotPresentException
     */
    @Test
    public void MappedDatasetsHaveValidParams() throws TypeNotPresentException, IOException, MalformedJsonException, SQLException, ParseException {
        
        // Unfortunately we need a db connection to test this (not actually queried, but necessary)
        String dbConfigPathTest = DBCONFIG_PATH + ".dev".replace("/", SEP);
        
        DataSource dsTest = Common.getNestDS(dbConfigPathTest);
        try (
            Connection conn = dsTest.getConnection();
        ) {

            Properties prop = new Properties();
            try (InputStream input = new FileInputStream(DATASETS_PATH)) {
                prop.load(input);
            }
            for(Entry<Object, Object> e : prop.entrySet()) {
                File f = new File((SERVLET_CONTEXT_PATH + "/web" + e.getValue().toString().replace("/", SEP)));
                assertTrue(f.exists());

                // Get the dataset json string from file
                final String datasetPath = f.toString();
                byte[] encoded = Files.readAllBytes(Paths.get(datasetPath));
                final String datasetJSON = new String(encoded, StandardCharsets.UTF_8);

                // Decode from JSON clean enclosing quotes and newlines then return
                JsonObject jObject = new JsonParser().parse(datasetJSON).getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entries = jObject.entrySet();//will return members of your object
                for (Map.Entry<String, JsonElement> entry: entries) {
                    String datasetSQL = entry.getValue().getAsString();
                    
                    // Remove the returned quotes as the SQL is a string, but only if its not null-length already.
                    datasetSQL = (datasetSQL.length() > 2) ? datasetSQL.substring(1, datasetSQL.length()-1) : "";
                    datasetSQL = datasetSQL.replace("\\r\\n", "\n").replace("\\n", "\n");
                    
                    // Parse the dataset parameters out of the dataset
                    Map<String, String> datasetParams = new HashMap<>();
                    List<String> datasetParamOrder = new ArrayList<>();
                    Common.parseDatasetParameters(datasetSQL, datasetParams, datasetParamOrder);
                    
                    final String cleanSQL = datasetSQL.replaceAll(Common.DATASETPARAM_REGEX, "?");
                    System.out.println(cleanSQL);
                    
                    // An exception will be thrown here if dataset parameters exists with unsupported types
                    try (PreparedStatement st = conn.prepareStatement(cleanSQL);) {
                        Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);
                    }
                }
            }
            // All mapped datasets have been resolved by the JSON parser.
            assertTrue(true);
        }
        Common.closeNestDS();
    }
}
