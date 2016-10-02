/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, Sept 2016
 **********************************************************/
package org.nestnz.api;

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
import java.util.Map.Entry;
import java.util.Properties;
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

    @Test
    /**
     * Test that any mapped dataset files are parsable JSON strings.
     * @throws IOException if the dataset mappings, or any datasets are unaccessible
     * @throws MalformedJsonException if a dataset cannot be parsed into JSON
     */
    public void MappedDatasetTargetsAreJSON() throws IOException, MalformedJsonException {
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
}
