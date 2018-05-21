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
import midi.loop.LoopModel;
import midi.loop.beat.Beat.BeatListener;

/**
 * @author oliver
 *
 */
public class Percussion implements BeatListener {

	private final Receiver receiver;
	private final int channel;
	private final PercussionModel model;
	private static final Logger logger = LoggerFactory.getLogger(Percussion.class);
	private int velocity = 127;
	final PercussionInstrument instruments[] = PercussionInstrument.values();
	final LoopEvent[] lastPlayed = new LoopEvent[instruments.length];
	final LoopModel[] loopModel = new LoopModel[instruments.length];

	public Percussion(final Receiver receiver, final int channel, final PercussionModel model) {
		super();
		this.receiver = receiver;
		this.channel = channel;
		this.model = model;
		for (final PercussionInstrument percussionInstrument : instruments) {
			loopModel[percussionInstrument.ordinal()] = model.getLoopModel(percussionInstrument)
					.get();
		}
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
		// TODO Auto-generated method stub

	}

	public void step(final int step) {

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
}
