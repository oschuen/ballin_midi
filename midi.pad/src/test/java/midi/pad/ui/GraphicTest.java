/**
 * Copyright (C) 2016 Oliver Schünemann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
