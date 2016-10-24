/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, August 2016
 **********************************************************/
package org.nestnz.api;

import com.berry.BCrypt;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assists with connecting to the Nest DB via JDBC:
 * 
 * @author Sam Hunt
 * @version 1.0
 */
public class Common {    
    private static DataSource nestDS = null;
    private static final Logger LOG = Logger.getLogger(Common.class.getName());

    //public final static String URLENTITY_REGEX = "/^\\/(?>([a-z][a-z-_]*))(?>\\/(\\d+))?/i";
    public final static String URLENTITY_REGEX = "\\/([\\w-]*)";
    public final static String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    public final static String DATASETPARAM_REGEX = "(#[\\w-]+:[\\w-]+#)";

    public final static int BCRYPT_COST = 12;
    public final static int SESSION_TIMEOUT = 30;

    // This is a better regex which captures only strictly typed dataset parameters
    // except we can't test for invalid uncaptured params easily and these will go straight to the db...
    //public final static String DATASETPARAM_REGEX "(#(?:\\binteger\\b|\\bstring\\b|\\bboolean\\b|\\bbit\\b|\\bnumeric\\b|\\bvarchar\\b|\\btimestamp\\b|\\bdate\\b|\\bbigint\\b|\\bdecimal\\b):[a-z][a-z0-9-_]*#)";

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
     * Build a map of typed parameters which appear in the retrieved query 
     * i.e. regex matches for #string:session-token# etc.
     * ParamOrder maintains insert positions as we dynamically bind our parameters later from the unordered map
     * @param dirtySQL
     * @param datasetParams
     * @param datasetParamOrder 
     */
    public static void parseDatasetParameters(String dirtySQL, Map<String, String> datasetParams, List<String> datasetParamOrder) {
        if (dirtySQL == null) {
            return;
        }
        // Find all parameters including their datatypes
        Matcher m = Pattern.compile(Common.DATASETPARAM_REGEX).matcher(dirtySQL.toLowerCase());
        while (m.find()) {
            final String param = m.group();
            // Discard the datatype in the parameter value map but not in the order list
            // This means we support casting the same value to different types in different places in the dataset if required
            datasetParamOrder.add(param.substring(1, param.length()-1));
            datasetParams.put(param.substring(param.indexOf(":")+1, param.length()-1), null);
        }
    }
    
    /**
     * Return a resultset as a csv formatted string with control chars removed.
     * @param rsh
     * @return
     * @throws SQLException
     * @throws IOException 
     */
    public static String resultSetAsCSV(ResultSet rsh) throws SQLException, IOException {
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData rsmd = rsh.getMetaData();
        int numColumns = rsmd.getColumnCount();
        String colName1 = rsmd.getColumnName(1).replace("\"", "").replace(",", "").trim();
        colName1 = colName1.substring(0, 1).toUpperCase() + ((colName1.length()>0)? colName1.substring(1) : "");
        String colNames = "\"" + colName1 + "\"";
        for (int i = 2; i < numColumns + 1; i++) {
            String colName = rsmd.getColumnName(i).replace("\"", "").replace(",", "").trim();
            colName = colName.substring(0, 1).toUpperCase() + ((colName.length()>0)? colName.substring(1) : "");
            colNames += ",\"" + colName + "\"";
        }
        sb.append(colNames).append("\n");
        System.out.println(sb.toString());
        while (rsh.next()) {
            String row = "\"" + rsh.getString(1).replace("\"", "").replace(",", "") + "\""; 
            for (int i = 2; i < numColumns + 1; i++) {
                String val = rsh.getString(i);
                row += ",\"" + ((val==null)?"":val).replace("\"", "").replace(",", "") + "\"";
            }
            sb.append(row).append("\n");
            System.out.println(row);
        }
        System.out.println(sb.toString());
        return sb.toString();
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

        SimpleDateFormat NZSIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");

        try {
            for (int i=1; i<=datasetParamOrder.size(); i++) {
                // Depending on the specified type, cast and bind
                nextParam = datasetParamOrder.get(i-1);
                final String paramType = nextParam.substring(0, nextParam.indexOf(":"));
                final String paramName = nextParam.substring(nextParam.indexOf(":") + 1);
                nextParamValue = datasetParams.get(paramName);
                // Check for null (with special handling for null-length but valid strings)
                nextParamValue = ((nextParamValue==null) || ((nextParamValue.length()==0)) &&
                        (!paramType.equals("string") && !paramType.equals("varchar") && !paramType.equals("nvarchar")))
                        ? null : nextParamValue;

                // Use this to test that parameters are parsed correctly
                //System.out.println("paramType: "+paramType+", paramName: "+paramName+", nextParamValue=\""+nextParamValue+"\", isNull: "+String.valueOf(nextParamValue==null));
                try {
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
                                    throw new ParseException("Value cannot be parsed to bit/boolean: " + nextParamValue, i);
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
                            java.util.Date dt1 = NZSIMPLEDATEFORMAT.parse(nextParamValue.trim());
                            st.setDate(i, new java.sql.Date(dt1.getTime()));
                            break;
                        case "timestamp":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.TIMESTAMP);
                                continue;
                            }
                            LocalDateTime ldt = LocalDateTime.parse(nextParamValue.trim().replaceAll(" ", "T"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            st.setTimestamp(i, Timestamp.valueOf(ldt));
                            break;
                        case "string-bcrypt":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.VARCHAR);
                                continue;
                            }
                            nextParamValue = BCrypt.hashpw(nextParamValue, BCrypt.gensalt(Common.BCRYPT_COST));
                        case "string":
                        case "varchar":
                            if (nextParamValue == null) {
                                st.setNull(i, java.sql.Types.VARCHAR);
                                continue;
                            }
                            st.setString(i, nextParamValue);
                            break;
                        default:
                            // Throw an exception if a type is encountered which cannot be handled above.
                            throw new java.lang.TypeNotPresentException(paramType, null);
                    }
                }
                catch (SQLFeatureNotSupportedException ex) {
                    // Attempt to parse as string if the db driver does not support a particular datatype
                    // The db may be able to implicitely case it to the correct type if it needs to.
                    if (nextParamValue == null) {
                        st.setNull(i, java.sql.Types.VARCHAR);
                        continue;
                    }
                    st.setString(i, nextParamValue);
                }
            }
        } catch (
            SQLException ex
        ) {
            LOG.log(Level.INFO, "An unexpected SQLException has occured. Unable to continue parsing: {0}", ex.getMessage());
            throw ex;
        } catch (
            DateTimeParseException | ParseException | NumberFormatException ex
        ) {
            // Write log within inner catch block so we still have params
            LOG.log(Level.INFO, "Supplied request parameter '" + nextParamValue + 
                    "' does not match expected parameter '" + nextParam + "'" , ex);
            throw ex;
        }
    }

    public static String BufferedReaderToString(BufferedReader in) throws IOException {
        String line;
        StringBuilder rslt = new StringBuilder();
        while ((line = in.readLine()) != null) {
            rslt.append(line);
        }
        return rslt.toString();
    }
}
