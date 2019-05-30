/**
 * Copyright (C) 2019 Oliver Schünemann
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
 * @since 30.05.2019
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import static midi.pad.ui.event.Runtime.getRuntime;

import midi.instrument.Instrument;
import midi.pad.ui.Color;
import midi.pad.ui.Graphic;
import midi.pad.ui.Widget;
import midi.pad.ui.event.Event;
import midi.pad.ui.event.PadEvent;
import midi.pad.ui.event.PadEvent.EVENT_TYPE;

/**
 * @author oliver
 *
 */
public class InstrumentSelector extends Widget {
	private final Runnable pressRunner;
	private final Instrument[][] instruments;
	private int selectX = 0;
	private int selectY = 0;

	public InstrumentSelector(final int x, final int y, final int width, final int height,
			final Instrument[] instruments, final Runnable pressRunner) {
		bounds.x = x;
		bounds.y = y;
		bounds.width = width;
		bounds.height = height;
		this.pressRunner = pressRunner;
		this.instruments = new Instrument[width][height];
		for (int i = 0; i < instruments.length; i++) {
			this.instruments[i % width][i / width] = instruments[i];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		for (int x = 0; x < bounds.width; ++x) {
			for (int y = 0; y < bounds.height; ++y) {
				if (x == selectX && y == selectY) {
					g.setPixel(x, y, Color.FULL_RED);
				} else if (instruments[x][y] == null) {
					g.setPixel(x, y, Color.BLACK);
				} else {
					g.setPixel(x, y, Color.FULL_GREEN);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#eventOccured(midi.pad.ui.event.Event)
	 */
	@Override
	public boolean eventOccured(final Event event) {
		if (event != null && EVENT_TYPE.PAD_RELEASED.equals(event.getEventType())) {
			final PadEvent padEvent = PadEvent.getEvent(event);
			if (instruments[padEvent.getX()][padEvent.getY()] != null) {
				selectX = padEvent.getX();
				selectY = padEvent.getY();
				getRuntime().schedule(pressRunner);
			}
		}
		return true;
	}

	public Instrument getInstrument() {
		return instruments[selectX][selectY];
	}
}
