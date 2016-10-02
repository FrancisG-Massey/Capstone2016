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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Sam
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DBConnectTests {

    private static String dbConfigPathTest = null;
    private static String dbConfigPathProd = null;
    
    public DBConnectTests() {
    }

    @BeforeClass
    public static void setUpClass() {
        final String servletContextPath = AllTests.getServletContextPath();
        final String sep = File.separator;
        //datasetsPath = servletContextPath + "/web/WEB-INF/datasets.properties".replace("/", sep);
        dbConfigPathTest = servletContextPath + "/web/WEB-INF/dbconfig.properties.dev".replace("/", sep);
        dbConfigPathProd = servletContextPath + "/web/WEB-INF/dbconfig.properties.prod".replace("/", sep);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test that the handler can connect to the test db server using the Common method
     * @throws IOException
     * @throws SQLException 
     */
    @Test
    public void TestDatabaseConnects() throws IOException, SQLException {
        // Load the db config properties
        DataSource dsProd = Common.getNestDS(dbConfigPathTest);
        Connection conn = dsProd.getConnection();
        Statement st = conn.createStatement();
        ResultSet rsh = st.executeQuery("SELECT 1;");
        
        assertTrue(rsh.isBeforeFirst());
        rsh.close();
        st.close();
        conn.close();
        dsProd.close();
    }

    /**
     * Test that the handler can connect to the prod db server using the Common method
     * @throws IOException
     * @throws SQLException 
     */
    @Test
    public void ProdDatabaseConnects() throws IOException, SQLException {
        // Load the db config properties
        DataSource dsProd = Common.getNestDS(dbConfigPathProd);
        Connection conn = dsProd.getConnection();
        Statement st = conn.createStatement();
        ResultSet rsh = st.executeQuery("SELECT 1;");

        assertTrue(rsh.isBeforeFirst());
        rsh.close();
        st.close();
        conn.close();
        dsProd.close();
    }
}
