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

import java.awt.Rectangle;

/**
 * @author oliver
 *
 */
public class Graphic {

	private final Rectangle viewPort = new Rectangle();

	private final Color screen[][];

	private final int WIDTH = 8;
	private final int HEIGHT = 8;

	public Graphic() {
		screen = new Color[8][8];
		viewPort.x = 0;
		viewPort.y = 0;
		viewPort.width = WIDTH;
		viewPort.height = HEIGHT;
		fill(Color.TRANSPARENT);
	}

	private Graphic(final Color screen[][], final int x, final int y, final int width,
			final int height) {
		this.screen = screen;
		viewPort.setBounds(x, y, width, height);
	}

	public void setPixel(final int x, final int y, final Color c) {
		if (c != null && x >= 0 && y >= 0 && x < viewPort.width && y < viewPort.height
				&& c != null) {
			screen[viewPort.y + y][viewPort.x + x] = c;
		}
	}

	public Color getPixel(final int x, final int y) {
		if (x >= 0 && y >= 0 && x < viewPort.width && y < viewPort.height) {
			return screen[viewPort.y + y][viewPort.x + x];
		}
		return null;
	}

	public void fill(final Color color) {
		if (color != null) {
			for (int i = 0; i < viewPort.height; i++) {
				for (int j = 0; j < viewPort.width; j++) {
					screen[viewPort.y + i][viewPort.x + j] = color;
				}
			}
		}
	}

	public Graphic create(final int x, final int y, final int width, final int height) {
		final int x1 = Math.min(Math.max(viewPort.x, x), viewPort.x + viewPort.width - 1);
		final int y1 = Math.min(Math.max(viewPort.y, y), viewPort.y + viewPort.height - 1);
		final int x2 = Math.min(Math.min(viewPort.x + viewPort.width, Math.max(x, x + width)), 8);
		final int y2 = Math.min(Math.min(viewPort.y + viewPort.height, Math.max(y, y + height)), 8);

		return new Graphic(screen, x1, y1, x2 - x1, y2 - y1);
	}

	/**
	 * @return the viewPort
	 */
	public Rectangle getViewPort() {
		return new Rectangle(viewPort);
	}

}
