package support;

/**
 * author: Nikolai Kolbenev ID 15897074
 */
public class MyVertex {
	public double x, y, z;

	public MyVertex(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static MyVertex getMidpoint(MyVertex v1, MyVertex v2) {
		return new MyVertex((v1.x + v2.x) / 2.0, (v1.y + v2.y) / 2.0, (v1.z + v2.z) / 2);
	}
}
