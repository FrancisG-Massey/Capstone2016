/**********************************************************
 *    Copyright (C) Project Nest NZ (2016)
 *    Unauthorised copying of this file, via any medium is 
 *        strictly prohibited.
 *    Proprietary and confidential
 *    Written by Sam Hunt <s2112201@ipc.ac.nz>, August 2016
 **********************************************************/
package org.nestnz.api;

/**
 * Manages access to the Nest DB:
 * 
 * @author Sam Hunt 14216618
 * @version 1.0
 */
public class NestDBHandler {
    private static NestDBHandler instance = new NestDBHandler();
    private boolean connected = false;
    
    private NestDBHandler(){
        super();
        // Connect to the Nest postgreSQL DB
        // If successful:
        connected = true;
    }

    public boolean isConnected() {
        return connected;
    }
    
    public void close() {
        // Cleanup the DB connection
        this.connected = false;
    }
    
    @Override 
    protected void finalize() throws Throwable {
        try {
            this.close();
        } catch (Throwable t) {
            throw t;
        } finally {
            super.finalize();
        }
    }
}
