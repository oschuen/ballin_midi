package midi.pad.sim;

import java.awt.Graphics;

public class Screen {
	private final Color[] buttons = new Color[80];

	public Screen() {
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new Color();
			buttons[i].setG(3);
			buttons[i].setR(3);
		}
	}

	public void draw(final Graphics g, final int width, final int height) {
		final int padding = 8;
		final int space = 1;
		final int xPad = width / (9 * padding + 10 * space);
		final int yPad = height / (9 * padding + 10 * space);
		final int xoff = space * xPad;
		final int xstep = xoff + padding * xPad;
		final int xsize = padding * xPad;
		final int yoff = space * yPad;
		final int ystep = yoff + padding * yPad;
		final int ysize = padding * yPad;

		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				g.setColor(buttons[x + y * 8].getAwtColor());
				g.fill3DRect(x * xstep + xoff, y * ystep + yoff + ystep, xsize, ysize, true);
			}
		}
		for (int y = 0; y < 8; y++) {
			g.setColor(buttons[y + 64].getAwtColor());
			g.fillOval(8 * xstep + xoff, y * ystep + ystep + yoff, xsize, ysize);
		}
		for (int x = 0; x < 8; x++) {
			g.setColor(buttons[x + 72].getAwtColor());
			g.fillOval(x * xstep + xoff, yoff, xsize, ysize);
		}
	}

	public int getButton(final int x, final int y, final int width, final int height) {
		final int padding = 8;
		final int space = 1;
		final int xPad = width / (9 * padding + 10 * space);
		final int yPad = height / (9 * padding + 10 * space);
		final int xoff = space * xPad;
		final int xstep = xoff + padding * xPad;
		final int xsize = padding * xPad;
		final int yoff = space * yPad;
		final int ystep = yoff + padding * yPad;
		final int ysize = padding * yPad;
		int bx = -1;
		int by = -1;
		for (int a = 0; a < 9 && bx < 0; a++) {
			if (x >= a * xstep + xoff && x < a * xstep + xoff + xsize) {
				bx = a;
			}
		}
		for (int a = 0; a < 9 && by < 0; a++) {
			if (y >= a * ystep + yoff && y < a * ystep + yoff + ysize) {
				by = a;
			}
		}
		if (bx >= 0 && by >= 0) {
			if (by == 0 && bx <= 7) {
				return 128 + bx;
			}
			if (by > 0 && bx <= 8) {
				return (by - 1) * 16 + bx;
			}
		}
		return -1;
	}

	public void setColor(final int button, final Color c) {
		if (button >= 0 && button < buttons.length) {
			buttons[button].setG(c.getG());
			buttons[button].setR(c.getR());
		}
	}

	public void setColor(final int button, final int r, final int g) {
		if (button >= 0 && button < buttons.length) {
			buttons[button].setG(g);
			buttons[button].setR(r);
		}
	}

	public Color getColor(final int button) {
		if (button >= 0 && button < buttons.length) {
			return buttons[button];
		}
		return null;
	}

	public void copy(final Screen screen) {
		for (int i = 0; i < buttons.length; i++) {
			setColor(i, screen.getColor(i));
		}
	}
}
