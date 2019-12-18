/**
 * Copyright (C) 2018 Oliver Schünemann
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
 * @since 05.05.2018
 * @version 1.0
 * @author oliver
 */
package jsequencer.ui.screen;

import java.util.Optional;

import jmidi.gui.model.IntegerModel;
import midi.instrument.Instrument;
import midi.instrument.model.PercussionModel;
import midi.loop.LoopModel;
import midi.loop.beat.Beat.BeatListener;
import midi.pad.ui.Color;
import midi.pad.ui.Screen;
import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.dialogs.NumberDialog;
import midi.pad.ui.event.Runtime;
import midi.pad.ui.widgets.ControlButton;
import midi.pad.ui.widgets.InstrumentSelector;
import midi.pad.ui.widgets.LoopConfig;
import midi.pad.ui.widgets.SimpleControlButton;
import midi.pad.ui.widgets.SimpleLooper;

/**
 * @author oliver
 *
 */
public class DrumLoopLayer extends HintDialog implements BeatListener {

	private final LoopConfig config;
	private final InstrumentSelector selector;
	private final PercussionModel model;
	private final SimpleLooper looper;
	private final SimpleLooper accent;
	private final SimpleControlButton accentButton;
	private final SimpleControlButton instrumentButton;

	public DrumLoopLayer(final PercussionModel model) {
		super("Drums");
		this.model = model;

		config = new LoopConfig(0, 0, new Runnable() {
			@Override
			public void run() {
				model.setNumberOfPages(config.getNumberOfPages());
				model.setQuarterDivision(config.getQuarterDivision());
				model.setQuarterPerPage(config.getQuarterPerPage());
			}
		}, new Runnable() {

			@Override
			public void run() {
				looper.setHold(config.isHold());
				looper.setHoldPage(config.getHoldPage());
				looper.setHoldQuarter(config.getHoldQuarter());
				accent.setHold(config.isHold());
				accent.setHoldPage(config.getHoldPage());
				accent.setHoldQuarter(config.getHoldQuarter());
			}
		});
		selector = new InstrumentSelector(0, 3, 8, 2, PercussionModel.PercussionInstrument.values(),
				new Runnable() {
					@Override
					public void run() {
						extraHint(selector.getInstrument().toString());
						final Optional<LoopModel> loopModel = model
								.getLoopModel(selector.getInstrument());
						loopModel.ifPresent(m -> looper.setLoopModel(m));
					}
				});
		accent = new SimpleLooper(0, 5);
		looper = new SimpleLooper(0, 6);
		final Optional<LoopModel> loopModel = model.getLoopModel(selector.getInstrument());
		loopModel.ifPresent(m -> looper.setLoopModel(m));
		accent.setLoopModel(model.getAccentModel());
		setWidgets(config, selector, accent, looper);
		accentButton = new SimpleControlButton(Color.GREEN, new ConfigureAccentVelocity());
		instrumentButton = new SimpleControlButton(Color.GREEN, new ConfigureDrumVelocity());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BeatListener#accept(long)
	 */
	@Override
	public void accept(final long beat) {
		config.accept(beat);
		accent.accept(beat);
		looper.accept(beat);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Layer#getAbcControlButton(int)
	 */
	@Override
	public Optional<ControlButton> getAbcControlButton(final int y) {
		if (y == 5) {
			return Optional.of(accentButton);

		} else if (y == 6) {
			return Optional.of(instrumentButton);
		}
		return Optional.empty();
	}

	private final class ConfigureAccentVelocity implements Runnable {

		public ConfigureAccentVelocity() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			final Screen screen = Runtime.getRuntime().getScreen();
			final IntegerModel vm = model.getAccentModel().getVelocityModel();
			screen.putLayer(4, new NumberDialog("Accent Velocity", vm, () -> {
				screen.removeLayer(4);
			}));
		}
	}

	private final class ConfigureDrumVelocity implements Runnable {

		public ConfigureDrumVelocity() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			final Screen screen = Runtime.getRuntime().getScreen();
			final Instrument instrument = selector.getInstrument();
			final Optional<LoopModel> loopModel = model.getLoopModel(instrument);
			loopModel.ifPresent(m -> {
				final IntegerModel vm = m.getVelocityModel();
				screen.putLayer(4, new NumberDialog(instrument.name() + " Velocity", vm, () -> {
					screen.removeLayer(4);
				}));
			});
		}
	}
}
