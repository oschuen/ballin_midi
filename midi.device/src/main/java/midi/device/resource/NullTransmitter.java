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
 * @since 15.06.2019
 * @version 1.0
 * @author oliver
 */
package midi.device.resource;

import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

/**
 * @author oliver
 *
 */
public class NullTransmitter implements Transmitter {

	private Receiver receiver;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Transmitter#setReceiver(javax.sound.midi.Receiver)
	 */
	@Override
	public void setReceiver(final Receiver receiver) {
		this.receiver = receiver;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Transmitter#getReceiver()
	 */
	@Override
	public Receiver getReceiver() {
		return receiver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Transmitter#close()
	 */
	@Override
	public void close() {
		// Do Nothing
	}
}
