package support;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

import support.Reusable;
import support.VectorMath3D;

/**
 * author: Nikolai Kolbenev ID 15897074
 */
public class Camera {
	public final double PITCH_ANGLE_MAX_LIMITED = 89.0; // With respect to XZ plane

	public final double ACCELERATED_SPEED_INCREASE = 600;
	public final double DEFAULT_SPEED = 100;
	private Specs specs;

	private double[] startAt; // For XY plane movement
	private double[] startUp; // For XY plane movement

	private double[] eye; // Position of the camera in world coordinates.
	private double[] at; // This is a vector! We are looking along it.
	private double[] up;

	private double fov;
	private double near;
	private double far;
	private final double[] worldUpVector = new double[] { 0, 1, 0 };

	private boolean isOrthographic;
	private boolean moveForward, moveBackward, moveLeft, moveRight, moveUp, moveDown;
	private boolean rollRight, rollLeft, pitchDown, pitchUp, yawRight, yawLeft;
	private boolean isAccelerated, showOrientationPlane, isSixDegreesMovement;
	private double speed;
	private double speedRotation; // Degrees per second
	private double pitchAngle, yawAngle;

	public Camera() {
		init();
	}

	public void init() {
		startAt = new double[] { 0, 0, -1 };
		startUp = new double[] { 0, 1, 0 };
		eye = new double[] { -414, -13, 1200 };
		at = new double[] { 0, 0, -1 };
		up = new double[] { 0, 1, 0 };
		fov = 60.0;
		near = 0.1;
		far = 2000000;
		speed = DEFAULT_SPEED;
		speedRotation = 45.0;
		
		pitchAngle = -18;
		yawAngle= 692;
		
		rotateWithRespectToXZ();

		isSixDegreesMovement = false;
	}

	public void apply(GL2 gl) {
		GLU glu = new GLU();

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		if (isOrthographic()) {
			gl.glOrtho(-1.0, 1.0, -1.0, 1.0, 0.1, 8);
		} else {
			glu.gluPerspective(fov, (double) specs.getWindowWidth() / specs.getWindowHeight(), near, far);
		}

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		double[] dummyPoint = getDummyPointInFrontOfTheCamera();
		glu.gluLookAt(eye[0], eye[1], eye[2], dummyPoint[0], dummyPoint[1], dummyPoint[2], up[0], up[1], up[2]);
	}

	/**
	 * Needed to calculate a point to pass as an argument to gluLookAt: Direct
	 * quote/hint from Assignment brief: "Make the camera look at a point just in
	 * front of its location."
	 * 
	 * @return
	 */
	public double[] getDummyPointInFrontOfTheCamera() {
		VectorMath3D lookAtVector = new VectorMath3D(at[0], at[1], at[2]);
		lookAtVector.normalize();
		return new double[] { eye[0] + lookAtVector.getX(), eye[1] + lookAtVector.getY(),
				eye[2] + lookAtVector.getZ() };
	}

	public void move(double timeElapsed) {
		double spd = this.speed;
		spd += (isAccelerated) ? ACCELERATED_SPEED_INCREASE : 0;
		double distanceMoved = spd * timeElapsed;
		double degreesRotated = speedRotation * timeElapsed;

		// Normalization prevents too small and too large vectors producing NaNs
		Reusable.normalize(at);
		Reusable.normalize(up);

		if (specs.isMousePressed()) {
			// Interactive control with mouse
			applyMouseControl(timeElapsed);
		}

		if (!isSixDegreesMovement) {
			updateAnglesForXZMovement(degreesRotated);
			rotateWithRespectToXZ();
			moveWithRespectToXZ(distanceMoved);
		} else if (isSixDegreesMovement) {
			rotateWithRespectToCamera(degreesRotated);
			moveWithRespectToCamera(distanceMoved);
		}
	}

