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
 * @since 20.12.2019
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import midi.pad.ui.Color;
import midi.pad.ui.Graphic;
import midi.pad.ui.Widget;
import midi.pad.ui.event.Event;
import midi.pad.ui.event.PadEvent;
import midi.pad.ui.event.PadEvent.EVENT_TYPE;
import midi.pad.ui.event.Runtime;

/**
 * @author oliver
 *
 */
public class ChannelSelection extends Widget {

	private int currentChannel;
	private final UsedProvider usedProvider;
	private final Runnable pressRunner;

	/**
	 * 
	 */
	public ChannelSelection(final int x, final int y, final int currentChannel,
			final UsedProvider usedProvider, final Runnable pressRunner) {
		bounds.x = x;
		bounds.y = y;
		bounds.width = 8;
		bounds.height = 2;
		this.currentChannel = currentChannel;
		this.usedProvider = usedProvider;
		this.pressRunner = pressRunner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		final boolean[] usedMatrix = usedProvider.getUsed();
		for (int y = 0; y < 2; ++y) {
			for (int x = 0; x < 8; ++x) {
				final int chn = y * 8 + x;
				if (chn == currentChannel) {
					g.setPixel(x, y, Color.FULL_GREEN);
				} else if (usedMatrix[chn]) {
					g.setPixel(x, y, Color.FULL_RED);
				} else {
					g.setPixel(x, y, Color.FULL_AMBER);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#eventOccured(midi.pad.ui.event.Event)
	 */
	@Override
	public boolean eventOccured(final Event event) {
		if (EVENT_TYPE.PAD_RELEASED.equals(event.getEventType())) {
			final PadEvent padEvent = (PadEvent) event;
			final boolean[] usedMatrix = usedProvider.getUsed();
			final int chn = padEvent.getY() * 8 + padEvent.getX();
			if (!usedMatrix[chn]) {
				usedMatrix[currentChannel] = false;
				usedMatrix[chn] = true;
				currentChannel = chn;
				Runtime.getRuntime().schedule(pressRunner);
			}
		}
		return true;
	}

	/**
	 * @return the currentChannel
	 */
	public int getCurrentChannel() {
		return currentChannel;
	}

	public interface UsedProvider {
		boolean[] getUsed();
	}
}
