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
 * @since 30.11.2019
 * @version 1.0
 * @author oliver
 */
package jsequencer;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oliver
 *
 */
public class Controller implements Receiver {
	private final Logger logger = LoggerFactory.getLogger(Controller.class);

	private final Orchester orchester;

	public Controller(final Orchester orchester) {
		super();
		this.orchester = orchester;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
	 */
	@Override
	public void send(final MidiMessage message, final long timeStamp) {
		if (message instanceof ShortMessage) {
			final ShortMessage shortMessage = (ShortMessage) message;
			if (logger.isDebugEnabled()) {
				logger.debug("Command = {} channel = {}  d1 = {}  d2 = {}",
						shortMessage.getCommand(), shortMessage.getChannel(),
						shortMessage.getData1(), shortMessage.getData2());
			}

			if (ShortMessage.CONTROL_CHANGE == shortMessage.getCommand()) {
				switch (shortMessage.getData1()) {
				case 73: // Fader 1
					orchester.getPercussionChannelConfig().setVolume(shortMessage.getData2());
					break;
				case 75: // Fader 2
					orchester.getGuitarChannelConfig().setVolume(shortMessage.getData2());
					break;
				case 79: // Fader 3
					orchester.getSequencerChannelConfig(0).setVolume(shortMessage.getData2());
					break;
				case 72: // Fader 4
					orchester.getSequencerChannelConfig(1).setVolume(shortMessage.getData2());
					break;
				case 80: // Fader 5
					orchester.getSequencerChannelConfig(2).setVolume(shortMessage.getData2());
					break;
				case 81: // Fader 6
					orchester.getSequencerChannelConfig(3).setVolume(shortMessage.getData2());
					break;
				case 82: // Fader 7
					orchester.getSequencerChannelConfig(4).setVolume(shortMessage.getData2());
					break;
				case 83: // Fader 8
					orchester.getSequencerChannelConfig(5).setVolume(shortMessage.getData2());
					break;
				case 74: // Poti 1
					orchester.getPercussionChannelConfig().setChoir(shortMessage.getData2());
					break;
				case 71: // Poti 2
					orchester.getGuitarChannelConfig().setChoir(shortMessage.getData2());
					break;
				case 76: // Poti 3
					orchester.getSequencerChannelConfig(0).setChoir(shortMessage.getData2());
					break;
				case 77: // Poti 4
					orchester.getSequencerChannelConfig(1).setChoir(shortMessage.getData2());
					break;
				case 93: // Poti 5
					orchester.getSequencerChannelConfig(2).setChoir(shortMessage.getData2());
					break;
				case 18: // Poti 6
					orchester.getSequencerChannelConfig(3).setChoir(shortMessage.getData2());
					break;
				case 19: // Poti 7
					orchester.getSequencerChannelConfig(4).setChoir(shortMessage.getData2());
					break;
				case 16: // Poti 8
					orchester.getSequencerChannelConfig(5).setChoir(shortMessage.getData2());
					break;
				case 85: // Main Fader
					orchester.getGuitar().setVelocity(shortMessage.getData2());
					orchester.getPercussion().setVelocity(shortMessage.getData2());
					for (int i = 0; i < 6; ++i) {
						orchester.getSequencer(i).setVelocity(shortMessage.getData2());
					}

					break;
				}
			}
		} else {
			System.out.println(message.getClass().toGenericString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Receiver#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
