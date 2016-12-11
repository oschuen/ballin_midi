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
 * @since 30.10.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui;

import java.awt.Rectangle;

import midi.pad.ui.event.PadEvent;

/**
 * @author oliver
 *
 */
public abstract class Widget {

	private final Rectangle bounds = new Rectangle();

	public int getWidth() {
		return bounds.width;
	}

	public int getHeight() {
		return bounds.height;
	}

	public int getX() {
		return bounds.x;
	}

	public int getY() {
		return bounds.y;
	}

	abstract void paint(Graphic g);

	/**
	 * @param event
	 *            coords in event must be relative to the origin of the widget
	 * @return true when event is consumed
	 */
	abstract boolean padEventOccured(PadEvent event);

	/**
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		return new Rectangle(bounds);
	}

}
