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
 * @since 06.11.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.event;

import java.awt.Rectangle;

public class PadEvent {

	public static enum EVENT_TYPE {
		PRESSED, HOLD, RELEASED
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
				&& x < bounds.y + bounds.height) {
			return new PadEvent(eventType, x - bounds.x, y - bounds.y);
		}
		return null;
	}
}