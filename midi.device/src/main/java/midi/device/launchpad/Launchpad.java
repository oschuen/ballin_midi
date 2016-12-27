/**
 * Copyright (C) 2016 Oliver Schünemann
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
 * 
 * @since 13.10.2016
 * @version 1.0
 * @author oliver
 */
package midi.device.launchpad;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import midi.device.resource.MidiDevices;

/**
 * @author oliver
 *
 */
public class Launchpad {

	public static void main(final String[] args) throws Exception {
		final MidiDevice launchPad = MidiDevices.getReceiverDevice("Mini [hw:3,0,0]");
		launchPad.open();
		final Receiver receiver = launchPad.getReceiver();
		String text = " hallo Welt";
		text = text + text;
		text = text + text;
		text = text + text;
		text = text + text;
		final ShortMessage msg = new ShortMessage();
		final long startTime = System.currentTimeMillis();
		int count = 0;
		for (int j = 0; j < text.length() * 3 + 1; j++) {
			msg.setMessage(ShortMessage.CONTROL_CHANGE, 0, 0, 32 + 4);
			receiver.send(msg, 0);
			Thread.sleep(75);
			int b[] = Font.getBitmap(text, j * 2);
			for (int i = 0; i < 32; ++i) {
				final ShortMessage msg2 = new ShortMessage();
				final int bb = i / 4;
				final int off = 2 * (i % 4);
				final int vel1 = (b[bb] & (1 << (off))) > 0 ? 0x03 : 0x00;
				final int vel2 = (b[bb] & (1 << (off + 1))) > 0 ? 0x03 : 0x00;
				msg2.setMessage(0x92, 2, vel1, vel2);
				receiver.send(msg2, 0);
			}
			msg.setMessage(ShortMessage.CONTROL_CHANGE, 0, 0, 32 + 1);
			receiver.send(msg, 0);
			Thread.sleep(75);
			receiver.send(msg, j * 2);
			b = Font.getBitmap(text, j * 2 + 1);
			for (int i = 0; i < 32; ++i) {
				final ShortMessage msg2 = new ShortMessage();
				final int bb = i / 4;
				final int off = 2 * (i % 4);
				final int vel1 = (b[bb] & (1 << (off))) > 0 ? 0x03 : 0x00;
				final int vel2 = (b[bb] & (1 << (off + 1))) > 0 ? 0x03 : 0x00;
				msg2.setMessage(0x92, 2, vel1, vel2);
				receiver.send(msg2, 0);
			}
			count += 2;
		}
		System.out.println(System.currentTimeMillis() - startTime);
		System.out.println((double) (System.currentTimeMillis() - startTime) / count);

		msg.setMessage(ShortMessage.CONTROL_CHANGE, 0, 0, 32 + 4);
		receiver.send(msg, 0);
		Thread.sleep(100);
	}
}
