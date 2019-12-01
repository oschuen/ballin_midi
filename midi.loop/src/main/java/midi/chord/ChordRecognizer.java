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
 * @since 11.11.2015
 * @version 1.0
 * @author oliver
 */
package midi.chord;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.device.resource.InputDevice;

/**
 * This Recognizer is able to identify chord presses on a keyboard in the
 * accompaniment section. A chord must be held for about 100 ms before it is
 * recognized.
 * 
 * @author oliver
 */
public class ChordRecognizer implements Receiver {
	public static final int splitTone = InputDevice.splitTone;
	public static final long recogHoldTime = 20;
	private final ScheduledExecutorService service;
	private ChordListener listener;
	private ScheduledFuture<?> recogTimeoutFuture = null;
	private final Recognizer recognizer = new Recognizer();
	private final Lock lock = new ReentrantLock();
	private int midiChannel = 0;
	private boolean split = true;

	private static final Logger logger = LoggerFactory.getLogger(ChordRecognizer.class);

	/**
	 * Constructor defining the client of the recognizer
	 * 
	 * @param listener
	 *            that is informed about the chords
	 */
	public ChordRecognizer() {
		service = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * handles midi ShortMessages that contain a Note_on command
	 * 
	 * @param shortMessage
	 *            containing a Note on Command
	 */
	private void handleNoteOn(final ShortMessage shortMessage) {
		lock.lock();
		try {

			final int onKey = shortMessage.getData1();
			final int velocity = shortMessage.getData2();
			if ((onKey <= splitTone || !split) && shortMessage.getChannel() == midiChannel) {
				if (recogTimeoutFuture != null) {
					recogTimeoutFuture.cancel(false);
				}
				if (velocity > 0) {
					recognizer.pressTone(onKey);
				} else {
					recognizer.releaseTone(onKey);
				}
				recogTimeoutFuture = service.schedule(recognizer, recogHoldTime,
						TimeUnit.MILLISECONDS);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * handles midi ShortMessages that contain a Note_Off command
	 * 
	 * @param shortMessage
	 *            containing a note off command
	 * 
	 */
	private void handleNoteOff(final ShortMessage shortMessage) {
		lock.lock();
		try {
			final int offKey = shortMessage.getData1();
			if ((offKey <= splitTone || !split) && shortMessage.getChannel() == midiChannel) {
				if (recogTimeoutFuture != null) {
					recogTimeoutFuture.cancel(false);
				}
				recognizer.releaseTone(offKey);
				recogTimeoutFuture = service.schedule(recognizer, recogHoldTime,
						TimeUnit.MILLISECONDS);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Filters the Notes within the accompaniment section of the keyboards and
	 * hands them over to the Recognizer
	 * 
	 * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
	 */
	@Override
	public void send(final MidiMessage message, final long timeStamp) {
		service.execute(new Runnable() {
			@Override
			public void run() {
				if (message instanceof ShortMessage) {
					final ShortMessage shortMessage = (ShortMessage) message;
					switch (shortMessage.getCommand()) {
					case ShortMessage.NOTE_ON:
						handleNoteOn(shortMessage);
						break;
					case ShortMessage.NOTE_OFF:
						handleNoteOff(shortMessage);
						break;
					default:
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Receiver#close()
	 */
	@Override
	public void close() {
		service.shutdown();
	}

	/**
	 * recognizes a chord within a midi stream and signals the chord to the
	 * client
	 * 
	 * @author oliver
	 */
	private class Recognizer implements Runnable {

		public int pressedTones = 0;

		private final int majorNotes[] = { 0, 2, 4, 5, 7, 9, 11 };
		private final int subNotes[] = { 1, 3, 6, 8, 10 };
		private final String majorNames[] = { "C", "D", "E", "F", "G", "A", "B" };
		private final String subNames[] = { "Db", "Eb", "Gb", "Ab", "Bb" };

		private final Map<Integer, String> chordMap = new HashMap<>();

		/**
		 * Constructor generating all dur, moll and the corresponding sept
		 * chords. Each chord is represented by a number defining all tones
		 * belonging to the chord within one octave
		 */
		public Recognizer() {

			for (int i = 0; i < majorNotes.length; ++i) {
				final int majorNote = majorNotes[i];
				int chord = 1 << (majorNote % 12);
				chord |= 1 << ((majorNote + 4) % 12);
				chord |= 1 << ((majorNote + 7) % 12);
				chordMap.put(Integer.valueOf(chord), majorNames[i]);
				int mChord = 1 << (majorNote % 12);
				mChord |= 1 << ((majorNote + 3) % 12);
				mChord |= 1 << ((majorNote + 7) % 12);
				chordMap.put(Integer.valueOf(mChord), majorNames[i] + "m");
				int chord7 = 1 << (majorNote % 12);
				chord7 |= 1 << ((majorNote + 4) % 12);
				chord7 |= 1 << ((majorNote + 7) % 12);
				chord7 |= 1 << ((majorNote + 10) % 12);
				chordMap.put(Integer.valueOf(chord7), majorNames[i] + "7");
				int chord7sus4 = 1 << (majorNote % 12);
				chord7sus4 |= 1 << ((majorNote + 5) % 12);
				chord7sus4 |= 1 << ((majorNote + 7) % 12);
				chord7sus4 |= 1 << ((majorNote + 10) % 12);
				chordMap.put(Integer.valueOf(chord7sus4), majorNames[i] + "7sus4");
				int mChord7 = 1 << (majorNote % 12);
				mChord7 |= 1 << ((majorNote + 3) % 12);
				mChord7 |= 1 << ((majorNote + 7) % 12);
				mChord7 |= 1 << ((majorNote + 10) % 12);
				chordMap.put(Integer.valueOf(mChord7), majorNames[i] + "m7");
			}

			for (int i = 0; i < subNotes.length; ++i) {
				final int subNote = subNotes[i];
				int chord = 1 << (subNote % 12);
				chord |= 1 << ((subNote + 4) % 12);
				chord |= 1 << ((subNote + 7) % 12);
				chordMap.put(Integer.valueOf(chord), subNames[i]);
				int mChord = 1 << (subNote % 12);
				mChord |= 1 << ((subNote + 3) % 12);
				mChord |= 1 << ((subNote + 7) % 12);
				chordMap.put(Integer.valueOf(mChord), subNames[i] + "m");
				int chord7 = 1 << (subNote % 12);
				chord7 |= 1 << ((subNote + 4) % 12);
				chord7 |= 1 << ((subNote + 7) % 12);
				chord7 |= 1 << ((subNote + 10) % 12);
				chordMap.put(Integer.valueOf(chord7), subNames[i] + "7");
				int chord7sus4 = 1 << (subNote % 12);
				chord7sus4 |= 1 << ((subNote + 5) % 12);
				chord7sus4 |= 1 << ((subNote + 7) % 12);
				chord7sus4 |= 1 << ((subNote + 10) % 12);
				chordMap.put(Integer.valueOf(chord7sus4), subNames[i] + "7sus4");
				int mChord7 = 1 << (subNote % 12);
				mChord7 |= 1 << ((subNote + 3) % 12);
				mChord7 |= 1 << ((subNote + 7) % 12);
				mChord7 |= 1 << ((subNote + 10) % 12);
				chordMap.put(Integer.valueOf(mChord7), subNames[i] + "m7");
			}
		}

		/**
		 * Signals that a key is pressed on the keyboard
		 * 
		 * @param tone
		 *            pressed key
		 */
		public void pressTone(final int tone) {
			pressedTones |= 1 << (tone % 12);
		}

		/**
		 * signals that a key is release on the keyboard
		 * 
		 * @param tone
		 *            release key
		 */
		public void releaseTone(final int tone) {
			pressedTones &= ~(1 << (tone % 12));
		}

		/**
		 * When 100 ms hold time is aspired this run method is called to check
		 * whether a chord is identified and if so signal it to the client
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			lock.lock();
			try {
				final String chord = chordMap.get(Integer.valueOf(pressedTones));
				if (logger.isDebugEnabled()) {
					if (chord == null) {
						logger.debug("Recognized : " + pressedTones + " No Chord");
					} else {
						logger.debug("Recognized : " + pressedTones + " Chord " + chord);
					}
				}
				if (chord == null) {
					service.submit(new Runnable() {
						@Override
						public void run() {
							listener.noChord();
						}
					});
				} else {
					service.submit(new Runnable() {
						@Override
						public void run() {
							listener.newChord(chord);
						}
					});
				}
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * Callback interface the user of this class has to implement
	 * 
	 * @author oliver
	 */
	public interface ChordListener {
		void newChord(String chord);

		void noChord();
	}

	/**
	 * @param listener
	 *            the listener to set
	 */
	public void setListener(final ChordListener listener) {
		this.listener = listener;
	}

	/**
	 * @return the midiChannel
	 */
	public int getMidiChannel() {
		return midiChannel;
	}

	/**
	 * @param midiChannel
	 *            the midiChannel to set
	 */
	public void setMidiChannel(final int midiChannel) {
		this.midiChannel = midiChannel;
	}

	/**
	 * @return the split
	 */
	public boolean isSplit() {
		return split;
	}

	/**
	 * @param split
	 *            the split to set
	 */
	public void setSplit(final boolean split) {
		this.split = split;
	}
}
