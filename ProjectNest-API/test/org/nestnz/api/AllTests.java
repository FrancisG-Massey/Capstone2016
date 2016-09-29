/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, Sept 2016
 **********************************************************/
package org.nestnz.api;

import java.io.File;
import java.net.URISyntaxException;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Sam
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.nestnz.api.ConfigFileTests.class,
    org.nestnz.api.SimpleTests.class,
    org.nestnz.api.DBInterfaceTests.class,
    org.nestnz.api.DBConnectTests.class
})
public class AllTests {
    // Run the test suite to impose a bit nicer execution order on test classes
    // Not semantically significant as test classes are all independent
    // However when running the tests, it is easier to see what is going on 
    // if the simple tests run earlier, building up to advanced tests later.

    private static String servletContextPath = null;

    public static String getServletContextPath() {
        // The API uses the servlet context path to resolve relative file paths
        // to properties files etc.
        // As the tests don't execute withint the servlet container, we don't
        // have access to this context and need to simulate the base path here.
        // Assumes tests run from './build/test/classes/' and we want './build/'
        if (servletContextPath != null) {
            return servletContextPath;
        }
        try {
            File f = new File(AllTests.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String scp = (new File(f.getParent())).getParent();
            servletContextPath = scp;
            return scp;
        } 
        catch (URISyntaxException ex) {
            return null;
        }
    }
}
