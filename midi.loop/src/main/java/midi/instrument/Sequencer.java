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

import java.util.Optional;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.instrument.model.SequencerModel;
import midi.loop.LoopEvent;
import midi.loop.beat.Beat;
import midi.loop.beat.Beat.BeatListener;
import midi.loop.config.OutputChannelConfig;

/**
 * @author oliver
 *
 */
public class Sequencer implements BeatListener, Receiver {

	private final Receiver receiver;
	private final OutputChannelConfig config;
	private SequencerModel model;
	private static final Logger logger = LoggerFactory.getLogger(Sequencer.class);
	private final int velocity = 127;
	private final SequencerModel defaultModel = new SequencerModel();
	private int midiInChannel = 0;

	public Sequencer(final Receiver receiver, final OutputChannelConfig config) {
		super();
		this.receiver = receiver;
		this.config = config;
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
		// TODO Auto-generated method stub

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
		final Optional<LoopEvent> event = model.getModel().getStepEvent(step);
		event.ifPresent(it -> {
			try {
				it.asWeightedEvent(velocity).playEvent(receiver, config.getChannel());
			} catch (final InvalidMidiDataException e) {
				logger.error("Couldn't play event", e);
			}
		});
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
}
