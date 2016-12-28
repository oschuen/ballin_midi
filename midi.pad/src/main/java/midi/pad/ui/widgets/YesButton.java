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
 * @since 27.12.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import midi.pad.ui.Color;
import midi.pad.ui.Graphic;

/**
 * @author oliver
 *
 */
public class YesButton extends Button {

	public YesButton(final int x, final int y, final Runnable pressRunner) {
		super(x, y, 4, 4, pressRunner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		g.fill(Color.BLACK);
		g.setPixel(0, 2, Color.FULL_GREEN);
		g.setPixel(1, 3, Color.FULL_GREEN);
		g.setPixel(2, 2, Color.FULL_GREEN);
		g.setPixel(3, 1, Color.FULL_GREEN);
		g.setPixel(3, 0, Color.FULL_GREEN);
	}
}
