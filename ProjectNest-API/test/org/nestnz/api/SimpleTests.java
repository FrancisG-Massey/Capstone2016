/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, Sept 2016
 **********************************************************/
package org.nestnz.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Sam
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SimpleTests {

    public SimpleTests() {
    }

    /**
     * Test of BufferedReaderToString method, of class Common.
     * @throws java.io.IOException
     */
    @Test
    public void BufferedReaderToStringWorks() throws IOException {
        // Prepare the input parameters
        InputStream is = new ByteArrayInputStream("test".getBytes());
        BufferedReader in = new BufferedReader(new InputStreamReader(is));;

        // Run the test
        String expResult = "test";
        String result = Common.BufferedReaderToString(in);
        assertEquals(expResult, result);
    }
    
    /**
     * Test the regex uses to parse injectable parameters and their types out of the datasets.
     */
    @Test
    public void RegexDatasetParamsMatch() {

        /**
         * Must:
         * consist of two parts separated by a colon
         * be enclosed by pound signs (hash)
         * not contain a space
         *
         * The goal of this regex is to capture everything that might be a
         * parameter from the datasets. We have a test to check that all used
         * types are valid and parsable later.
         */
        Pattern p = Pattern.compile(Common.DATASETPARAM_REGEX);

        // Should match these
        assertTrue(p.matcher("#string:name#").find());
        assertTrue(p.matcher("#string:name1#").find());
        assertTrue(p.matcher("#string:name-2#").find());
        assertTrue(p.matcher("#string:name_3#").find());
        assertTrue(p.matcher("###string:name##@#").find());
        assertTrue(p.matcher("1a#string:Name#sca").find());
        assertTrue(p.matcher("#1:1#").find());
        assertTrue(p.matcher("#1string:name#").find());
        assertTrue(p.matcher("#string1:name#").find());
        assertTrue(p.matcher("#string:1name#").find());
        assertTrue(p.matcher("#STRING:name#").find());
        assertTrue(p.matcher("#string:NAME#").find());
        assertTrue(p.matcher("#string-string:name#").find());
        assertTrue(p.matcher("#string_string:name#").find());
        assertTrue(p.matcher("#string:name#").find());

        // Should not match these
        assertFalse(p.matcher("").find());
        assertFalse(p.matcher("##").find());
        assertFalse(p.matcher("#:#").find());
        assertFalse(p.matcher("##########").find());
        assertFalse(p.matcher("#string#").find());
        assertFalse(p.matcher("#name#").find());
        assertFalse(p.matcher("#1#").find());
        assertFalse(p.matcher("# # ### #").find());
        assertFalse(p.matcher("#string string:name#").find());
        assertFalse(p.matcher("#string: name#").find());
        assertFalse(p.matcher("#string:na  me#").find());
        assertFalse(p.matcher("string:name").find());
    }

    /**
     * Test the regex uses to validation the format of UUIDs used as session tokens.
     */
    @Test
    public void RegexUuidMatches() {

        Pattern p = Pattern.compile(Common.UUID_REGEX);

        // Should match these
        assertTrue(p.matcher("ba2cc3c4-27ea-4b8d-89cd-298ceed06ade").find());
        assertTrue(p.matcher("418106a5-5562-4f15-aa64-b3f09a9e4d95").find());
        assertTrue(p.matcher("efa4414f-5daf-4f45-950b-02440302e4d4").find());
        assertTrue(p.matcher("86c2913f-bd0b-4096-9642-eae563a73610").find());
        assertTrue(p.matcher("a2d13c66-84c7-4d35-89bd-7b9f8cf2e300").find());

        // Should not match these
        assertFalse(p.matcher("sadjlnc0-cijn-897d-cnla-sdcj7320bcnx").find());
        assertFalse(p.matcher("12345678-yolo-what-mate-notatrueuuid").find());
        assertFalse(p.matcher("some random text token that I found").find());
        assertFalse(p.matcher("sujdcnasiudncposaudncpoasidnc-38267085210").find());
        assertFalse(p.matcher("DFGHJKL59ty0-7guasdb   \\][\\").find());
        assertFalse(p.matcher("ba2cc3c4-27ea-4b8d-89cd-298ceed06ade1").find());
        assertFalse(p.matcher("").find());
    }
    
    /**
     * Test the regex uses to match the entity name and ID in urls requested to the handler.
     */
    @Test
    public void RegexUrlEntityMatches() {

        Pattern p = Pattern.compile(Common.URLENTITY_REGEX);

        fail("Test not implemented yet");
    }
}
