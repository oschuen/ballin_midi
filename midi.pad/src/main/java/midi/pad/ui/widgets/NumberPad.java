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
 * @since 28.12.2016
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
public class NumberPad extends Widget {
	private final int maxValue;
	private int currentValue = 0;
	private final Runnable finishRunner;
	private final Runnable valueRunner;
	private static boolean callValueRunner = true;

	public NumberPad(final int x, final int y, final int maxValue, final int currentValue,
			final Runnable finishRunner, final Runnable valueRunner) {
		bounds.x = x;
		bounds.y = y;
		bounds.height = 5;
		bounds.width = 3;
		this.maxValue = maxValue;
		this.currentValue = currentValue;
		this.finishRunner = finishRunner;
		this.valueRunner = valueRunner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		g.fill(Color.FULL_YELLOW);
		g.setPixel(0, 3, Color.RED);
		g.setPixel(2, 3, Color.GREEN);
		g.setPixel(0, 4, Color.LOW_AMBER);
		g.setPixel(2, 4, Color.GREEN);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#padEventOccured(midi.pad.ui.event.PadEvent)
	 */
	@Override
	public boolean padEventOccured(final PadEvent event) {
		if (event != null && EVENT_TYPE.RELEASED.equals(event.getEventType())) {
			if (event.getY() == 4) {
				if (event.getX() == 0) {
					callValueRunner = !callValueRunner;
				} else if (event.getX() == 1) {
					getRuntime().schedule(valueRunner);
				} else {
					getRuntime().schedule(finishRunner);
					return true;
				}
			} else if (event.getY() == 3) {
				if (event.getX() == 0) {
					currentValue = currentValue / 10;
				} else if (event.getX() == 1) {
					currentValue = currentValue * 10;
				} else {
					getRuntime().schedule(finishRunner);
					return true;
				}
			} else {
				currentValue = currentValue * 10 + 1 + (2 - event.getY()) * 3 + event.getX();
			}
			currentValue = Math.min(maxValue, currentValue);
			if (callValueRunner) {
				getRuntime().schedule(valueRunner);
			}
		}
		return true;
	}

	/**
	 * @return the currentValue
	 */
	public int getCurrentValue() {
		return currentValue;
	}
}
