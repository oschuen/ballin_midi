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
 * @since 03.10.2016
 * @version 1.0
 * @author oliver
 */
package jmidi.gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author oliver
 *
 */
@SuppressWarnings("serial")
public class Component extends JPanel {

	public static final Font sf = new Font("Arial Black", Font.PLAIN, 20);
	private static final Logger logger = LogManager.getLogger(Component.class);

	public Component() {
		super();
		final MouseAdapter adapter = new MouseAdapter() {
			int pressX = 0;
			int pressY = 0;
			boolean drag = false;

			@Override
			public void mousePressed(final MouseEvent e) {
				pressX = e.getX();
				pressY = e.getY();
				drag = isInDragArea(pressX, pressY);
				Component.this.mousePressed(pressX, pressY);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				Component.this.mouseReleased(e.getX(), e.getY());
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				if (drag) {
					Component.this.mouseDragged(pressX, pressY, e.getX(), e.getY());
				}
			}

		};
		final MouseWheelListener wheelListener = new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				mouseWheelEvent(e.getWheelRotation());
			}
		};
		this.addMouseListener(adapter);
		this.addMouseMotionListener(adapter);
		this.addMouseWheelListener(wheelListener);
	}

	@Override
	public void repaint() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Component.super.repaint();
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});

	}

	public boolean isInDragArea(final int x, final int y) {
		return true;
	}

	public void mousePressed(final int x, final int y) {

	}

	public void mouseReleased(final int x, final int y) {

	}

	public void mouseDragged(final int origin_x, final int origin_y, final int x, final int y) {

	}

	public void mouseWheelEvent(final int steps) {

	}
}
