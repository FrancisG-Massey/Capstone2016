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
}
