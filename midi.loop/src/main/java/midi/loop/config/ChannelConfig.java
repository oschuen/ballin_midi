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
	private int velocity;

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
		this.bank = bank;
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
		this.program = program;
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
		this.midiOut = midiOut;
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
		this.reverb = reverb;
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
		this.choir = choir;
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
