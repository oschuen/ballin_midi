/**
 * Copyright (C) 2016 Oliver Schünemann
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
 * 
 * @since 30.10.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui;

import static org.junit.Assert.assertEquals;

import java.awt.Rectangle;

import org.junit.Test;

/**
 * @author oliver
 *
 */
public class GraphicTest {

	@Test
	public void testViewPort() {
		final Graphic g = new Graphic();
		final Graphic g2 = g.create(5, 6, 8, 8);
		final Rectangle r = g2.getViewPort();

		assertEquals(5, r.x);
		assertEquals(6, r.y);
		assertEquals(3, r.width);
		assertEquals(2, r.height);
	}

	@Test
	public void testViewPortNegativ() {
		final Graphic g = new Graphic();
		final Graphic g2 = g.create(5, 6, -5, -3);
		final Rectangle r = g2.getViewPort();

		assertEquals(5, r.x);
		assertEquals(6, r.y);
		assertEquals(0, r.width);
		assertEquals(0, r.height);
	}

	@Test
	public void testViewPortOverlap() {
		final Graphic g = new Graphic();
		final Graphic g2 = g.create(-5, -6, 32, 32);
		final Rectangle r = g2.getViewPort();

		assertEquals(0, r.x);
		assertEquals(0, r.y);
		assertEquals(8, r.width);
		assertEquals(8, r.height);
	}

}
