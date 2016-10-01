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
import java.io.BufferedReader;
import java.sql.*;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Assists with connecting to the Nest DB via JDBC:
 * 
 * @author Sam Hunt
 * @version 1.0
 */
public class Common {    
    private static DataSource nestDS = null;
    private static final Logger LOG = Logger.getLogger(Common.class.getName());

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
                    case java.sql.Types.BIT:
                        jsonObj.addProperty(column_name, rsh.getBoolean(i));
                        break;
                    case java.sql.Types.NUMERIC:
                    case java.sql.Types.DECIMAL:
                        jsonObj.addProperty(column_name, rsh.getBigDecimal(i));
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
    
    public static void bindDynamicParameters(PreparedStatement st, Map<String, String> datasetParams, List<String> datasetParamOrder) throws ParseException, NumberFormatException, SQLException {
        // Bind all of the parameters to their placeholders
        String nextParam = null;
        String nextParamValue = null;

        SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

        try {
            for (int i=1; i<=datasetParamOrder.size(); i++) {
                // Depending on the specified type, cast and bind
                nextParam = datasetParamOrder.get(i-1);
                final String paramType = nextParam.substring(1, nextParam.indexOf(":"));
                final String paramName = nextParam.substring(nextParam.indexOf(":") + 1, nextParam.length()-1);
                nextParamValue = datasetParams.get(paramName);
                nextParamValue = ((nextParamValue==null) || (nextParamValue.length()==0)) ? null : nextParamValue;

                // Use this to test that parameters are parsed correctly
                //response.setHeader("Param-" + i, nextParam + ", " + nextParamValue + ", " + paramType + ", " + paramName);

                switch (paramType) {
                    case "bigint":
                        if (nextParamValue == null) {
                            st.setNull(i, java.sql.Types.BIGINT);
                            continue;
                        }
                        st.setLong(i, Long.parseLong(nextParamValue));
                        break;
                    case "bit":
                    case "boolean":
                        if (nextParamValue == null) {
                            st.setNull(i, java.sql.Types.BOOLEAN);
                            continue;
                        }
                        switch (nextParamValue.trim().toLowerCase()) {
                            case "true":
                            case "1":
                                st.setBoolean(i, true);
                                break;
                            case "false":
                            case "0":
                                st.setBoolean(i, false);
                                break;
                            default:
                                st.setNull(i, java.sql.Types.BOOLEAN);
                                break;
                        }
                        break;
                    case "decimal":
                        if (nextParamValue == null) {
                            st.setNull(i, java.sql.Types.DECIMAL);
                            continue;
                        }
                    case "numeric":
                        if (nextParamValue == null) {
                            st.setNull(i, java.sql.Types.NUMERIC);
                            continue;
                        }
                        st.setBigDecimal(i, new BigDecimal(nextParamValue));
                        break;
                    case "double":
                        if (nextParamValue == null) {
                            st.setNull(i, java.sql.Types.DOUBLE);
                            continue;
                        }
                        st.setDouble(i, Double.parseDouble(nextParamValue));
                        break;
                    case "float":
                        if (nextParamValue == null) {
                            st.setNull(i, java.sql.Types.FLOAT);
                            continue;
                        }
                        st.setFloat(i, Float.parseFloat(nextParamValue));
                        break;
                    case "integer":
                        if (nextParamValue == null) {
                            st.setNull(i, java.sql.Types.INTEGER);
                            continue;
                        }
                        st.setInt(i, Integer.parseInt(nextParamValue));
                        break;
                    case "nvarchar":
                        if (nextParamValue == null) {
                            st.setNull(i, java.sql.Types.NVARCHAR);
                            continue;
                        }
                        st.setNString(i, nextParamValue);
                        break;
                    case "date":
                        if (nextParamValue == null) {
                            st.setNull(i, java.sql.Types.DATE);
                            continue;
                        }
                        java.util.Date dt1 = ISO8601DATEFORMAT.parse(nextParamValue.trim().replaceAll(" ", "T"));
                        st.setDate(i, new java.sql.Date(dt1.getTime()));
                        break;
                    case "timestamp":
                        if (nextParamValue == null) {
                            st.setNull(i, java.sql.Types.TIMESTAMP);
                            continue;
                        }
                        java.util.Date dt2 = ISO8601DATEFORMAT.parse(nextParamValue.trim().replaceAll(" ", "T"));
                        st.setTimestamp(i, new java.sql.Timestamp(dt2.getTime()));
                        break;
                    //case "string":
                    //case "varchar":
                    default:
                        if (nextParamValue == null) {
                            st.setNull(i, java.sql.Types.VARCHAR);
                            continue;
                        }
                        st.setString(i, nextParamValue);
                        break;
                }
            }
        } catch (
            ParseException | NumberFormatException ex
        ) {
            // Write log within inner catch block so we still have params
            LOG.log(Level.INFO, "Supplied request parameter '" + nextParamValue + 
                    "' does not match expected parameter '" + nextParam + "'" , ex);
            throw ex;
        }
    }

    public static String BufferedReaderToString(BufferedReader in) throws IOException {
        String line = null;
        StringBuilder rslt = new StringBuilder();
        while ((line = in.readLine()) != null) {
            rslt.append(line);
        }
        return rslt.toString();
    }
}
