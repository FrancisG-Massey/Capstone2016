/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, August 2016
 **********************************************************/
package org.nestnz.api;

import java.sql.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Assists with connecting to the Nest DB via JDBC:
 * 
 * @author Sam Hunt
 * @version 1.0
 */
public class NestDBHandler {
    private static Connection dbConnection = null;

    /**
     * Attempt a fresh connection to the specified DB and get a handle to it.
     * @param propPath The absolute file-path to the properties file specifying connection parameters
     * @return A handle to the connected db (or null on failure).
     * @throws java.io.IOException The specified properties file could not be loaded.
     * @throws java.lang.ClassNotFoundException The postgres driver could not be located (try adding to classpath)
     * @throws java.sql.SQLException
     */
    public static Connection getDbConnection(String propPath) throws IOException, ClassNotFoundException, SQLException {
        // Load the connection parameters from the specified config file
        dbConnection = null;
        Properties prop = new Properties();
        InputStream input = new FileInputStream(propPath);
        prop.load(input);
        input.close();
        // The url needs to be specified separately in the connection method call
        final String url = prop.getProperty("url");
        prop.remove("url");

        // Load the postgres JDBC driver
        Class.forName("org.postgresql.Driver");
        // Attempt the actualy connection
        dbConnection = DriverManager.getConnection(url, prop);
        return dbConnection;
    }
    
    /**
     * Method to easily check whether the db connection is still alive
     * @return whether the db will continue to accept requests or not over the current connection
     */
    public static boolean ping() {
        try {
            Statement st = dbConnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT 1;");
            rs.close(); st.close();
        } catch (SQLException ex){
            // TODO: Log ex
            return false;
        }
        return true;
    }
    
    /**
     * Attempt to close the connection to the DB.
     */
    public static void close() {
        // Cleanup the DB connection
        try {
            dbConnection.close();
        } catch (SQLException ex) {
            // TODO: Log ex
        }
    }
}
