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

import static midi.pad.ui.event.Runtime.getRuntime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import midi.pad.ui.Color;
import midi.pad.ui.Layer;
import midi.pad.ui.Widget;
import midi.pad.ui.widgets.SinglePixelButton;
import midi.pad.ui.widgets.TextField;

/**
 * @author oliver
 *
 */
public class HintDialog extends Layer {

	private enum MODE {
		NONE, HINT, EXTRA_HINT, WIDGET
	};

	private final List<Widget> currentWidgets = new ArrayList<>();
	private final List<Widget> widgets = new ArrayList<>();
	private TextField textField;
	private final SinglePixelButton stopButton;
	private MODE currentMode = MODE.NONE;
	private final ToggleWidgets toggletoWidgets;
	private final ToggleWidgets toggletoHint;

	public HintDialog(final String hint) {
		super(true);
		setHint(hint);
		toggletoWidgets = new ToggleWidgets(widgets, MODE.WIDGET);
		stopButton = new SinglePixelButton(0, 0, new Color(Color.FULL_RED, true, true),
				toggletoWidgets);
		textField = new TextField(0, 0, hint, toggletoWidgets);
		toggletoHint = new ToggleWidgets(Arrays.asList(stopButton, textField), MODE.HINT) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see midi.pad.ui.dialogs.HintDialog.ToggleWidgets#run()
			 */
			@Override
			public void run() {
				super.run();
				textField.start();
			}
		};
	}

	protected void setWidgets(final Widget... widgets) {
		this.widgets.clear();
		this.widgets.addAll(Arrays.asList(widgets));
		addAllWidget(this.widgets);
	}

	public void start() {
		if (!getRuntime().hasExtraDisplay()) {
			getRuntime().schedule(toggletoHint);
		}
	}

	protected void extraHint(final String extraHint) {
		setExtraHint(extraHint);
		if (MODE.WIDGET.equals(currentMode) && !getRuntime().hasExtraDisplay()) {
			final SinglePixelButton stopButton = new SinglePixelButton(0, 0,
					new Color(Color.FULL_RED, true, true), toggletoWidgets);
			textField = new TextField(0, 0, extraHint, toggletoWidgets);
			getRuntime().schedule(
					new ToggleWidgets(Arrays.asList(stopButton, textField), MODE.EXTRA_HINT) {
						/*
						 * (non-Javadoc)
						 * 
						 * @see
						 * midi.pad.ui.dialogs.HintDialog.ToggleWidgets#run()
						 */
						@Override
						public void run() {
							super.run();
							textField.start();
						}
					});
		}
	}

	private class ToggleWidgets implements Runnable {
		private final List<Widget> widgets;
		private final MODE finalMode;

		public ToggleWidgets(final List<Widget> widgets, final MODE finalMode) {
			super();
			this.widgets = widgets;
			this.finalMode = finalMode;
		}

		@Override
		public void run() {
			clearWidgets();
			currentWidgets.clear();
			currentWidgets.addAll(widgets);
			addAllWidget(currentWidgets);
			currentMode = finalMode;
		}
	};
}
