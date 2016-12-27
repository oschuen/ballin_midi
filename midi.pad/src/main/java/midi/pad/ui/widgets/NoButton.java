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

import static midi.pad.ui.event.Runtime.getRuntime;

import midi.pad.ui.Color;
import midi.pad.ui.Graphic;
import midi.pad.ui.Widget;
import midi.pad.ui.event.PadEvent;
import midi.pad.ui.event.PadEvent.EVENT_TYPE;

/**
 * @author oliver
 *
 */
public class NoButton extends Widget {

	private final Runnable pressRunner;

	public NoButton(final int x, final int y, final Runnable pressRunner) {
		bounds.x = x;
		bounds.y = y;
		bounds.width = 4;
		bounds.height = 4;
		this.pressRunner = pressRunner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		g.fill(Color.BLACK);
		g.setPixel(0, 0, Color.FULL_RED);
		g.setPixel(1, 1, Color.FULL_RED);
		g.setPixel(2, 2, Color.FULL_RED);
		g.setPixel(3, 3, Color.FULL_RED);
		g.setPixel(3, 0, Color.FULL_RED);
		g.setPixel(2, 1, Color.FULL_RED);
		g.setPixel(1, 2, Color.FULL_RED);
		g.setPixel(0, 3, Color.FULL_RED);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#padEventOccured(midi.pad.ui.event.PadEvent)
	 */
	@Override
	public boolean padEventOccured(final PadEvent event) {
		if (event != null && EVENT_TYPE.RELEASED.equals(event.getEventType())) {
			getRuntime().schedule(pressRunner);
		}
		return true;
	}

}
