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
 * @since 20.12.2019
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import midi.pad.ui.Color;
import midi.pad.ui.Graphic;
import midi.pad.ui.Widget;
import midi.pad.ui.event.Event;
import midi.pad.ui.event.PadEvent;
import midi.pad.ui.event.PadEvent.EVENT_TYPE;
import midi.pad.ui.event.Runtime;

/**
 * @author oliver
 *
 */
public class MidiDeviceSelection extends Widget {

	private int currentMidi;
	private final Runnable pressRunner;

	/**
	 * 
	 */
	public MidiDeviceSelection(final int x, final int y, final int currentMidi,
			final Runnable pressRunner) {
		bounds.x = x;
		bounds.y = y;
		bounds.width = 4;
		bounds.height = 1;
		this.currentMidi = currentMidi;
		this.pressRunner = pressRunner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		for (int x = 0; x < 8; ++x) {
			if (x == currentMidi) {
				g.setPixel(x, 0, Color.FULL_GREEN);
			} else {
				g.setPixel(x, 0, Color.FULL_AMBER);
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
		if (EVENT_TYPE.PAD_RELEASED.equals(event.getEventType())) {
			final PadEvent padEvent = (PadEvent) event;
			final int chn = padEvent.getX();
			currentMidi = chn;
			Runtime.getRuntime().schedule(pressRunner);
		}
		return true;
	}

	/**
	 * @return the currentMidi
	 */
	public int getCurrentMidi() {
		return currentMidi;
	}
}
