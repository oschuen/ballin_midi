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
package midi.instrument;

import static midi.instrument.Sequencer.RecordMode.OFF;

import java.util.Optional;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmidi.gui.model.IntegerModel;
import midi.instrument.model.SequencerModel;
import midi.loop.LoopEvent;
import midi.loop.LoopEvent.COMMAND;
import midi.loop.beat.Beat;
import midi.loop.beat.Beat.BeatListener;
import midi.loop.config.InputChannelConfig;
import midi.loop.config.OutputChannelConfig;
import midi.loop.config.OutputChannelConfig.PlayMode;
import midi.record.MultiNoteRecorder;

/**
 * @author oliver
 *
 */
public class Sequencer implements BeatListener, Receiver {

	private final Receiver receiver;
	private final OutputChannelConfig config;
	private final InputChannelConfig inConfig;
	private SequencerModel model;
	private static final Logger logger = LoggerFactory.getLogger(Sequencer.class);
	private int velocity = 127;
	private final SequencerModel defaultModel = new SequencerModel();
	private int midiInChannel = 0;
	private final MultiNoteRecorder recorder = new MultiNoteRecorder(new RecordStepRunnable());

	private final IntegerModel defaultRecStep = new IntegerModel(-1, 0, 0);
	private IntegerModel recStepModel = defaultRecStep;
	private int stepWidth = 0;
	private RecordMode recMode = OFF;
	private Optional<LoopEvent> lastEvent = Optional.empty();

	public enum RecordMode {
		OFF, NOTE_ON, NOTE_OFF, NOTE_HOLD, FILL, FILL_RANDOM
	};

	public Sequencer(final Receiver receiver, final OutputChannelConfig config,
			final InputChannelConfig inConfig) {
		super();
		this.receiver = receiver;
		this.config = config;
		this.inConfig = inConfig;
	}

	/**
	 * Mutes all Sounds played
	 */
	public void panic() {
		try {
			final ShortMessage msg = new ShortMessage();
			msg.setMessage(ShortMessage.CONTROL_CHANGE, config.getChannel(), 123, 0);
			receiver.send(msg, -1);
		} catch (final InvalidMidiDataException e) {
			logger.error("Panic Failed", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
	 */
	@Override
	public void send(final MidiMessage message, final long timeStamp) {
		if (config.getMode() != PlayMode.OFF && message instanceof ShortMessage) {
			final ShortMessage shortMessage = (ShortMessage) message;
			recorder.send(shortMessage);
			if (shortMessage.getChannel() == inConfig.getChannel()) {
				final LoopEvent event = LoopEvent.fromShortMessage(shortMessage);
				try {
					event.asWeightedEvent(velocity).playEvent(receiver, config.getChannel());
				} catch (final InvalidMidiDataException e) {
					logger.error("Couldn't play Event", event.toString());
				}
			}
		}
	}

	/**
	 * Close the Accompaniment and release resources.
	 */
	@Override
	public void close() {
		receiver.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BeatListener#accept(long)
	 */
	@Override
	public void accept(final long beat) {
		final long division = Beat.BEAT_DIVISION / model.getQuarterDivision();
		if (beat % (division) == 0) {
			final long steps = model.getQuarterDivision() * model.getQuarterPerPage()
					* model.getNumberOfPages();
			final int step = (int) ((beat / division) % (steps));
			step(step);
		}
	}

	public void step(final int step) {
		if (config.getMode() == PlayMode.LOOP) {
			final Optional<LoopEvent> event = model.getModel().getStepEvent(step);
			lastEvent.ifPresent(it -> {
				try {
					it.asOffEvent().playEvent(receiver, config.getChannel());
				} catch (final InvalidMidiDataException e) {
					logger.error("Couldn't play event", e);
				}
			});
			lastEvent = event;
			event.ifPresent(it -> {
				try {
					it.asWeightedEvent(velocity).playEvent(receiver, config.getChannel());
				} catch (final InvalidMidiDataException e) {
					logger.error("Couldn't play event", e);
				}
			});
		}
	}

	/**
	 * @return the model
	 */
	public SequencerModel getModel() {
		return model;
	}

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(final SequencerModel model) {
		if (model == null) {
			this.model = defaultModel;
		} else {
			this.model = model;
		}
	}

	/**
	 * @return the midiInChannel
	 */
	public int getMidiInChannel() {
		return midiInChannel;
	}

	/**
	 * @param midiInChannel
	 *            the midiInChannel to set
	 */
	public void setMidiInChannel(final int midiInChannel) {
		this.midiInChannel = midiInChannel;
	}

	/**
	 * @return the velocity
	 */
	public int getVelocity() {
		return velocity;
	}

	/**
	 * @param velocity
	 *            the velocity to set
	 */
	public void setVelocity(final int velocity) {
		this.velocity = velocity;
	}

	public void setRecStepModel(final IntegerModel stepModel) {
		if (stepModel == null) {
			recStepModel = defaultRecStep;
		} else {
			recStepModel = stepModel;
		}
	}

	/**
	 * @param stepWidth
	 *            the stepWidth to set
	 */
	public void setStepWidth(final int stepWidth) {
		this.stepWidth = stepWidth;
	}

	/**
	 * @param recMode
	 *            the recMode to set
	 */
	public void setRecMode(final RecordMode recMode) {
		this.recMode = recMode;
	}

	private void singleStep(final COMMAND command) {
		final int steps = model.getQuarterDivision() * model.getQuarterPerPage()
				* model.getNumberOfPages();
		final int step = (recStepModel.getValue() + stepWidth) % steps;
		final LoopEvent event = model.getModel().getStepEvent(step)
				.orElse(new LoopEvent(command, 127)).asCommandEvent(command);
		model.getModel().setStepEvent(event, step);
		recStepModel.setValue(step);
		recorder.setEvent(event);
	}

	private class RecordStepRunnable implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			switch (recMode) {
			case FILL:
				break;
			case FILL_RANDOM:
				break;
			case NOTE_HOLD:
				singleStep(COMMAND.IGNORE);
				break;
			case NOTE_OFF:
				singleStep(COMMAND.NOTE_OFF);
				break;
			case NOTE_ON:
				singleStep(COMMAND.NOTE_ON);
				break;
			case OFF:
			default:
				break;
			}
		}
	}
}
