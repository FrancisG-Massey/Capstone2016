/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, August 2016
 **********************************************************/
package org.nestnz.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.*;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;


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
public class Common {    
    private static DataSource nestDS = null;

    /**
     * Attempt a fresh connection to the specified DB and get a handle to it.
     * @param propPath The absolute file-path to the properties file specifying connection parameters
     * @return A handle to the connected db (or null on failure).
     * @throws java.io.IOException The specified properties file could not be loaded.
     */
    public static DataSource getNestDS(String propPath) throws IOException {
        
        if (nestDS == null) {
            // Load the connection parameters from the specified config file
            Properties prop = new Properties();
            try (InputStream input = new FileInputStream(propPath)) {
                prop.load(input);
            }

            PoolProperties p = new PoolProperties();

            p.setDriverClassName(  prop.getProperty("driver")  );
            p.setUsername(         prop.getProperty("user")    );
            p.setPassword(         prop.getProperty("password"));
            p.setUrl(              prop.getProperty("url")     );
            p.setValidationQuery(  "SELECT 1"                  );
            //p.setMaxAge(5800000);
            
            nestDS = new DataSource(p);
        }
        return nestDS;
    }
    
    public static void closeNestDS() throws SQLException {
        if (nestDS != null) {
            nestDS.close();
            nestDS = null;
        }
    }
    
    /**
     * Convert a JDBC result set into a JSON array string
     * @param rsh The JDBC query result-set object
     * @return the JSON string equivalent of the table
     * @throws SQLException 
     */
    public static String resultSetAsJSON(ResultSet rsh) throws SQLException {
        JsonArray jsonArray = new JsonArray();
        ResultSetMetaData rsmd = rsh.getMetaData();
        int numColumns = rsmd.getColumnCount();
        while (rsh.next()) {
            JsonObject jsonObj = new JsonObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                switch (rsmd.getColumnType(i)) {
                    case java.sql.Types.BIGINT:
                        jsonObj.addProperty(column_name, rsh.getInt(i));
                        break;
                    case java.sql.Types.BOOLEAN:
                        jsonObj.addProperty(column_name, rsh.getBoolean(i));
                        break;
                    case java.sql.Types.DOUBLE:
                        jsonObj.addProperty(column_name, rsh.getDouble(i));
                        break;
                    case java.sql.Types.FLOAT:
                        jsonObj.addProperty(column_name, rsh.getFloat(i));
                        break;
                    case java.sql.Types.INTEGER:
                        jsonObj.addProperty(column_name, rsh.getInt(i));
                        break;
                    case java.sql.Types.NVARCHAR:
                        jsonObj.addProperty(column_name, rsh.getNString(i));
                        break;
                    case java.sql.Types.TINYINT:
                        jsonObj.addProperty(column_name, rsh.getInt(i));
                        break;
                    case java.sql.Types.SMALLINT:
                        jsonObj.addProperty(column_name, rsh.getInt(i));
                        break;
                    //case java.sql.Types.VARCHAR:
                    //case java.sql.Types.DATE:
                    //case java.sql.Types.TIMESTAMP:
                    default:
                        jsonObj.addProperty(column_name, rsh.getString(i));
                        break;
                }
            }
            jsonArray.add(jsonObj);
        }
        return jsonArray.toString();
    }
}
