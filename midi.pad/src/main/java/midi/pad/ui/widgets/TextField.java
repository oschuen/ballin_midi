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

import java.util.concurrent.TimeUnit;

import midi.device.launchpad.Font;
import midi.pad.ui.Color;
import midi.pad.ui.Graphic;
import midi.pad.ui.Widget;
import midi.pad.ui.event.PadEvent;
import midi.pad.ui.event.Runtime;

/**
 * @author oliver
 *
 */
public class TextField extends Widget {

	private final String text;
	private int offset = 0;

	public TextField(final int x, final int y, final String text, final Runnable finishRunner) {
		this.text = "  " + text.trim();
		bounds.x = x;
		bounds.y = y;
		bounds.height = 8;
		bounds.width = 8;

		Runtime.getRuntime().scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (offset < TextField.this.text.length() * 6) {
					offset++;
				} else {
					getRuntime().stop(this);
					getRuntime().schedule(finishRunner);
				}
				Runtime.getRuntime().invalidate();
			}
		}, 75, 75, TimeUnit.MILLISECONDS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		final int mask[] = Font.getBitmap(text, offset);
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				g.setPixel(x, y, ((mask[y] & (0x01 << x)) > 0) ? Color.FULL_GREEN : Color.BLACK);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#padEventOccured(midi.pad.ui.event.PadEvent)
	 */
	@Override
	public boolean padEventOccured(final PadEvent event) {
		return true;
	}
}
