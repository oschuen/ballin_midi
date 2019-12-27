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

import static midi.pad.ui.event.Runtime.getRuntime;

import java.util.Optional;

import jmidi.gui.model.IntegerModel;
import midi.instrument.Sequencer;
import midi.instrument.Sequencer.RecordMode;
import midi.instrument.model.SequencerModel;
import midi.loop.beat.Beat.BeatListener;
import midi.pad.ui.Color;
import midi.pad.ui.Screen;
import midi.pad.ui.dialogs.ConfirmDialog;
import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.dialogs.NumberDialog;
import midi.pad.ui.event.Runtime;
import midi.pad.ui.widgets.ControlButton;
import midi.pad.ui.widgets.LoopConfig;
import midi.pad.ui.widgets.LoopRecordWidget;
import midi.pad.ui.widgets.RecordLooper;
import midi.pad.ui.widgets.SimpleControlButton;

/**
 * @author oliver
 *
 */
public class SequencerLoopLayer extends HintDialog implements BeatListener {

	private final LoopConfig config;
	private final Sequencer sequencer;
	private final SequencerModel model;
	private final RecordLooper looper = new RecordLooper(0, 7);
	private final SimpleControlButton velocityButton;
	private final LoopRecordWidget loopRecordWidget;
	private final IntegerModel recStepModel = new IntegerModel(-1, 0, 0);
	private final RecordModeChangeRunnable modeChangeRunnable = new RecordModeChangeRunnable();
	private ConfirmDialog dialog;

	public SequencerLoopLayer(final String name, final Sequencer sequencer) {
		super(name);
		this.sequencer = sequencer;
		model = sequencer.getModel();
		recStepModel.setMaxValue(
				model.getNumberOfPages() * model.getQuarterPerPage() * model.getQuarterDivision());
		config = new LoopConfig(0, 0, new Runnable() {
			@Override
			public void run() {
				model.setNumberOfPages(config.getNumberOfPages());
				model.setQuarterDivision(config.getQuarterDivision());
				model.setQuarterPerPage(config.getQuarterPerPage());
				recStepModel.setMaxValue(model.getNumberOfPages() * model.getQuarterPerPage()
						* model.getQuarterDivision());
			}
		}, new Runnable() {

			@Override
			public void run() {
				looper.setHold(config.isHold());
				looper.setHoldPage(config.getHoldPage());
				looper.setHoldQuarter(config.getHoldQuarter());
			}
		});
		loopRecordWidget = new LoopRecordWidget(3, () -> {
			Runtime.getRuntime().schedule(modeChangeRunnable);
		});

		looper.setLoopModel(model.getModel());
		setWidgets(config, loopRecordWidget, looper);
		velocityButton = new SimpleControlButton(Color.GREEN, new ConfigureInstrumentVelocity());
		sequencer.setRecStepModel(recStepModel);
		looper.setRecStepModel(recStepModel);
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

	private void delete() {
		final Screen screen = getRuntime().getScreen();
		dialog = new ConfirmDialog("Delete?", () -> {
			model.clear();
			screen.removeLayer(dialog);
			getRuntime().invalidate();
		}, () -> {
			screen.removeLayer(dialog);
		});
		screen.putLayer(4, dialog);

	}

	private final class RecordModeChangeRunnable implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			switch (loopRecordWidget.getMode()) {
			case CLEAR:
				delete();
				break;
			case FILL:
				sequencer.setRecMode(RecordMode.FILL);
				break;
			case FILL_RANDOM:
				sequencer.setRecMode(RecordMode.FILL_RANDOM);
				break;
			case NOTE_HOLD:
				sequencer.setRecMode(RecordMode.NOTE_HOLD);
				break;
			case NOTE_OFF:
				sequencer.setRecMode(RecordMode.NOTE_OFF);
				break;
			case NOTE_ON:
				sequencer.setRecMode(RecordMode.NOTE_ON);
				break;
			case OFF:
				sequencer.setRecMode(RecordMode.OFF);
				break;
			default:
				break;
			}
			sequencer.setStepWidth(loopRecordWidget.getSteps());
			sequencer.setStepLength(loopRecordWidget.getLength());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Layer#getAbcControlButton(int)
	 */
	@Override
	public Optional<ControlButton> getAbcControlButton(final int y) {
		if (y == 8) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Layer#stopLayer()
	 */
	@Override
	public void stopLayer() {
		sequencer.setRecStepModel(null);
		looper.setRecStepModel(null);
		sequencer.setRecMode(RecordMode.OFF);
	}
}
