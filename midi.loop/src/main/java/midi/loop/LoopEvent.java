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

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oliver
 *
 */
public class LoopEvent {

	public static enum COMMAND {
		NOTE_ON, NOTE_OFF, IGNORE
	};

	private static final LoopEvent nullEvent = new LoopEvent(true);
	private final List<Integer> notes = new ArrayList<>();
	private int velocity = 127;

	private COMMAND command = COMMAND.NOTE_ON;

	private static final Logger logger = LoggerFactory.getLogger(LoopEvent.class);

	private final boolean isNull;

	public LoopEvent() {
		super();
		isNull = false;
	}

	public LoopEvent(final boolean isNull) {
		super();
		this.isNull = isNull;
	}

	public LoopEvent(final COMMAND command, final int velocity, final List<Integer> notes) {
		super();
		this.command = command;
		this.velocity = velocity;
		this.notes.addAll(notes);
		isNull = false;
	}

	public LoopEvent(final COMMAND command, final int velocity, final Integer... notes) {
		super();
		this.command = command;
		this.velocity = velocity;
		this.notes.addAll(Arrays.asList(notes));
		isNull = false;
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

	public void playEvent(final Receiver receiver, final int channel)
			throws InvalidMidiDataException {
		if (command != COMMAND.IGNORE) {
			for (final Integer tone : notes) {
				final ShortMessage msg = new ShortMessage();
				if (command == COMMAND.NOTE_OFF) {
					msg.setMessage(ShortMessage.NOTE_OFF, channel, tone, 0);
				} else {
					msg.setMessage(ShortMessage.NOTE_ON, channel, tone, velocity);
				}
				receiver.send(msg, -1);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug(toString());
		}
	}

	public LoopEvent asOffEvent() {
		return asCommandEvent(COMMAND.NOTE_OFF);
	}

	public LoopEvent asOnEvent() {
		return asCommandEvent(COMMAND.NOTE_ON);
	}

	public LoopEvent asWeightedEvent(final int velocity) {
		return new LoopEvent(command, this.velocity * velocity / 127,
				new ArrayList<Integer>(notes));
	}

	public LoopEvent asCommandEvent(final COMMAND command) {
		return new LoopEvent(command, velocity, new ArrayList<Integer>(notes));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LoopEvent [command=" + command.name() + ", velocity=" + velocity + ", notes="
				+ notes + "]";
	}

	public JsonObject toJson() {
		final JsonObject ret = Json.createObjectBuilder().add("command", command.name())
				.add("velocity", velocity).add("isnull", isNull)
				.add("notes", Json.createArrayBuilder(notes)).build();
		return ret;
	}

	public void fromJson(final JsonObject json) {
		command = COMMAND.valueOf(json.getString("command"));
		velocity = json.getInt("velocity");
		final JsonArray jnotes = json.getJsonArray("notes");
		notes.clear();
		for (int i = 0; i < jnotes.size(); ++i) {
			notes.add(Integer.valueOf(jnotes.getInt(i)));
		}
	}

	/**
	 * @return the isNull
	 */
	public boolean isNull() {
		return isNull;
	}

	public static boolean isNull(final JsonObject json) {
		return json.getBoolean("isnull");
	}

	public static LoopEvent getNullEvent() {
		return nullEvent;
	}

	public static LoopEvent fromShortMessage(final ShortMessage message) {
		COMMAND command = COMMAND.IGNORE;
		int velocity;
		int note;
		if (message.getCommand() == ShortMessage.NOTE_ON) {
			if (message.getData2() == 0) {
				command = COMMAND.NOTE_OFF;
				velocity = 0;
			} else {
				command = COMMAND.NOTE_ON;
				velocity = message.getData2();
			}
			note = message.getData1();
		} else if (message.getCommand() == ShortMessage.NOTE_OFF) {
			command = COMMAND.NOTE_OFF;
			velocity = message.getData2();
			note = message.getData1();
		} else {
			command = COMMAND.IGNORE;
			note = message.getData1();
			velocity = message.getData2();
		}
		return new LoopEvent(command, velocity, note);
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public void setCommand(final COMMAND command) {
		this.command = command;
	}
}
