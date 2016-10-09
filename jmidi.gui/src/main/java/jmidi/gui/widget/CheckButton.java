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
 * @since 08.10.2016
 * @version 1.0
 * @author oliver
 */
package jmidi.gui.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import jmidi.gui.Component;
import jmidi.gui.model.BooleanModel;
import jmidi.gui.model.BooleanModel.ValueObserver;

/**
 * @author oliver
 *
 */
@SuppressWarnings("serial")
public class CheckButton extends Component {

	BooleanModel model = new BooleanModel();
	private final String label = "Velocity";

	public void addValueObserver(final ValueObserver observer) {
		model.addValueObserver(observer);
	}

	public void removeValueObserver(final ValueObserver observer) {
		model.removeValueObserver(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jmidi.gui.Component#mousePressed(int, int)
	 */
	@Override
	public void mousePressed(final int x, final int y) {
		model.toggle();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g) {
		final int height = getHeight();
		final int width = getWidth();

		if (model.isValue()) {
			g.setColor(Color.DARK_GRAY);
		} else {
			g.setColor(Color.LIGHT_GRAY);
		}
		g.fillRect(0, 0, width - 1, height - 1);

		if (model.isValue()) {
			g.setColor(Color.LIGHT_GRAY);
		} else {
			g.setColor(Color.DARK_GRAY);
		}
		g.drawRect(0, 0, width - 1, height - 1);
		g.drawRect(1, 1, width - 2, height - 2);
		g.setColor(Color.WHITE);
		if (model.isValue()) {
			g.drawLine(width - 1, 0, width - 1, height - 1);
			g.drawLine(width - 2, 1, width - 2, height - 2);
			g.drawLine(0, height - 1, width - 1, height - 1);
			g.drawLine(1, height - 2, width - 2, height - 2);
		} else {
			g.drawLine(0, 0, width - 1, 0);
			g.drawLine(1, 1, width - 2, 1);
			g.drawLine(0, 0, 0, height - 1);
			g.drawLine(1, 1, 1, height - 2);
		}

		g.setFont(new Font("Arial Black", Font.PLAIN, 20));
		g.setColor(Color.GREEN);
		final FontMetrics fm = g.getFontMetrics();
		final Rectangle2D r = fm.getStringBounds(label, g);
		final int y = (height - (int) r.getHeight()) / 2 + fm.getAscent();
		g.drawString(label, 5, y);
	}
}
