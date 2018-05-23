/**
 * Copyright (C) 2018 Oliver Schünemann
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
 * @since 29.04.2018
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import static midi.pad.ui.event.Runtime.getRuntime;

import java.util.Arrays;
import java.util.List;

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
public class LoopConfig extends Widget implements BeatListener {

	private long beat = 0;
	private final List<LoopModel> models;
	private int quarterPerPage = 4;
	private int numberOfPages = 1;
	private int quarterDivision = 4;
	private int holdPage = 0;
	private int holdQuarter = 0;
	private boolean hold = false;
	private boolean holding = false;
	private final Runnable fixRunnable;
	private final Runnable changeRunnable;

	public LoopConfig(final int x, final int y, final Runnable changeRunnable,
			final Runnable fixRunnable, final LoopModel... models) {
		bounds.x = x;
		bounds.y = y;
		bounds.width = 8;
		bounds.height = 3;
		this.fixRunnable = fixRunnable;
		this.changeRunnable = changeRunnable;
		this.models = Arrays.asList(models);
		if (models.length > 0) {
			setQuarterPerPage(models[0].getQuarterPerPage());
			setQuarterDivision(models[0].getQuarterDivision());
			setNumberOfPages(models[0].getNumberOfPages());
		}
	}

	/**
	 * @param quarterPerPage
	 *            the quarterPerPage to set
	 */
	private void setQuarterPerPage(final int quarterPerPage) {
		if (this.quarterPerPage != quarterPerPage) {
			this.quarterPerPage = quarterPerPage;
			if (quarterPerPage < holdQuarter + 1) {
				holdQuarter = quarterPerPage - 1;
			}
			for (final LoopModel loopModel : models) {
				loopModel.setQuarterPerPage(quarterPerPage);
			}
			getRuntime().schedule(changeRunnable);
		}
	}

	/**
	 * @param numberOfPages
	 *            the numberOfPages to set
	 */
	private void setNumberOfPages(final int numberOfPages) {
		if (this.numberOfPages != numberOfPages) {
			this.numberOfPages = numberOfPages;
			if (numberOfPages < holdPage + 1) {
				holdPage = numberOfPages - 1;
			}
			for (final LoopModel loopModel : models) {
				loopModel.setNumberOfPages(numberOfPages);
			}
			getRuntime().schedule(changeRunnable);
		}
	}

	/**
	 * @param quarterDivision
	 *            the quarterDivision to set
	 */
	private void setQuarterDivision(final int quarterDivision) {
		if (this.quarterDivision != quarterDivision) {
			this.quarterDivision = quarterDivision;
			for (final LoopModel loopModel : models) {
				loopModel.setQuarterDivision(quarterDivision);
			}
			getRuntime().schedule(changeRunnable);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		final long division = Beat.BEAT_DIVISION / quarterDivision;
		final int page = (int) ((beat / division / quarterPerPage / quarterDivision)
				% numberOfPages);
		final int quarter = (int) ((beat / division / quarterDivision) % quarterPerPage);
		for (int i = 0; i < numberOfPages; ++i) {
			if (hold && i == holdPage && i == page) {
				g.setPixel(i, 0, Color.LOW_GREEN);
			} else if (hold && i == holdPage) {
				g.setPixel(i, 0, Color.FULL_RED);
			} else if (i == page) {
				g.setPixel(i, 0, Color.FULL_GREEN);
			} else {
				g.setPixel(i, 0, Color.LOW_AMBER);
			}
		}
		for (int i = 0; i < quarterPerPage; ++i) {
			if (hold && i == holdQuarter && i == quarter) {
				g.setPixel(i, 1, Color.LOW_GREEN);
			} else if (hold && i == holdQuarter) {
				g.setPixel(i, 1, Color.FULL_RED);
			} else if (i == quarter) {
				g.setPixel(i, 1, Color.FULL_GREEN);
			} else {
				g.setPixel(i, 1, Color.LOW_AMBER);
			}
		}
		for (int i = 0; i < quarterDivision; ++i) {
			g.setPixel(i, 2, Color.LOW_AMBER);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#eventOccured(midi.pad.ui.event.Event)
	 */
	@Override
	public boolean eventOccured(final Event event) {
		if (event == null) {
			return false;
		}
		if (EVENT_TYPE.PAD_HOLD.equals(event.getEventType())) {
			final PadEvent padEvent = (PadEvent) event;
			if (holding) {
				return true;
			}
			if (padEvent.getY() == 0) {
				setNumberOfPages(padEvent.getX() + 1);
			} else if (padEvent.getY() == 1 && padEvent.getX() < 6) {
				setQuarterPerPage(padEvent.getX() + 1);
			} else if (padEvent.getY() == 2 && padEvent.getX() < 6) {
				setQuarterDivision(padEvent.getX() + 1);
			}
			getRuntime().schedule(changeRunnable);
			holding = true;
		} else if (EVENT_TYPE.PAD_RELEASED.equals(event.getEventType())) {
			final PadEvent padEvent = (PadEvent) event;
			if (holding) {
				holding = false;
				return true;
			} else if (hold && padEvent.getY() == 0 && padEvent.getX() == holdPage) {
				hold = false;
				getRuntime().schedule(fixRunnable);
			} else if (padEvent.getY() == 0 && padEvent.getX() < numberOfPages) {
				holdPage = padEvent.getX();
				hold = true;
				getRuntime().schedule(fixRunnable);
			} else if (padEvent.getY() == 1 && padEvent.getX() < quarterPerPage) {
				holdQuarter = padEvent.getX();
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BeatListener#accept(long)
	 */
	@Override
	public void accept(final long beat) {
		final long division = Beat.BEAT_DIVISION / quarterDivision;
		if (beat % (division) == 0) {
			this.beat = beat;
			getRuntime().invalidate();
		}
	}

	/**
	 * @return the quarterPerPage
	 */
	public int getQuarterPerPage() {
		return quarterPerPage;
	}

	/**
	 * @return the numberOfPages
	 */
	public int getNumberOfPages() {
		return numberOfPages;
	}

	/**
	 * @return the quarterDivision
	 */
	public int getQuarterDivision() {
		return quarterDivision;
	}

	/**
	 * @return the holdPage
	 */
	public int getHoldPage() {
		return holdPage;
	}

	/**
	 * @return the holdQuarter
	 */
	public int getHoldQuarter() {
		return holdQuarter;
	}

	/**
	 * @return the hold
	 */
	public boolean isHold() {
		return hold;
	}

}
