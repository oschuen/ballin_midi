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
 * @since 30.10.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui;

/**
 * @author oliver
 *
 */
public class Color {

	public static enum Brighness {
		OFF, LOW, NORMAL, FULL
	};

	private final Brighness r;
	private final Brighness g;
	private final boolean flashing;
	private final boolean opaque;

	public Color(final Brighness r, final Brighness g) {
		super();
		this.r = r;
		this.g = g;
		flashing = false;
		opaque = true;
	}

	public Color(final Brighness r, final Brighness g, final boolean flashing,
			final boolean opaque) {
		super();
		this.r = r;
		this.g = g;
		this.flashing = flashing;
		this.opaque = opaque;
	}

	public Color(final Color c, final boolean flashing, final boolean opaque) {
		super();
		r = c.r;
		g = c.g;
		this.flashing = flashing;
		this.opaque = opaque;
	}

	public static Color BLACK = new Color(Brighness.OFF, Brighness.OFF);
	public static Color LOW_RED = new Color(Brighness.LOW, Brighness.OFF);
	public static Color RED = new Color(Brighness.NORMAL, Brighness.OFF);
	public static Color FULL_RED = new Color(Brighness.FULL, Brighness.OFF);
	public static Color LOW_AMBER = new Color(Brighness.LOW, Brighness.LOW);
	public static Color FULL_AMBER = new Color(Brighness.FULL, Brighness.FULL);
	public static Color FULL_YELLOW = new Color(Brighness.LOW, Brighness.FULL);
	public static Color LOW_GREEN = new Color(Brighness.OFF, Brighness.LOW);
	public static Color GREEN = new Color(Brighness.OFF, Brighness.NORMAL);
	public static Color FULL_GREEN = new Color(Brighness.OFF, Brighness.FULL);
	public static Color TRANSPARENT = new Color(Brighness.OFF, Brighness.OFF, false, false);

	/**
	 * @return the flashing
	 */
	public boolean isFlashing() {
		return flashing;
	}

	/**
	 * @return the opaque
	 */
	public boolean isOpaque() {
		return opaque;
	}

	/**
	 * @return the r
	 */
	public Brighness getRed() {
		return r;
	}

	/**
	 * @return the g
	 */
	public Brighness getGreen() {
		return g;
	}

	public byte getMidiValue() {
		return (byte) (0x10 * g.ordinal() + r.ordinal());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Color [r=" + r + ", g=" + g + ", flashing=" + flashing + ", opaque=" + opaque + "]";
	}
}
