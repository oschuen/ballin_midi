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
 * @since 14.11.2015
 * @version 1.0
 * @author oliver
 */
package jaccompaniment.filter;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.chord.ChordRecognizer;

/**
 * Midi Through device that forwads all midi commands from one Transmitter to
 * another receiver. It is able to filter all NOTE_ON and NOTE_OFF commands that
 * belong to the accompaniment area.
 * 
 * @author oliver
 */
public class MidiThroughFilter implements Receiver {

	public static final int splitTone = ChordRecognizer.splitTone;
	private final Receiver receiver;
	private boolean midiThrough = true;
	private boolean filterChord = true;

	private static final Logger logger = LoggerFactory.getLogger(MidiThroughFilter.class);

	/**
	 * @param receiver
	 *            to which the received midi commands shall be forwarded
	 */
	public MidiThroughFilter(final Receiver receiver) {
		super();
		this.receiver = receiver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
	 */
	@Override
	public void send(final MidiMessage message, final long timeStamp) {
		if (message instanceof ShortMessage && midiThrough) {
			final ShortMessage shortMessage = (ShortMessage) message;
			switch (shortMessage.getCommand()) {
			case ShortMessage.NOTE_ON:
				final int onKey = shortMessage.getData1();
				if (onKey > splitTone || !filterChord) {
					try {
						shortMessage.setMessage(ShortMessage.NOTE_ON, shortMessage.getChannel(),
								shortMessage.getData1(), shortMessage.getData2());
					} catch (final InvalidMidiDataException e) {
						logger.error(e.getMessage(), e);
					}
					receiver.send(shortMessage, -1);
				}
				break;
			case ShortMessage.NOTE_OFF:
				final int offKey = shortMessage.getData1();
				if (offKey > splitTone || !filterChord) {
					receiver.send(shortMessage, -1);
				}
				break;
			case ShortMessage.PITCH_BEND:
				break;
			case ShortMessage.CONTROL_CHANGE:
				if (shortMessage.getData1() != 1) {
					receiver.send(shortMessage, -1);
				}
				break;
			default:
				receiver.send(shortMessage, -1);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Receiver#close()
	 */
	@Override
	public void close() {
		receiver.close();
	}

	/**
	 * @return the midiThrough
	 */
	public boolean isMidiThrough() {
		return midiThrough;
	}

	/**
	 * @param midiThrough
	 *            enables forwarding of midi commands
	 */
	public void setMidiThrough(final boolean midiThrough) {
		this.midiThrough = midiThrough;
	}

	/**
	 * @return the filterChord
	 */
	public boolean isFilterChord() {
		return filterChord;
	}

	/**
	 * @param filterChord
	 *            enables filtering of tones within the accompaniment area of a
	 *            keyboard
	 */
	public void setFilterChord(final boolean filterChord) {
		this.filterChord = filterChord;
	}

	/**
	 * Mutes all Sounds played
	 */
	public void panic() {
		try {
			for (int i = 0; i < 16; ++i) {
				final ShortMessage msg = new ShortMessage();
				msg.setMessage(ShortMessage.CONTROL_CHANGE, i, 123, 0);
				receiver.send(msg, -1);
			}
		} catch (final InvalidMidiDataException e) {
			logger.error("Panic Failed", e);
		}
	}

}
