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
 * @since 02.06.2019
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import static midi.pad.ui.event.Runtime.getRuntime;

import midi.pad.ui.Color;
import midi.pad.ui.event.AbcButtonEvent;
import midi.pad.ui.event.AbcButtonEvent.EVENT_TYPE;
import midi.pad.ui.event.Event;

/**
 * @author oliver
 *
 */
public class AbcControlButton {
	private final Runnable pressRunner;
	private final Color color;

	public AbcControlButton(final Color c, final Runnable pressRunner) {
		this.pressRunner = pressRunner;
		color = c;
	}

	public boolean eventOccured(final Event event) {
		if (AbcButtonEvent.isEventOfThisType(event)) {
			final AbcButtonEvent buttonEvent = AbcButtonEvent.getEvent(event);
			if (EVENT_TYPE.ABC_RELEASED.equals(buttonEvent.getEventType())) {
				getRuntime().schedule(pressRunner);
			}
		}
		return true;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}
}
