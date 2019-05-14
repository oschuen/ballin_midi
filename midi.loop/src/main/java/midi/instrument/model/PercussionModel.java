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
 * @since 20.05.2018
 * @version 1.0
 * @author oliver
 */
package midi.instrument.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import midi.instrument.Instrument;
import midi.loop.LoopEvent;
import midi.loop.LoopEvent.COMMAND;
import midi.loop.LoopModel;

/**
 * @author oliver
 *
 */
public class PercussionModel {

	private final LoopModel accent;
	private final Map<Instrument, LoopModel> tomsMap = new HashMap<>();

	public PercussionModel() {
		accent = new LoopModel();
		for (final PercussionInstrument instrument : PercussionInstrument.values()) {
			final LoopModel model = new LoopModel();
			model.setEffect((event) -> {
				return new LoopEvent(event.getCommand(), event.getVelocity(), instrument.tone);
			});
			tomsMap.put(instrument, model);
		}
		setNumberOfPages(accent.getNumberOfPages());
		setQuarterDivision(accent.getQuarterDivision());
		setQuarterPerPage(accent.getQuarterPerPage());
	}

	/**
	 * enum containing all playable drums, toms and hats.
	 * 
	 * @author oliver
	 */
	public static enum PercussionInstrument implements Instrument {
		CYMBAL("Cymbal", 51), CLOSED_HIGH_HAT("Closed High Hat", 42), OPEN_HIGH_HAT("Open High Hat",
				46), HIGH_TOM("High Tom", 50), HIGH_MID_TOM("High Mid Tom",
						48), ACOUSTIC_SNARE("Acoustic Snare", 38), RIM_SHOT("Rim Shot",
								37), LOW_FLOOR_TOM("Low Floor Tom", 41), CLAPS("Hand Clap",
										39), COW_BELL("Cowbell", 56), BASS_DRUM("Bass Drum", 36);

		private final String speekyName;
		private final int tone;

		/**
		 * Constructor for a Percussion Instrument
		 * 
		 * @param speekyName
		 *            name that can be used for an HMI
		 * @param tone
		 *            the tone within GM Standard drum map
		 */
		PercussionInstrument(final String speekyName, final int tone) {
			this.speekyName = speekyName;
			this.tone = tone;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return speekyName;
		}

		public int getTone() {
			return tone;
		}
	}

	public Optional<LoopModel> getLoopModel(final Instrument instrument) {
		return Optional.ofNullable(tomsMap.get(instrument));
	}

	public LoopModel getAccentModel() {
		return accent;
	}

	private LoopEvent modifyAccentEvent(final Optional<LoopEvent> event) {
		return event.map(ev -> new LoopEvent(COMMAND.NOTE_ON, 127, 0))
				.orElse(new LoopEvent(COMMAND.NOTE_ON, accent.getVelocity(), 0));
	}

	public LoopEvent getAccentStepEvent(final int step) {
		return modifyAccentEvent(accent.getStepEvent(step));
	}

	public Optional<LoopEvent> getStepEvent(final Instrument instrument, final int step) {
		final LoopModel model = tomsMap.get(instrument);
		if (model == null) {
			return Optional.empty();
		}
		return model.getStepEvent(step);
	}

	/**
	 * @return
	 * @see midi.loop.LoopModel#getQuarterPerPage()
	 */
	public int getQuarterPerPage() {
		return accent.getQuarterPerPage();
	}

	/**
	 * @param quarterPerPage
	 * @see midi.loop.LoopModel#setQuarterPerPage(int)
	 */
	public void setQuarterPerPage(final int quarterPerPage) {
		accent.setQuarterPerPage(quarterPerPage);
		for (final LoopModel model : tomsMap.values()) {
			model.setQuarterPerPage(quarterPerPage);
		}
	}

	/**
	 * @return
	 * @see midi.loop.LoopModel#getNumberOfPages()
	 */
	public int getNumberOfPages() {
		return accent.getNumberOfPages();
	}

	/**
	 * @param numberOfPages
	 * @see midi.loop.LoopModel#setNumberOfPages(int)
	 */
	public void setNumberOfPages(final int numberOfPages) {
		accent.setNumberOfPages(numberOfPages);
		for (final LoopModel model : tomsMap.values()) {
			model.setNumberOfPages(numberOfPages);
		}
	}

	/**
	 * @return
	 * @see midi.loop.LoopModel#getQuarterDivision()
	 */
	public int getQuarterDivision() {
		return accent.getQuarterDivision();
	}

	/**
	 * @param quarterDivision
	 * @see midi.loop.LoopModel#setQuarterDivision(int)
	 */
	public void setQuarterDivision(final int quarterDivision) {
		accent.setQuarterDivision(quarterDivision);
		for (final LoopModel model : tomsMap.values()) {
			model.setQuarterDivision(quarterDivision);
		}

	}
}
