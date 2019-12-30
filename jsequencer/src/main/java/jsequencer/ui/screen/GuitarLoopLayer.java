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
 * @since 02.06.2019
 * @version 1.0
 * @author oliver
 */
package jsequencer.ui.screen;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jmidi.gui.model.IntegerModel;
import midi.instrument.Instrument;
import midi.instrument.model.GuitarModel;
import midi.instrument.model.GuitarModel.GuitarInstrument;
import midi.loop.beat.Beat.BeatListener;
import midi.pad.ui.Color;
import midi.pad.ui.Screen;
import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.dialogs.NumberDialog;
import midi.pad.ui.event.Runtime;
import midi.pad.ui.widgets.ControlButton;
import midi.pad.ui.widgets.LoopConfig;
import midi.pad.ui.widgets.SimpleControlButton;
import midi.pad.ui.widgets.SimpleLooper;

/**
 * @author oliver
 *
 */
public class GuitarLoopLayer extends HintDialog implements BeatListener {
	private final LoopConfig config;
	private final GuitarModel model;
	private final SimpleLooper accent = new SimpleLooper(0, 3);
	private final SimpleLooper bassLooper = new SimpleLooper(0, 4);
	private final SimpleLooper gLooper = new SimpleLooper(0, 5);
	private final SimpleLooper bLooper = new SimpleLooper(0, 6);
	private final SimpleLooper eLooper = new SimpleLooper(0, 7);
	private final List<SimpleLooper> guitarLoopers = Arrays.asList(bassLooper, gLooper, bLooper,
			eLooper);
	private final SimpleControlButton accentButton;
	private final SimpleControlButton bassButton;
	private final SimpleControlButton gButton;
	private final SimpleControlButton bButton;
	private final SimpleControlButton eButton;

	public GuitarLoopLayer(final GuitarModel model) {
		super("Guitar");
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
				for (final SimpleLooper looper : guitarLoopers) {
					looper.setHold(config.isHold());
					looper.setHoldPage(config.getHoldPage());
					looper.setHoldQuarter(config.getHoldQuarter());
				}
				accent.setHold(config.isHold());
				accent.setHoldPage(config.getHoldPage());
				accent.setHoldQuarter(config.getHoldQuarter());
			}
		}, model.getAccentModel());
		accent.setLoopModel(model.getAccentModel());
		model.getLoopModel(GuitarInstrument.BASS_STRING).ifPresent(m -> bassLooper.setLoopModel(m));
		model.getLoopModel(GuitarInstrument.G_STRING).ifPresent(m -> gLooper.setLoopModel(m));
		model.getLoopModel(GuitarInstrument.B_STRING).ifPresent(m -> bLooper.setLoopModel(m));
		model.getLoopModel(GuitarInstrument.E_STRING).ifPresent(m -> eLooper.setLoopModel(m));
		setWidgets(config, accent, bassLooper, gLooper, bLooper, eLooper);
		accentButton = new SimpleControlButton(Color.GREEN, new ConfigureAccentVelocity());
		bassButton = new SimpleControlButton(Color.GREEN,
				new ConfigureInstrumentVelocity(GuitarInstrument.BASS_STRING));
		gButton = new SimpleControlButton(Color.GREEN,
				new ConfigureInstrumentVelocity(GuitarInstrument.G_STRING));
		bButton = new SimpleControlButton(Color.GREEN,
				new ConfigureInstrumentVelocity(GuitarInstrument.B_STRING));
		eButton = new SimpleControlButton(Color.GREEN,
				new ConfigureInstrumentVelocity(GuitarInstrument.E_STRING));
		start();
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
		for (final SimpleLooper looper : guitarLoopers) {
			looper.accept(beat);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Layer#getAbcControlButton(int)
	 */
	@Override
	public Optional<ControlButton> getAbcControlButton(final int y) {
		switch (y) {
		case 3:
			return Optional.of(accentButton);
		case 4:
			return Optional.of(bassButton);
		case 5:
			return Optional.of(gButton);
		case 6:
			return Optional.of(bButton);
		case 7:
			return Optional.of(eButton);

		}
		if (y == 3) {

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

	private final class ConfigureInstrumentVelocity implements Runnable {

		private final Instrument instrument;

		public ConfigureInstrumentVelocity(final Instrument instrument) {
			super();
			this.instrument = instrument;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			final Screen screen = Runtime.getRuntime().getScreen();
			model.getLoopModel(instrument).ifPresent(
					m -> screen.putLayer(4, new NumberDialog(instrument.toString() + " Velocity",
							m.getVelocityModel(), () -> screen.removeLayer(4))));

		}
	}

}
