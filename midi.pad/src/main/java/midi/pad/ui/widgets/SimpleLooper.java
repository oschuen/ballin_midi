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

import midi.loop.LoopEvent;
import midi.loop.LoopEvent.COMMAND;
import midi.loop.LoopModel;
import midi.loop.beat.Beat;
import midi.loop.beat.Beat.BeatListener;
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
public class SimpleLooper extends Widget implements BeatListener {

	private final LoopModel defaultModel = new LoopModel();
	private LoopModel loopModel = defaultModel;
	private int holdPage = 0;
	private int holdQuarter = 0;
	private boolean hold = true;
	private long beat = 0;

	/**
	 * 
	 */
	public SimpleLooper(final int x, final int y) {
		bounds.x = x;
		bounds.y = y;
		bounds.width = 8;
		bounds.height = 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		final int page;
		final int quarter;
		final int quarterDivision = loopModel.getQuarterDivision();
		final int quarterPerPage = loopModel.getQuarterPerPage();
		final int numberOfPages = loopModel.getNumberOfPages();
		final long division = Beat.BEAT_DIVISION / quarterDivision;
		if (hold) {
			page = holdPage;
			quarter = holdQuarter;
		} else {
			page = (int) ((beat / division / quarterPerPage / quarterDivision) % numberOfPages);
			quarter = (int) ((beat / division / quarterDivision) % quarterPerPage);
		}
		for (int i = 0; i < bounds.width; ++i) {
			final int step = i + (page * quarterPerPage + quarter) * quarterDivision;
			if (loopModel.getStepEvent(step).isPresent()) {
				g.setPixel(i, 0, Color.FULL_RED);
			} else {
				if ((step / quarterDivision) % 2 == 0) {
					g.setPixel(i, 0, Color.LOW_AMBER);
				} else {
					g.setPixel(i, 0, Color.FULL_AMBER);
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
		final int page;
		final int quarter;
		final int quarterDivision = loopModel.getQuarterDivision();
		final int quarterPerPage = loopModel.getQuarterPerPage();
		final long division = Beat.BEAT_DIVISION / quarterDivision;
		if (hold) {
			page = holdPage;
			quarter = holdQuarter;
		} else {
			page = (int) ((beat / division / quarterPerPage / quarterDivision)
					% loopModel.getNumberOfPages());
			quarter = (int) ((beat / division / quarterDivision) % quarterPerPage);
		}

		if (EVENT_TYPE.PAD_RELEASED.equals(event.getEventType())) {
			final PadEvent padEvent = (PadEvent) event;
			final int step = padEvent.getX() + (page * quarterPerPage + quarter) * quarterDivision;
			if (loopModel.getStepEvent(step).isPresent()) {
				loopModel.setStepEvent(null, step);
			} else {
				loopModel.setStepEvent(new LoopEvent(COMMAND.NOTE_ON, 127, 0), step);
			}
		}
		return true;
	}

	/**
	 * @param loopModel
	 *            the loopModel to set
	 */
	public void setLoopModel(final LoopModel loopModel) {
		if (loopModel == null) {
			this.loopModel = defaultModel;
		} else {
			this.loopModel = loopModel;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BeatListener#accept(long)
	 */
	@Override
	public void accept(final long beat) {
		final long division = Beat.BEAT_DIVISION / loopModel.getQuarterDivision();
		if (beat % (division) == 0) {
			this.beat = beat;
			getRuntime().invalidate();
		}
	}

	/**
	 * @return the holdPage
	 */
	public int getHoldPage() {
		return holdPage;
	}

	/**
	 * @param holdPage
	 *            the holdPage to set
	 */
	public void setHoldPage(final int holdPage) {
		this.holdPage = holdPage;
	}

	/**
	 * @return the holdQuarter
	 */
	public int getHoldQuarter() {
		return holdQuarter;
	}

	/**
	 * @param holdQuarter
	 *            the holdQuarter to set
	 */
	public void setHoldQuarter(final int holdQuarter) {
		this.holdQuarter = holdQuarter;
	}

	/**
	 * @return the hold
	 */
	public boolean isHold() {
		return hold;
	}

	/**
	 * @param hold
	 *            the hold to set
	 */
	public void setHold(final boolean hold) {
		this.hold = hold;
	}
}
