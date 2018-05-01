/**
 * Copyright (C) 2018 Oliver Schünemann
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
 * @since 29.04.2018
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import midi.loop.LoopModel;
import midi.loop.beat.Beat.BeatListener;
import midi.pad.ui.Graphic;
import midi.pad.ui.Widget;
import midi.pad.ui.event.Event;

/**
 * @author oliver
 *
 */
public class LoopConfig extends Widget implements BeatListener {

	public LoopConfig(final int x, final int y, final Runnable fixRunnable,
			final LoopModel... models) {
		bounds.x = x;
		bounds.y = y;
		bounds.width = 8;
		bounds.height = 3;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#eventOccured(midi.pad.ui.event.Event)
	 */
	@Override
	public boolean eventOccured(final Event event) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BeatListener#accept(long)
	 */
	@Override
	public void accept(final long beat) {
		// TODO Auto-generated method stub

	}

}
