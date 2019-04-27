/**
 * Copyright (C) 2015 Oliver Schünemann
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
 * @since 12.11.2015
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

import midi.instrument.model.GuitarModel;
import midi.instrument.model.GuitarModel.GuitarInstrument;
import midi.loop.LoopEvent;
import midi.loop.LoopModel;
import midi.loop.beat.Beat;
import midi.loop.beat.Beat.BeatListener;

/**
 * Guitar accompaniment playing picking patterns. Pattern must be provided for
 * bass, G, B and E String in one-sixteenth resolution.
 * 
 * @author oliver
 */
public class Guitar implements BeatListener {

	private final Receiver receiver;
	private final int channel;
	private GuitarModel model;
	private static final Logger logger = LoggerFactory.getLogger(Percussion.class);
	private int velocity = 127;
	final GuitarInstrument instruments[] = GuitarInstrument.values();
	final LoopEvent[] lastPlayed = new LoopEvent[instruments.length];
	final LoopModel[] loopModel = new LoopModel[instruments.length];

	private final GuitarModel defaultModel = new GuitarModel();

	public Guitar(final Receiver receiver, final int channel) {
		super();
		this.receiver = receiver;
		this.channel = channel;
		model = defaultModel;
		for (final GuitarInstrument percussionInstrument : instruments) {
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

	public void step(final int step) {
		final int accent = model.getAccentStepEvent(step).getVelocity() * velocity / 127;
		for (int i = 0; i < instruments.length; i++) {
			final int var = i;
			final Optional<LoopEvent> event = loopModel[i].getStepEvent(step);
			event.ifPresent(it -> {
				try {
					it.asWeightedEvent(accent).playEvent(receiver, channel);
				} catch (final InvalidMidiDataException e) {
					logger.error("Couldn't play event", e);
				}
				lastPlayed[var] = it.asOffEvent();
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
	public GuitarModel getModel() {
		return model;
	}

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(final GuitarModel model) {
		if (model == null) {
			this.model = defaultModel;
		} else {
			this.model = model;
		}
		for (final GuitarInstrument guitarInstrument : instruments) {
			loopModel[guitarInstrument.ordinal()] = this.model.getLoopModel(guitarInstrument).get();
		}
	}
}
