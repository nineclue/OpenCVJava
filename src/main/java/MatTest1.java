import javafx.scene.Camera;
import net.nineclue.opencv.OpenCVViewer;
import net.nineclue.opencv.OpenCVFramesViewer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.ArrayList;

import static java.lang.Math.abs;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.*;

public class MatTest1 {
    static public void main(String[] as) {
        System.out.println("Hello, OpenCV");
        test4();
    }
    static private void test0() {
        OpenCVViewer.setup();
        Mat image1 = Imgcodecs.imread("/Users/nineclue/Pictures/Image1.jpg");
        OpenCVViewer.view(image1);
        Scalar tcolor = new Scalar(230, 190, 130);
        int t = 200;
        Mat image2 = cdetect(image1, tcolor, t);
        OpenCVViewer.view(image2);
        Mat image3 = cvcdetect(image1, tcolor, t);
        OpenCVViewer.view(image3);
    }
    static private Mat cdetect(Mat m, Scalar target, int threshold) {
        Mat result = new Mat(m.rows(), m.cols(), CvType.CV_8U);
        int buffSize = (int)(m.total() * m.elemSize());
        byte[] data = new byte [buffSize];
        byte[] rdata = new byte[(int)m.total()];
        m.get(0, 0, data);
        for (int i=0; i<m.total(); i++) {
            int dpos = i * 3;
            int diff = (int)(abs(data[dpos] - target.val[0]) + abs(data[dpos+1] - target.val[1]) + abs(data[dpos+2] - target.val[2]));
            if (diff < threshold)
                rdata[i] = (byte)255;
            else
                rdata[i] = (byte)0;
        }
        result.put(0, 0, rdata);
        return result;
    }
    static private Mat cvcdetect(Mat m, Scalar target, int threshold) {
        Mat result = new Mat();
        Core.absdiff(m, target, result);
        ArrayList<Mat> ms = new ArrayList<Mat>();
        Core.split(result, ms);
        Core.add(ms.get(0), ms.get(1), result);
        Core.add(result, ms.get(2), result);
        Imgproc.threshold(result, result, threshold, 255, Imgproc.THRESH_BINARY);
        return result;
    }
    static private void test1() {
        OpenCVViewer.setup();
        Mat image1 = Imgcodecs.imread("/Users/nineclue/Pictures/Image2.jpg");
        OpenCVViewer.view(image1);

        Mat hsv = new Mat();
        Imgproc.cvtColor(image1, hsv, COLOR_BGR2HSV);
        ArrayList<Mat> ms = new ArrayList<Mat>();
        Core.split(hsv, ms);
        OpenCVViewer.view(ms.get(0), "Hue");
        OpenCVViewer.view(ms.get(1), "Saturation");
        OpenCVViewer.view(ms.get(2), "Value");
    }
    static private void test2() {
        OpenCVViewer.setup();
        Mat hsv = new Mat(128, 360, CV_8UC3);
        for (int h = 0; h<360; h++) {
            for (int s=0; s<128; s++) {
                hsv.put(s, h, new byte[] {(byte)(h/2), (byte)(255-s*2), (byte)255});
            }
        }
        Mat bgr = new Mat();
        Imgproc.cvtColor(hsv, bgr, COLOR_HSV2BGR);
        OpenCVViewer.view(bgr);
    }
    static private void test3() {
        OpenCVViewer.setup();
        Mat image = Imgcodecs.imread("/Users/nineclue/Pictures/Image2.jpg");
        OpenCVViewer.view(image, "original");
        Imgproc.cvtColor(image, image, COLOR_BGR2HSV);
        ArrayList<Mat> ms = new ArrayList<Mat>();
        Core.split(image, ms);
        ms.get(2).setTo(new Scalar(255));
        Core.merge(ms, image);
        Imgproc.cvtColor(image, image, COLOR_HSV2BGR);
        OpenCVViewer.view(image, "Maximum value");
    }
    public static class CameraException extends RuntimeException {}
    static VideoCapture camera;
    static Mat camMat, hsvMat;
    static ArrayList<Mat> ms;
    static Mat feedCamera() throws CameraException {
        double minHue = 160, maxHue = 10;
        double minSat = 25, maxSat = 166;
        camera.read(camMat);
        Imgproc.cvtColor(camMat, hsvMat, COLOR_BGR2HSV);
        Core.split(hsvMat, ms);

        Mat mask1 = new Mat();
        Mat mask2 = new Mat();
        Imgproc.threshold(ms.get(0), mask1, maxHue, 255, THRESH_BINARY_INV);
        Imgproc.threshold(ms.get(0), mask2, minHue, 255, THRESH_BINARY);
        Mat hueMask = new Mat();
        if (minHue < maxHue) Core.bitwise_and(mask1, mask2, hueMask);
        else Core.bitwise_or(mask1, mask2, hueMask);

        Mat satMask = new Mat();
        Core.inRange(ms.get(1), new Scalar(minSat), new Scalar(maxSat), satMask);

        Mat mask = new Mat();
        Core.bitwise_and(hueMask, satMask, mask);

        Mat detected = new Mat(camMat.size(), CV_8UC3, new Scalar(0, 0, 0));
        camMat.copyTo(detected, mask);
        return detected;
    }
    static private void test4() throws CameraException{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        camera = new VideoCapture(0);
        int w = (int)camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int h = (int)camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        camMat = new Mat(h, w, CvType.CV_8UC3);
        hsvMat = new Mat(h, w, CvType.CV_8UC3);
        ms = new ArrayList<>();
        OpenCVFramesViewer.setup(w, h, true);
        OpenCVFramesViewer.apply(MatTest1::feedCamera);
    }

}
