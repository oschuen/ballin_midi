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
 * @since 06.11.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import midi.pad.ui.event.Event;
import midi.pad.ui.event.PadEvent;
import midi.pad.ui.widgets.ControlButton;

/**
 * @author oliver
 *
 */
public class Layer extends Widget {

	private final List<Widget> widgets = new ArrayList<>();
	private boolean exclusive = false;
	private String title = "";
	private String hint = "";
	private String extraHint = "";

	public Layer(final boolean exclusive) {
		super();
		this.exclusive = exclusive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.pad.ui.Widget#paint(midi.pad.ui.Graphic)
	 */
	@Override
	public void paint(final Graphic g) {
		if (exclusive) {
			g.fill(Color.BLACK);
		}
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
	public boolean eventOccured(final Event event) {
		for (final Widget widget : widgets) {
			if (event != null && PadEvent.isEventOfThisType(event)) {
				final PadEvent subEvent = PadEvent.getEvent(event).translate(widget.getBounds());
				if (subEvent != null && widget.eventOccured(subEvent)) {
					return true;
				}
			}
		}
		return exclusive;
	}

	public void stopLayer() {

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

	public Optional<ControlButton> getAbcControlButton(final int y) {
		return Optional.empty();
	}

	public Optional<ControlButton> getNumControlButton(final int x) {
		return Optional.empty();
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * @return the hint
	 */
	public String getHint() {
		return hint;
	}

	/**
	 * @param hint
	 *            the hint to set
	 */
	public void setHint(final String hint) {
		this.hint = hint;
	}

	/**
	 * @return the extraHint
	 */
	public String getExtraHint() {
		return extraHint;
	}

	/**
	 * @param extraHint
	 *            the extraHint to set
	 */
	public void setExtraHint(final String extraHint) {
		this.extraHint = extraHint;
	}
}
