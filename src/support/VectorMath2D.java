package support;


/**
 * author: Nikolai Kolbenev ID 15897074
 */
public class VectorMath2D {
	private double x, y;
	private double length;

	public VectorMath2D(double x, double y) {
		this.x = x;
		this.y = y;
		updateLength();
	}

	public static VectorMath2D getVector(MyVertex startVert, MyVertex endVert) {
		double xChange = (endVert.x - startVert.x);
		double yChange = (endVert.y - startVert.y);
		return new VectorMath2D(xChange, yChange);
	}

	public VectorMath2D copy() {
		return new VectorMath2D(x, y);
	}

	public void normalize() {
		x /= length;
		y /= length;
		updateLength();
	}

	public double getChangeInX(double distanceMoved) {
		return x * distanceMoved / getLength();
	}

	public double getChangeInY(double distanceMoved) {
		return y * distanceMoved / getLength();
	}

	private void updateLength() {
		this.length = Math.sqrt(x * x + y * y);
	}

	public double getLength() {
		return this.length;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	/**
	 * If angle is positive - rotate clockwise around vector tail. Otherwise
	 * rotate counter-clockwise
	 * 
	 * @param angle
	 *            in radians
	 */
	public void rotateVector(double angle) {
		double xprime = x * Math.cos(angle) - y * Math.sin(angle);
		double yprime = x * Math.sin(angle) + y * Math.cos(angle);

		this.x = xprime;
		this.y = yprime;
	}

	/**
	 * @param angle
	 *            in radians measured counter-clockwise, beginning at x = 1, y = 0
	 * @return unit vector: radius = length = 1
	 */
	public static VectorMath2D getUnitVector(double angle) {
		return new VectorMath2D(Math.cos(angle) * 1, Math.sin(angle) * 1);
	}

	public static double findAngle(VectorMath2D vec1, VectorMath2D vec2) {
		return Math.acos((vec1.x * vec2.x + vec1.y * vec2.y) / (vec1.length * vec2.length));
	}
}
