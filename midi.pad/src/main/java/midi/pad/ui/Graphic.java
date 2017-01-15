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
		if (c != null && x >= 0 && y >= 0 && x < viewPort.width && y < viewPort.height) {
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
