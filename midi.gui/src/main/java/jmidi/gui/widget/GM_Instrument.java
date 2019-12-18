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
 * @since 03.10.2016
 * @version 1.0
 * @author oliver
 */
package jmidi.gui.widget;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmidi.gui.Component;
import jmidi.gui.model.IntegerModel;
import jmidi.gui.model.IntegerModel.ValueObserver;

/**
 * @author oliver
 *
 */
@SuppressWarnings("serial")
public class GM_Instrument extends Component {

	private static final Logger logger = LoggerFactory.getLogger(GM_Instrument.class);
	private final List<Instrument> instruments = new ArrayList<>();
	private final IntegerModel model;

	public GM_Instrument() {
		super();
		try {
			final BufferedReader br = new BufferedReader(
					new InputStreamReader(GM_Instrument.class.getResourceAsStream("font.txt")));
			String line;
			line = br.readLine();
			while (line != null) {
				final String tokens[] = line.split(",");
				if (tokens.length == 3) {
					final String name = tokens[0];
					final int bank = Integer.valueOf(tokens[1]);
					final int program = Integer.valueOf(tokens[2]);
					instruments.add(new Instrument(name, bank, program));
				} else {
					logger.error("Number of tokens is not correct : " + line);
				}
				line = br.readLine();
			}
			br.close();
			Collections.sort(instruments, new Comparator<Instrument>() {
				@Override
				public int compare(final Instrument o1, final Instrument o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
		}
		model = new IntegerModel(0, instruments.size() - 1, 0);
		model.addValueObserver(new ValueObserver() {
			@Override
			public void valueChanged(final int newValue) {
				EventQueue.invokeLater(() -> {
					repaint();
					firePropertyChange("instrument", null, getInstrument());
				});
			}
		});
	}

	public static class Instrument {
		private final String name;
		private final int bank;
		private final int program;

		public Instrument(final String name, final int bank, final int program) {
			super();
			this.name = name;
			this.bank = bank;
			this.program = program;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the bank
		 */
		public int getBank() {
			return bank;
		}

		/**
		 * @return the program
		 */
		public int getProgram() {
			return program;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		final int width = getWidth();
		final int height = getHeight();
		g.setColor(Color.DARK_GRAY);

		g.fillRect(0, 0, width - 1, height - 1);
		g.setFont(new Font("Arial Black", Font.PLAIN, 20));
		g.setColor(Color.GREEN);
		final String label = instruments.get(model.getValue()).getName();
		final FontMetrics fm = g.getFontMetrics();
		final Rectangle2D r = fm.getStringBounds(label, g);
		final int y = (height - (int) r.getHeight()) / 2 + fm.getAscent();
		g.drawString(label, 5, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jmidi.gui.Component#mouseWheelEvent(int)
	 */
	@Override
	public void mouseWheelEvent(final int steps) {
		if (steps > 0) {
			model.increment(steps);
		} else {
			model.decrement(steps);
		}
	}

	public Instrument getInstrument() {
		return instruments.get(model.getValue());
	}

}
