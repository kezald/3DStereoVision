package support;

import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.VectorUtil;

/**
 * author: Nikolai Kolbenev ID 15897074
 */
public class Reusable {

	/**
	 * The libraries that I am using to do vector operatons are all based on float
	 * type, so I need to convert my double data to float (and do this on every
	 * single item)
	 */
	public static float[] convertToFloat(double[] arr) {
		float[] newArr = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			newArr[i] = (float) arr[i];
		}
		return newArr;
	}

	public static float[] convertTo4dFloat(double[] arr) {
		float[] newArr = new float[4];

		for (int i = 0; i < 4; i++) {
			newArr[i] = (i < arr.length) ? (float) arr[i] : 0;
		}

		return newArr;
	}

	public static double[] convertTo4dDouble(float[] arr) {
		double[] newArr = new double[4];

		for (int i = 0; i < 4; i++) {
			newArr[i] = (i < arr.length) ? arr[i] : 0;
		}

		return newArr;
	}

	public static void updateTarget(double[] target, float[] source) {
		updateTarget(target, Reusable.convertTo4dDouble(source));
	}

	public static void updateTarget(double[] target, double[] source) {
		for (int i = 0; i < target.length; i++) {
			if (i < source.length) {
				target[i] = source[i];
			} else {
				break;
			}
		}
	}

	public static float getVectorLength(float[] vector) {
		VectorMath3D vec = new VectorMath3D(vector[0], vector[1], vector[2]);
		return (float) vec.getLength();
	}

	public static double getVectorLength(double[] vector) {
		return getVectorLength(convertToFloat(vector));
	}

	/**
	 * @return Angle between two vectors in radians
	 */
	public static float getAngle3D(double[] vec1, double[] vec2) {
		return VectorUtil.angleVec3(Reusable.convertTo4dFloat(vec1), Reusable.convertTo4dFloat(vec2));
	}

	public static double[] getCross3D(double[] vec1, double[] vec2) {
		float[] result = new float[4];
		VectorUtil.crossVec3(result, Reusable.convertTo4dFloat(vec1), Reusable.convertTo4dFloat(vec2));
		return Reusable.convertTo4dDouble(result);
	}

	public static float[] getRotatedVector(float radians, float[] aroundVector, float[] vectorToRotate) {
		float[] result = new float[4];
		vectorToRotate = new float[] { vectorToRotate[0], vectorToRotate[1], vectorToRotate[2], 0 };

		Matrix4 mat = new Matrix4();
		mat.loadIdentity();
		mat.rotate((float) radians, (float) aroundVector[0], (float) aroundVector[1], (float) aroundVector[2]);
		mat.multVec(vectorToRotate, result);

		return result;
	}

	public static double[] getRotatedVector(double radians, double[] aroundVector, double[] vectorToRotate) {
		return Reusable.convertTo4dDouble(getRotatedVector((float) radians, Reusable.convertToFloat(aroundVector),
				Reusable.convertToFloat(vectorToRotate)));
	}

	public static void setVectorLength(double length, float[] vec) {
		float[] normalized = VectorUtil.normalizeVec3(vec);
		for (int i = 0; i < vec.length; i++) {
			vec[i] = (float) (normalized[i] * length);
		}
	}

	public static void setVectorLength(double length, double[] vec) {
		float[] normalized = VectorUtil.normalizeVec3(Reusable.convertTo4dFloat(vec));
		for (int i = 0; i < vec.length; i++) {
			vec[i] = normalized[i] * length;
		}
	}

	public static double[] getVector(double length, double xAngle, double yAngle, double zAngle) {
		float[] defaultVec = new float[] { (float) length, 0, 0, 0 };
		float[] vecRotated = new float[4];
		Matrix4 mat = new Matrix4();
		mat.loadIdentity();
		mat.rotate((float) xAngle, 1, 0, 0);
		mat.rotate((float) yAngle, 0, 1, 0);
		mat.rotate((float) zAngle, 0, 0, 1);
		mat.multVec(defaultVec, vecRotated);

		return Reusable.convertTo4dDouble(vecRotated);
	}

	public static void normalize(double[] vector) {
		double length = getVectorLength(vector);
		for (int i = 0; i < vector.length; i++) {
			vector[i] /= length;
		}
	}

	public static float[] getDisplacedPoint(float[] point, float[] vectorDisplacement, float distanceMoved) {
		VectorMath3D vec = new VectorMath3D(vectorDisplacement[0], vectorDisplacement[1], vectorDisplacement[2]);

		float[] newPt = new float[3];

		newPt[0] = point[0] + (float) vec.getChangeInX(distanceMoved);
		newPt[1] = point[1] + (float) vec.getChangeInY(distanceMoved);
		newPt[2] = point[2] + (float) vec.getChangeInZ(distanceMoved);

		return newPt;
	}

	public static void displacePointAlongVector(double[] point, double[] vector, double distanceMoved) {
		VectorMath3D vec = new VectorMath3D(vector[0], vector[1], vector[2]);

		point[0] += vec.getChangeInX(distanceMoved);
		point[1] += vec.getChangeInY(distanceMoved);
		point[2] += vec.getChangeInZ(distanceMoved);
	}

	public static float[] getVector3D(float[] pointStart, float[] pointEnd) {
		return new float[] { pointEnd[0] - pointStart[0], pointEnd[1] - pointStart[1], pointEnd[2] - pointStart[2] };
	}
}
