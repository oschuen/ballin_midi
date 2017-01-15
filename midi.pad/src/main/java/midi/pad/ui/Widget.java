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
 * @since 30.10.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui;

import java.awt.Rectangle;

import midi.pad.ui.event.Event;

/**
 * @author oliver
 *
 */
public abstract class Widget {

	protected final Rectangle bounds = new Rectangle();

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

	public abstract void paint(Graphic g);

	/**
	 * @param event
	 *            coords in event must be relative to the origin of the widget
	 * @return true when event is consumed
	 */
	public abstract boolean eventOccured(Event event);

	/**
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		return new Rectangle(bounds);
	}

}
