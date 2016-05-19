package cz.vutbr.fit.strade.components.opticdisc;

import cz.vutbr.fit.strade.opencv.Histogram;
import cz.vutbr.fit.strade.util.opencv.OpenCVUtil;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Lukas on 12/3/2015.
 */
public class OpticDiscRecognizer {

    private Mat greenChannelImage;
    private Mat redChannelImage;
    private Mat grayChannelImage;

    public OpticDiscRecognizer(Mat greenChannelImage, Mat redChannelImage, Mat grayChannelImage) {
        this.greenChannelImage = greenChannelImage;
        this.redChannelImage = redChannelImage;
        this.grayChannelImage = grayChannelImage;
    }

    public Mat getGreenChannelImage() {
        return greenChannelImage;
    }

    public void setGreenChannelImage(Mat greenChannelImage) {
        this.greenChannelImage = greenChannelImage;
    }

    public Mat getRedChannelImage() {
        return redChannelImage;
    }

    public void setRedChannelImage(Mat redChannelImage) {
        this.redChannelImage = redChannelImage;
    }

    public Mat getGrayChannelImage() {
        return grayChannelImage;
    }

    public void setGrayChannelImage(Mat grayChannelImage) {
        this.grayChannelImage = grayChannelImage;
    }

    /**
     *
     * @param input Gray level image
     * @return
     */
    public Mat segmentation(Mat input){
        Mat output = input.clone();

        //opperation gaussian filter followed by close
        output = OpenCVUtil.close(OpenCVUtil.gaussianFilter(input));

        //calc histogram
        Mat hist = Histogram.calcHistogram(output);

        //compute optimal threshhold
        Double sum = 0d;
        Double threshold = 0d;
        //iterate from highest intensity to the lowest
        for(int i = hist.rows()-1; i > 0; i--){
            sum += hist.get(i, 0)[0];

            if (sum > Histogram.PIXEL_LIMIT) {
                threshold = (255d - ((255/Histogram.BINS)*i))/2;
                break;
            }
        }

        //Threshold image
        Imgproc.threshold(output, output, threshold, 255d, output.type());

        return output;

    }
}
