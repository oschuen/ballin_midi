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

import java.util.Optional;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.instrument.model.PercussionModel;
import midi.instrument.model.PercussionModel.PercussionInstrument;
import midi.loop.LoopEvent;
import midi.loop.LoopModel;
import midi.loop.beat.Beat;
import midi.loop.beat.Beat.BeatListener;
import midi.loop.config.OutputChannelConfig;
import midi.loop.config.OutputChannelConfig.PlayMode;

/**
 * @author oliver
 *
 */
public class Percussion implements BeatListener {

	private final Receiver receiver;
	private final OutputChannelConfig config;
	private PercussionModel model;
	private static final Logger logger = LoggerFactory.getLogger(Percussion.class);
	private int velocity = 127;
	public final PercussionInstrument instruments[] = PercussionInstrument.values();
	final LoopEvent[] lastPlayed = new LoopEvent[instruments.length];
	final LoopModel[] loopModel = new LoopModel[instruments.length];

	private final PercussionModel defaultModel = new PercussionModel();

	public Percussion(final Receiver receiver, final OutputChannelConfig config) {
		super();
		this.receiver = receiver;
		this.config = config;
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
			msg.setMessage(ShortMessage.CONTROL_CHANGE, config.getChannel(), 123, 0);
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

	public void step(final int step) {
		final int accent = model.getAccentStepEvent(step).getVelocity() * velocity / 127;
		if (PlayMode.LOOP.equals(config.getMode())) {
			for (int i = 0; i < instruments.length; i++) {
				final int var = i;
				final Optional<LoopEvent> event = loopModel[i].getStepEvent(step);
				event.ifPresent(it -> {
					try {
						it.asWeightedEvent(accent).playEvent(receiver, config.getChannel());
					} catch (final InvalidMidiDataException e) {
						logger.error("Couldn't play event", e);
					}
					lastPlayed[var] = it.asOffEvent();
				});
			}
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
		for (final PercussionInstrument percussionInstrument : instruments) {
			loopModel[percussionInstrument.ordinal()] = this.model
					.getLoopModel(percussionInstrument).get();
		}
	}
}
