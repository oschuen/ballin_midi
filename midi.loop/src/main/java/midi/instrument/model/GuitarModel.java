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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.chord.ChordRecognizer.ChordListener;
import midi.instrument.Instrument;
import midi.loop.LoopEvent;
import midi.loop.LoopEvent.COMMAND;
import midi.loop.LoopModel;

/**
 * @author oliver
 *
 */
public class GuitarModel implements ChordListener {

	private final LoopModel accent;
	private final Map<Instrument, LoopModel> stringMap = new HashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(GuitarModel.class);
	private Optional<Chord> activeChord = Optional.empty();

	public GuitarModel() {
		accent = new LoopModel();
		for (final GuitarInstrument instrument : GuitarInstrument.values()) {
			final LoopModel model = new LoopModel();
			stringMap.put(instrument, model);
		}

		stringMap.get(GuitarInstrument.BASS_STRING).setEffect(event -> {
			return activeChord.map(chord -> {
				return new LoopEvent(event.getCommand(), event.getVelocity(), chord.getBaseTone());
			}).orElse(new LoopEvent(COMMAND.NOTE_OFF, 0, 0));
		});
		stringMap.get(GuitarInstrument.G_STRING).setEffect(event -> {
			return activeChord.map(chord -> {
				return new LoopEvent(event.getCommand(), event.getVelocity(), chord.getGTone());
			}).orElse(new LoopEvent(COMMAND.NOTE_OFF, 0, 0));
		});
		stringMap.get(GuitarInstrument.B_STRING).setEffect(event -> {
			return activeChord.map(chord -> {
				return new LoopEvent(event.getCommand(), event.getVelocity(), chord.getBTone());
			}).orElse(new LoopEvent(COMMAND.NOTE_OFF, 0, 0));
		});
		stringMap.get(GuitarInstrument.E_STRING).setEffect(event -> {
			return activeChord.map(chord -> {
				return new LoopEvent(event.getCommand(), event.getVelocity(), chord.getETone());
			}).orElse(new LoopEvent(COMMAND.NOTE_OFF, 0, 0));
		});
		setNumberOfPages(accent.getNumberOfPages());
		setQuarterDivision(accent.getQuarterDivision());
		setQuarterPerPage(accent.getQuarterPerPage());
	}

	/**
	 * Each String is defined as a single instrument.
	 * 
	 * @author oliver
	 */
	public static enum GuitarInstrument implements Instrument {

		BASS_STRING("Bass String"), G_STRING("G String"), B_STRING("B_String"), E_STRING(
				"E String");

		private final String speekyName;

		GuitarInstrument(final String speekyName) {
			this.speekyName = speekyName;
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
	}

	public Optional<LoopModel> getLoopModel(final Instrument instrument) {
		return Optional.ofNullable(stringMap.get(instrument));
	}

	public LoopModel getAccentModel() {
		return accent;
	}

	private LoopEvent modifyAccentEvent(final Optional<LoopEvent> event) {
		return event.map(ev -> new LoopEvent(COMMAND.NOTE_ON, 127, 0))
				.orElse(new LoopEvent(COMMAND.NOTE_ON, accent.getVelocity(), 0));
	}

	public LoopEvent getAccentEvent(final long beat) {
		return modifyAccentEvent(accent.getEvent(beat));
	}

	public LoopEvent getAccentStepEvent(final int step) {
		return modifyAccentEvent(accent.getStepEvent(step));
	}

	public Optional<LoopEvent> getEvent(final Instrument instrument, final long beat) {
		final LoopModel model = stringMap.get(instrument);
		if (model == null) {
			return Optional.empty();
		}
		return model.getEvent(beat);
	}

