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
import java.util.List;

/**
 * @author oliver
 *
 */
public class LoopEvent {

	public static enum COMMAND {
		NONE, NOTE_ON, NOTE_OFF
	};

	private final List<Integer> notes = new ArrayList<>();
	private String name = "C";
	private boolean isChord = false;
	private boolean accent = false;

	private COMMAND command = COMMAND.NONE;

	public LoopEvent() {
		super();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public LoopEvent setName(final String name) {
		this.name = name;
		return this;
	}

	/**
	 * @return the isChord
	 */
	public boolean isChord() {
		return isChord;
	}

	/**
	 * @param isChord
	 *            the isChord to set
	 */
	public LoopEvent setChord(final boolean isChord) {
		this.isChord = isChord;
		return this;
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
	 * @param command
	 *            the command to set
	 */
	public LoopEvent setCommand(final COMMAND command) {
		this.command = command;
		return this;
	}

	/**
	 * @return the accent
	 */
	public boolean isAccent() {
		return accent;
	}

	/**
	 * @param accent
	 *            the accent to set
	 */
	public LoopEvent setAccent(final boolean accent) {
		this.accent = accent;
		return this;
	}

	/**
	 * @param notes
	 *            the notes to set
	 */
	public LoopEvent setNotes(final List<Integer> notes) {
		this.notes.clear();
		this.notes.addAll(notes);
		return this;
	}
}
