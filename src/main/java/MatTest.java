import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import net.nineclue.opencv.OpenCVViewer;
import net.nineclue.opencv.OpenCVFramesViewer;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.Random;

import static java.lang.Math.sin;

public class MatTest {
    VideoCapture camera;
    Mat camMat;
    public static void main(String[] args) {
        System.out.println("Hello, OpenCV");
        MatTest t = new MatTest();
        t.test7();
    }
    void test1() {
        OpenCVViewer.setup();
        // Mat image1 = Imgcodecs.imread("/Users/nineclue/Pictures/Image2.jpg");
        // OpenCVViewer.view(image1);
        Mat image2 = Imgcodecs.imread("/Users/nineclue/Pictures/Image1.jpg");
        OpenCVViewer.view(image2);
    }
    public static class CameraException extends RuntimeException {}
    Mat feedCamera() throws CameraException {
        camera.read(camMat);
        if (!camera.isOpened()) {
            throw new CameraException();
        }
        return camMat;
    }
    void test2() throws CameraException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        camera = new VideoCapture(0);
        int w = (int)camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int h = (int)camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        camMat = new Mat(h, w, CvType.CV_8UC3);
        OpenCVFramesViewer.setup(w, h, true);
        OpenCVFramesViewer.apply(this::feedCamera);
    }
    void test3() {
        OpenCVViewer.setup();
        Mat image = Imgcodecs.imread("/Users/nineclue/Pictures/Image1.jpg");
        salt(image, 2000);
        OpenCVViewer.view(image);
    }
    private void salt(Mat m, int n) {
        Random r = new Random();
        for (int i=0; i<n; i++) {
            int x = r.nextInt(m.cols());
            int y = r.nextInt(m.rows());
            m.put(y, x, 255, 255, 255);
        }
    }
    void test4() {
        OpenCVViewer.setup();
        Mat image = Imgcodecs.imread("/Users/nineclue/Pictures/Image0.jpg");
        colorReduce(image, 128);
        OpenCVViewer.view(image);
    }
    private void colorReduce(Mat m, int div) {
        int size = (int)(m.total() * m.channels());
        byte[] data = new byte[size];
        m.get(0, 0, data);
        for (int i=0; i<size; i++) {
            data[i] = (byte)((data[i] + 128)/div*div + div/2 - 128);
        }
        m.put(0, 0, data);
    }
    void test5() {
        OpenCVViewer.setup();
        Mat image = Imgcodecs.imread("/Users/nineclue/Pictures/Image2.jpg");
        OpenCVViewer.view(image);
        sharpen2D(image);
        OpenCVViewer.view(image);
        image = Imgcodecs.imread("/Users/nineclue/Pictures/Image2.jpg");
        sharpen(image);
        OpenCVViewer.view(image);
    }
    private byte[] getByteBuffer(Mat m) {
        int size = (int)(m.total() * m.elemSize());
        byte[] data = new byte[size];
        m.get(0, 0, data);
        return data;
    }
    private int indexOf3channels(int x, int y, int cols, int channel) {
        int result;
        if (y == 0) result = x * 3 + channel;
        else result = ((y - 1) * cols + x) * 3 + channel;
        // System.out.format("%d %d %d %d => %d\n", x, y, cols, channel, result);
        // return ((y - 1) * cols + x) * 3 + channel;
        return result;
    }
    private int toInt(byte b) {
        if (b < 0)
            return b + 256;
        else
            return (int)b;
    }
    private byte toByte(int i) {
        /*
        if (i > 127)
            return (byte)(i - 256);
        else
            return (byte)(i);
            */
        return (byte)i;
    }
    private void sharpen(Mat m) {
        byte[] data = getByteBuffer(m);
        int cols = m.cols();
        int rows = m.rows();
        int channels = m.channels();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                for (int c = 0; c < channels; c++) {
                    int above, below, left, right;
                    if (y > 0) above = toInt(data[indexOf3channels(x, y-1, cols, c)]);
                    else above = 0;
                    if (y < rows - 1) below = toInt(data[indexOf3channels(x, y+1, cols, c)]);
                    else below = 0;
                    if (x > 0) left = toInt(data[indexOf3channels(x-1, y, cols, c)]);
                    else left = 0;
                    if (x < cols - 1) right = toInt(data[indexOf3channels(x+1, y, cols, c)]);
                    else right = 0;
                    int result = toInt(data[indexOf3channels(x, y, cols, c)]) * 5 - above - below - left - right;
                    if (result >= 256) result = 255;
                    if (result < 0) result = 0;
                    data[indexOf3channels(x, y, cols, c)] = toByte(result);
                }
            }
        }
        m.put(0, 0, data);
    }
    void sharpen2D(Mat m) {
        Mat kernel = new Mat(3, 3, CvType.CV_32F, new Scalar(0.0));
        kernel.put(1, 1, 5.0);
        kernel.put(0, 1, -1.0);
        kernel.put(2, 1, -1.0);
        kernel.put(1, 0, -1.0);
        kernel.put(1, 2, -1.0);

        Imgproc.filter2D(m, m, m.depth(), kernel);
    }
    void test6() {
        OpenCVViewer.setup();
        Mat image = Imgcodecs.imread("/Users/nineclue/Pictures/Image2.jpg");
        wave(image);
        OpenCVViewer.view(image);
    }
    void wave(Mat m) {
        Mat srcX = new Mat(m.rows(), m.cols(), CvType.CV_32F);
        Mat srcY = new Mat(m.rows(), m.cols(), CvType.CV_32F);

        for (int y=0; y<m.rows(); y++) {
            for (int x=0; x<m.cols(); x++) {
                srcX.put(y, x, x);
                srcY.put(y, x, y+5*sin(x/10.0));
            }
        }

        Imgproc.remap(m, m, srcX, srcY, Imgproc.INTER_LINEAR);
    }
    private boolean isRightSide(int i, int cols) {
        // int y = i / (cols * 3);
        int x = (i % (cols * 3)) / 3;
        return (x > (cols / 2));
    }
    Mat feedMyCamera() throws CameraException {
        camera.read(camMat);
        if (!camera.isOpened()) {
            throw new CameraException();
        }
        byte [] data = getByteBuffer(camMat);
        for (int i=0; i<data.length; i+=3) {
            if (isRightSide(i, camMat.cols())) {
                int avg = ((data[i] & 0xff) + (data[i+1] & 0xff) + (data[i+2] & 0xff)) / 3;
                data[i] = (byte) avg;
                data[i+1] = (byte) avg;
                data[i+2] = (byte) avg;
            }
        }
        camMat.put(0, 0, data);
        return camMat;
    }
    void test7() throws CameraException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        camera = new VideoCapture(0);
        int w = (int)camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int h = (int)camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        camMat = new Mat(h, w, CvType.CV_8UC3);
        OpenCVFramesViewer.setup(w, h, true);
        OpenCVFramesViewer.apply(this::feedMyCamera);
    }
}
