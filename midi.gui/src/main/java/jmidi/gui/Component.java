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
package jmidi.gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oliver
 *
 */
@SuppressWarnings("serial")
public class Component extends JPanel {

	public static final Font sf = new Font("Arial Black", Font.PLAIN, 20);
	private static final Logger logger = LoggerFactory.getLogger(Component.class);

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
