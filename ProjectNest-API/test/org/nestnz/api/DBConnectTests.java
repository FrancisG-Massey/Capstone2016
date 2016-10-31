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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.tomcat.jdbc.pool.DataSource;
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
        DataSource dsTest = Common.getNestDS(dbConfigPathTest);
        Connection conn = dsTest.getConnection();
        Statement st = conn.createStatement();
        ResultSet rsh = st.executeQuery("SELECT 1;");
        
        assertTrue(rsh.isBeforeFirst());
        rsh.close();
        st.close();
        conn.close();
        dsTest.close();
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
