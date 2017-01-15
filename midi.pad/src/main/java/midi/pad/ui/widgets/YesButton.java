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

import midi.pad.ui.Color;
import midi.pad.ui.Graphic;

/**
 * @author oliver
 *
 */
public class YesButton extends Button {

	public YesButton(final int x, final int y, final Runnable pressRunner) {
		super(x, y, 4, 4, pressRunner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		g.fill(Color.BLACK);
		g.setPixel(0, 2, Color.FULL_GREEN);
		g.setPixel(1, 3, Color.FULL_GREEN);
		g.setPixel(2, 2, Color.FULL_GREEN);
		g.setPixel(3, 1, Color.FULL_GREEN);
		g.setPixel(3, 0, Color.FULL_GREEN);
	}
}
