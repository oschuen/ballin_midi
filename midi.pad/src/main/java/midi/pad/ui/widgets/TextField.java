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
 * @since 27.12.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.widgets;

import static midi.pad.ui.event.Runtime.getRuntime;

import java.util.concurrent.TimeUnit;

import midi.pad.ui.Color;
import midi.pad.ui.Graphic;
import midi.pad.ui.Widget;
import midi.pad.ui.event.Event;
import midi.pad.ui.widgets.font.Font;

/**
 * @author oliver
 *
 */
public class TextField extends Widget {

	private final String text;
	private int offset = 0;
	private Runnable looper = null;
	private final Runnable finishRunner;

	public TextField(final int x, final int y, final String text, final Runnable finishRunner) {
		this.text = " " + text.trim();
		bounds.x = x;
		bounds.y = y;
		bounds.height = 8;
		bounds.width = 8;
		this.finishRunner = finishRunner;
		start();
	}

	public void start() {
		offset = 0;
		getRuntime().checkRuntimeContext();
		stop();
		looper = new Runnable() {
			@Override
			public void run() {
				if (offset < text.length() * 6) {
					offset++;
				} else {
					stop();
					getRuntime().schedule(finishRunner);
				}
				getRuntime().invalidate();
			}
		};
		getRuntime().scheduleWithFixedDelay(looper, 75, 75, TimeUnit.MILLISECONDS);
	}

	public void stop() {
		getRuntime().checkRuntimeContext();
		if (looper != null) {
			getRuntime().stop(looper);
			looper = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		final int mask[] = Font.getBitmap(text, offset);
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				g.setPixel(x, y, ((mask[y] & (0x01 << x)) > 0) ? Color.FULL_GREEN : Color.BLACK);
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
		return true;
	}
}
