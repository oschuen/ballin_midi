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
import static midi.loop.LoopEvent.COMMAND.IGNORE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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

	private final IntegerModel defaultRecStep = new IntegerModel(-1, -1, -1);
	private IntegerModel recStepModel = defaultRecStep;
	private int stepWidth = 0;
	private int stepLength = 0;
	private RecordMode recMode = OFF;
	private Optional<LoopEvent> lastEvent = Optional.empty();
	private final SetStepObserver setStepObserver = new SetStepObserver();
	private final LoopEvent ignoreEvent = new LoopEvent(IGNORE, 0);
	private boolean dropEvent = true;
	private final Random random = new Random(System.nanoTime());
	private PlayMode lastMode = PlayMode.OFF;

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
			if (recMode != RecordMode.OFF && config.getMode() == PlayMode.LOOP) {
				recorder.send(shortMessage);
			}
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
			final LoopEvent event = model.getModel().getStepEvent(step).orElse(ignoreEvent);
			if (event.getCommand() != COMMAND.IGNORE) {
				lastEvent.ifPresent(it -> {
					try {
						it.asOffEvent().playEvent(receiver, config.getChannel());
					} catch (final InvalidMidiDataException e) {
						logger.error("Couldn't play event", e);
					}
				});
				lastEvent = Optional.of(event);
			}
			try {
				event.asWeightedEvent(velocity).playEvent(receiver, config.getChannel());
			} catch (final InvalidMidiDataException e) {
				logger.error("Couldn't play event", e);
			}
		} else if (lastMode != PlayMode.OFF && config.getMode() == PlayMode.OFF) {
			panic();
		}
		lastMode = config.getMode();
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
		if (recStepModel != null) {
			recStepModel.removeValueObserver(setStepObserver);
		}
		if (stepModel == null) {
			recStepModel = defaultRecStep;
		} else {
			recStepModel = stepModel;
		}
		if (recStepModel != null) {
			recStepModel.addValueObserver(setStepObserver);
		}
		gotoStep(recStepModel.getValue());
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

	private COMMAND getPlayCommand() {
		switch (recMode) {
		case NOTE_OFF:
			return COMMAND.NOTE_OFF;
		case FILL:
		case NOTE_ON:
		case FILL_RANDOM:
			return COMMAND.NOTE_ON;
		case OFF:
		case NOTE_HOLD:
		default:
			return COMMAND.IGNORE;
		}
	}

	private LoopEvent getStepEvent(final int step) {
		final LoopEvent event;
		if (step >= 0) {
			event = model.getModel().getStepEvent(step).orElse(new LoopEvent(COMMAND.IGNORE, 127));
			model.getModel().setStepEvent(event, step);
		} else {
			event = new LoopEvent(COMMAND.IGNORE, 127);
		}
		return event;
	}

	private void setStepCommand(final COMMAND command, final int step) {
		if (step >= 0) {
			final LoopEvent event = getStepEvent(step).asCommandEvent(command);
			model.getModel().setStepEvent(event, step);
		}
	}

	private void gotoStep(final int step) {
		final LoopEvent event = getStepEvent(step);
		recorder.setEvent(event);
		dropEvent = true;
		recStepModel.setValue(step);
		dropEvent = false;
	}

	// this is called when new Notes were recorded
	private class RecordStepRunnable implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			final int steps = model.getQuarterDivision() * model.getQuarterPerPage()
					* model.getNumberOfPages();
			if (recMode != RecordMode.OFF) {
				setStepCommand(getPlayCommand(), recStepModel.getValue());
			}
			if (recStepModel.getValue() >= 0 && recMode == RecordMode.NOTE_ON) {
				for (int i = 1; i < stepWidth; ++i) {
					if (i == stepLength) {
						setStepCommand(COMMAND.NOTE_OFF, (recStepModel.getValue() + i) % steps);
					} else {
						setStepCommand(COMMAND.IGNORE, (recStepModel.getValue() + i) % steps);
					}
				}
				gotoStep((recStepModel.getValue() + stepWidth) % steps);
			} else if (recStepModel.getValue() >= 0 && recMode == RecordMode.FILL) {
				final List<Integer> notes = new ArrayList<>(
						getStepEvent(recStepModel.getValue()).getNotes());
				for (int j = 0; j < steps; j += stepWidth) {
					final int baseStep = recStepModel.getValue() + j;
					getStepEvent(baseStep).getNotes().clear();
					getStepEvent(baseStep).getNotes().addAll(notes);
					setStepCommand(COMMAND.NOTE_ON, baseStep);
					for (int i = 1; i < stepWidth; ++i) {
						if (i == stepLength) {
							setStepCommand(COMMAND.NOTE_OFF, (baseStep + i) % steps);
						} else {
							setStepCommand(COMMAND.IGNORE, (baseStep + i) % steps);
						}
					}
				}
				gotoStep((recStepModel.getValue()) % steps);
			} else if (recStepModel.getValue() >= 0 && recMode == RecordMode.FILL_RANDOM) {
				final List<Integer> notes = new ArrayList<>(
						getStepEvent(recStepModel.getValue()).getNotes());
				for (int j = 0; j < steps; j += stepWidth) {
					final int baseStep = recStepModel.getValue() + j;
					getStepEvent(baseStep).getNotes().clear();
					getStepEvent(baseStep).getNotes().addAll(notes);
					setStepCommand(random.nextInt(2) < 1 ? COMMAND.NOTE_ON : COMMAND.IGNORE,
							baseStep);
					for (int i = 1; i < stepWidth; ++i) {
						if (i == stepLength) {
							setStepCommand(COMMAND.NOTE_OFF, (baseStep + i) % steps);
						} else {
							setStepCommand(COMMAND.IGNORE, (baseStep + i) % steps);
						}
					}
				}
				gotoStep((recStepModel.getValue()) % steps);
			}
		}
	}

	// this is called when new Position for sequencing is choosen.
	private class SetStepObserver implements IntegerModel.ValueObserver {

		@Override
		public void valueChanged(final int newValue) {
			if (!dropEvent) {
				if (recMode != RecordMode.OFF) {
					setStepCommand(getPlayCommand(), newValue);
				}
				gotoStep(newValue);
			}
		}
	}

	/**
	 * @param stepLength
	 *            the stepLength to set
	 */
	public void setStepLength(final int stepLength) {
		this.stepLength = stepLength;
	}
}
