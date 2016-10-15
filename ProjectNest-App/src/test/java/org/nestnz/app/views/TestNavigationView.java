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
	Trap trap4;

	@Before
	public void setUp() throws Exception {
		navView = new NavigationView(true);
		trapline = new Trapline(10, "Test trapline", new Region(20, "Test Region"), "Test Start");
		trap1 = new Trap(100, 1, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
		trap2 = new Trap(102, 2, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
		trap3 = new Trap(101, 3, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
		trap4 = new Trap(99, 4, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
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
	
	@Test
	public void testEarlyFinishSequence() {
		trapline.getTraps().add(trap1);
		trapline.getTraps().add(trap3);
		trapline.getTraps().add(trap2);
		trapline.getTraps().add(trap4);
		navView.setTrapline(trapline);
		
		navView.setNavigationSequence(1, 3, 1);
		
		assertFalse(navView.hasPreviousTrap());
		
		assertEquals(trap1, navView.trapProperty.getValue());
		navView.nextTrap();
		assertEquals(trap2, navView.trapProperty.getValue());
		navView.nextTrap();
		assertEquals(trap3, navView.trapProperty.getValue());
		
		assertFalse(navView.hasNextTrap());//This should be the last trap in the sequence, as we finish on #3
	}
	
	@Test
	public void testLateStartSequence() {
		trapline.getTraps().add(trap1);
		trapline.getTraps().add(trap3);
		trapline.getTraps().add(trap2);
		trapline.getTraps().add(trap4);
		navView.setTrapline(trapline);
		
		navView.setNavigationSequence(2, 4, 1);
		
		assertFalse(navView.hasPreviousTrap());//This should be the first trap in the sequence
		
		assertEquals(trap2, navView.trapProperty.getValue());
		navView.nextTrap();
		assertEquals(trap3, navView.trapProperty.getValue());
		navView.nextTrap();
		assertEquals(trap4, navView.trapProperty.getValue());
		
		assertFalse(navView.hasNextTrap());
	}
	
	@Test
	public void testReverseSequence() {
		trapline.getTraps().add(trap1);
		trapline.getTraps().add(trap3);
		trapline.getTraps().add(trap2);
		trapline.getTraps().add(trap4);
		navView.setTrapline(trapline);
		
		navView.setNavigationSequence(4, 2, -1);
		
		assertFalse("Previous trap should not exist!", navView.hasPreviousTrap());//This should be the first trap in the sequence
		
		assertEquals(trap4, navView.trapProperty.getValue());
		navView.nextTrap();
		assertEquals(trap3, navView.trapProperty.getValue());
		navView.nextTrap();
		assertEquals(trap2, navView.trapProperty.getValue());
		
		assertFalse(navView.hasNextTrap());
	}
	
	@Test
	public void testJumpSequence() {
		trapline.getTraps().add(trap1);
		trapline.getTraps().add(trap3);
		trapline.getTraps().add(trap2);
		trapline.getTraps().add(trap4);
		navView.setTrapline(trapline);
		
		navView.setNavigationSequence(1, 4, 2);
		
		assertFalse(navView.hasPreviousTrap());//This should be the first trap in the sequence
		
		assertEquals(trap1, navView.trapProperty.getValue());
		navView.nextTrap();
		assertEquals(trap3, navView.trapProperty.getValue());
		
		assertFalse(navView.hasNextTrap());//This should be the last trap in the sequence, as trap 4 is less than 2 away from trap 3

	}
	
	@Test
	public void testTwoTrapSequence() {
		trapline.getTraps().add(trap1);
		trapline.getTraps().add(trap2);
		navView.setTrapline(trapline);
		
		navView.setNavigationSequence(1, 2, 1);
		
		assertFalse(navView.hasPreviousTrap());//This should be the first trap in the sequence
		
		assertEquals(trap1, navView.trapProperty.getValue());
		navView.nextTrap();
		assertEquals(trap2, navView.trapProperty.getValue());
		
		assertFalse(navView.hasNextTrap());

	}

}
