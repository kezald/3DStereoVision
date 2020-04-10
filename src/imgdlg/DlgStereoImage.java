package imgdlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.calib3d.StereoBM;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class DlgStereoImage extends JDialog {
	DlgStereoImage self;
	JLabel jLabelDisparityValue;
	JSlider jSliderDisparity;
	JLabel jLabelBlockSize;
	JSlider jSliderBlockSize;
	JLabel jLabelMinDisparity;
	JSlider jSliderMinDisparity;
	JLabel jLabelNumDisparities;
	JSlider jSliderNumDisparities;
	JLabel jLabelSpeckleWindowSize;
	JSlider jSliderSpeckleWindowSize;
	JLabel jLabelSpeckleRange;
	JSlider jSliderSpeckleRange;

	private MyMenuBar myMenuBar;

	JPanel jPanelTop;
	JPanel jPanelDisparityValue;
	JPanel jPanelCanvas;

	ImageIcon imageIconOriginal;
	JLabel jLabelOriginal;
	// JPanel jPanelPhotoFixer;
	// JScrollPane jScrollPanePhoto;

	public JButton jButtonApply;

	ImageIcon imageIconDisparity;
	JLabel jLabelDisparity;
	// JPanel jPanelDisparity;
	// JScrollPane jScrollPaneDisparity;

	Mat matrix;
	int blockSize;
	int numDisparities;
	public BufferedImage imageGray;
	public Mat imgL;

	public DlgStereoImage(Frame parent) {
		super(parent, false);
		self = this;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setTitle("Timer Controller");

		initUI();
		attachHandlers();
		loadLayout();

		pack();
		setLocationRelativeTo(parent);
		setSize(800, 800);
		setVisible(true);
	}

	private void initUI() {
		myMenuBar = new MyMenuBar();
		this.setJMenuBar(myMenuBar);

		jPanelTop = new JPanel(new BorderLayout());
		jPanelCanvas = new javax.swing.JPanel(new GridBagLayout());
		jPanelCanvas.setBackground(new Color(0, 0, 255));

		jButtonApply = new JButton("Reconstruct 3D");

		jPanelDisparityValue = new JPanel(new GridBagLayout());

		jLabelOriginal = new JLabel();
		// jPanelPhotoFixer = new JPanel(new GridBagLayout());
		// jPanelPhotoFixer.add(jLabelOriginal);
		// jScrollPanePhoto = new JScrollPane(jPanelPhotoFixer);

		jLabelDisparity = new JLabel();
		jLabelBlockSize = new JLabel("Block Size:");
		jLabelMinDisparity = new JLabel("MinDisparity");
		// jPanelDisparity = new JPanel(new GridBagLayout());
		// jPanelDisparity.add(jLabelDisparity);
		// jScrollPaneDisparity = new JScrollPane(jPanelDisparity);

		jLabelDisparityValue = new JLabel("Image Shift: ");
		jSliderDisparity = new JSlider();
		jSliderDisparity.setMinimum(0);
		jSliderDisparity.setMaximum(1000);

		jSliderBlockSize = new JSlider();
		jSliderBlockSize.setMinimum(7);
		jSliderBlockSize.setMaximum(254);
		jSliderBlockSize.setValue(7);

		jSliderMinDisparity = new JSlider();
		jSliderMinDisparity.setMinimum(0);
		jSliderMinDisparity.setMaximum(1000);
		jSliderMinDisparity.setValue(0);

		jLabelNumDisparities = new JLabel("Num Disparities: ");
		jSliderNumDisparities = new JSlider();
		jSliderNumDisparities.setMinimum(1);
		jSliderNumDisparities.setMaximum(10);
		jSliderNumDisparities.setValue(2);
		jSliderNumDisparities.setMajorTickSpacing(1);

		jLabelSpeckleWindowSize = new JLabel("Speckle Window Size: ");
		jSliderSpeckleWindowSize = new JSlider();
		jSliderSpeckleWindowSize.setMinimum(0);
		jSliderSpeckleWindowSize.setMaximum(100);
		jSliderSpeckleWindowSize.setValue(0);

		jLabelSpeckleRange = new JLabel("Speckle Range:");
		jSliderSpeckleRange = new JSlider();
		jSliderSpeckleRange.setMinimum(1);
		jSliderSpeckleRange.setMaximum(100);
		jSliderSpeckleRange.setValue(1);
	}

	private void attachHandlers() {
		myMenuBar.menuItemChooseImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				File imageFile = browseImagesDirectory(self);

				Imgcodecs imageCodecs = new Imgcodecs();

				// Reading the Image from the file
				if (imageFile.exists()) {

					matrix = imageCodecs.imread(imageFile.getAbsolutePath());
					if (imageFile != null) {
						try {
							// BufferedImage img = ImageIO.read(imageFile);
							Image img = HighGui.toBufferedImage(matrix);
							Image scaledImage = img.getScaledInstance(jPanelCanvas.getWidth() / 2,
									jPanelCanvas.getHeight(), Image.SCALE_DEFAULT);

							jLabelOriginal.setIcon(null);
							imageIconOriginal = new ImageIcon(scaledImage);
							jLabelOriginal.setIcon(imageIconOriginal);
							jLabelOriginal.setText("");

							jSliderDisparity.setMaximum(matrix.width() - numDisparities - blockSize);
						} catch (Exception ex) {
							jLabelOriginal.setText("Cannot read file");
						}
					}
				}
			}
		});

		jSliderBlockSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				jLabelBlockSize.setText("BlockSize: " + blockSize);
				updateDisparityMap();
			}
		});

		jSliderMinDisparity.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				jLabelMinDisparity.setText("MinDisparity: " + jSliderMinDisparity.getValue());
				updateDisparityMap();
			}
		});

		jSliderDisparity.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				jLabelDisparityValue.setText("Image Shift: " + jSliderDisparity.getValue());
				updateDisparityMap();
			}
		});

		jSliderNumDisparities.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				jLabelNumDisparities.setText("Num Disparities: " + numDisparities);
				updateDisparityMap();
			}
		});

		jSliderSpeckleRange.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				jLabelSpeckleRange.setText("SpeckleRange: " + jSliderSpeckleRange.getValue());
				updateDisparityMap();
			}
		});

		jSliderSpeckleWindowSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				jLabelSpeckleWindowSize.setText("SpeckleWindowSize: " + jSliderSpeckleWindowSize.getValue());
				updateDisparityMap();
			}
		});
	}

	private void updateDisparityMap() {
		if (matrix != null) {
			int disparity = jSliderDisparity.getValue();

			numDisparities = jSliderNumDisparities.getValue() * 16; // (must be positive and divisble by 16)
			blockSize = jSliderBlockSize.getValue(); // default 15 (SADWindowSize must be odd, be within 5..255)
			if (blockSize % 2 == 0) {
				blockSize -= 1;
			}
			StereoBM stereo = StereoBM.create(numDisparities, blockSize);
			stereo.setMinDisparity(jSliderMinDisparity.getValue());
			stereo.setSpeckleRange(jSliderSpeckleRange.getValue());
			stereo.setSpeckleWindowSize(jSliderSpeckleWindowSize.getValue());

			Rect rectCropLeft = new Rect(0, 0, matrix.width() - disparity, matrix.height());
			Rect rectCropRight = new Rect(disparity, 0, matrix.width() - disparity, matrix.height());

			imgL = new Mat(matrix, rectCropLeft);
			Mat imgR = new Mat(matrix, rectCropRight);

			Mat imgLGray = new Mat(imgL.height(), imgL.width(), CvType.CV_8UC1);
			Imgproc.cvtColor(imgL, imgLGray, Imgproc.COLOR_RGB2GRAY);

			Mat imgRGray = new Mat(imgR.height(), imgR.width(), CvType.CV_8UC1);
			Imgproc.cvtColor(imgR, imgRGray, Imgproc.COLOR_RGB2GRAY);

			Mat disparityMapGray = new Mat();
			stereo.compute(imgLGray, imgRGray, disparityMapGray);

			short[] data1 = new short[disparityMapGray.rows() * disparityMapGray.cols()];
			disparityMapGray.get(0, 0, data1);

			for (int i = 0; i < data1.length; i++) {
				data1[i] = (short) ((data1[i] + 16) * 16);
			}

			imageGray = new BufferedImage(disparityMapGray.cols(), disparityMapGray.rows(),
					BufferedImage.TYPE_USHORT_GRAY);
			imageGray.getRaster().setDataElements(0, 0, disparityMapGray.cols(), disparityMapGray.rows(), data1);

			Image scaledImage = imageGray.getScaledInstance(jPanelCanvas.getWidth() / 2, jPanelCanvas.getHeight(),
					Image.SCALE_DEFAULT);

			imageIconDisparity = new ImageIcon(scaledImage);
			jLabelDisparity.setIcon(imageIconDisparity);
			jLabelDisparity.setText("");
		}
	}

	private void loadLayout() {
		int anchor = GridBagConstraints.CENTER;
		int fill = GridBagConstraints.HORIZONTAL;
		Insets insets = new Insets(5, 5, 0, 5);
		Insets insets2 = new Insets(2, 5, 5, 5);

		jPanelDisparityValue.add(jLabelDisparityValue,
				new GridBagConstraints(0, 0, 1, 1, 1.0d, 0.0d, anchor, fill, insets, 0, 0));
		jPanelDisparityValue.add(jSliderDisparity,
				new GridBagConstraints(0, 1, 1, 1, 1.0d, 0.0d, anchor, fill, insets2, 0, 0));
		jPanelDisparityValue.add(jLabelBlockSize,
				new GridBagConstraints(0, 2, 1, 1, 1.0d, 0.0d, anchor, fill, insets, 0, 0));
		jPanelDisparityValue.add(jSliderBlockSize,
				new GridBagConstraints(0, 3, 1, 1, 1.0d, 0.0d, anchor, fill, insets2, 0, 0));
		jPanelDisparityValue.add(jLabelMinDisparity,
				new GridBagConstraints(0, 4, 1, 1, 1.0d, 0.0d, anchor, fill, insets, 0, 0));
		jPanelDisparityValue.add(jSliderMinDisparity,
				new GridBagConstraints(0, 5, 1, 1, 1.0d, 0.0d, anchor, fill, insets2, 0, 0));
		jPanelDisparityValue.add(jLabelNumDisparities,
				new GridBagConstraints(0, 6, 1, 1, 1.0d, 0.0d, anchor, fill, insets, 0, 0));
		jPanelDisparityValue.add(jSliderNumDisparities,
				new GridBagConstraints(0, 7, 1, 1, 1.0d, 0.0d, anchor, fill, insets2, 0, 0));
		jPanelDisparityValue.add(jLabelSpeckleRange,
				new GridBagConstraints(0, 8, 1, 1, 1.0d, 0.0d, anchor, fill, insets, 0, 0));
		jPanelDisparityValue.add(jSliderSpeckleRange,
				new GridBagConstraints(0, 9, 1, 1, 1.0d, 0.0d, anchor, fill, insets2, 0, 0));
		jPanelDisparityValue.add(jLabelSpeckleWindowSize,
				new GridBagConstraints(0, 10, 1, 1, 1.0d, 0.0d, anchor, fill, insets, 0, 0));
		jPanelDisparityValue.add(jSliderSpeckleWindowSize,
				new GridBagConstraints(0, 11, 1, 1, 1.0d, 0.0d, anchor, fill, insets2, 0, 0));
		
		jPanelDisparityValue.add(jButtonApply,
				new GridBagConstraints(0, 12, 1, 1, 1.0d, 0.0d, anchor, fill, insets2, 0, 0));

		jPanelCanvas.add(jLabelOriginal, new GridBagConstraints(0, 0, 1, 1, 1.0d, 1.0d, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanelCanvas.add(jLabelDisparity, new GridBagConstraints(1, 0, 1, 1, 1.0d, 1.0d, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		jPanelTop.add(jPanelCanvas, BorderLayout.CENTER);
		jPanelTop.add(jPanelDisparityValue, BorderLayout.SOUTH);
		getContentPane().add(jPanelTop);
	}

	public static final String DEFAULT_IMAGES_DIRECTORY_PATH = "images" + File.separator;
	private static JFileChooser fileChooserImages = new JFileChooser(DEFAULT_IMAGES_DIRECTORY_PATH);

	public void buildPath(String filePath) {
		File file = new File(filePath);
		file.mkdirs();
	}

	public File browseImagesDirectory(Component owner) {
		buildPath(DEFAULT_IMAGES_DIRECTORY_PATH);

		File selectedFile = null;
		int returnVal = fileChooserImages.showOpenDialog(owner);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooserImages.getSelectedFile();
		}

		return selectedFile;
	}
}
