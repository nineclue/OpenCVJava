import net.nineclue.opencv.OpenCVViewer;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC3;

public class MatTest2 {
    public static void main(String[] as) {
        System.out.println("Hello, OpenCV");
        test0();
    }
    static void test0() {
        OpenCVViewer.setup();
        Mat image = Imgcodecs.imread("/Users/nineclue/Pictures/Image2.jpg");
        OpenCVViewer.view(image, "원본");

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        OpenCVViewer.view(gray, "흑백");

        ArrayList<Mat> ms = new ArrayList<>();
        ms.add(gray);
        MatOfInt cs = new MatOfInt(0);
        Mat hist = new Mat();
        MatOfInt hsize = new MatOfInt(256);
        MatOfFloat rs = new MatOfFloat(0.0f, 256.0f);
        Imgproc.calcHist(ms, cs, new Mat(), hist, hsize, rs);
        /*
        System.out.format("Histogram : %d %d\n", hist.cols(), hist.rows());
        for (int i=0; i<256; i++) {
            System.out.format("%d [%d]\n", i, (int)(hist.get(i, 0)[0]));
        }
        */
        Mat himage = histogram(hist, 1);
        OpenCVViewer.view(himage, "흑백 히스토그램");

        ArrayList<Mat> h3 = calcHist3(image);
        /*
        for (int i = 0; i<3; i++) {
            System.out.format("%d %d %d %d\n", h3.get(i).channels(), h3.get(i).rows(), h3.get(i).cols(), h3.get(i).type());
        }
        */
        OpenCVViewer.view(histogram3(h3, 1), "컬러 히스토그램");
    }
    static ArrayList<Mat> calcHist3(Mat m) {
        ArrayList<Mat> r = new ArrayList<>();
        ArrayList<Mat> cs = new ArrayList<>();
        ArrayList<Mat> ms = new ArrayList<>();
        Core.split(m, cs);
        for (int i=0; i<3; i++) {
            Mat h = new Mat();
            ms.clear();
            ms.add(cs.get(i));
            Imgproc.calcHist(ms, new MatOfInt(0), new Mat(), h, new MatOfInt(256),
                    new MatOfFloat(0, 256));
            r.add(h);
        }
        return r;
    }
    static Mat histogram(Mat h, int zoom) {
        int size = h.rows() * zoom;
        Mat r = new Mat(size, size, CV_8U, new Scalar(255));
        Core.MinMaxLocResult mm = Core.minMaxLoc(h);
        // System.out.format("size : %d min : %f  max : %f\n", size, mm.minVal, mm.maxVal);
        for (int i=0; i<256; i++) {
            // System.out.format("%d %d %f\n", i, (int)(h.get(i, 0)[0]), size - ((mm.maxVal - h.get(i, 0)[0]) / mm.maxVal) * 0.9 * size);
            Imgproc.line(r, new Point(i*zoom, size),
                    new Point(i*zoom, ((mm.maxVal - h.get(i, 0)[0]) / mm.maxVal) * size * 0.9 + size * 0.1),
                    new Scalar(50), zoom);
        }
        return r;
    }
    static Mat histogram3(ArrayList<Mat> hs, int zoom) {
        double alpha = 0.5;
        int size = hs.get(0).rows() * zoom;
        Mat r = new Mat(size, size, CV_8UC3, new Scalar(255, 255, 255));
        Scalar[] cs = new Scalar[] {new Scalar(255, 0, 0), new Scalar(0, 255, 0), new Scalar(0, 0, 255)};
        double max = 0;
        for (int i=0; i<3; i++) {
            Core.MinMaxLocResult mm = Core.minMaxLoc(hs.get(i));
            if (mm.maxVal > max) max = mm.maxVal;
        }
        for (int i=0; i<3; i++) {
            for (int j=0; j<256; j++) {
                int y = (int)(((max - hs.get(i).get(j, 0)[0]) / max) * size * 0.9 + size * 0.1);
                for (int k=y; k<size; k++) {
                    double[] v = r.get(k, j);
                    for (int l=0; l<2; l++) {
                        v[l] = cs[i].val[l] * alpha + (1 - alpha) * v[l];
                    }
                    r.put(k, j, v);
                }
            }
        }
        return r;
    }
}
