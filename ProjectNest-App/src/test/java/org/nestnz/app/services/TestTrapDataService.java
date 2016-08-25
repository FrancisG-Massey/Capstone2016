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
package org.nestnz.app.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;

import javafx.collections.ListChangeListener;
import javafx.embed.swing.JFXPanel;

public class TestTrapDataService {

    private static final Logger LOG = Logger.getLogger(TestTrapDataService.class.getName());
	
	@BeforeClass
    public static void initToolkit() throws InterruptedException {
		//Source: http://stackoverflow.com/questions/11273773/javafx-2-1-toolkit-not-initialized
		final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // initializes JavaFX environment
            latch.countDown();
        });

        if (!latch.await(5L, TimeUnit.SECONDS)) {
            throw new ExceptionInInitializerError();
        }
    }
	
	TrapDataService dataService;
	Path cachePath;

	@Before
	public void setUp() throws Exception {
		cachePath = Files.createTempDirectory("trapDataCache");
		dataService = new TrapDataService(cachePath.toFile());
	}

	@After
	public void tearDown() throws Exception {
		dataService = null;
	}

	/**
	 * Verifies that a file is actually saved when the "update" method is called.
	 * Does not ensure the file content is valid (this is a separate test case)
	 */
	@Test
	public void testFileSaved() {
    	assumeFalse(Files.exists(cachePath.resolve("20.json")));
		Trapline trapline = new Trapline(20, "Test trapline", new Region(20, "Test Region"), "Test Start");
    	dataService.updateTrapline(trapline);
    	assertTrue(Files.exists(cachePath.resolve("20.json")));
	}
	
	@Test
	public void testFileLoading () throws Exception {
		Path testData = Paths.get(TestTrapDataService.class.getResource("20.json").toURI());
		Files.copy(testData, cachePath.resolve("20.json"), StandardCopyOption.REPLACE_EXISTING);
		LOG.log(Level.INFO, String.format("Moved test resource from %s to %s", testData.toString(), cachePath.resolve("20.json").toString()));
		
		dataService.getTraplines().clear();
		dataService.loadTraplines();
		
		//Since traplines are loaded asynchronosly, we need to add a listener & wait for them to load
		CompletableFuture<Trapline> future = new CompletableFuture<Trapline>();
		dataService.getTraplines().addListener((ListChangeListener<Trapline>)change -> {
			change.next();
			if (change.wasAdded()) {
				future.complete(change.getAddedSubList().get(0));
			} else {
				fail("Invalid change type: "+change);
			}
		});
		
		Trapline trapline = future.get(2, TimeUnit.SECONDS);//Wait 2 seconds at most
		
		Trapline oracle = new Trapline(20, "Test trapline", null, "Test Start");
				
		//Check if the trapline itself is equal to the expected trapline
		assertEquals(oracle, trapline);
		
		//Make sure the trapline has 3 traps
		assertEquals(3, trapline.getTraps().size());
		
		Trap oracleTrap = new Trap(2, -40.311086, 175.775306, null, LocalDateTime.parse("2016-04-16T10:30:07"), LocalDateTime.parse("2016-08-16T10:30:07"));
		
		//Check one of the traps to ensure it loaded correctly
		assertEquals(oracleTrap, trapline.getTraps().get(1));    	
	}
	
	@Test
	public void testSaveAndLoad () throws Exception {
		Trapline trapline = new Trapline(20, "Test trapline", null, "Test Start");
		Trap trap1 = new Trap(1, -40.314206, 175.779946, null, LocalDateTime.parse("2016-04-16T10:26:07"), LocalDateTime.parse("2016-08-16T10:28:07"));
		Trap trap2 = new Trap(2, 4.7238, 50.8456, null, LocalDateTime.parse("2016-08-16T10:37:07.565"), null);
		trapline.getTraps().add(trap1);
		trapline.getTraps().add(trap2);
		
		dataService.updateTrapline(trapline);
    	assumeTrue(Files.exists(cachePath.resolve("20.json")));
    	
    	dataService.getTraplines().clear();
		dataService.loadTraplines();
		
		//Since traplines are loaded asynchronosly, we need to add a listener & wait for them to load
		CompletableFuture<Trapline> future = new CompletableFuture<Trapline>();
		dataService.getTraplines().addListener((ListChangeListener<Trapline>)change -> {
			change.next();
			if (change.wasAdded()) {
				future.complete(change.getAddedSubList().get(0));
			} else {
				fail("Invalid change type: "+change);
			}
		});
		
		Trapline result = future.get(2, TimeUnit.SECONDS);//Wait 2 seconds at most
		
		assertEquals(trapline, result);//Make sure the trapline itself remains the same
		
		assertEquals(2, trapline.getTraps().size());//Make sure the resulting trapline has 2 traps
		
		assertEquals(trap1, trapline.getTraps().get(0));//Make sure trap #1 is saved & loaded corr
		
		assertEquals(trap2, trapline.getTraps().get(1));//Make sure trap #2 is saved & loaded correctlyectly
	}

}
