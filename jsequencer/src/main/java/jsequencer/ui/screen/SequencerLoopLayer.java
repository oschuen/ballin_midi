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
 * @since 16.12.2019
 * @version 1.0
 * @author oliver
 */
package jsequencer.ui.screen;

import java.util.Optional;

import midi.instrument.model.SequencerModel;
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
public class SequencerLoopLayer extends HintDialog implements BeatListener {

	private final LoopConfig config;
	private final SequencerModel model;
	private final SimpleLooper looper = new SimpleLooper(0, 4);
	private final SimpleControlButton velocityButton;

	public SequencerLoopLayer(final String name, final SequencerModel model) {
		super(name);
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
			}
		});
		looper.setLoopModel(this.model.getModel());
		setWidgets(config, looper);
		velocityButton = new SimpleControlButton(Color.GREEN, new ConfigureInstrumentVelocity());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BeatListener#accept(long)
	 */
	@Override
	public void accept(final long beat) {
		config.accept(beat);
		looper.accept(beat);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Layer#getAbcControlButton(int)
	 */
	@Override
	public Optional<ControlButton> getAbcControlButton(final int y) {
		if (y == 4) {
			return Optional.of(velocityButton);
		}
		return Optional.empty();
	}

	private final class ConfigureInstrumentVelocity implements Runnable {

		public ConfigureInstrumentVelocity() {
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

			screen.putLayer(4, new NumberDialog("Velocity", model.getModel().getVelocityModel(),
					() -> screen.removeLayer(4)));

		}
	}
}
