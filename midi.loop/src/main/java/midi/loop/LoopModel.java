/**
 * Copyright (C) 2017 Oliver Schünemann
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
 * @since 11.02.2017
 * @version 1.0
 * @author oliver
 */
package midi.loop;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jmidi.gui.model.IntegerModel;
import midi.loop.beat.Beat;

/**
 * @author oliver
 *
 */
public class LoopModel {
	private int quarterPerPage = 4;
	private int numberOfPages = 1;
	private int quarterDivision = 4;
	private int steps = quarterPerPage * numberOfPages * quarterDivision;
	private final IntegerModel velocity = new IntegerModel(0, 127, 127);
	private LoopEvent[] events = new LoopEvent[steps];
	private final Lock lock = new ReentrantLock();
	private long division = Beat.BEAT_DIVISION / quarterDivision;
	private final Effect standardEffect = (event) -> {
		return event;
	};

	private Effect effect = standardEffect;

	public LoopModel() {
		super();
	}

	public LoopModel(final Effect effect) {
		super();
		this.effect = effect;
	}

	/**
	 * @return the quarterPerPage
	 */
	public int getQuarterPerPage() {
		return quarterPerPage;
	}

	/**
	 * @param quarterPerPage
	 *            the quarterPerPage to set
	 */
	public void setQuarterPerPage(final int quarterPerPage) {
		lock.lock();
		try {
			this.quarterPerPage = quarterPerPage;
			adaptSize();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @return the numberOfPages
	 */
	public int getNumberOfPages() {
		return numberOfPages;
	}

	/**
	 * @param numberOfPages
	 *            the numberOfPages to set
	 */
	public void setNumberOfPages(final int numberOfPages) {
		lock.lock();
		try {
			this.numberOfPages = numberOfPages;
			adaptSize();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @return the quarterDivision
	 */
	public int getQuarterDivision() {
		return quarterDivision;
	}

	/**
	 * @param quarterDivision
	 *            the quarterDivision to set
	 */
	public void setQuarterDivision(final int quarterDivision) {
		lock.lock();
		try {
			this.quarterDivision = quarterDivision;
			division = Beat.BEAT_DIVISION / quarterDivision;
			adaptSize();
		} finally {
			lock.unlock();
		}
	}

	private void adaptSize() {
		steps = quarterPerPage * numberOfPages * quarterDivision;
		if (steps > events.length) {
			final LoopEvent[] newEvents = new LoopEvent[steps];
			System.arraycopy(events, 0, newEvents, 0, events.length);
			events = newEvents;
		}
	}

	public void setEvent(final LoopEvent event, final long beat) {
		lock.lock();
		try {
			events[(int) ((beat / division) % steps)] = event;
		} finally {
			lock.unlock();
		}
	}

	public Optional<LoopEvent> getEvent(final long beat) {
		if (beat % (division) == 0) {
			final int step = (int) ((beat / division) % (steps));
			return getStepEvent(step);
		}
		return Optional.empty();
	}

	public void setStepEvent(final LoopEvent event, final int step) {
		lock.lock();
		try {
			if (step >= 0) {
				events[step % steps] = event;
			}
		} finally {
			lock.unlock();
		}
	}

	public Optional<LoopEvent> getStepEvent(final int step) {
		lock.lock();
		try {
			if (step >= 0) {
				return Optional.ofNullable(events[step % steps])
						.map(event -> event.asWeightedEvent(velocity.getValue())).map(effect);
			}
		} finally {
			lock.unlock();
		}
		return Optional.empty();
	}

	/**
	 * @return the velocity
	 */
	public int getVelocity() {
		return velocity.getValue();
	}

	/**
	 * @param velocity
	 *            the velocity to set
	 */
	public void setVelocity(final int velocity) {
		this.velocity.setValue(velocity);
	}

	public IntegerModel getVelocityModel() {
		return velocity;
	}

	/**
	 * @param effect
	 *            the effect to set
	 */
	public void setEffect(final Effect effect) {
		if (effect == null) {
			this.effect = standardEffect;
		} else {
			this.effect = effect;
		}
	}
}
