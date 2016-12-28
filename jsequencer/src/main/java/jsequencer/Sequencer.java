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
 * @since 27.12.2016
 * @version 1.0
 * @author oliver
 */
package jsequencer;

import midi.pad.ui.Screen;
import midi.pad.ui.dialogs.ConfirmDialog;
import midi.pad.ui.event.Runtime;

/**
 * @author oliver
 *
 */
public class Sequencer {
	public static void main(final String[] args) {
		final Screen screen = Runtime.getRuntime().getScreen();
		final Runnable finishRunnable = new Runnable() {

			@Override
			public void run() {
				System.exit(0);
			}
		};
		Runtime.getRuntime().schedule(new Runnable() {
			@Override
			public void run() {
				screen.putLayer(4,
						new ConfirmDialog("Zufrieden ?", finishRunnable, finishRunnable));

			}
		});

	}
}
