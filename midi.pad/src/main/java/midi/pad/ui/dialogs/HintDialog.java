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
	private final TextField textField;
	private final SinglePixelButton stopButton;
	private MODE currentMode = MODE.NONE;
	private final ToggleWidgets toggletoWidgets;
	private final ToggleWidgets toggletoHint;

	public HintDialog(final String hint) {
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
	}

	public void start() {
		getRuntime().schedule(toggletoHint);
	}

	protected void extraHint(final String extraHint) {
		if (MODE.WIDGET.equals(currentMode)) {
			final SinglePixelButton stopButton = new SinglePixelButton(0, 0,
					new Color(Color.FULL_RED, true, true), toggletoWidgets);
			final TextField hintField = new TextField(0, 0, extraHint, toggletoWidgets);
			getRuntime().schedule(
					new ToggleWidgets(Arrays.asList(stopButton, hintField), MODE.EXTRA_HINT));
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
			removeAllWidget(currentWidgets);
			currentWidgets.clear();
			currentWidgets.addAll(widgets);
			addAllWidget(currentWidgets);
			currentMode = finalMode;
		}
	};
}
