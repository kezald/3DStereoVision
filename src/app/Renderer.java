package app;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.PriorityQueue;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.util.Animator;

import imgdlg.DlgStereoImage;
import support.Camera;
import support.Specs;

public class Renderer implements GLEventListener, KeyListener, MouseMotionListener, MouseListener {
	static Frame frame;
	private Camera camera;
	private static Specs specs;

	private double timeBefore = System.currentTimeMillis() / 1000.0;

	boolean reconstruct;
	BufferedImage imgHeights;
	Mat imgColors;

	private GLU glu;
	private GLUquadric quadric;

	int displayListIDSimple;
	int displayListIDComplex;

	boolean isSimple = false;

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		if (reconstruct) {
			reconstruct = false;
			reconstruct3D(gl);
		}
		
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		camera.apply(gl);

		float ambientLight[] = { 0.0f, 0.0f, 0.0f, 1 }; // no ambient
		float diffuseLight[] = { 1, 1, 1, 1 }; // white light for diffuse
		float specularLight[] = { 1, 1, 1, 1 }; // white light for specular

		// setup the light 0 properties
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambientLight, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuseLight, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specularLight, 0);

		// position the light
		if (imgColors != null) {
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION,
					new float[] { (float) (imgColors.width() / 2), -imgColors.height() / 2, 10000.0f, 1.0f }, 0);
		}

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glRotated(-90, 0, 0, 1);
		float factor = 0.01f;
		gl.glScaled(1, 1, factor);

		if (isSimple) {
			gl.glCallList(displayListIDSimple);
		} else {
			gl.glCallList(displayListIDComplex);
		}

		gl.glPopMatrix();

		drawAxes(gl);

		drawMousePressOverlay(gl);

		// ================== Animation ====================
		double delta = System.currentTimeMillis() / 1000.0 - timeBefore;
		timeBefore += delta;
		camera.move(delta);

		gl.glFlush();
	}

	private void drawAxes(GL2 gl) {
		gl.glLineWidth(1);
		gl.glBegin(GL2.GL_LINES);
		gl.glColor3d(1, 0, 0);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(100, 0, 0);

		gl.glColor3d(0, 1, 0);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(0, 100, 0);

		gl.glColor3d(0, 0, 1);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(0, 0, 100);
		gl.glEnd();
	}

	private void drawMousePressOverlay(GL2 gl) {
		// Mouse Press Location Overlay
		if (specs.isMousePressed()) {
			// gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glOrtho(-1.0, 1.0, -1.0, 1.0, 0, 1);

			double[] mouseCoordsPress = specs.pixelToGLcoords(specs.getMousePosOnPress());
			double[] mouseCoordsCur = specs.pixelToGLcoords(specs.getMousePosCurrent());

			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glPointSize(5);

			gl.glBegin(GL2.GL_POINTS);
			gl.glColor3d(1, 1, 1);
			gl.glVertex2d(mouseCoordsPress[0], mouseCoordsPress[1]);
			gl.glEnd();

			gl.glLineWidth(2);
			gl.glBegin(GL2.GL_LINES);
			gl.glColor3d(0, 0, 1);
			gl.glVertex3d(mouseCoordsPress[0], mouseCoordsPress[1], 0);
			gl.glVertex3d(mouseCoordsCur[0], mouseCoordsCur[1], 0);
			gl.glEnd();
		}
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_BLEND);
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.setSwapInterval(1);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		glu = new GLU();
		quadric = glu.gluNewQuadric();

		// parameters for light 0
		float ambientLight[] = { 0.0f, 0.0f, 0.0f, 1 }; // no ambient
		float diffuseLight[] = { 1, 1, 1, 1 }; // white light for diffuse
		float specularLight[] = { 1, 1, 1, 1 }; // white light for specular

		// setup the light 0 properties
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambientLight, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuseLight, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specularLight, 0);

		// amount of global ambient light
		float globalAmbientLight[] = { 0.4f, 0.4f, 0.4f, 1 };

		// set the global ambient light level
		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globalAmbientLight, 0);

		// enable lighting
		gl.glEnable(GL2.GL_LIGHTING);

		// enable light 0
		gl.glEnable(GL2.GL_LIGHT0);

		// normalize the normals
		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);

		camera = new Camera();
		camera.setSpecs(specs);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();
		height = (height == 0) ? 1 : height; // prevent divide by zero

		gl.glViewport(0, 0, width, height);

		specs.setWindowWidth(width);
		specs.setWindowHeight(height);
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//Use below when creating a jar, then place "dll/" folder next to it.
//		System.loadLibrary("dll/" + Core.NATIVE_LIBRARY_NAME);
		
		printControlKeys();
		specs = new Specs();
		specs.setWindowWidth(500);
		specs.setWindowHeight(500);

		frame = new Frame("Algorithmic Modelling");
		GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile);
		GLCanvas canvas = new GLCanvas(capabilities);
		Renderer drawing = new Renderer();
		canvas.addGLEventListener(drawing);
		canvas.addKeyListener(drawing);
		canvas.addMouseMotionListener(drawing);
		canvas.addMouseListener(drawing);
		frame.add(canvas);
		frame.setSize(specs.getWindowWidth(), specs.getWindowHeight());

		final Animator animator = new Animator(canvas);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// Run this on another thread than the AWT event queue to
				// make sure the call to Animator.stop() completes before
				// exiting
				new Thread(new Runnable() {

					public void run() {
						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		canvas.requestFocusInWindow();

		animator.start();
	}

	private static void printControlKeys() {
		String info = "3 - change isOrthographic\n" + "4 - increase Level\n" + "5 - decrease Level\n"
				+ "6 - increase FOV\n" + "7 - decrease FOV\n";
		System.out.println(info);
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		specs.setMousePressed(true);
		specs.setMousePosOnPress(e.getPoint());
		specs.setMousePosCurrent(e.getPoint());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		specs.setMousePressed(false);
		specs.setMousePosOnRelease(e.getPoint());
		specs.setMousePosCurrent(null);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		specs.setMousePosCurrent(e.getPoint());
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		switch (key) {
		case KeyEvent.VK_3:
			camera.setOrthographic(!camera.isOrthographic());
			break;
		case KeyEvent.VK_6:
			if (camera.getFov() < 170) {
				camera.setFov(camera.getFov() + 1);
			}
			break;
		case KeyEvent.VK_7:
			if (camera.getFov() > 10) {
				camera.setFov(camera.getFov() - 1);
			}

			break;

		case KeyEvent.VK_W:
			camera.setMoveForward(true);
			break;
		case KeyEvent.VK_S:
			camera.setMoveBackward(true);
			break;
		case KeyEvent.VK_A:
			camera.setMoveLeft(true);
			break;
		case KeyEvent.VK_D:
			camera.setMoveRight(true);
			break;
		case KeyEvent.VK_SPACE:
			camera.setMoveUp(true);
			break;
		case KeyEvent.VK_X:
			camera.setMoveDown(true);
			break;
		case KeyEvent.VK_Q:
			camera.setRollLeft(true);
			break;
		case KeyEvent.VK_E:
			camera.setRollRight(true);
			break;
		case KeyEvent.VK_SHIFT:
			camera.setAccelerated(true);
			break;
		case KeyEvent.VK_DOWN:
			camera.setPitchDown(true);
			break;
		case KeyEvent.VK_UP:
			camera.setPitchUp(true);
			break;
		case KeyEvent.VK_LEFT:
			camera.setYawLeft(true);
			break;
		case KeyEvent.VK_RIGHT:
			camera.setYawRight(true);
			break;
		case KeyEvent.VK_L:
			DlgStereoImage dlg = new DlgStereoImage(frame);

			dlg.jButtonApply.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Make image data available
					imgHeights = dlg.imageGray;
					imgColors = dlg.imgL;
					reconstruct = true;
					dlg.setVisible(false);
					dlg.dispose();
				}
			});

			break;
		case KeyEvent.VK_K:
			isSimple = !isSimple;
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();

		switch (key) {
		case KeyEvent.VK_W:
			camera.setMoveForward(false);
			break;
		case KeyEvent.VK_S:
			camera.setMoveBackward(false);
			break;
		case KeyEvent.VK_A:
			camera.setMoveLeft(false);
			break;
		case KeyEvent.VK_D:
			camera.setMoveRight(false);
			break;
		case KeyEvent.VK_SPACE:
			camera.setMoveUp(false);
			break;
		case KeyEvent.VK_X:
			camera.setMoveDown(false);
			break;
		case KeyEvent.VK_Q:
			camera.setRollLeft(false);
			break;
		case KeyEvent.VK_E:
			camera.setRollRight(false);
			break;
		case KeyEvent.VK_SHIFT:
			camera.setAccelerated(false);
			break;
		case KeyEvent.VK_DOWN:
			camera.setPitchDown(false);
			break;
		case KeyEvent.VK_UP:
			camera.setPitchUp(false);
			break;
		case KeyEvent.VK_LEFT:
			camera.setYawLeft(false);
			break;
		case KeyEvent.VK_RIGHT:
			camera.setYawRight(false);
			break;
		}
	}

	void reconstruct3D(GL2 gl) {
		displayListIDSimple = gl.glGenLists(1);
		gl.glNewList(displayListIDSimple, GL2.GL_COMPILE);

		gl.glNormal3d(0, 0, 1);

		gl.glBegin(GL2.GL_TRIANGLES);

		for (int row = 0; row < imgHeights.getHeight() - 1; row++) {
			for (int col = 0; col < imgHeights.getWidth() - 1; col++) {
				int[] heightTopLeft = new int[3];
				imgHeights.getRaster().getPixel(col, row, heightTopLeft);

				int[] heightTopRight = new int[3];
				imgHeights.getRaster().getPixel(col + 1, row, heightTopRight);

				int[] heightBottomLeft = new int[3];
				imgHeights.getRaster().getPixel(col, row + 1, heightBottomLeft);

				int[] heightBottomRight = new int[3];
				imgHeights.getRaster().getPixel(col + 1, row + 1, heightBottomRight);

				double[] colorsTopLeft = imgColors.get(row, col);
				double[] colorsTopRight = imgColors.get(row, col + 1);
				double[] colorsBottomLeft = imgColors.get(row + 1, col);
				double[] colorsBottomRight = imgColors.get(row + 1, col + 1);

				float[] vec1 = new float[] { 0, 1, heightBottomLeft[0] - heightTopLeft[0] };
				float[] vec2 = new float[] { 1, 0, heightBottomRight[0] - heightBottomLeft[0] };

				// First Triangle
				gl.glColor3d(colorsTopLeft[0] / 255.0f, colorsTopLeft[1] / 255.0f, colorsTopLeft[2] / 255.0f);
				gl.glVertex3d(row, col, heightTopLeft[0]);

				gl.glColor3d(colorsBottomLeft[0] / 255.0f, colorsBottomLeft[1] / 255.0f, colorsBottomLeft[2] / 255.0f);
				gl.glVertex3d(row + 1, col, heightBottomLeft[0]);

				gl.glColor3d(colorsBottomRight[0] / 255.0f, colorsBottomRight[1] / 255.0f,
						colorsBottomRight[2] / 255.0f);
				gl.glVertex3d(row + 1, col + 1, heightBottomRight[0]);

				float[] vec3 = new float[] { 0, -1, heightBottomRight[0] - heightTopRight[0] };
				float[] vec4 = new float[] { -1, -1, heightBottomRight[0] - heightTopLeft[0] };

				// Second Triangle=
				gl.glColor3d(colorsBottomRight[0] / 255.0f, colorsBottomRight[1] / 255.0f,
						colorsBottomRight[2] / 255.0f);
				gl.glVertex3d(row + 1, col + 1, heightBottomRight[0]);

				gl.glColor3d(colorsTopRight[0] / 255.0f, colorsTopRight[1] / 255.0f, colorsTopRight[2] / 255.0f);
				gl.glVertex3d(row, col + 1, heightTopRight[0]);

				gl.glColor3d(colorsTopLeft[0] / 255.0f, colorsTopLeft[1] / 255.0f, colorsTopLeft[2] / 255.0f);
				gl.glVertex3d(row, col, heightTopLeft[0]);

			}
		}

		gl.glEnd();

		gl.glEndList();

		displayListIDComplex = gl.glGenLists(1);
		gl.glNewList(displayListIDComplex, GL2.GL_COMPILE);

		gl.glBegin(GL2.GL_TRIANGLES);

		for (int row = 0; row < imgHeights.getHeight() - 1; row++) {
			for (int col = 0; col < imgHeights.getWidth() - 1; col++) {
				int[] heightTopLeft = new int[3];
				imgHeights.getRaster().getPixel(col, row, heightTopLeft);

				int[] heightTopRight = new int[3];
				imgHeights.getRaster().getPixel(col + 1, row, heightTopRight);

				int[] heightBottomLeft = new int[3];
				imgHeights.getRaster().getPixel(col, row + 1, heightBottomLeft);

				int[] heightBottomRight = new int[3];
				imgHeights.getRaster().getPixel(col + 1, row + 1, heightBottomRight);

				double[] colorsTopLeft = imgColors.get(row, col);
				double[] colorsTopRight = imgColors.get(row, col + 1);
				double[] colorsBottomLeft = imgColors.get(row + 1, col);
				double[] colorsBottomRight = imgColors.get(row + 1, col + 1);

				float[] vec1 = new float[] { 0, 1, heightBottomLeft[0] - heightTopLeft[0] };
				float[] vec2 = new float[] { 1, 0, heightBottomRight[0] - heightBottomLeft[0] };

				// First Triangle
				gl.glNormal3fv(getNormal(vec1, vec2), 0);

				gl.glColor3d(colorsTopLeft[0] / 255.0f, colorsTopLeft[1] / 255.0f, colorsTopLeft[2] / 255.0f);
				gl.glVertex3d(row, col, heightTopLeft[0]);

				gl.glColor3d(colorsBottomLeft[0] / 255.0f, colorsBottomLeft[1] / 255.0f, colorsBottomLeft[2] / 255.0f);
				gl.glVertex3d(row + 1, col, heightBottomLeft[0]);

				gl.glColor3d(colorsBottomRight[0] / 255.0f, colorsBottomRight[1] / 255.0f,
						colorsBottomRight[2] / 255.0f);
				gl.glVertex3d(row + 1, col + 1, heightBottomRight[0]);

				float[] vec3 = new float[] { 0, -1, heightBottomRight[0] - heightTopRight[0] };
				float[] vec4 = new float[] { -1, -1, heightBottomRight[0] - heightTopLeft[0] };

				// Second Triangle=
				gl.glNormal3fv(getNormal(vec3, vec4), 0);

				gl.glColor3d(colorsBottomRight[0] / 255.0f, colorsBottomRight[1] / 255.0f,
						colorsBottomRight[2] / 255.0f);
				gl.glVertex3d(row + 1, col + 1, heightBottomRight[0]);

				gl.glColor3d(colorsTopRight[0] / 255.0f, colorsTopRight[1] / 255.0f, colorsTopRight[2] / 255.0f);
				gl.glVertex3d(row, col + 1, heightTopRight[0]);

				gl.glColor3d(colorsTopLeft[0] / 255.0f, colorsTopLeft[1] / 255.0f, colorsTopLeft[2] / 255.0f);
				gl.glVertex3d(row, col, heightTopLeft[0]);

			}
		}

		gl.glEnd();

		gl.glEndList();
	}

	float[] getNormal(float[] vec2, float[] vec1) {
		float[] normal = new float[3];

		VectorUtil.crossVec3(normal, vec1, vec2);

		return normal;
	}
}