	public Optional<LoopEvent> getStepEvent(final Instrument instrument, final int step) {
		final LoopModel model = stringMap.get(instrument);
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
		for (final LoopModel model : stringMap.values()) {
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
		for (final LoopModel model : stringMap.values()) {
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
		for (final LoopModel model : stringMap.values()) {
			model.setQuarterDivision(quarterDivision);
		}
	}

	/**
	 * @param chord
	 *            that shall be played. the currently played tone is finished
	 *            and the new tone for the chord is played
	 */
	@Override
	public void newChord(final String chord) {
		try {
			final Chord temp = Chord.valueOf(chord);
			activeChord = Optional.ofNullable(temp);
		} catch (final Throwable thr) {
			logger.error("Set chord failed", thr);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jaccompaniment.chord.ChordRecognizer.ChordListener#noChord()
	 */
	@Override
	public void noChord() {
		activeChord = Optional.empty();
	}

	/**
	 * Enum containing all playable Chords. Currently dur, moll, and dur 7 and
	 * moll 7 chords are known.
	 * 
	 * @author oliver
	 */
	public static enum Chord {

		C(new int[] { -1, 3, 2, 0, 1, 0 }),

		D(new int[] { -1, -1, 0, 2, 3, 2 }),

		E(new int[] { 0, 2, 2, 1, 0, 0 }),

		F(new int[] { -1, -1, 3, 2, 1, 1 }),

		G(new int[] { 3, 2, 0, 0, 0, 3 }),

		A(new int[] { -1, 0, 2, 2, 2, 0 }),

		B(new int[] { -1, 2, 4, 4, 4, 2 }),

		C7(new int[] { -1, 3, 2, 3, 1, 0 }),

		D7(new int[] { -1, -1, 0, 2, 1, 2 }),

		E7(new int[] { 0, 2, 0, 1, 0, 0 }),

		F7(new int[] { 1, 3, 1, 2, 1, 1 }),

		G7(new int[] { 3, 2, 0, 0, 0, 1 }),

		A7(new int[] { -1, 0, 2, 0, 2, 0 }),

		B7(new int[] { -1, 2, 1, 2, 0, 2 }),

		C7sus4(new int[] { -1, 3, 3, 3, 1, 0 }),

		D7sus4(new int[] { -1, -1, 0, 2, 1, 3 }),

		E7sus4(new int[] { 0, 2, 0, 2, 0, 0 }),

		F7sus4(new int[] { 1, 3, 1, 3, 1, 1 }),

		G7sus4(new int[] { 3, 3, 0, 0, 1, 1 }),

		A7sus4(new int[] { -1, 0, 2, 0, 3, 0 }),

		B7sus4(new int[] { -1, 2, 2, 2, 0, 2 }),

		Cm(new int[] { -1, 3, 5, 5, 4, 3 }),

		Dm(new int[] { -1, -1, 0, 2, 3, 1 }),

		Em(new int[] { 0, 2, 2, 0, 0, 0 }),

		Fm(new int[] { 1, 3, 3, 1, 1, 1 }),

		Gm(new int[] { 3, 5, 5, 3, 3, 3 }),

		Am(new int[] { -1, 0, 2, 2, 1, 0 }),

		Bm(new int[] { -1, 2, 4, 4, 3, 2 }),

		Cm7(new int[] { -1, 3, 5, 3, 4, 3 }),

		Dm7(new int[] { -1, -1, 0, 2, 1, 1 }),

		Em7(new int[] { 0, 2, 2, 0, 3, 0 }),

		Fm7(new int[] { 1, 3, 1, 1, 1, 1 }),

		Gm7(new int[] { 3, 5, 3, 3, 3, 3 }),

		Am7(new int[] { -1, 0, 2, 0, 1, 0 }),

		Bm7(new int[] { -1, 2, 4, 2, 3, 2 }),

		Db(new int[] { -1, 4, 3, 1, 2, 1 }),

		Dbm(new int[] { -1, 4, 6, 6, 5, 4 }),

		Db7(new int[] { -1, 4, 6, 4, 6, 4 }),

		Dbm7(new int[] { -1, 4, 6, 4, 5, 4 }),

		Cm7sus4(new int[] { -1, 3, 3, 0, 0, 3 }),

		Dm7sus4(new int[] { -1, -1, 0, 0, 2, 3 }),

		Em7sus4(new int[] { 0, 0, 1, 2, 0, 0 }),

		Fm7sus4(new int[] { 1, 1, 2, 3, 1, 1 }),

		Gm7sus4(new int[] { 3, 3, 4, 5, 3, 3 }),

		Am7sus4(new int[] { -1, 0, 0, 1, 3, 0 }),

		Bm7sus4(new int[] { -1, 2, 2, 3, 0, 0 }),

		Db7sus4(new int[] { -1, 4, 4, 4, 2, 2 }),

		Eb(new int[] { -1, -1, 1, 3, 4, 3 }),

		Ebm(new int[] { -1, 6, 8, 8, 7, 6 }),

		Eb7(new int[] { -1, -1, 1, 0, 2, 3 }),

		Ebm7(new int[] { -1, -1, 1, 3, 2, 2 }),

		Gb(new int[] { 2, 4, 4, 3, 2, 2 }),

		Gbm(new int[] { 2, 4, 4, 2, 2, 2 }),

		Gb7(new int[] { 2, 4, 2, 3, 2, 2 }),

		Gbm7(new int[] { 2, 0, 2, 2, 2, 0 }),

		Ab(new int[] { 4, 6, 6, 5, 4, 4 }),

		Abm(new int[] { 4, 6, 6, 4, 4, 4 }),

		Ab7(new int[] { 4, 6, 4, 5, 4, 4 }),

		Abm7(new int[] { 4, 6, 4, 4, 4, 4 }),

		Bb(new int[] { -1, 1, 3, 3, 3, 1 }),

		Bbm(new int[] { -1, 1, 3, 3, 2, 1 }),

		Bb7(new int[] { -1, 1, 3, 1, 3, 1 }),

		Bbm7(new int[] { -1, 1, 3, 1, 2, 1 });

		private final int fret[];
		private final int baseMidi[] = new int[] { 40, 45, 50, 55, 59, 64 };

		/**
		 * @param fret
		 *            defines which string are fretted and in which segment
		 */
		private Chord(final int[] fret) {
			if (fret.length != 6) {
				logger.error("number of presses must be 6");
				throw new IllegalArgumentException("number of presses must be 6");
			}
			if (fret[0] < 0 && fret[1] < 0 && fret[2] < 0) {
				logger.error("There must be a base tone");
				throw new IllegalArgumentException("There must be a base tone");
			}
			if (fret[3] < 0 || fret[4] < 0 || fret[5] < 0) {
				logger.error("All nylon tones must be set");
				throw new IllegalArgumentException("All nylon tones must be set");
			}
			this.fret = Arrays.copyOf(fret, fret.length);
		}

		/**
		 * @return the deepest tone
		 */
		public int getBaseTone() {
			for (int i = 0; i < 3; ++i) {
				if (fret[i] >= 0) {
					return baseMidi[i] + fret[i];
				}
			}
			throw new IllegalArgumentException("There must be a base tone");
		}

		/**
		 * @return the tone of the G-String
		 */
		public int getGTone() {
			return baseMidi[3] + fret[3];
		}

		/**
		 * @return the tone of the B-String
		 */
		public int getBTone() {
			return baseMidi[4] + fret[4];
		}

		/**
		 * @return the tone to the E-String
		 */
		public int getETone() {
			return baseMidi[5] + fret[5];
		}
	}
}
