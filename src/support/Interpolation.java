package support;

/**
 * author: Nikolai Kolbenev ID 15897074
 */
public class Interpolation {

	private double[] startValues;
	private double[] endValues;

	public Interpolation(double[] startVals, double[] endVals) {
		setNew(startVals, endVals);
	}

	/**
	 * Gradually change from start state into end state
	 * 
	 * @param progress. A value between 0 and 1
	 * @return Value that reflects current state
	 */
	public double[] getValue(double progress) {
		double[] value = new double[startValues.length];

		for (int i = 0; i < value.length; i++) {
			value[i] = startValues[i] + progress * (endValues[i] - startValues[i]);
		}

		return value;
	}

	public void setNew(double[] startVals, double[] endVals) {
		this.startValues = startVals;
		this.endValues = endVals;
	}
}
