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
 * @since 01.06.2019
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.event;

/**
 * @author oliver
 *
 */
public class NumButtonEvent implements Event {

	public static enum EVENT_TYPE implements EventType {
		NUM_PRESSED, NUM_HOLD, NUM_RELEASED;
	}

	private final EVENT_TYPE eventType;
	private final int x;

	public NumButtonEvent(final EVENT_TYPE eventType, final int x) {
		super();
		this.eventType = eventType;
		this.x = x;
	}

	/**
	 * @return the eventType
	 */
	@Override
	public EVENT_TYPE getEventType() {
		return eventType;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * midi.pad.ui.event.Event.EventType#isEventOfThisType(midi.pad.ui.event
	 * .Event.EventType)
	 */
	public static final boolean isEventOfThisType(final Event event) {
		if (event == null || event.getEventType() == null) {
			return false;
		}
		final EVENT_TYPE[] values = EVENT_TYPE.values();
		final int ordinal = event.getEventType().ordinal();
		return (ordinal >= 0 && ordinal <= values.length
				&& values[ordinal].equals(event.getEventType()));
	}

	public static NumButtonEvent getEvent(final Event event) {
		return (NumButtonEvent) event;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "NumButtonEvent [eventType=" + eventType + ", x=" + x + "]";
	}

}
