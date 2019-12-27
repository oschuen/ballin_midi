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
 * @since 23.12.2019
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import static midi.pad.ui.widgets.LoopRecordWidget.RECORD_MODE.CLEAR;
import static midi.pad.ui.widgets.LoopRecordWidget.RECORD_MODE.FILL;
import static midi.pad.ui.widgets.LoopRecordWidget.RECORD_MODE.FILL_RANDOM;
import static midi.pad.ui.widgets.LoopRecordWidget.RECORD_MODE.NOTE_HOLD;
import static midi.pad.ui.widgets.LoopRecordWidget.RECORD_MODE.NOTE_OFF;
import static midi.pad.ui.widgets.LoopRecordWidget.RECORD_MODE.NOTE_ON;
import static midi.pad.ui.widgets.LoopRecordWidget.RECORD_MODE.OFF;

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
public class LoopRecordWidget extends Widget {

	private int steps = 0;
	private int length = 0;

	public enum RECORD_MODE {
		OFF, NOTE_ON, NOTE_OFF, NOTE_HOLD, FILL, FILL_RANDOM, CLEAR
	}

	private RECORD_MODE mode = OFF;
	private final Runnable modeChangeRunner;

	public LoopRecordWidget(final int y, final Runnable modeChangeRunner) {
		bounds.x = 0;
		bounds.y = y;
		bounds.width = 8;
		bounds.height = 3;
		this.modeChangeRunner = modeChangeRunner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		g.fill(Color.FULL_AMBER);
		if (steps == 0) {
			g.setPixel(0, 1, Color.FULL_GREEN);
		} else {
			g.setPixel(1 + steps, 1, Color.FULL_GREEN);
		}
		if (length == 0) {
			g.setPixel(0, 2, Color.FULL_RED);
		} else {
			g.setPixel(1 + length, 2, Color.FULL_RED);
		}
		g.setPixel(1, 1, Color.BLACK);
		g.setPixel(1, 2, Color.BLACK);
		g.setPixel(7, 0, Color.BLACK);
		if (mode == OFF) {
			g.setPixel(0, 0, Color.FULL_AMBER);
		} else {
			g.setPixel(0, 0, Color.LOW_AMBER);
		}
		if (mode == NOTE_ON) {
			g.setPixel(1, 0, Color.FULL_GREEN);
		} else {
			g.setPixel(1, 0, Color.LOW_GREEN);
		}
		if (mode == NOTE_OFF) {
			g.setPixel(2, 0, Color.FULL_RED);
		} else {
			g.setPixel(2, 0, Color.LOW_RED);
		}
		if (mode == NOTE_HOLD) {
			g.setPixel(3, 0, Color.FULL_AMBER);
		} else {
			g.setPixel(3, 0, Color.LOW_AMBER);
		}
		if (mode == FILL) {
			g.setPixel(4, 0, Color.FULL_GREEN);
		} else {
			g.setPixel(4, 0, Color.LOW_GREEN);
		}
		if (mode == FILL_RANDOM) {
			g.setPixel(5, 0, Color.FULL_GREEN);
		} else {
			g.setPixel(5, 0, Color.LOW_GREEN);
		}
		if (mode == CLEAR) {
			g.setPixel(6, 0, Color.FULL_RED);
		} else {
			g.setPixel(6, 0, Color.LOW_RED);
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
			if (padEvent.getY() == 1) {
				if (padEvent.getX() == 0) {
					steps = 0;
				} else if (padEvent.getX() > 1) {
					steps = padEvent.getX() - 1;
				}
			} else if (padEvent.getY() == 2) {
				if (padEvent.getX() == 0) {
					length = 0;
				} else if (padEvent.getX() > 1) {
					length = padEvent.getX() - 1;
				}
			} else {
				switch (padEvent.getX()) {
				case 0:
					mode = OFF;
					break;
				case 1:
					mode = NOTE_ON;
					break;
				case 2:
					mode = NOTE_OFF;
					break;
				case 3:
					mode = NOTE_HOLD;
					break;
				case 4:
					mode = FILL;
					break;
				case 5:
					mode = FILL_RANDOM;
					break;
				case 6:
					mode = CLEAR;
					break;
				default:
					break;
				}
			}
			Runtime.getRuntime().schedule(modeChangeRunner);
		}
		return true;
	}

	/**
	 * @return the steps
	 */
	public int getSteps() {
		return steps;
	}

	/**
	 * @return the mode
	 */
	public RECORD_MODE getMode() {
		return mode;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

}
