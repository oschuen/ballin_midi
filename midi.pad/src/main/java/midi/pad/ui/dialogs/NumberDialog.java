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
 * @since 28.12.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.dialogs;

import midi.pad.ui.Color;
import midi.pad.ui.widgets.NumberPad;
import midi.pad.ui.widgets.SinglePixelButton;

/**
 * @author oliver
 *
 */
public class NumberDialog extends HintDialog {

	private final NumberPad numberPad;

	public NumberDialog(final String question, final int maxValue, final int currentValue,
			final Runnable confirmRunner) {
		super(question + " " + Integer.toString(currentValue));
		numberPad = new NumberPad(5, 3, maxValue, currentValue, confirmRunner, new Runnable() {
			@Override
			public void run() {
				extraHint(Integer.toString(numberPad.getCurrentValue()));
			}
		});
		setWidgets(new SinglePixelButton(7, 7, Color.LOW_AMBER, () -> {
			extraHint(question);
		}), numberPad);
		start();
	}
}
