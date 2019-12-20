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
 * @since 22.06.2019
 * @version 1.0
 * @author oliver
 */
package jsequencer.ui.screen;

import static midi.pad.ui.event.Runtime.getRuntime;

import java.util.Optional;

import jmidi.gui.model.TimedIntegerModel;
import jmidi.gui.model.TimedIntegerModel.ValueObserver;
import jsequencer.Orchester;
import jsequencer.ui.dialog.setting.LooperConfigDialog;
import jsequencer.ui.dialog.setting.LooperInputDialog;
import jsequencer.ui.model.SongModel;
import midi.loop.beat.Beat;
import midi.loop.beat.Beat.BarListener;
import midi.loop.config.InputChannelConfig;
import midi.loop.config.InputChannelConfig.InputMode;
import midi.loop.config.OutputChannelConfig.PlayMode;
import midi.pad.ui.Color;
import midi.pad.ui.Screen;
import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.event.AbcButtonEvent;
import midi.pad.ui.event.Event;
import midi.pad.ui.event.Runtime;
import midi.pad.ui.widgets.ControlButton;
import midi.pad.ui.widgets.SimpleControlButton;
import midi.pad.ui.widgets.TrackConfig;

/**
 * @author oliver
 *
 */
public class SongLayer extends HintDialog implements BarListener {

	private final TrackConfig[] config = new TrackConfig[8];
	private final SongModel model;
	private final Beat beat;
	private final Orchester orchester;
	private int currentLayer = 0;
	private int nextLayer = 0;
	private final InputModeControlButton guitarInputModeControlButton;
	private final InputModeControlButton[] sequencerInputModeControlButton = new InputModeControlButton[6];

	/**
	 * @param hint
	 */
	public SongLayer(final Orchester orchester, final SongModel model, final Beat beat) {
		super("J 1.0");
		this.model = model;
		this.beat = beat;
		this.orchester = orchester;
		guitarInputModeControlButton = new InputModeControlButton(orchester.getGuitarInputConfig());

		config[0] = new TrackConfig(0, () -> {
			final DrumLoopLayer layer = new DrumLoopLayer(
					this.model.getPercussionModel(currentLayer));
			final Screen screen = Runtime.getRuntime().getScreen();
			this.beat.addBeatListener(layer);
			screen.putLayer(3, layer, () -> {
				beat.removeBeatListener(layer);
				screen.removeLayer(layer);
			});
			layer.start();
		}, () -> {
			getRuntime().invalidate();
		}, () -> {
			final Screen screen = Runtime.getRuntime().getScreen();
			final LooperConfigDialog layer = new LooperConfigDialog("Percussion Output", model,
					model.getPercussionChannelConfig());
			screen.putLayer(3, layer, () -> {
				screen.removeLayer(layer);
				orchester.applyConfigs();
			});
			layer.start();
			getRuntime().invalidate();
			getRuntime().invalidate();
		}, () -> {
			getRuntime().invalidate();
		}, model.getPercussionChannelConfig(), model.getLayerModel(0), PlayMode.LOOP);

		config[1] = new TrackConfig(1, () -> {
			final GuitarLoopLayer layer = new GuitarLoopLayer(
					this.model.getGuitarModel(currentLayer));
			final Screen screen = Runtime.getRuntime().getScreen();
			this.beat.addBeatListener(layer);
			screen.putLayer(3, layer, () -> {
				beat.removeBeatListener(layer);
				screen.removeLayer(layer);
			});
			layer.start();
		}, () -> {
			final Screen screen = Runtime.getRuntime().getScreen();
			final LooperInputDialog layer = new LooperInputDialog("Guitar Input",
					model.getGuitarInputConfig());
			screen.putLayer(3, layer, () -> {
				screen.removeLayer(layer);
				orchester.applyConfigs();
			});
			layer.start();
			getRuntime().invalidate();
		}, () -> {
			final Screen screen = Runtime.getRuntime().getScreen();
			final LooperConfigDialog layer = new LooperConfigDialog("Guitar Output", model,
					model.getGuitarChannelConfig());
			screen.putLayer(3, layer, () -> {
				screen.removeLayer(layer);
				orchester.applyConfigs();
			});
			layer.start();
			getRuntime().invalidate();
		}, () -> {
			getRuntime().invalidate();
		}, model.getGuitarChannelConfig(), model.getLayerModel(1), PlayMode.THROUGH);

		for (int i = 2; i < config.length; i++) {
			final int seqNum = i - 2;
			config[i] = new TrackConfig(i, () -> {
				final SequencerLoopLayer layer = new SequencerLoopLayer("Seq " + (seqNum + 1),
						this.model.getSequencerModel(seqNum, currentLayer));
				final Screen screen = Runtime.getRuntime().getScreen();

				this.beat.addBeatListener(layer);
				screen.putLayer(3, layer, () -> {
					beat.removeBeatListener(layer);
					screen.removeLayer(layer);
				});
				layer.start();
				getRuntime().invalidate();
			}, () -> {
				final Screen screen = Runtime.getRuntime().getScreen();
				final LooperInputDialog layer = new LooperInputDialog(
						"Seq " + (seqNum + 1) + " Input",
						model.getSequencerInputChannelConfig(seqNum));
				screen.putLayer(3, layer, () -> {
					screen.removeLayer(layer);
					orchester.applyConfigs();
				});
				layer.start();
				getRuntime().invalidate();
			}, () -> {
				final Screen screen = Runtime.getRuntime().getScreen();
				final LooperConfigDialog layer = new LooperConfigDialog(
						"Seq " + (seqNum + 1) + " Output", model,
						model.getSequencerChannelConfig(seqNum));
				screen.putLayer(3, layer, () -> {
					screen.removeLayer(layer);
					orchester.applyConfigs();
				});
				layer.start();
				getRuntime().invalidate();
			}, () -> {
				getRuntime().invalidate();
			}, model.getSequencerChannelConfig(seqNum), model.getLayerModel(i));
		}
		setWidgets(config);
		for (int i = 1; i < 8; i++) {
			config[i].setIn(true);
		}
		for (int i = 0; i < 6; i++) {
			sequencerInputModeControlButton[i] = new InputModeControlButton(
					orchester.getSequencerInputConfig(i));
		}
		start();
	}