	private void applyMouseControl(double timeElapsed) {
		double[] glCoordsPress = specs.pixelToGLcoords(specs.getMousePosOnPress());
		double[] glCoordsCurrent = specs.pixelToGLcoords(specs.getMousePosCurrent());
		double dx = glCoordsCurrent[0] - glCoordsPress[0];
		double dy = glCoordsCurrent[1] - glCoordsPress[1];

		double speed = 180.0;

		if (!isSixDegreesMovement) {
			yawAngle += -dx * (speed * timeElapsed);
			pitchAngle += dy * (speed * timeElapsed);
		} else {
			double[] sideVec = Reusable.getCross3D(at, up);
			double[] pitchAt = Reusable.getRotatedVector(Math.toRadians(dy * (speed * timeElapsed)), sideVec, at);
			Reusable.updateTarget(at, pitchAt);
			Reusable.updateTarget(up, Reusable.getCross3D(sideVec, at));

			double[] rollPitchAt = Reusable.getRotatedVector(Math.toRadians(-dx * (speed * timeElapsed)), up, at);
			Reusable.updateTarget(at, rollPitchAt);
		}
	}

	private void updateAnglesForXZMovement(double degreesRotated) {
		double dirSign = 1;

		// ============Update angles===============
		if (pitchDown || pitchUp) {
			dirSign = (pitchDown) ? -1 : 1;
			pitchAngle += dirSign * degreesRotated;
		}

		if (yawRight || yawLeft) {
			dirSign = (yawRight) ? -1 : 1;
			yawAngle += dirSign * degreesRotated;
		}

		// ============Ensure angles are valid==========
		if (!isSixDegreesMovement) {
			if (pitchAngle > PITCH_ANGLE_MAX_LIMITED) {
				pitchAngle = PITCH_ANGLE_MAX_LIMITED;
			} else if (pitchAngle < -PITCH_ANGLE_MAX_LIMITED) {
				pitchAngle = -PITCH_ANGLE_MAX_LIMITED;
			}
		}

		while (yawAngle >= 360) {
			yawAngle -= 360;
		}
		while (yawAngle <= 360) {
			yawAngle += 360;
		}
	}

	private void rotateWithRespectToXZ() {
		// Pitch
		double[] startSideVec = Reusable.getCross3D(startAt, startUp);
		double[] pitchAt = Reusable.getRotatedVector(Math.toRadians(pitchAngle), startSideVec, startAt);
		double[] pitchUp = new double[4];
		Reusable.updateTarget(pitchUp, Reusable.getCross3D(startSideVec, pitchAt));

		double[] YawAt = new double[4];
		double[] YawUp = new double[4];

		double angleRotate = yawAngle;
		// Yaw with respect to Y axis
		double[] rotated = Reusable.getRotatedVector(Math.toRadians(angleRotate), worldUpVector, pitchAt);
		Reusable.updateTarget(YawAt, rotated);
		rotated = Reusable.getRotatedVector(Math.toRadians(angleRotate), worldUpVector, pitchUp);
		Reusable.updateTarget(YawUp, rotated);

		Reusable.updateTarget(at, YawAt);
		Reusable.updateTarget(up, YawUp);
	}

	private void moveWithRespectToXZ(double distanceMoved) {
		double dirSign = 1;

		if (!isSixDegreesMovement) {
			if (moveForward || moveBackward) {
				dirSign = (moveBackward) ? -1 : 1;
				Reusable.displacePointAlongVector(eye, at, dirSign * distanceMoved);
			}

			if (moveLeft || moveRight) {
				dirSign = (moveLeft) ? -1 : 1;
				Reusable.displacePointAlongVector(eye, Reusable.getCross3D(at, up), dirSign * distanceMoved);
			}

			if (moveDown || moveUp) {
				dirSign = (moveDown) ? -1 : 1;
				Reusable.displacePointAlongVector(eye, worldUpVector, dirSign * distanceMoved);
			}
		}
	}

	private void rotateWithRespectToCamera(double degreesRotated) {
		double dirSign = 1;
		if (pitchDown || pitchUp) {
			dirSign = (pitchDown) ? -1 : 1;
			double[] sideVec = Reusable.getCross3D(at, up);
			double[] rotatedAt = Reusable.getRotatedVector(Math.toRadians(dirSign * degreesRotated), sideVec, at);
			Reusable.updateTarget(at, rotatedAt);
			Reusable.updateTarget(up, Reusable.getCross3D(sideVec, at));
		}

		if (yawRight || yawLeft) {
			dirSign = (yawRight) ? -1 : 1;
			double[] rotatedAt = Reusable.getRotatedVector(Math.toRadians(dirSign * degreesRotated), up, at);
			Reusable.updateTarget(at, rotatedAt);
		}

		if (rollLeft || rollRight) {
			dirSign = (rollLeft) ? -1 : 1;
			double[] rotatedUp = Reusable.getRotatedVector(Math.toRadians(dirSign * degreesRotated), at, up);
			Reusable.updateTarget(up, rotatedUp);
		}
	}

