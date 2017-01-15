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
package midi.pad.ui.dialogs;

import jmidi.gui.model.IntegerModel;
import midi.pad.ui.Color;
import midi.pad.ui.widgets.EatAllEventsField;
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
		this(question, new IntegerModel(0, maxValue, currentValue), confirmRunner);
	}

	public NumberDialog(final String question, final IntegerModel model,
			final Runnable confirmRunner) {
		super(question + " " + Integer.toString(model.getValue()));
		numberPad = new NumberPad(5, 3, model, confirmRunner, new Runnable() {
			@Override
			public void run() {
				extraHint(Integer.toString(numberPad.getCurrentValue()));
			}
		});
		setWidgets(new SinglePixelButton(7, 7, Color.LOW_AMBER, () -> {
			extraHint(question);
		}), numberPad, new EatAllEventsField(0, 0, 8, 8));
		start();
	}

	public int getValue() {
		return numberPad.getCurrentValue();
	}
}
