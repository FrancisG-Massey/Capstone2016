package org.nestnz.app.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.TrapStatus;
import org.nestnz.app.model.Trapline;

import javafx.embed.swing.JFXPanel;

public class TestNavigationView {
	
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
	
	
	NavigationView navView;
	
	Trapline trapline;
	
	Trap trap1;	
	Trap trap2;
	Trap trap3;	

	@Before
	public void setUp() throws Exception {
		navView = new NavigationView(true);
		trapline = new Trapline(10, "Test trapline", new Region(20, "Test Region"), "Test Start");
		trap1 = new Trap(1, 1, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now());
		trap2 = new Trap(2, 2, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now());
		trap3 = new Trap(3, 3, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now());
	}

	@After
	public void tearDown() throws Exception {
		navView = null;
	}

	@Test
	public void testStartOfTrapline() {
		trapline.getTraps().add(trap1);
		trapline.getTraps().add(trap3);
		navView.setTrapline(trapline);
		
		assertFalse(navView.hasPreviousTrap());
		assertTrue(navView.hasNextTrap());
	}

	@Test
	public void testOneTrap() {
		trapline.getTraps().add(trap1);
		navView.setTrapline(trapline);
		assertFalse(navView.hasNextTrap());
		assertFalse(navView.hasPreviousTrap());
	}
	
	@Test
	public void testTrapNextButtonVisible () {
		trapline.getTraps().add(trap1);
		trapline.getTraps().add(trap3);
		navView.setTrapline(trapline);
		
		assumeTrue(navView.hasNextTrap());
		assertTrue(navView.next.isVisible());
	}
	
	@Test
	public void testTrapPrevButtonVisible () {
		trapline.getTraps().add(trap1);
		trapline.getTraps().add(trap3);
		navView.setTrapline(trapline);
		
		navView.nextTrap();
		
		assumeTrue(navView.hasPreviousTrap());
		assertTrue(navView.prev.isVisible());
	}
	
	@Test
	public void testTrapNextButtonNotVisible () {
		trapline.getTraps().add(trap1);
		navView.setTrapline(trapline);
		
		assumeFalse(navView.hasNextTrap());
		assertFalse(navView.next.isVisible());
	}
	
	@Test
	public void testTrapPrevButtonNotVisible () {
		trapline.getTraps().add(trap1);
		navView.setTrapline(trapline);
		
		assumeFalse(navView.hasPreviousTrap());
		assertFalse(navView.prev.isVisible());
	}

	@Test
	public void testTrapsUnordered() {
		trapline.getTraps().add(trap1);
		trapline.getTraps().add(trap3);
		trapline.getTraps().add(trap2);
		navView.setTrapline(trapline);
		
		assertEquals(trap1, navView.trapProperty.getValue());
		navView.nextTrap();
		assertEquals(trap2, navView.trapProperty.getValue());
		navView.nextTrap();
		assertEquals(trap3, navView.trapProperty.getValue());
	}

}
