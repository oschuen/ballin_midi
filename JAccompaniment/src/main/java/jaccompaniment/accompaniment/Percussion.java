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
 * @since 13.11.2015
 * @version 1.0
 * @author oliver
 */
package jaccompaniment.accompaniment;

import jaccompaniment.ui.LoopPanel.Instrument;

import java.util.Arrays;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

/**
 * Percussion configured for GM standard drum map
 * 
 * @author oliver
 */
public class Percussion {

	/**
	 * enum containing all playable drums, toms and hats.
	 * 
	 * @author oliver
	 */
	public static enum PercussionInstrument implements Instrument {
		ACCENT("Accent", 0), CYMBAL("Cymbal", 51), CLOSED_HIGH_HAT("Closed High Hat", 42), OPEN_HIGH_HAT(
				"Open High Hat", 46), HIGH_TOM("High Tom", 50), HIGH_MID_TOM("High Mid Tom", 48), ACOUSTIC_SNARE(
				"Acoustic Snare", 38), RIM_SHOT("Rim Shot", 37), LOW_FLOOR_TOM("Low Floor Tom", 41), CLAPS(
				"Hand Clap", 39), COW_BELL("Cowbell", 56), BASS_DRUM("Bass Drum", 36);

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

	private final Receiver receiver;
	private final int channel;
	private int velocity[];
	private final int instrument[];
	private final int lastPlayed[];
	private String pattern[] = new String[] {};

	/**
	 * Constructor defining the midi receiver the percussion shall use and which
	 * channel. The sound bank must be configured correctly at the synthesizer
	 * 
	 * @param receiver
	 *            midi receiver where the midi commands shall be send to
	 * @param channel
	 *            where a guitar is defined. Preferred GM Midi number 24
	 */
	public Percussion(final Receiver receiver, final int channel) {
		this.receiver = receiver;
		this.channel = channel;
		final PercussionInstrument instruments[] = PercussionInstrument.values();
		velocity = new int[instruments.length];
		lastPlayed = new int[instruments.length];
		instrument = new int[instruments.length];
		for (int i = 0; i < instruments.length; i++) {
			final PercussionInstrument percussionInstrument = instruments[i];
			velocity[i] = 127;
			lastPlayed[i] = -1;
			instrument[i] = percussionInstrument.getTone();
		}
	}

	/**
	 * beats a Drum, Tom or hat.
	 * 
	 * @param pattern
	 *            for the percussion
	 * @param beat
	 *            which beat is currently active
	 * @param tone
	 *            of the Tom
	 * @param lastTone
	 *            last tone of the tom
	 * @param velocity
	 *            of the beating
	 * @return if tom is beaten the new tone otherwise the lastTone
	 * @throws InvalidMidiDataException
	 */
	private int playTom(final String pattern, final int beat, final int tone, final int lastTone,
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
	 */
	public void beat(final int beat) throws InvalidMidiDataException {
		final char baseChar = pattern[0].charAt(beat % pattern[0].length());
		final int factor;
		if (baseChar == ' ') {
			factor = velocity[0];
		} else {
			factor = 127;
		}
		for (int i = 1; i < pattern.length; ++i) {
			lastPlayed[i] = playTom(pattern[i], beat, instrument[i], lastPlayed[i], factor
					* velocity[i] / 127);
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
	 * Close the Accompaniment and release resources.
	 */
	public void close() {
		receiver.close();
	}
}
