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
 * @since 21.06.2019
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import static midi.pad.ui.event.Runtime.getRuntime;

import jmidi.gui.model.LayerModel;
import jmidi.gui.model.TimedIntegerModel;
import jmidi.gui.model.TimedIntegerModel.ValueObserver;
import midi.loop.config.InputChannelConfig;
import midi.loop.config.InputChannelConfig.InputMode;
import midi.pad.ui.Color;
import midi.pad.ui.Graphic;
import midi.pad.ui.Widget;
import midi.pad.ui.event.Event;
import midi.pad.ui.event.PadEvent;
import midi.pad.ui.event.PadEvent.EVENT_TYPE;
import midi.pad.ui.event.Runtime;

/**
 * <pre>
 * {@code
 *  Note Edit
 *  Channel In Config
 *  Channel Out Config
 *  Model 1
 *  Model 2
 *  Model 3
 *  Model 4
 *  Input: none, low, high, all
 *    
 *  Output: Loop, Through, off
 *  }
 * </pre>
 * 
 * @author oliver
 *
 */
public class TrackConfig extends Widget {

	private final Runnable noteEditRunnable;
	private final Runnable channelInConfigRunnable;
	private final Runnable channelOutConfigRunnable;
	private final Runnable changeRunnable;
	private final LayerModel layerModel;
	private final InputChannelConfig inConfig;
	private boolean in;
	private final TimedIntegerModel<InputMode> inputModeModel;

	public TrackConfig(final int y, final Runnable noteEditRunnable,
			final Runnable channelInConfigRunnable, final Runnable channelOutConfigRunnable,
			final Runnable changeRunnable, final InputChannelConfig inConfig,
			final LayerModel layerModel) {
		this(y, noteEditRunnable, channelInConfigRunnable, channelOutConfigRunnable, changeRunnable,
				inConfig, layerModel, InputMode.OFF, InputMode.BELOW, InputMode.ABOVE,
				InputMode.ALL);
	}

	public TrackConfig(final int y, final Runnable noteEditRunnable,
			final Runnable channelInConfigRunnable, final Runnable channelOutConfigRunnable,
			final Runnable changeRunnable, final InputChannelConfig outConfig,
			final LayerModel layerModel, final InputMode... modes) {
		super();
		bounds.x = 0;
		bounds.y = y;
		bounds.width = 8;
		bounds.height = 1;
		this.noteEditRunnable = noteEditRunnable;
		this.channelInConfigRunnable = channelInConfigRunnable;
		this.channelOutConfigRunnable = channelOutConfigRunnable;
		this.changeRunnable = changeRunnable;
		this.layerModel = layerModel;
		inConfig = outConfig;
		inputModeModel = new TimedIntegerModel<>(InputMode.OFF, modes);
		inputModeModel.setValue(inConfig.getMode());
		inputModeModel.addValueObserver(new ValueObserver<InputMode>() {
			@Override
			public void valueChanged(final InputMode newValue) {
				Runtime.getRuntime().schedule(() -> {
					outConfig.setMode(newValue);
					getRuntime().schedule(changeRunnable);
				});
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		g.setPixel(0, 0, Color.GREEN);
		g.setPixel(1, 0, in ? Color.RED : Color.BLACK);
		g.setPixel(2, 0, Color.FULL_AMBER);
		if (inputModeModel.getValue() == InputMode.OFF || !in) {
			g.setPixel(7, 0, Color.BLACK);
		} else if (inputModeModel.getValue() == InputMode.ABOVE) {
			g.setPixel(7, 0, Color.FULL_GREEN);
		} else if (inputModeModel.getValue() == InputMode.BELOW) {
			g.setPixel(7, 0, Color.FULL_RED);
		} else {
			g.setPixel(7, 0, Color.FULL_YELLOW);
		}
		for (int i = 0; i < 4; i++) {
			switch (layerModel.getLayer(i)) {
			case 0:
				g.setPixel(3 + i, 0, Color.BLACK);
				break;
			case 1:
				g.setPixel(3 + i, 0, Color.GREEN);
				break;
			case 2:
				g.setPixel(3 + i, 0, Color.RED);
				break;
			case 3:
				g.setPixel(3 + i, 0, Color.FULL_AMBER);
				break;
			case 4:
				g.setPixel(3 + i, 0, Color.LOW_AMBER);
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#eventOccured(midi.pad.ui.event.Event)
	 */
	@Override
	public boolean eventOccured(final Event event) {
		boolean changed = false;
		if (event != null && EVENT_TYPE.PAD_RELEASED.equals(event.getEventType())) {
			final PadEvent padEvent = PadEvent.getEvent(event);

			switch (padEvent.getX()) {
			case 0:
				getRuntime().schedule(noteEditRunnable);
				break;
			case 1:
				if (in) {
					getRuntime().schedule(channelInConfigRunnable);
				}
				break;
			case 2:
				getRuntime().schedule(channelOutConfigRunnable);
				break;
			case 7:
				inputModeModel.stopIncrementing();
				changed = true;
				break;
			default:
				layerModel.increment(padEvent.getX() - 3);
				changed = true;
			}
		}
		if (in && event != null && EVENT_TYPE.PAD_PRESSED.equals(event.getEventType())) {
			final PadEvent padEvent = PadEvent.getEvent(event);
			if (padEvent.getX() == 7) {
				inputModeModel.increment();
			}
		}
		if (in && event != null && EVENT_TYPE.PAD_HOLD.equals(event.getEventType())) {
			final PadEvent padEvent = PadEvent.getEvent(event);
			if (padEvent.getX() == 7) {
				inputModeModel.startIncrementing();
			}
		}
		if (changed) {
			getRuntime().schedule(changeRunnable);
		}
		return true;
	}

	/**
	 * @return the in
	 */
	public boolean isIn() {
		return in;
	}

	/**
	 * @param in
	 *            the in to set
	 */
	public void setIn(final boolean in) {
		this.in = in;
	}

	/**
	 * @return the mode
	 */
	public InputMode getMode() {
		return inputModeModel.getValue();
	}

	public void updateMode() {
		inputModeModel.setValue(inConfig.getMode());
	}

	/**
	 * @return the model
	 */
	public int getModel(final int x) {
		return layerModel.getLayer(x);
	}
}
