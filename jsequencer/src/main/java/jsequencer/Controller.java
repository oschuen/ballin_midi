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

/**
 * @author oliver
 *
 */
public class Controller implements Receiver {

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
			System.out.println("Command = " + shortMessage.getCommand() + " channel "
					+ shortMessage.getChannel() + " d1 = " + shortMessage.getData1() + " d2 = "
					+ shortMessage.getData2());
			if (ShortMessage.CONTROL_CHANGE == shortMessage.getCommand()) {
				switch (shortMessage.getData1()) {
				case 73:
					orchester.getPercussionChannelConfig().setVolume(shortMessage.getData2());
					break;
				case 74:
					orchester.getPercussionChannelConfig().setReverb(shortMessage.getData2());
					break;
				case 85:
					orchester.getGuitar().setVelocity(shortMessage.getData2());
					orchester.getPercussion().setVelocity(shortMessage.getData2());
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
