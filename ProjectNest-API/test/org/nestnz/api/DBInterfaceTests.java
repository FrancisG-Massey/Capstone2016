/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, Sept 2016
 **********************************************************/
package org.nestnz.api;

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
    public void ResultSetAsJSONWorks() throws SQLException {
        
        try {
            Statement st = conn.createStatement();
            ResultSet rsh = st.executeQuery("SELECT 1 AS test");
            String expResult = "[{\"test\":1}]";
            String result = Common.resultSetAsJSON(rsh);
            assertEquals(expResult, result);
        } catch (Exception ex) {
            System.out.println(ex.toString());
            fail();
        }
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
            datasetParamOrder.add("#bigint:some-value#");
            datasetParamOrder.add("#bigint:some-negative#");
            datasetParamOrder.add("#bigint:some-null#");
            
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
        String sqlQuery = "SELECT ?, ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("hungry", "1");
            datasetParams.put("thirsty", "0");
            datasetParams.put("tired", "banana");
            datasetParams.put("happy", null);
            
            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("#bit:hungry#");
            datasetParamOrder.add("#bit:thirsty#");
            datasetParamOrder.add("#bit:tired#");
            datasetParamOrder.add("#bit:happy#");
            
            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);
            
            String expResult = "SELECT '1', '0', NULL, NULL AS test";
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
        String sqlQuery = "SELECT ?, ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("hungry", "TrUe");
            datasetParams.put("thirsty", "0");
            datasetParams.put("tired", "banana");
            datasetParams.put("happy", null);
            
            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("#boolean:hungry#");
            datasetParamOrder.add("#boolean:thirsty#");
            datasetParamOrder.add("#boolean:tired#");
            datasetParamOrder.add("#boolean:happy#");
            
            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);
            
            String expResult = "SELECT '1', '0', NULL, NULL AS test";
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
            datasetParamOrder.add("#string:some-string#");
            datasetParamOrder.add("#string:some-empty#");
            datasetParamOrder.add("#string:some-null#");

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
            datasetParamOrder.add("#numeric:some-numeric#");
            datasetParamOrder.add("#numeric:some-negative#");
            datasetParamOrder.add("#numeric:some-null#");

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
            datasetParamOrder.add("#decimal:some-decimal#");
            datasetParamOrder.add("#decimal:some-negative#");
            datasetParamOrder.add("#decimal:some-null#");

            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            String expResult = "SELECT '-3.1415654', '3.1415654', NULL AS test";
            String result = st.toString();

            assertEquals(expResult, result);
        }
    }

    /**
     * Test of bindDynamicParameters method, of class Common.
     * Ensure that the float datatype is supported by dataset syntax
     * @throws SQLException 
     * @throws java.text.ParseException 
     * @throws java.lang.NumberFormatException
     */
    @Test
    public void BindDynamicParametersSupportsFloat() throws SQLException, NumberFormatException, ParseException {
        String sqlQuery = "SELECT ?, ?, ? AS test";
        // Prepare the parameter map
        try (PreparedStatement st = conn.prepareStatement(sqlQuery)) {
            // Prepare the parameter map
            Map<String, String> datasetParams = new HashMap<>();
            datasetParams.put("some-negative", "-3.1416");
            datasetParams.put("some-float", "3.1416");
            datasetParams.put("some-null", null);

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("#float:some-negative#");
            datasetParamOrder.add("#float:some-float#");
            datasetParamOrder.add("#float:some-null#");

            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            String expResult = "SELECT -3.1416, 3.1416, NULL AS test";
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
            datasetParamOrder.add("#double:some-negative#");
            datasetParamOrder.add("#double:some-double#");
            datasetParamOrder.add("#double:some-null#");

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
            datasetParamOrder.add("#integer:some-value#");
            datasetParamOrder.add("#integer:some-negative#");
            datasetParamOrder.add("#integer:some-null#");

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
            datasetParamOrder.add("#nvarchar:some-nvarchar#");
            datasetParamOrder.add("#nvarchar:some-empty#");
            datasetParamOrder.add("#nvarchar:some-null#");

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
            datasetParamOrder.add("#timestamp:some-timestamp-withT#");
            datasetParamOrder.add("#timestamp:some-timestamp-withoutT#");
            datasetParamOrder.add("#timestamp:some-null#");

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
            datasetParams.put("some-date", "28-09-2016");
            datasetParams.put("some-null", null);

            // Prepare the parameter order store
            List<String> datasetParamOrder = new ArrayList<>();
            datasetParamOrder.add("#date:some-date#");
            datasetParamOrder.add("#date:some-null#");

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
            datasetParamOrder.add("#banana:some-fruit#");

            // Test the method
            // This should throw an exception
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            fail("TypeNotPresentException not thrown!");
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
            datasetParamOrder.add("#string:fruit#");
            datasetParamOrder.add("#string:quantity#");
            datasetParamOrder.add("#bigint:quantity#");

            // Test the method
            Common.bindDynamicParameters(st, datasetParams, datasetParamOrder);

            String expResult = "SELECT 'apple', '42', 42 AS test";
            String result = st.toString();

            assertEquals(expResult, result);
        }
    }
}
