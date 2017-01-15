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
	private static final long holdDelayTime = 1000;
	private static final long holdRepeatTime = 500;

	public PadReceiver(final Screen screen) {
		this.screen = screen;
	}

	private void handleNoteOn(final int key) {
		padEvent(key, EVENT_TYPE.PAD_PRESSED);
		stopRunner(key);
		final Runnable newRunner = new HoldRunnable(key);
		holdMap.put(Integer.valueOf(key), newRunner);
		getRuntime().scheduleWithFixedDelay(newRunner, holdDelayTime, holdRepeatTime,
				TimeUnit.MILLISECONDS);
	}

	private void handleNoteOff(final int key) {
		stopRunner(key);
		padEvent(key, EVENT_TYPE.PAD_RELEASED);
	}

	private void stopRunner(final int key) {
		final Runnable currentRunner = holdMap.remove(Integer.valueOf(key));
		if (currentRunner != null) {
			getRuntime().stop(currentRunner);
		}
	}

	private void padEvent(final int key, final PadEvent.EVENT_TYPE eventType) {
		final int x = key % 16;
		final int y = key / 16;
		if (y < 8) {
			if (x < 8) {
				getRuntime().schedule(new Runnable() {
					@Override
					public void run() {
						screen.eventOccured(new PadEvent(eventType, x, y));
					}
				});
			}
		}
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
			default:
			}
		}

	}

	@Override
	public void close() {

	}

	private class HoldRunnable implements Runnable {
		private final int key;

		public HoldRunnable(final int key) {
			super();
			this.key = key;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			padEvent(key, EVENT_TYPE.PAD_HOLD);
		}
	}
}
