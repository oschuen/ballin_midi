/**
 * Copyright (C) 2016 Oliver Schünemann
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
 * @since 31.12.2016
 * @version 1.0
 * @author oliver
 */
package midi.loop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author oliver
 *
 */
public class LoopEvent {

	public static enum COMMAND {
		NOTE_ON, NOTE_OFF
	};

	private final List<Integer> notes = new ArrayList<>();
	private int velocity = 127;

	private COMMAND command = COMMAND.NOTE_ON;

	public LoopEvent() {
		super();
	}

	public LoopEvent(final COMMAND command, final int velocity, final List<Integer> notes) {
		super();
		this.command = command;
		this.velocity = velocity;
		this.notes.addAll(notes);
	}

	public LoopEvent(final COMMAND command, final int velocity, final Integer... notes) {
		super();
		this.command = command;
		this.velocity = velocity;
		this.notes.addAll(Arrays.asList(notes));
	}

	/**
	 * @return the notes
	 */
	public List<Integer> getNotes() {
		return notes;
	}

	/**
	 * @return the command
	 */
	public COMMAND getCommand() {
		return command;
	}

	/**
	 * @return the velocity
	 */
	public int getVelocity() {
		return velocity;
	}

	public LoopEvent asOffEvent() {
		return new LoopEvent(COMMAND.NOTE_OFF, 0, notes);
	}

	public LoopEvent asOnEvent() {
		return new LoopEvent(COMMAND.NOTE_ON, velocity, notes);
	}

	public LoopEvent asWeightedEvent(final int velocity) {
		return new LoopEvent(command, this.velocity * velocity / 127, notes);
	}
}
