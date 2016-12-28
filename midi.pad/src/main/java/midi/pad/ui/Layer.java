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
 * @since 06.11.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import midi.pad.ui.event.PadEvent;

/**
 * @author oliver
 *
 */
public class Layer extends Widget {

	private final List<Widget> widgets = new ArrayList<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		for (int i = widgets.size() - 1; i >= 0; i--) {
			final Widget widget = widgets.get(i);
			final Graphic subG = g.create(widget.getX(), widget.getY(), widget.getWidth(),
					widget.getHeight());
			widget.paint(subG);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#padEventOccured(midi.pad.ui.event.PadEvent)
	 */
	@Override
	public boolean padEventOccured(final PadEvent event) {
		for (final Widget widget : widgets) {
			final PadEvent subEvent = event.translate(widget.getBounds());
			if (subEvent != null && widget.padEventOccured(subEvent)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return
	 * @see java.util.List#size()
	 */
	public int size() {
		return widgets.size();
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean addWidget(final Widget e) {
		return widgets.add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean removeWidget(final Object o) {
		return widgets.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAllWidget(final Collection<? extends Widget> c) {
		return widgets.addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAllWidget(final Collection<?> c) {
		return widgets.removeAll(c);
	}

	/**
	 * 
	 * @see java.util.List#clear()
	 */
	public void clearWidgets() {
		widgets.clear();
	}
}
