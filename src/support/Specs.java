package support;

import java.awt.Point;

/**
 * author: Nikolai Kolbenev ID 15897074
 */
public class Specs {
	private int windowWidth;
	private int windowHeight;
	private boolean isMousePressed;
	private boolean rightButtonPressed;
	private boolean leftButtonPressed;
	private Point mousePosOnPress;
	private Point mousePosOnRelease;
	private Point mousePosCurrent;
	// lastProcessedPoint may have no useas of now, but is left here because it may
	// be useful later
	private Point lastProcessedPoint; // May be used by camera to remember last processed position for interactive
										// control

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	public boolean isMousePressed() {
		return isMousePressed;
	}

	public void setMousePressed(boolean isMousePressed) {
		this.isMousePressed = isMousePressed;

		if (isMousePressed == false) {
			lastProcessedPoint = null;
		}
	}

	public boolean isRightButtonPressed() {
		return rightButtonPressed;
	}

	public void setRightButtonPressed(boolean rightButtonPressed) {
		this.rightButtonPressed = rightButtonPressed;
	}

	public boolean isLeftButtonPressed() {
		return leftButtonPressed;
	}

	public void setLeftButtonPressed(boolean leftButtonPressed) {
		this.leftButtonPressed = leftButtonPressed;
	}

	public Point getMousePosOnPress() {
		return mousePosOnPress;
	}

	public void setMousePosOnPress(Point mousePosOnPress) {
		this.mousePosOnPress = mousePosOnPress;
	}

	public Point getMousePosOnRelease() {
		return mousePosOnRelease;
	}

	public void setMousePosOnRelease(Point mousePosOnRelease) {
		this.mousePosOnRelease = mousePosOnRelease;
	}

	public Point getMousePosCurrent() {
		return mousePosCurrent;
	}

	public void setMousePosCurrent(Point mousePosCurrent) {
		this.mousePosCurrent = mousePosCurrent;
	}

	public Point getLastProcessedPoint() {
		return lastProcessedPoint;
	}

	public void setLastProcessedPoint(Point lastProcessedPoint) {
		this.lastProcessedPoint = lastProcessedPoint;
	}

	public double[] pixelToGLcoords(Point point) {
		return pixelToGLcoords(point.getX(), point.getY());
	}

	public double[] pixelToGLcoords(double windowX, double windowY) {
		double invertY = windowHeight - windowY;
		double mouseX = 2.0f * (windowX / windowWidth) - 1.0f;
		double mouseY = 2.0f * (invertY / windowHeight) - 1.0f;
		return new double[] { mouseX, mouseY };
	}

	public double pixelToGLVerticalDistance(double pixelDistance) {
		return pixelDistance / windowHeight * 2.0;
	}

	public double pixelToGLHorizontalDistance(double pixelDistance) {
		return pixelDistance / windowWidth * 2.0;
	}
}
