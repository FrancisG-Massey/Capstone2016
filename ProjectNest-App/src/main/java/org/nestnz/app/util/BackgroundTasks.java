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
package org.nestnz.app.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BackgroundTasks {

    private static final Logger LOG = Logger.getLogger(BackgroundTasks.class.getName());
    
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(0);
    
    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5, runnable -> {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setName("BackgroundThread-" + THREAD_NUMBER.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    });
    
    public static void runInBackground (Runnable runnable) {
    	executorService.execute(() -> {
    		try {
    			runnable.run();
    		} catch (RuntimeException ex) {
    			LOG.log(Level.SEVERE, "Error executing task", ex);
    		}
    	});
    }
    
    /**
     * Schedule a task to run repeatedly, based on the provided {@code delay}
     * @param runnable The task to execute
     * @param delay The delay, in {@code unit}, between runs of the task
     * @param unit The {@link TimeUnit} of the {@code delay} parameter
     * @return A {@link ScheduledFuture} which can be used to cancel the task
     */
    public static ScheduledFuture<?> scheduleRepeating (Runnable runnable, long delay, TimeUnit unit) {
    	return executorService.scheduleAtFixedRate(() -> {
    		try {
    			runnable.run();
    		} catch (RuntimeException ex) {
    			LOG.log(Level.SEVERE, "Error executing task", ex);
    		}
    	}, delay, delay, unit);
    }
}