	private void moveWithRespectToCamera(double distanceMoved) {
		double dirSign = 1;

		// Six degrees movement, with respect to current orientation "at", "up"
		if (moveForward || moveBackward) {
			dirSign = (moveBackward) ? -1 : 1;
			Reusable.displacePointAlongVector(eye, at, dirSign * distanceMoved);
		}

		if (moveLeft || moveRight) {
			dirSign = (moveLeft) ? -1 : 1;
			Reusable.displacePointAlongVector(eye, Reusable.getCross3D(at, up), dirSign * distanceMoved);
		}

		if (moveDown || moveUp) {
			dirSign = (moveDown) ? -1 : 1;
			Reusable.displacePointAlongVector(eye, up, dirSign * distanceMoved);
		}
	}

	public void setMoveForward(boolean moveForward) {
		this.moveForward = moveForward;
	}

	public void setMoveBackward(boolean moveBackward) {
		this.moveBackward = moveBackward;
	}

	public void setMoveLeft(boolean moveLeft) {
		this.moveLeft = moveLeft;
	}

	public void setMoveRight(boolean moveRight) {
		this.moveRight = moveRight;
	}

	public void setMoveUp(boolean moveUp) {
		this.moveUp = moveUp;
	}

	public void setMoveDown(boolean moveDown) {
		this.moveDown = moveDown;
	}

	public void setRollRight(boolean rollRight) {
		this.rollRight = rollRight;
	}

	public void setRollLeft(boolean rollLeft) {
		this.rollLeft = rollLeft;
	}

	public void setPitchDown(boolean pitchDown) {
		this.pitchDown = pitchDown;
	}

	public void setPitchUp(boolean pitchUp) {
		this.pitchUp = pitchUp;
	}

	public void setYawRight(boolean yawRight) {
		this.yawRight = yawRight;
	}

	public void setYawLeft(boolean yawLeft) {
		this.yawLeft = yawLeft;
	}

	public boolean isAccelerated() {
		return isAccelerated;
	}

	public void setAccelerated(boolean isAccelerated) {
		this.isAccelerated = isAccelerated;
	}

	public double getFov() {
		return fov;
	}

	public void setFov(double fov) {
		this.fov = fov;
	}

	public double getNearPlane() {
		return near;
	}

	public void setNearPlane(double near) {
		this.near = near;
	}

	public double getFar() {
		return far;
	}

	public void setFar(double far) {
		this.far = far;
	}

	public void setShowOrientationPlane(boolean showOrientationPlane) {
		this.showOrientationPlane = showOrientationPlane;
	}

	public boolean getShowOrientationPlane() {
		return showOrientationPlane;
	}

	public boolean isOrthographic() {
		return isOrthographic;
	}

	public void setOrthographic(boolean isOrthographic) {
		this.isOrthographic = isOrthographic;
	}

	public double[] getEye() {
		return eye;
	}

	public double getSpeed() {
		return (isAccelerated) ? speed + ACCELERATED_SPEED_INCREASE : speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void setSpecs(Specs specs) {
		this.specs = specs;
	}

	public boolean isSixDegreesMovement() {
		return isSixDegreesMovement;
	}

	public void setSixDegreesMovement(boolean isSixDegreesMovement) {
		this.isSixDegreesMovement = isSixDegreesMovement;

		// Convert from free camera orientation to fixed XY plane orientation
		if (!isSixDegreesMovement) {
			pitchAngle = 90.0 - Math.toDegrees(Reusable.getAngle3D(at, worldUpVector));

			double[] xyProjection = new double[] { at[0], 0, at[2] };
			yawAngle = Math.toDegrees(Reusable.getAngle3D(xyProjection, startAt));

			if (Reusable.getAngle3D(Reusable.getCross3D(startAt, startUp), xyProjection) < Math.PI / 2) {
				yawAngle *= -1;
			}
		}
	}

	public void resetMovementKeys() {
		moveForward = moveBackward = moveLeft = moveRight = moveUp = moveDown = rollRight = rollLeft = pitchDown = pitchUp = yawRight = yawLeft = false;
	}

	public String getCameraType() {
		String type = "None";

		if (isSixDegreesMovement) {
			type = "Self-Orientational";
		} else {
			type = "XZ Plane";
		}

		return type;
	}
}
