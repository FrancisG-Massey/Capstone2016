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
    org.nestnz.api.DBConnectTests.class,
    org.nestnz.api.DBInterfaceTests.class,
    org.nestnz.api.NestHttpSessionTests.class,
    org.nestnz.api.NestHttpGetTests.class,
    org.nestnz.api.NestHttpPostTests.class,
    org.nestnz.api.NestHttpPutTests.class,
    org.nestnz.api.NestHttpDeleteTests.class
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
        // This will be different if we build from gradle instead of netbeans,
        // but we won't have time to sort his out so maybe in the future...
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
    
    public static String getTestServerBaseUrl() {
        return "http://localhost:8084/api";
    }
}
