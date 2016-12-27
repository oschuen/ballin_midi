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
package midi.pad.ui.dialogs;

import static midi.pad.ui.event.Runtime.getRuntime;

import midi.pad.ui.Layer;
import midi.pad.ui.event.PadEvent;
import midi.pad.ui.event.PadEvent.EVENT_TYPE;
import midi.pad.ui.widgets.NoButton;
import midi.pad.ui.widgets.TextField;
import midi.pad.ui.widgets.YesButton;

/**
 * @author oliver
 *
 */
public class ConfirmDialog extends Layer {
	private final YesButton yesButton;
	private final NoButton noButton;
	private final TextField textField;
	private boolean textMode = true;
	private final Runnable showButtonRunnable = new Runnable() {

		@Override
		public void run() {
			if (textMode) {
				textMode = false;
				removeWidget(textField);
				addWidget(yesButton);
				addWidget(noButton);
			}
		}
	};

	public ConfirmDialog(final String question, final Runnable confirmRunner,
			final Runnable regretRunner) {
		yesButton = new YesButton(4, 4, confirmRunner);
		noButton = new NoButton(0, 4, regretRunner);
		textField = new TextField(0, 0, question, showButtonRunnable);
		addWidget(textField);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Layer#padEventOccured(midi.pad.ui.event.PadEvent)
	 */
	@Override
	public boolean padEventOccured(final PadEvent event) {
		if (textMode) {
			if (event != null && EVENT_TYPE.RELEASED.equals(event.getEventType())) {
				getRuntime().schedule(showButtonRunnable);
				return true;
			}
		}
		return super.padEventOccured(event);
	}
}
