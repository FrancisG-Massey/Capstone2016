package org.nestnz.app.services;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

import java.io.IOException;
import java.nio.file.DirectoryStream;
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
		clearDirectory(cachePath);
		dataService = new TrapDataService(cachePath);
	}
	
	/**
	 * Clears the specified directory of all top-level files & deletes the directory
	 * @param path
	 * @throws IOException
	 */
	private void clearDirectory (Path path) throws IOException {
		if (Files.exists(path) && Files.isDirectory(path)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
		    	for (Path traplineFile : stream) {
		    		Files.delete(traplineFile);
		    	}
			}
		}
		Files.delete(path);
	}

	@After
	public void tearDown() throws Exception {
		dataService = null;
		clearDirectory(cachePath);
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
		
		Trapline trapline = future.get();
		
		Trapline oracle = new Trapline(20, "Test trapline", null, "Test Start");
				
		//Check if the trapline itself is equal to the expected trapline
		assertEquals(oracle, trapline);
		
		//Make sure the trapline has 3 traps
		assertEquals(3, trapline.getTraps().size());
		
		Trap oracleTrap = new Trap(2, null, 4.7238, 50.8456, null, LocalDateTime.parse("2016-08-16T10:37:07.565"), null);
		
		//Check one of the traps to ensure it loaded correctly
		assertEquals(oracleTrap, trapline.getTraps().get(1));    	
	}

}
