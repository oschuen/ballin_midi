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
 * @since 22.12.2019
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
 * @author oliver
 *
 */
public class SongSelection extends Widget {

	private final boolean[] matrix;
	private int currentSong;
	private int selection;
	private final Runnable selectRunnable;
	private final Runnable deleteRunnable;
	private final Runnable copyRunnable;
	private int copyFrom;
	private int copyFromCount = 0;

	public SongSelection(final int currentSong, final boolean[] matrix,
			final Runnable selectRunnable, final Runnable deleteRunnable,
			final Runnable copyRunnable) {
		bounds.x = 0;
		bounds.y = 0;
		bounds.width = 8;
		bounds.height = 8;
		this.matrix = matrix;
		this.currentSong = currentSong;
		selection = currentSong;
		this.selectRunnable = selectRunnable;
		this.copyRunnable = copyRunnable;
		this.deleteRunnable = deleteRunnable;
		copyFrom = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		for (int y = 0; y < 8; ++y) {
			for (int x = 0; x < 8; ++x) {
				final int song = y * 8 + x;
				if (song == copyFrom) {
					g.setPixel(x, y, Color.FULL_RED);
				} else if (song == currentSong) {
					g.setPixel(x, y, Color.FULL_GREEN);
				} else if (matrix[song]) {
					g.setPixel(x, y, Color.FULL_YELLOW);
				} else {
					g.setPixel(x, y, Color.BLACK);
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
		if (event != null && EVENT_TYPE.PAD_HOLD.equals(event.getEventType())) {
			final PadEvent padEvent = PadEvent.getEvent(event);
			copyFrom = padEvent.getY() * 8 + padEvent.getX();
			copyFromCount++;
			if (copyFromCount == 10) {
				selection = copyFrom;
				getRuntime().schedule(deleteRunnable);
			}
			getRuntime().invalidate();
		} else if (event != null && EVENT_TYPE.PAD_PRESSED.equals(event.getEventType())) {
			final PadEvent padEvent = PadEvent.getEvent(event);
			selection = padEvent.getY() * 8 + padEvent.getX();
			if (copyFrom >= 0 && selection != copyFrom) {
				getRuntime().schedule(copyRunnable);
			} else {
				getRuntime().schedule(selectRunnable);
			}
		} else if (event != null && EVENT_TYPE.PAD_RELEASED.equals(event.getEventType())) {
			reset();
		}
		return true;
	}

	/**
	 * @param selection
	 *            the selection to set
	 */
	public int getSelection() {
		return selection;
	}

	/**
	 * @param currentSong
	 *            the currentSong to set
	 */
	public void setCurrentSong(final int currentSong) {
		this.currentSong = currentSong;
	}

	/**
	 * @return the copyFrom
	 */
	public int getCopyFrom() {
		return copyFrom;
	}

	public void reset() {
		copyFrom = -1;
		selection = currentSong;
		copyFromCount = 0;
	}

}
