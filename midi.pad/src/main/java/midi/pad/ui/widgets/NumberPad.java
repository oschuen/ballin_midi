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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
	private final PropertyChangeListener listener;

	public NumberPad(final int x, final int y, final int maxValue, final int currentValue,
			final Runnable finishRunner, final PropertyChangeListener listener) {
		bounds.x = x;
		bounds.y = y;
		bounds.height = 4;
		bounds.width = 3;
		this.maxValue = maxValue;
		this.currentValue = currentValue;
		this.finishRunner = finishRunner;
		this.listener = listener;
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#padEventOccured(midi.pad.ui.event.PadEvent)
	 */
	@Override
	public boolean padEventOccured(final PadEvent event) {
		final int oldValue = currentValue;
		if (event != null && EVENT_TYPE.RELEASED.equals(event.getEventType())) {
			if (event.getY() == 3) {
				if (event.getX() == 0) {
					currentValue = currentValue / 10;
				} else if (event.getX() == 0) {
					currentValue = currentValue * 10;
				} else {
					getRuntime().schedule(finishRunner);
					return true;
				}
			} else {
				currentValue = currentValue * 10 + 1 + (event.getY() - 2) * 3 + event.getX();
			}
			currentValue = Math.min(maxValue, currentValue);
			listener.propertyChange(new PropertyChangeEvent(this, "value", oldValue, currentValue));
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
