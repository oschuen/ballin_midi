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
 * @since 22.12.2019
 * @version 1.0
 * @author oliver
 */
package jsequencer.ui.screen;

import static midi.pad.ui.event.Runtime.getRuntime;

import jsequencer.persistence.Persistence;
import midi.pad.ui.Screen;
import midi.pad.ui.dialogs.ConfirmDialog;
import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.widgets.SongSelection;

/**
 * @author oliver
 *
 */
public class SongSelectionLayer extends HintDialog {

	private final Persistence persistence;
	private final SongSelection songSelection;
	private ConfirmDialog dialog;

	/**
	 * @param hint
	 */
	public SongSelectionLayer(final Persistence persistence) {
		super("Select Song");
		this.persistence = persistence;
		songSelection = new SongSelection(this.persistence.getCurrentSong(),
				this.persistence.getSongMatrix(), new SelectSongRunnable(),
				new DeleteSongRunnable(), new CopySongRunnable());
		setWidgets(songSelection);
		start();
	}

	private class SelectSongRunnable implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			final int newSong = songSelection.getSelection();
			songSelection.setCurrentSong(newSong);
			persistence.loadSong(newSong);
		}
	}

	private class DeleteSongRunnable implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			final int song = songSelection.getSelection();
			final Screen screen = getRuntime().getScreen();
			songSelection.reset();
			dialog = new ConfirmDialog("Delete current Song?", () -> {
				persistence.deleteSong(song);
				screen.removeLayer(dialog);
				getRuntime().invalidate();
			}, () -> {
				screen.removeLayer(dialog);
			});
			screen.putLayer(4, dialog);
		}
	}

	private class CopySongRunnable implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			final int fromSong = songSelection.getCopyFrom();
			final int toSong = songSelection.getSelection();
			final Screen screen = getRuntime().getScreen();
			songSelection.reset();
			dialog = new ConfirmDialog("Copy Song?", () -> {
				persistence.copySong(fromSong, toSong);
				screen.removeLayer(dialog);
				getRuntime().invalidate();
			}, () -> {
				screen.removeLayer(dialog);
			});
			screen.putLayer(4, dialog);
		}
	}
}
