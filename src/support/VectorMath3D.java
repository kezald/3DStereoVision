package support;

/**
 * author: Nikolai Kolbenev ID 15897074
 */
public class VectorMath3D {
	private double x, y, z;
	private double length;

	public VectorMath3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		updateLength();
	}

	public static VectorMath3D getVector(MyVertex startVert, MyVertex endVert) {
		double xChange = (endVert.x - startVert.x);
		double yChange = (endVert.y - startVert.y);
		double zChange = (endVert.z - startVert.z);
		return new VectorMath3D(xChange, yChange, zChange);
	}

	public VectorMath3D copy() {
		return new VectorMath3D(x, y, z);
	}

	public void normalize() {
		x /= length;
		y /= length;
		z /= length;
		updateLength();
	}

	public double getChangeInX(double distanceMoved) {
		return x * distanceMoved / getLength();
	}

	public double getChangeInY(double distanceMoved) {
		return y * distanceMoved / getLength();
	}

	public double getChangeInZ(double distanceMoved) {
		return z * distanceMoved / getLength();
	}

	private void updateLength() {
		this.length = Math.sqrt(x * x + y * y + z * z);
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

	public double getZ() {
		return z;
	}

	public static double findAngle(VectorMath3D vec1, VectorMath3D vec2) {
		return Math.acos((vec1.x * vec2.x + vec1.y * vec2.y + vec1.z * vec2.z) / (vec1.length * vec2.length));
	}

	/**
	 * Follows the right-hand rule
	 * 
	 * @param vec1 Vector One
	 * @param vec2 Vector Two
	 * @return The perpendicular to Vector One and Vector Two, following right-hand rule
	 */
	public static VectorMath3D getCrossProduct3DVector(VectorMath3D vec1, VectorMath3D vec2) {
		double x, y, z;
		x = vec1.y * vec2.z - vec1.z * vec2.y;
		y = vec1.x * vec2.z - vec1.z * vec2.x;
		z = vec1.x * vec2.y - vec1.y * vec2.x;
		return new VectorMath3D(x, y, z);
	}
}
