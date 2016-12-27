/**
 * Copyright (C) 2016 Oliver Schünemann
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
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
		padEvent(key, EVENT_TYPE.PRESSED);
		stopRunner(key);
		final Runnable newRunner = new HoldRunnable(key);
		holdMap.put(Integer.valueOf(key), newRunner);
		getRuntime().scheduleWithFixedDelay(newRunner, holdDelayTime, holdRepeatTime,
				TimeUnit.MILLISECONDS);
	}

	private void handleNoteOff(final int key) {
		stopRunner(key);
		padEvent(key, EVENT_TYPE.RELEASED);
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
						screen.padEventOccured(new PadEvent(eventType, x, y));
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
			padEvent(key, EVENT_TYPE.HOLD);
		}
	}
}
