package cz.vutbr.fit.strade.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lukas on 12/17/2015.
 */
public class Histogram {

    public static final int BINS = 25;
    public static final int PIXEL_LIMIT = 10000;

    /**
     * Computes histogram of grayscale image
     * @param src
     */
    public static Mat calcHistogram(Mat src){

        Mat histogram = new Mat();                 // destination hist
        MatOfFloat range = new MatOfFloat(0,256);  // range (0 - 255)
        MatOfInt histSize = new MatOfInt(BINS);    // # of bins
        MatOfInt channels = new MatOfInt(0);       // channels - grayImage = 0
        Mat mask = new Mat();                      //

        List<Mat> sourceImages = new ArrayList<Mat>();
        sourceImages.add(src);

        Imgproc.calcHist(
                sourceImages, channels, mask, histogram, histSize, range, false
        );

//        Core.normalize(histogram, histogram);

        return histogram;
    }


}
