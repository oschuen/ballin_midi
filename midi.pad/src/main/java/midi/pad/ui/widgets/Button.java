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
 * @since 28.12.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import static midi.pad.ui.event.Runtime.getRuntime;

import midi.pad.ui.Graphic;
import midi.pad.ui.Widget;
import midi.pad.ui.event.Event;
import midi.pad.ui.event.PadEvent.EVENT_TYPE;

/**
 * @author oliver
 *
 */
public abstract class Button extends Widget {

	private final Runnable pressRunner;
	private boolean sawPress = false;

	public Button(final int x, final int y, final int width, final int height,
			final Runnable pressRunner) {
		bounds.x = x;
		bounds.y = y;
		bounds.width = width;
		bounds.height = height;
		this.pressRunner = pressRunner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public abstract void paint(Graphic g);

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#padEventOccured(midi.pad.ui.event.PadEvent)
	 */
	@Override
	public boolean eventOccured(final Event event) {
		if (event != null && EVENT_TYPE.PAD_PRESSED.equals(event.getEventType())) {
			sawPress = true;
		}
		if (event != null && sawPress && EVENT_TYPE.PAD_RELEASED.equals(event.getEventType())) {
			sawPress = false;
			getRuntime().schedule(pressRunner);
		}
		return true;
	}

}
