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
 * limitations under the License.ree Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
 * 
 * @since 06.11.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.event;

import java.awt.Rectangle;

public class PadEvent implements Event {

	public static enum EVENT_TYPE implements EventType {
		PAD_PRESSED, PAD_HOLD, PAD_RELEASED;
	}

	private final EVENT_TYPE eventType;
	private final int x;
	private final int y;

	public PadEvent(final EVENT_TYPE eventType, final int x, final int y) {
		super();
		this.eventType = eventType;
		this.x = x;
		this.y = y;
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

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	public PadEvent translate(final Rectangle bounds) {
		if (x >= bounds.x && x < bounds.x + bounds.width && y >= bounds.y
				&& y < bounds.y + bounds.height) {
			return new PadEvent(eventType, x - bounds.x, y - bounds.y);
		}
		return null;
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

	public static PadEvent getEvent(final Event event) {
		return (PadEvent) event;
	}

}