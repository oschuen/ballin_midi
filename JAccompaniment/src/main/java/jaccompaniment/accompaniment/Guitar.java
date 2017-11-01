/**
 * Copyright (C) 2015 Oliver Schünemann
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
 * @since 12.11.2015
 * @version 1.0
 * @author oliver
 */
package jaccompaniment.accompaniment;

import java.util.Arrays;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaccompaniment.chord.ChordRecognizer.ChordListener;
import jmidi.gui.group.LoopPanel.Instrument;

/**
 * Guitar accompaniment playing picking patterns. Pattern must be provided for
 * bass, G, B and E String in one-sixteenth resolution.
 * 
 * @author oliver
 */
public class Guitar implements ChordListener {

	private final Receiver receiver;
	private final int channel;
	private Chord activeChord = null;

	private String pattern[];
	private int velocity[];
	private int lastBaseTone = -1;
	private int lastGTone = -1;
	private int lastBTone = -1;
	private int lastETone = -1;
	private Integer lostBeat = null;
	private int looseActiveChordBeats = -1;

	private static final Logger logger = LoggerFactory.getLogger(Guitar.class);

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

	/**
	 * Close the Accompaniment and release resources.
	 */
	public void close() {
		receiver.close();
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

	/**
	 * Constructor defining the midi receiver the guitar shall use and which
	 * channel. The GM instrument must be configured correctly at the
	 * synthesizer
	 * 
	 * @param receiver
	 *            midi receiver where the midi commands shall be send to
	 * @param channel
	 *            where a guitar is defined. Preferred GM Midi number 24
	 */
	public Guitar(final Receiver receiver, final int channel) {
		this.receiver = receiver;
		this.channel = channel;

		final GuitarInstrument instruments[] = GuitarInstrument.values();
		velocity = new int[instruments.length];
		for (int i = 0; i < instruments.length; i++) {
			velocity[i] = 127;
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
			final Integer lostBeat = this.lostBeat;
			if (temp != null) {
				looseActiveChordBeats = -1;
				activeChord = temp;
				if (lostBeat != null) {
					beat(lostBeat.intValue());
				}
			}
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
		looseActiveChordBeats = 2;
	}

	/**
	 * picks one string
	 * 
	 * @param pattern
	 *            for the string
	 * @param beat
	 *            which beat is currently active
	 * @param tone
	 *            of the string
	 * @param lastTone
	 *            last tone that was picked on that string
	 * @param velocity
	 *            of the picking
	 * @return if string is picked the new tone otherwise the lastTone
	 * @throws InvalidMidiDataException
	 *             when receiver can't handle command
	 */
	private int playString(final String pattern, final int beat, final int tone, final int lastTone,
			final int velocity) throws InvalidMidiDataException {
		final ShortMessage msg = new ShortMessage();
		final char baseChar = pattern.charAt(beat % pattern.length());
		if (baseChar == ' ') {
			return lastTone;
		} else {
			if (lastTone >= 0) {
				msg.setMessage(ShortMessage.NOTE_OFF, channel, lastTone, 0);
				receiver.send(msg, -1);
			}
			msg.setMessage(ShortMessage.NOTE_ON, channel, tone, velocity);
			receiver.send(msg, -1);
			return tone;
		}
	}

	/**
	 * @param beat
	 *            number of one-sixteenth tone that has to be played next
	 * @throws InvalidMidiDataException
	 *             when beat couldn't process by midi system for whatever reason
	 */
	public void beat(final int beat) throws InvalidMidiDataException {
		looseActiveChordBeats = looseActiveChordBeats > 0 ? looseActiveChordBeats - 1 : -1;
		if (looseActiveChordBeats == 0) {
			looseActiveChordBeats = -1;
			activeChord = null;
		}
		if (activeChord == null) {
			lostBeat = Integer.valueOf(beat);
		} else {
			lostBeat = null;
			lastBaseTone = playString(pattern[0], beat, activeChord.getBaseTone(), lastBaseTone,
					velocity[0]);
			lastGTone = playString(pattern[1], beat, activeChord.getGTone(), lastGTone,
					velocity[1]);
			lastBTone = playString(pattern[2], beat, activeChord.getBTone(), lastBTone,
					velocity[2]);
			lastETone = playString(pattern[3], beat, activeChord.getETone(), lastETone,
					velocity[3]);
		}
	}

	/**
	 * @param pattern
	 *            the pattern to set for the 4 Strings
	 */
	public void setPattern(final String[] pattern) {
		this.pattern = Arrays.copyOf(pattern, pattern.length);
	}

	/**
	 * @param velocity
	 *            the velocity to set for each String
	 */
	public void setVelocity(final int[] velocity) {
		this.velocity = Arrays.copyOf(velocity, velocity.length);
	}

	/**
	 * Mutes all Sounds played
	 */
	public void panic() {
		try {
			final ShortMessage msg = new ShortMessage();
			msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, 123, 0);
			receiver.send(msg, -1);
		} catch (final InvalidMidiDataException e) {
			logger.error("Panic Failed", e);
		}
	}
}
