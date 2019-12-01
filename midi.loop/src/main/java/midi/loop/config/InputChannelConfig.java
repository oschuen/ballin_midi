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
 * @since 01.12.2019
 * @version 1.0
 * @author oliver
 */
package midi.loop.config;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author oliver
 *
 */
public class InputChannelConfig {
	private int midiIn;
	private int channel;
	private boolean changed;

	/**
	 * @return the midiOut
	 */
	public int getMidiIn() {
		return midiIn;
	}

	/**
	 * @param midiIn
	 *            the midiIn to set
	 */
	public void setMidiIn(final int midiIn) {
		if (this.midiIn != midiIn) {
			this.midiIn = midiIn;
			changed = true;
		}
	}

	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 *            the channel to set
	 */
	public void setChannel(final int channel) {
		if (this.channel != channel) {
			this.channel = channel;
			changed = true;
		}
	}

	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	public void applied() {
		changed = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "InputChannelConfig [midiIn=" + midiIn + ", channel=" + channel + ", changed="
				+ changed + "]";
	}

	public JsonObject toJson() {
		return Json.createObjectBuilder().add("channel", channel).add("midiIn", midiIn).build();
	}

	public void fromJson(final JsonObject json) {
		midiIn = json.getInt("midiIn");
		channel = json.getInt("channel");
		changed = true;
	}

}
