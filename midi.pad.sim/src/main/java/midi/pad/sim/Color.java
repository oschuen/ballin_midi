package midi.pad.sim;

public class Color {
	private int r;
	private int g;
	private java.awt.Color matrix[][] = {
			{java.awt.Color.BLACK, new java.awt.Color(0,80,0), new java.awt.Color(0,160,0), new java.awt.Color(0,240,0)},   
			{new java.awt.Color(80,0,0), new java.awt.Color(80,40,0), java.awt.Color.BLUE, new java.awt.Color(240,240,0)},
			{new java.awt.Color(160,0,0), java.awt.Color.BLUE, java.awt.Color.BLUE, java.awt.Color.BLUE},
			{new java.awt.Color(240,0,0), java.awt.Color.BLUE, java.awt.Color.BLUE, new java.awt.Color(240,160,0)},
	};

	public int getR() {
		return r;
	}

	public void setR(int r) {
		this.r = Math.max(0, Math.min(3, r));
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = Math.max(0, Math.min(3, g));
	}

	public java.awt.Color getAwtColor() {
		return matrix[r][g];
	}
}
