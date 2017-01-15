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
 * @since 28.12.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import static midi.pad.ui.event.Runtime.getRuntime;

import jmidi.gui.model.IntegerModel;
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
public class NumberPad extends Widget {
	private final Runnable finishRunner;
	private final Runnable valueRunner;
	private static boolean callValueRunner = true;
	private final IntegerModel model;

	public NumberPad(final int x, final int y, final IntegerModel model,
			final Runnable finishRunner, final Runnable valueRunner) {
		bounds.x = x;
		bounds.y = y;
		bounds.height = 5;
		bounds.width = 3;
		this.finishRunner = finishRunner;
		this.valueRunner = valueRunner;
		this.model = model;
	}

	public NumberPad(final int x, final int y, final int maxValue, final int currentValue,
			final Runnable finishRunner, final Runnable valueRunner) {
		bounds.x = x;
		bounds.y = y;
		bounds.height = 5;
		bounds.width = 3;
		this.finishRunner = finishRunner;
		this.valueRunner = valueRunner;
		model = new IntegerModel(0, maxValue, currentValue);
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
	 * @see midi.pad.ui.Widget#eventOccured(midi.pad.ui.event.Event)
	 */
	@Override
	public boolean eventOccured(final Event event) {
		if (event != null && EVENT_TYPE.PAD_RELEASED.equals(event.getEventType())) {
			final PadEvent padEvent = PadEvent.getEvent(event);
			int currentValue = model.getValue();
			if (padEvent.getY() == 4) {
				if (padEvent.getX() == 0) {
					callValueRunner = !callValueRunner;
				} else if (padEvent.getX() == 1) {
					getRuntime().schedule(valueRunner);
				} else {
					getRuntime().schedule(finishRunner);
					return true;
				}
			} else if (padEvent.getY() == 3) {
				if (padEvent.getX() == 0) {
					currentValue = currentValue / 10;
				} else if (padEvent.getX() == 1) {
					currentValue = currentValue * 10;
				} else {
					getRuntime().schedule(finishRunner);
					return true;
				}
			} else {
				currentValue = currentValue * 10 + 1 + (2 - padEvent.getY()) * 3 + padEvent.getX();
			}
			model.setValue(currentValue);
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
		return model.getValue();
	}
}
