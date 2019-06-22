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
 * @since 21.06.2019
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import static midi.pad.ui.event.Runtime.getRuntime;

import midi.pad.ui.Color;
import midi.pad.ui.Graphic;
import midi.pad.ui.Widget;
import midi.pad.ui.event.Event;
import midi.pad.ui.event.PadEvent;
import midi.pad.ui.event.PadEvent.EVENT_TYPE;

/**
 * <pre>
 * {@code
 *  Note Edit
 *  Channel In Config
 *  Channel Out Config
 *  Loop, Through, off
 *  Model 1
 *  Model 2
 *  Model 3
 *  Model 4
 *  
 *  Receive (none, low, high, all)
 *  }
 * </pre>
 * 
 * @author oliver
 *
 */
public class TrackConfig extends Widget {

	private final Runnable noteEditRunnable;
	private final Runnable channelInConfigRunnable;
	private final Runnable channelOutConfigRunnable;
	private final Runnable changeRunnable;
	private PlayMode mode;
	private long lastModeChange;
	private PlayMode lastBeforeOff;
	private boolean in;
	private final int[] model = { 1, 0, 0, 0 };
	private static final long offTime = 1000;

	public enum PlayMode {
		OFF, THROUGH, LOOP
	}

	public TrackConfig(final int y, final Runnable noteEditRunnable,
			final Runnable channelInConfigRunnable, final Runnable channelOutConfigRunnable,
			final Runnable changeRunnable) {
		super();
		bounds.x = 0;
		bounds.y = y;
		bounds.width = 8;
		bounds.height = 1;
		this.noteEditRunnable = noteEditRunnable;
		this.channelInConfigRunnable = channelInConfigRunnable;
		this.channelOutConfigRunnable = channelOutConfigRunnable;
		this.changeRunnable = changeRunnable;
		lastModeChange = System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		g.setPixel(0, 0, Color.GREEN);
		g.setPixel(1, 0, in ? Color.RED : Color.BLACK);
		g.setPixel(2, 0, Color.FULL_AMBER);
		if (mode == PlayMode.OFF) {
			g.setPixel(3, 0, Color.BLACK);
		} else if (mode == PlayMode.THROUGH) {
			g.setPixel(3, 0, Color.GREEN);
		} else {
			g.setPixel(3, 0, Color.RED);
		}
		for (int i = 0; i < 4; i++) {
			switch (model[i]) {
			case 0:
				g.setPixel(4 + i, 0, Color.BLACK);
				break;
			case 1:
				g.setPixel(4 + i, 0, Color.GREEN);
				break;
			case 2:
				g.setPixel(4 + i, 0, Color.RED);
				break;
			case 3:
				g.setPixel(4 + i, 0, Color.FULL_AMBER);
				break;
			case 4:
				g.setPixel(4 + i, 0, Color.LOW_AMBER);
				break;
			}
		}
	}

	private void increaseMode() {
		if (System.currentTimeMillis() > lastModeChange + offTime) {
			if (mode == PlayMode.OFF) {
				if (lastBeforeOff == PlayMode.OFF) {
					mode = PlayMode.THROUGH;
				} else {
					mode = lastBeforeOff;
				}
			} else {
				lastBeforeOff = mode;
				mode = PlayMode.OFF;
			}
		} else {
			if (mode == PlayMode.OFF) {
				mode = PlayMode.THROUGH;
			} else if (mode == PlayMode.THROUGH) {
				mode = PlayMode.LOOP;
			} else {
				mode = PlayMode.OFF;
			}
		}
		lastModeChange = System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#eventOccured(midi.pad.ui.event.Event)
	 */
	@Override
	public boolean eventOccured(final Event event) {
		boolean changed = false;
		if (event != null && EVENT_TYPE.PAD_RELEASED.equals(event.getEventType())) {
			final PadEvent padEvent = PadEvent.getEvent(event);

			switch (padEvent.getX()) {
			case 0:
				getRuntime().schedule(noteEditRunnable);
				break;
			case 1:
				if (in) {
					getRuntime().schedule(channelInConfigRunnable);
				}
				break;
			case 2:
				getRuntime().schedule(channelOutConfigRunnable);
				break;
			case 3:
				increaseMode();
				changed = true;
				break;
			default:
				model[padEvent.getX() - 4] = (model[padEvent.getX() - 4] + 1) % 5;
				changed = true;
			}
		}
		if (changed) {
			getRuntime().schedule(changeRunnable);
		}
		return true;
	}

	/**
	 * @return the in
	 */
	public boolean isIn() {
		return in;
	}

	/**
	 * @param in
	 *            the in to set
	 */
	public void setIn(final boolean in) {
		this.in = in;
	}

	/**
	 * @return the mode
	 */
	public PlayMode getMode() {
		return mode;
	}

	/**
	 * @return the model
	 */
	public int getModel(final int x) {
		if (x >= 0 && x < model.length) {
			return model[x];
		}
		return 0;
	}
}
