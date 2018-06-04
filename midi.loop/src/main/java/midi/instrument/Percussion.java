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
 * @since 21.05.2018
 * @version 1.0
 * @author oliver
 */
package midi.instrument;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.instrument.model.PercussionModel;
import midi.instrument.model.PercussionModel.PercussionInstrument;
import midi.loop.LoopEvent;
import midi.loop.LoopEvent.COMMAND;
import midi.loop.LoopModel;
import midi.loop.beat.Beat;
import midi.loop.beat.Beat.BeatListener;

/**
 * @author oliver
 *
 */
public class Percussion implements BeatListener {

	private final Receiver receiver;
	private final int channel;
	private PercussionModel model;
	private static final Logger logger = LoggerFactory.getLogger(Percussion.class);
	private int velocity = 127;
	final PercussionInstrument instruments[] = PercussionInstrument.values();
	final LoopEvent[] lastPlayed = new LoopEvent[instruments.length];
	final LoopModel[] loopModel = new LoopModel[instruments.length];

	private final PercussionModel defaultModel = new PercussionModel();

	public Percussion(final Receiver receiver, final int channel) {
		super();
		this.receiver = receiver;
		this.channel = channel;
		model = defaultModel;
		for (final PercussionInstrument percussionInstrument : instruments) {
			loopModel[percussionInstrument.ordinal()] = model.getLoopModel(percussionInstrument)
					.get();
		}
	}

	/**
	 * Close the Accompaniment and release resources.
	 */
	public void close() {
		receiver.close();
	}

	/**
	 * Mutes all Sounds played
	 */
	public void panic() {
		try {
			final ShortMessage msg = new ShortMessage();
			msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, 123, 0);
			receiver.send(msg, -1);
		} catch (final InvalidMidiDataException e) {
			logger.error("Panic Failed", e);
		}
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

	private void playEvent(final LoopEvent event) {
		try {
			for (final Integer note : event.getNotes()) {
				final ShortMessage msg = new ShortMessage();
				if (event.getCommand() == COMMAND.NOTE_ON) {
					msg.setMessage(ShortMessage.NOTE_ON, channel, note, event.getVelocity());
				} else {
					msg.setMessage(ShortMessage.NOTE_OFF, channel, note, 0);
				}
				receiver.send(msg, -1);
			}
		} catch (final InvalidMidiDataException e) {
			logger.error("Can't play Notes", e);
		}
	}

	public void step(final int step) {
		final int accent = model.getAccentStepEvent(step).getVelocity() * velocity / 127;
		for (int ii = 0; ii < loopModel.length && ii < lastPlayed.length; ++ii) {
			final int i = ii;
			if (lastPlayed[i] != null) {
				playEvent(lastPlayed[i]);
			}
			loopModel[i].getStepEvent(step).ifPresent(event -> {
				playEvent(event.asWeightedEvent(accent));
				lastPlayed[i] = event.asOffEvent();
			});
		}
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

	/**
	 * @return the model
	 */
	public PercussionModel getModel() {
		return model;
	}

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(final PercussionModel model) {
		if (model == null) {
			this.model = defaultModel;
		} else {
			this.model = model;
		}
	}
}
