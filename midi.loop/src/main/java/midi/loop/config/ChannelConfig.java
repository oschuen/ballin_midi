/**
 * Copyright (C) 2017 Oliver Schünemann
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
 * @since 15.01.2017
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
public class ChannelConfig {
	private int bank;
	private int program;
	private int midiOut;
	private int reverb;
	private int choir;
	private int channel;
	private boolean changed;

	/**
	 * @return the bank
	 */
	public int getBank() {
		return bank;
	}

	/**
	 * @param bank
	 *            the bank to set
	 */
	public void setBank(final int bank) {
		if (this.bank != bank) {
			this.bank = bank;
			changed = true;
		}
	}

	/**
	 * @return the program
	 */
	public int getProgram() {
		return program;
	}

	/**
	 * @param program
	 *            the program to set
	 */
	public void setProgram(final int program) {
		if (this.program != program) {
			this.program = program;
			changed = true;
		}
	}

	/**
	 * @return the midiOut
	 */
	public int getMidiOut() {
		return midiOut;
	}

	/**
	 * @param midiOut
	 *            the midiOut to set
	 */
	public void setMidiOut(final int midiOut) {
		if (this.midiOut != midiOut) {
			this.midiOut = midiOut;
			changed = true;
		}
	}

	/**
	 * @return the reverb
	 */
	public int getReverb() {
		return reverb;
	}

	/**
	 * @param reverb
	 *            the reverb to set
	 */
	public void setReverb(final int reverb) {
		if (this.reverb != reverb) {
			this.reverb = reverb;
			changed = true;
		}
	}

	/**
	 * @return the choir
	 */
	public int getChoir() {
		return choir;
	}

	/**
	 * @param choir
	 *            the choir to set
	 */
	public void setChoir(final int choir) {
		if (this.choir != choir) {
			this.choir = choir;
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
		return "ChannelConfig [bank=" + bank + ", program=" + program + ", midiOut=" + midiOut
				+ ", reverb=" + reverb + ", choir=" + choir + ", channel=" + channel + ", changed="
				+ changed + "]";
	}

	public JsonObject toJson() {
		return Json.createObjectBuilder().add("bank", bank).add("program", program)
				.add("reverb", reverb).add("choir", choir).add("channel", channel)
				.add("midiOut", midiOut).build();
	}

	public void fromJson(final JsonObject json) {
		bank = json.getInt("bank");
		program = json.getInt("program");
		midiOut = json.getInt("midiOut");
		reverb = json.getInt("reverb");
		choir = json.getInt("choir");
		channel = json.getInt("channel");
		changed = true;
	}
}