	@Override
	public Optional<ControlButton> getNumControlButton(final int x) {
		final Screen screen = Runtime.getRuntime().getScreen();
		if (screen.isTopLayer(this)) {
			if (x >= 4) {
				final Color color;
				if (x - 4 == nextLayer) {
					color = new Color(Color.LOW_GREEN, true, false);
				} else if (x - 4 == currentLayer) {
					color = Color.GREEN;
				} else {
					color = Color.BLACK;
				}
				return Optional.of(new SimpleControlButton(color, () -> {
					nextLayer = x - 4;
				}));
			}
		} else {
			if (x == 0) {
				return Optional.of(new SimpleControlButton(Color.GREEN, () -> {
					screen.showBottomLayer();
					getRuntime().invalidate();
				}));
			}
		}
		return super.getNumControlButton(x);
	}

	@Override
	public Optional<ControlButton> getAbcControlButton(final int y) {
		if (y == 1) {
			return Optional.of(guitarInputModeControlButton);
		}
		if (y >= 2 && y < 8) {
			return Optional.of(sequencerInputModeControlButton[y - 2]);
		}
		return Optional.empty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BarListener#getNumberOfQuarterPerBar()
	 */
	@Override
	public long getNumberOfQuarterPerBar() {
		return model.getPercussionModel(currentLayer).getQuarterPerPage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BarListener#accept(long)
	 */
	@Override
	public void accept(final long bar) {
		if (nextLayer >= 0) {
			currentLayer = nextLayer;
			orchester.setLayer(currentLayer);
		}
		nextLayer = -1;
	}

	private class InputModeControlButton implements ControlButton, ValueObserver<InputMode> {

		private final InputChannelConfig config;
		private final TimedIntegerModel<InputMode> mode = new TimedIntegerModel<>(InputMode.OFF,
				InputMode.BELOW, InputMode.ABOVE, InputMode.ALL);

		public InputModeControlButton(final InputChannelConfig config) {
			this.config = config;
			mode.setValue(this.config.getMode());
			mode.addValueObserver(this);
		}

		@Override
		public Color getColor() {
			switch (config.getMode()) {
			case ABOVE:
				return Color.FULL_YELLOW;
			case ALL:
				return Color.FULL_GREEN;
			case BELOW:
				return Color.FULL_RED;
			case OFF:
			default:
				return Color.BLACK;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * midi.pad.ui.widgets.ControlButton#eventOccured(midi.pad.ui.event.
		 * Event)
		 */
		@Override
		public boolean eventOccured(final Event event) {
			if (AbcButtonEvent.isEventOfThisType(event)) {
				final AbcButtonEvent buttonEvent = AbcButtonEvent.getEvent(event);
				if (AbcButtonEvent.EVENT_TYPE.ABC_PRESSED.equals(buttonEvent.getEventType())) {
					mode.increment();
				} else if (AbcButtonEvent.EVENT_TYPE.ABC_HOLD.equals(buttonEvent.getEventType())) {
					mode.startIncrementing();
				} else if (AbcButtonEvent.EVENT_TYPE.ABC_RELEASED
						.equals(buttonEvent.getEventType())) {
					mode.stopIncrementing();
				}
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jmidi.gui.model.TimedIntegerModel.ValueObserver#valueChanged(java.
		 * lang.Object)
		 */
		@Override
		public void valueChanged(final InputMode newValue) {
			Runtime.getRuntime().schedule(() -> {
				config.setMode(newValue);
				orchester.applyConfigs();
				Runtime.getRuntime().invalidate();
			});
		}
	}
}
