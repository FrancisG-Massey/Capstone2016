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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
public class DBInterfaceTests {

    private static String dbConfigPath = null;
    private static DataSource ds = null;
    private static Connection conn = null;

    public DBInterfaceTests() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        // Get the relative path context
        final String servletContextPath = AllTests.getServletContextPath();
        final String sep = File.separator;
        dbConfigPath = servletContextPath + "/web/WEB-INF/dbconfig.properties.dev".replace("/", sep);

        // Load the db config properties
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(dbConfigPath)) {
            prop.load(input);
        }
        PoolProperties p = new PoolProperties();

        p.setDriverClassName(  prop.getProperty("driver")  );
        p.setUsername(         prop.getProperty("user")    );
        p.setPassword(         prop.getProperty("password"));
        p.setUrl(              prop.getProperty("url")     );
        p.setValidationQuery(  "SELECT 1;"                  );

        ds = new DataSource(p);
    }

    @AfterClass
    public static void tearDownClass() {
        if (ds != null)
            ds.close();
    }

    @Before
    public void setUp() throws SQLException {
        conn = ds.getConnection();
    }

    @After
    public void tearDown() throws SQLException {
        if (conn != null)
            conn.close();
    }

    /**
     * Test of resultSetAsJSON method, of class Common.
     * @throws java.sql.SQLException
     */
    @Test
    public void ResultSetAsJsonReturnsJsonArray() throws SQLException {
        
        Statement st = conn.createStatement();
        ResultSet rsh = st.executeQuery("SELECT 1 AS test");
        
        // Call the method
        String result = Common.resultSetAsJSON(rsh);

        // An exception will be thrown here if the dataset cannot be parsed as JSON.
        JsonArray jArray = new JsonParser().parse(result).getAsJsonArray();
        
        assertTrue(true);
    }
    
    /**
     * Test of resultSetAsCSV method, of class Common.
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    @Test
    public void ResultSetAsCSVReturnsString() throws SQLException, IOException {
        
        Statement st = conn.createStatement();
        ResultSet rsh = st.executeQuery("SELECT 1::bigint AS test, 2::text as test2");
        
        // Call the method
        String result = Common.resultSetAsCSV(rsh);
        
        // An exception will be thrown here if the dataset cannot be parsed as JSON.
        assertTrue("Returned CSV string has null length!", result.length() > 0);
    }

    /**
     * Test of resultSetAsJSON method, of class Common.
     * @throws java.sql.SQLException
     */
    @Test
    public void ResultSetAsJsonReturnsCorrectFormats() throws SQLException {

        Statement st = conn.createStatement();
        String testQuery = ""
                + "SELECT "
                + "     cast(1 AS integer)              AS some_int,"
                + "     cast(-2 AS bigint)              AS some_bigint,"
                + "     cast(3.0 AS numeric)            AS some_numeric,"
                + "     cast(-4.0 AS decimal)           AS some_decimal,"
                + "     cast(5.0 AS double precision)   AS some_double,"
                + "     cast(true AS boolean)              AS some_boolean,"
                + "     cast('2016-09-28' AS date)      AS some_date,"
                + "     cast('2016-09-28 03:46:14.123' "
                + "         AS timestamp)               AS some_timestamp,"
                + "     cast('Strings for days' AS text)AS some_string,"
                + "     NULL                            AS some_null"
                + ";";
        // Get the result set from the db
        ResultSet rsh = st.executeQuery(testQuery);

        // Call the method
        String result = Common.resultSetAsJSON(rsh);

        // Check the result
        JsonObject values = new JsonParser().parse(result).getAsJsonArray().get(0).getAsJsonObject();
        assertEquals("1", values.get("some_int").toString());
        assertEquals("-2", values.get("some_bigint").toString());
        assertEquals("3.0", values.get("some_numeric").toString());
        assertEquals("-4.0", values.get("some_decimal").toString());
        assertEquals("5.0", values.get("some_double").toString());
        assertEquals("true", values.get("some_boolean").toString());
        assertEquals("\"2016-09-28\"", values.get("some_date").toString());
        assertEquals("\"2016-09-28 03:46:14.123\"", values.get("some_timestamp").toString());
        assertEquals("\"Strings for days\"", values.get("some_string").toString());
        assertEquals("null", values.get("some_null").toString());
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that the bigint datatype is supported by dataset syntax
     * @throws SQLException 
     * @throws java.text.ParseException 
     * @throws java.lang.NumberFormatException
     */
    @Test
    public void BindDynamicParametersSupportsBigInt() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("some-value", "42");
            datasetParams.put("some-negative", "-42");
            datasetParams.put("some-null", null);
            
            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("bigint:some-value");
            datasetParamOrder.add("bigint:some-negative");
            datasetParamOrder.add("bigint:some-null");
            
            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);
            
            String expResult = "SELECT 42, -42, NULL AS test";
            String result = st.toString();
            
            assertEquals(expResult, result);
        }
    }

    /**
    * Test of bindDynamicParameters method, of class Common.
    * Ensure that the bit datatype is supported by dataset syntax
    * @throws SQLException 
    * @throws java.text.ParseException 
    * @throws java.lang.NumberFormatException
    */
    @Test
    public void BindDynamicParametersSupportsBit() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("hungry", "1");
            datasetParams.put("thirsty", "0");
            datasetParams.put("happy", null);
            
            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("bit:hungry");
            datasetParamOrder.add("bit:thirsty");
            datasetParamOrder.add("bit:happy");
            
            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);
            
            String expResult = "SELECT '1', '0', NULL AS test";
            String result = st.toString();
            
            assertEquals(expResult, result);
        }
    }

    /**
    * Test of bindDynamicParameters method, of class Common.
    * Ensure that the boolean datatype is supported by dataset syntax
    * @throws SQLException 
    * @throws java.text.ParseException 
    * @throws java.lang.NumberFormatException
    */
    @Test
    public void BindDynamicParametersSupportsBoolean() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("hungry", "TrUe");
            datasetParams.put("thirsty", "0");
            datasetParams.put("happy", null);
            
            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("boolean:hungry");
            datasetParamOrder.add("boolean:thirsty");
            datasetParamOrder.add("boolean:happy");
            
            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);
            
            String expResult = "SELECT '1', '0', NULL AS test";
            String result = st.toString();
            
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that the string datatype is supported by dataset syntax
     * @throws SQLException 
     * @throws java.text.ParseException 
     * @throws java.lang.NumberFormatException
     */
    @Test
    public void BindDynamicParametersSupportsString() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("some-string", "Well hi there");
            datasetParams.put("some-empty", "");
            datasetParams.put("some-null", null);

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("string:some-string");
            datasetParamOrder.add("string:some-empty");
            datasetParamOrder.add("string:some-null");

            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            String expResult = "SELECT 'Well hi there', '', NULL AS test";
            String result = st.toString();

            assertEquals(expResult, result);
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that the numeric datatype is supported by dataset syntax
     * @throws SQLException 
     * @throws java.text.ParseException 
     * @throws java.lang.NumberFormatException
     */
    @Test
    public void BindDynamicParametersSupportsNumeric() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("some-numeric", "-3.1415654");
            datasetParams.put("some-negative", "3.1415654");
            datasetParams.put("some-null", null);

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("numeric:some-numeric");
            datasetParamOrder.add("numeric:some-negative");
            datasetParamOrder.add("numeric:some-null");

            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            String expResult = "SELECT '-3.1415654', '3.1415654', NULL AS test";
            String result = st.toString();

            assertEquals(expResult, result);
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that the decimal datatype is supported by dataset syntax
     * @throws SQLException 
     * @throws java.text.ParseException 
     * @throws java.lang.NumberFormatException
     */
    @Test
    public void BindDynamicParametersSupportsDecimal() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("some-decimal", "-3.1415654");
            datasetParams.put("some-negative", "3.1415654");
            datasetParams.put("some-null", null);

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("decimal:some-decimal");
            datasetParamOrder.add("decimal:some-negative");
            datasetParamOrder.add("decimal:some-null");

            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            String expResult = "SELECT '-3.1415654', '3.1415654', NULL AS test";
            String result = st.toString();

            assertEquals(expResult, result);
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that the double datatype is supported by dataset syntax
     * @throws SQLException 
     * @throws java.text.ParseException 
     * @throws java.lang.NumberFormatException
     */
    @Test
    public void BindDynamicParametersSupportsDouble() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("some-negative", "-3.1415926");
            datasetParams.put("some-double", "3.1415926");
            datasetParams.put("some-null", null);

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("double:some-negative");
            datasetParamOrder.add("double:some-double");
            datasetParamOrder.add("double:some-null");

            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            String expResult = "SELECT -3.1415926, 3.1415926, NULL AS test";
            String result = st.toString();

            assertEquals(expResult, result);
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that the integer datatype is supported by dataset syntax
     * @throws SQLException 
     * @throws java.text.ParseException 
     * @throws java.lang.NumberFormatException
     */
    @Test
    public void BindDynamicParametersSupportsInteger() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("some-value", "42");
            datasetParams.put("some-negative", "-42");
            datasetParams.put("some-null", null);

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("integer:some-value");
            datasetParamOrder.add("integer:some-negative");
            datasetParamOrder.add("integer:some-null");

            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            String expResult = "SELECT 42, -42, NULL AS test";
            String result = st.toString();

            assertEquals(expResult, result);
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that the nvarchar datatype is supported by dataset syntax
     * @throws SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test
    public void BindDynamicParametersSupportsNVarChar() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("some-nvarchar", "Well hi there");
            datasetParams.put("some-empty", "");
            datasetParams.put("some-null", null);

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("nvarchar:some-nvarchar");
            datasetParamOrder.add("nvarchar:some-empty");
            datasetParamOrder.add("nvarchar:some-null");

            // Test the method
            // This fails in postgres with SQLFeatureNotSupportedException.
            // This is because postgres implicitely treats normal text columns like nvarchars
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            String expResult = "SELECT 'Well hi there', '', NULL AS test";
            String result = st.toString();

            assertEquals(expResult, result);
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that the nvarchar datatype is supported by dataset syntax
     * @throws SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test
    public void BindDynamicParametersSupportsTimestamp() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("some-timestamp-withT", "2016-09-28T03:46:14.123");
            datasetParams.put("some-timestamp-withoutT", "2016-09-28 03:46:14.123");
            datasetParams.put("some-null", null);

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("timestamp:some-timestamp-withT");
            datasetParamOrder.add("timestamp:some-timestamp-withoutT");
            datasetParamOrder.add("timestamp:some-null");

            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            // JDBC adds timezone to timestamps. DB should handle this fine though.
            String expResult = "SELECT '2016-09-28 03:46:14.123000+13', '2016-09-28 03:46:14.123000+13', NULL AS test";
            String result = st.toString();

            assertEquals(expResult, result);
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that the date datatype is supported by dataset syntax
     * @throws SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test
    public void BindDynamicParametersSupportsDate() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("some-date", "2016-09-28");
            datasetParams.put("some-null", null);

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("date:some-date");
            datasetParamOrder.add("date:some-null");

            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            // JDBC adds timezone to dates and changes date-part order to ISO. DB should handle this fine though.
            String expResult = "SELECT '2016-09-28 +13', NULL AS test";
            String result = st.toString();

            assertEquals(expResult, result);
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that types unsupported by dataset syntax throw an exception.
     * @throws SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test(expected=TypeNotPresentException.class)
    public void BindDynamicParametersUnknownTypesThrowEx() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("some-fruit", "banana");

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("banana:some-fruit");

            // Test the method
            // This should throw an exception
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            fail("TypeNotPresentException not thrown!");
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that non-bits in bit fields throw an exception.
     * @throws SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test(expected=ParseException.class)
    public void BindDynamicParametersBadBitsThrowEx() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("not-a-bit", "yep, not a bit");

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("bit:not-a-bit");

            // Test the method
            // This should throw an exception
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            fail("ParseException not thrown!");
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that non-booleans in boolean fields throw an exception.
     * @throws SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test(expected=ParseException.class)
    public void BindDynamicParametersBadBoolsThrowEx() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("not-a-boolean", "yep, not a boolean");

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("boolean:not-a-boolean");

            // Test the method
            // This should throw an exception
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            fail("ParseException not thrown!");
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that non-integers in integer fields throw an exception.
     * @throws SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test(expected=NumberFormatException.class)
    public void BindDynamicParametersBadIntsThrowEx() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("not-an-integer", "yep, not a integer");

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("integer:not-an-integer");

            // Test the method
            // This should throw an exception
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            fail("NumberFormatException not thrown!");
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that non-numerics in numeric fields throw an exception.
     * @throws SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test(expected=NumberFormatException.class)
    public void BindDynamicParametersBadNumericsThrowEx() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("not-a-numeric", "yep, not a numeric");

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("numeric:not-a-numeric");

            // Test the method
            // This should throw an exception
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            fail("NumberFormatException not thrown!");
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that non-decimals in decimal fields throw an exception.
     * @throws SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test(expected=NumberFormatException.class)
    public void BindDynamicParametersBadDecimalsThrowEx() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("not-a-decimal", "yep, not a decimal");

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("decimal:not-a-decimal");

            // Test the method
            // This should throw an exception
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            fail("NumberFormatException not thrown!");
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that non-bigints in bigint fields throw an exception.
     * @throws SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test(expected=NumberFormatException.class)
    public void BindDynamicParametersBadBigintsThrowEx() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("not-a-bigint", "yep, not a bigint");

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("bigint:not-a-bigint");

            // Test the method
            // This should throw an exception
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            fail("NumberFormatException not thrown!");
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that non-timestamps in timestamp fields throw an exception.
     * @throws SQLException
     * @throws java.time.format.DateTimeParseException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test(expected=DateTimeParseException.class)
    public void BindDynamicParametersBadTimestampsThrowsParseEx() throws DateTimeParseException, SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("not-a-timestamp", "yep, not a timestamp");

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("timestamp:not-a-timestamp");

            // Test the method
            // This should throw an exception
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            fail("TypeNotPresentException not thrown!");
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that non-dates in date fields throw an exception.
     * @throws SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test(expected=ParseException.class)
    public void BindDynamicParametersBadDatesThrowsParseEx() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("not-a-date", "yep, not a date");

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("date:not-a-date");

            // Test the method
            // This should throw an exception
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            fail("ParseException not thrown!");
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * @throws java.sql.SQLException
     * @throws java.text.ParseException
     * @throws java.lang.NumberFormatException
     */
    @Test
    public void BindDynamicParametersPreservesOrder() throws SQLException, ParseException, NumberFormatException  {
        // Prepare the dirty sql
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("quantity", "42");
            datasetParams.put("fruit", "apple");

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("string:fruit");
            datasetParamOrder.add("string:quantity");
            datasetParamOrder.add("bigint:quantity");

            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            String expResult = "SELECT 'apple', '42', 42 AS test";
            String result = st.toString();

            assertEquals(expResult, result);
        }
    }
}
