package midi.pad.sim;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class Screen {
	private final Color[] buttons = new Color[80];
	private final char[] text = new char[80];

	public Screen() {
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new Color();
			buttons[i].setG(3);
			buttons[i].setR(3);
		}
		final char t[] = "01234567890123456789012345678901234567890123456789012345678901234567890123456789"
				.toCharArray();
		for (int i = 0; i < t.length; ++i) {
			setChar(i, t[i]);
		}
	}

	public void draw(final Graphics g, final int width, final int height) {
		final int padding = 8;
		final int space = 1;
		final int xPad = (width - 200) / (9 * padding + 10 * space);
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
		g.setColor(java.awt.Color.BLUE);
		g.fill3DRect(width - 200, 25, 170, 80, false);
		final Map<TextAttribute, Object> attributes = new HashMap<>();

		attributes.put(TextAttribute.FAMILY, Font.MONOSPACED);
		attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD);
		attributes.put(TextAttribute.SIZE, 14);
		final Font font = Font.getFont(attributes);
		g.setFont(font);
		g.setColor(java.awt.Color.WHITE);
		g.drawChars(text, 0, 20, width - 195, 40);
		g.drawChars(text, 20, 20, width - 195, 60);
		g.drawChars(text, 40, 20, width - 195, 80);
		g.drawChars(text, 60, 20, width - 195, 100);
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

	public void setChar(final int pos, final char c) {
		final String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890.-_!\"§$%&/()=?<>#*+- ";
		if (allowed.indexOf(c) >= 0 && pos >= 0 && pos < text.length) {
			text[pos] = c;
		}
	}

	public void clearText() {
		for (int i = 0; i < text.length; i++) {
			text[i] = ' ';
		}
	}
}
