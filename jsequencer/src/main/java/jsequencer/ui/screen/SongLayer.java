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

import jmidi.gui.model.IntegerModel;
import jmidi.gui.model.TimedIntegerModel;
import jmidi.gui.model.TimedIntegerModel.ValueObserver;
import jsequencer.Orchester;
import jsequencer.persistence.Persistence;
import jsequencer.ui.dialog.setting.LooperConfigDialog;
import jsequencer.ui.dialog.setting.LooperInputDialog;
import jsequencer.ui.model.SongModel;
import midi.loop.beat.Beat;
import midi.loop.beat.Beat.BarListener;
import midi.loop.config.OutputChannelConfig;
import midi.loop.config.OutputChannelConfig.PlayMode;
import midi.pad.ui.Color;
import midi.pad.ui.Screen;
import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.dialogs.NumberDialog;
import midi.pad.ui.event.AbcButtonEvent;
import midi.pad.ui.event.Event;
import midi.pad.ui.event.NumButtonEvent;
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
	private final Persistence persistence;
	private final Orchester orchester;
	private int currentLayer = 0;
	private int nextLayer = 0;
	private final OutModeControlButton percussionInputModeControlButton;
	private final OutModeControlButton guitarInputModeControlButton;
	private final OutModeControlButton[] sequencerInputModeControlButton = new OutModeControlButton[6];

	/**
	 * @param hint
	 */
	public SongLayer(final Orchester orchester, final SongModel model,
			final Persistence persistence, final Beat beat) {
		super("J 1.0");
		this.model = model;
		this.beat = beat;
		this.orchester = orchester;
		this.persistence = persistence;
		guitarInputModeControlButton = new OutModeControlButton(orchester.getGuitarChannelConfig(),
				PlayMode.OFF, PlayMode.LOOP);
		percussionInputModeControlButton = new OutModeControlButton(
				orchester.getPercussionChannelConfig(), PlayMode.OFF, PlayMode.LOOP);

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
		}, () -> {
			getRuntime().invalidate();
			orchester.applyConfigs();
		}, model.getPercussionInputConfig(), model.getLayerModel(0));

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
			orchester.applyConfigs();
			getRuntime().invalidate();
		}, model.getGuitarInputConfig(), model.getLayerModel(1));

		for (int i = 2; i < config.length; i++) {
			final int seqNum = i - 2;
			config[i] = new TrackConfig(i, () -> {
				final SequencerLoopLayer layer = new SequencerLoopLayer("Seq " + (seqNum + 1),
						this.orchester.getSequencer(seqNum));
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
				orchester.applyConfigs();
				getRuntime().invalidate();
			}, model.getSequencerInputChannelConfig(seqNum), model.getLayerModel(i));
		}
		setWidgets(config);
		for (int i = 1; i < 8; i++) {
			config[i].setIn(true);
		}
		for (int i = 0; i < 6; i++) {
			sequencerInputModeControlButton[i] = new OutModeControlButton(
					orchester.getSequencerChannelConfig(i));
		}
		start();
	}

	private void updateModels() {
		for (final TrackConfig trackConfig : config) {
			trackConfig.updateMode();
		}
	}

	@Override
	public Optional<ControlButton> getNumControlButton(final int x) {
		final Screen screen = Runtime.getRuntime().getScreen();
		if (screen.isTopLayer(this)) {
			if (x == 0) {
				return Optional.of(new SimpleControlButton(Color.FULL_RED, () -> {
					final SongSelectionLayer songSelLayer = new SongSelectionLayer(persistence);
					screen.putLayer(3, songSelLayer, () -> {
						updateModels();
						screen.removeLayer(songSelLayer);
					});
				}));
			} else if (x >= 3 && x < 7) {
				final Color color;
				if (x - 3 == nextLayer) {
					color = new Color(Color.LOW_GREEN, true, false);
				} else if (x - 3 == currentLayer) {
					color = Color.GREEN;
				} else {
					color = Color.BLACK;
				}
				return Optional.of(new SimpleControlButton(color, () -> {
					nextLayer = x - 3;
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
		if (x == 1) {
			return Optional.of(new StartStopButton());
		} else if (x == 2) {
			return Optional.of(new SimpleControlButton(Color.FULL_AMBER, () -> {
				final IntegerModel model = new IntegerModel(0, 240, beat.getBpm());
				screen.putLayer(4, new NumberDialog("BpM", model, () -> {
					beat.setBpm(Math.max(1, model.getValue()));
					screen.removeLayer(4);
				}));
			}));
		} else if (x == 7)

		{
			return Optional.of(new SimpleControlButton(Color.FULL_RED, () -> {
				orchester.panic();
			}));
		}

		return super.getNumControlButton(x);
	}

	@Override
	public Optional<ControlButton> getAbcControlButton(final int y) {
		if (y == 0) {
			return Optional.of(percussionInputModeControlButton);
		} else if (y == 1) {
			return Optional.of(guitarInputModeControlButton);
		} else if (y >= 2 && y < 8) {
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

	private class StartStopButton implements ControlButton {
		@Override
		public Color getColor() {
			if (beat.isRunning()) {
				return Color.FULL_GREEN;
			} else {
				return Color.FULL_RED;
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
			if (NumButtonEvent.isEventOfThisType(event)) {
				final NumButtonEvent buttonEvent = NumButtonEvent.getEvent(event);
				if (NumButtonEvent.EVENT_TYPE.NUM_RELEASED.equals(buttonEvent.getEventType())) {
					if (beat.isRunning()) {
						beat.stop();
					} else {
						beat.start();
					}
					getRuntime().invalidate();
				}
			}
			return true;
		}
	}

	private class OutModeControlButton implements ControlButton, ValueObserver<PlayMode> {

		private final OutputChannelConfig config;
		private final TimedIntegerModel<PlayMode> mode;

		public OutModeControlButton(final OutputChannelConfig config) {
			this(config, PlayMode.OFF, PlayMode.LOOP, PlayMode.THROUGH);
		}

		public OutModeControlButton(final OutputChannelConfig config, final PlayMode stableValue,
				final PlayMode... modes) {
			this.config = config;
			mode = new TimedIntegerModel<>(stableValue, modes);
			mode.setValue(this.config.getMode());
			mode.addValueObserver(this);

		}

		@Override
		public Color getColor() {
			switch (config.getMode()) {
			case OFF:
				return Color.BLACK;
			case LOOP:
				return Color.FULL_RED;
			case THROUGH:
				return Color.FULL_GREEN;
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
		public void valueChanged(final PlayMode newValue) {
			Runtime.getRuntime().schedule(() -> {
				config.setMode(newValue);
				orchester.applyConfigs();
				Runtime.getRuntime().invalidate();
			});
		}
	}
}
