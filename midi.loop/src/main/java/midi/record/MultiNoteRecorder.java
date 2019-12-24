/**
 * Copyright (C) 2019 Oliver Schünemann
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
 * @since 24.12.2019
 * @version 1.0
 * @author oliver
 */
package midi.record;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.loop.LoopEvent;

/**
 * @author oliver
 *
 */
public class MultiNoteRecorder {
	private final Set<Integer> pressed = new HashSet<>();
	private final List<Event> events = new ArrayList<>();
	private final Runnable stepRunnable;
	private final LoopEvent defaultEvent = new LoopEvent();
	private LoopEvent event = new LoopEvent();
	private final long HOLD_LENGTH = 250;
	private final static Logger logger = LoggerFactory.getLogger(MultiNoteRecorder.class);

	public MultiNoteRecorder(final Runnable stepRunnable) {
		this.stepRunnable = stepRunnable;
	}

	public void send(final ShortMessage shortMessage) {
		final int data1 = shortMessage.getData1();
		final int data2 = shortMessage.getData1();
		if (shortMessage.getCommand() == ShortMessage.NOTE_OFF
				|| (shortMessage.getCommand() == ShortMessage.NOTE_ON && data2 == 0)) {
			final Event event = new Event(data1, System.currentTimeMillis());
			events.add(event);
			pressed.remove(Integer.valueOf(shortMessage.getData1()));
			if (pressed.isEmpty()) {
				step(event);
			}
		} else if (shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
			pressed.add(Integer.valueOf(data1));
		}
	}

	/**
	 * @param lastEvent
	 * 
	 */
	private void step(final Event lastEvent) {
		final List<Integer> notes = event.getNotes();
		notes.clear();
		final long bound = lastEvent.getTime() - HOLD_LENGTH;
		final Set<Integer> newNotes = events.stream().filter(it -> it.getTime() > bound)
				.map(Event::getNote).collect(Collectors.toSet());
		notes.addAll(newNotes);
		if (stepRunnable != null) {
			stepRunnable.run();
		}
		events.clear();

		logger.info("Step : {}", notes);
	}

	private class Event {
		private final int note;
		private final long time;

		public Event(final int note, final long time) {
			super();
			this.note = note;
			this.time = time;
		}

		/**
		 * @return the note
		 */
		public int getNote() {
			return note;
		}

		/**
		 * @return the time
		 */
		public long getTime() {
			return time;
		}

	}

	/**
	 * @param event
	 *            the event to set
	 */
	public void setEvent(final LoopEvent event) {
		if (event == null) {
			this.event = defaultEvent;
		} else {
			this.event = event;
		}
		pressed.clear();
	}
}
