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
	public static Color FULL_YELLOW = new Color(Brighness.FULL, Brighness.NORMAL);
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
}
