/**
 * Copyright (C) 2016 Oliver Schünemann
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
 * @since 27.12.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.event;

import static midi.pad.ui.event.Runtime.getRuntime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import midi.pad.ui.Screen;
import midi.pad.ui.event.PadEvent.EVENT_TYPE;

/**
 * @author oliver
 *
 */
public class PadReceiver implements Receiver {
	private final Screen screen;
	private final Map<Integer, Runnable> holdMap = new HashMap<>();
	private final Map<Integer, Runnable> buttonHoldMap = new HashMap<>();
	private static final long holdDelayTime = 1000;
	private static final long holdRepeatTime = 500;

	public PadReceiver(final Screen screen) {
		this.screen = screen;
	}

	private void handleNoteOn(final int key) {
		final int x = key % 16;
		final int y = key / 16;
		if (y < 8) {
			final Runnable newRunner;
			stopRunner(key);
			if (x < 8) {
				padEvent(x, y, EVENT_TYPE.PAD_PRESSED);
				newRunner = new HoldRunnable(x, y);
			} else {
				abcButtonEvent(y, AbcButtonEvent.EVENT_TYPE.ABC_PRESSED);
				newRunner = new AbcHoldRunnable(y);
			}
			holdMap.put(Integer.valueOf(key), newRunner);
			getRuntime().scheduleWithFixedDelay(newRunner, holdDelayTime, holdRepeatTime,
					TimeUnit.MILLISECONDS);
		}
	}

	private void handleNoteOff(final int key) {
		stopRunner(key);
		final int x = key % 16;
		final int y = key / 16;
		if (y < 8) {
			if (x < 8) {
				padEvent(x, y, EVENT_TYPE.PAD_RELEASED);
			} else {
				abcButtonEvent(y, AbcButtonEvent.EVENT_TYPE.ABC_RELEASED);
			}
		}
	}

	private void handleControlOn(final int key) {
		final int x = key % 8;
		final Runnable newRunner;
		stopNumRunner(key);
		numButtonEvent(x, NumButtonEvent.EVENT_TYPE.NUM_PRESSED);
		newRunner = new NumHoldRunnable(x);
		buttonHoldMap.put(Integer.valueOf(key), newRunner);
		getRuntime().scheduleWithFixedDelay(newRunner, holdDelayTime, holdRepeatTime,
				TimeUnit.MILLISECONDS);
	}

	private void handleControlOff(final int key) {
		final int x = key % 8;
		stopNumRunner(key);
		numButtonEvent(x, NumButtonEvent.EVENT_TYPE.NUM_RELEASED);
	}

	private void stopRunner(final int key) {
		final Runnable currentRunner = holdMap.remove(Integer.valueOf(key));
		if (currentRunner != null) {
			getRuntime().stop(currentRunner);
		}
	}

	private void stopNumRunner(final int x) {
		final Runnable currentRunner = buttonHoldMap.remove(Integer.valueOf(x));
		if (currentRunner != null) {
			getRuntime().stop(currentRunner);
		}
	}

	private void padEvent(final int x, final int y, final PadEvent.EVENT_TYPE eventType) {
		getRuntime().schedule(new Runnable() {
			@Override
			public void run() {
				screen.eventOccured(new PadEvent(eventType, x, y));
			}
		});
	}

	private void abcButtonEvent(final int y, final AbcButtonEvent.EVENT_TYPE eventType) {
		getRuntime().schedule(new Runnable() {
			@Override
			public void run() {
				screen.eventOccured(new AbcButtonEvent(eventType, y));
			}
		});
	}

	private void numButtonEvent(final int x, final NumButtonEvent.EVENT_TYPE eventType) {
		getRuntime().schedule(new Runnable() {
			@Override
			public void run() {
				screen.eventOccured(new NumButtonEvent(eventType, x));
			}
		});
	}

	@Override
	public void send(final MidiMessage message, final long timeStamp) {
		if (message instanceof ShortMessage) {
			final ShortMessage shortMessage = (ShortMessage) message;
			switch (shortMessage.getCommand()) {
			case ShortMessage.NOTE_ON:
				if (shortMessage.getData2() > 0) {
					handleNoteOn(shortMessage.getData1());
				} else {
					handleNoteOff(shortMessage.getData1());
				}
				break;
			case ShortMessage.NOTE_OFF:
				handleNoteOff(shortMessage.getData1());
				break;
			case ShortMessage.CONTROL_CHANGE:
				if (shortMessage.getData2() > 0) {
					handleControlOn(shortMessage.getData1());
				} else {
					handleControlOff(shortMessage.getData1());
				}

			default:
			}
		}

	}

	@Override
	public void close() {

	}

	private class HoldRunnable implements Runnable {
		private final int x;
		private final int y;

		public HoldRunnable(final int x, final int y) {
			super();
			this.x = x;
			this.y = y;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			padEvent(x, y, EVENT_TYPE.PAD_HOLD);
		}
	}

	private class AbcHoldRunnable implements Runnable {
		private final int y;

		public AbcHoldRunnable(final int y) {
			super();
			this.y = y;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			abcButtonEvent(y, AbcButtonEvent.EVENT_TYPE.ABC_HOLD);
		}
	}

	private class NumHoldRunnable implements Runnable {
		private final int x;

		public NumHoldRunnable(final int x) {
			super();
			this.x = x;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			numButtonEvent(x, NumButtonEvent.EVENT_TYPE.NUM_HOLD);
		}
	}
}
