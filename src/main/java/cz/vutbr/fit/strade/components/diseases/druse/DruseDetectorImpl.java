package cz.vutbr.fit.strade.components.diseases.druse;

import cz.vutbr.fit.strade.model.ContourFeatures;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Lukas on 5/12/2016.
 */
public class DruseDetectorImpl implements DruseDetector {



    private static final int MASK_CONTOURS_LINE_THICKNESS = 7;
    private static final int DEFAULT_OPTIC_DISC_SIZE = 60;
    private static final int S1 = 5;
    private static final int S2 = 10;
    private static final int KERNEL_BRANCH_SIZE = 5;

    private static final int MAX_CLUSTERING_ITERATIONS = 200;
    private static final int MAX_CLUSTERING_DISTANCE = 40;
    private static final double MAX_MACULA_ECCENTRICITY = 0.5;

    private static final int BG_MASK_BY_CONTOURS = 0;
    private static final int BG_MASK_BY_LOCAL_VARIANCE = 1;

    private static final int LEARN_DRUSE = 0;
    private static final int LEARN_EXUDATE = 1;
    private static final int LEARN_HEMORRHAGE = 2;

    @Override
    public Mat druseToMat(ContourFeatures druse) {
        if(druse != null){
            return new Mat(1, 12, CvType.CV_64FC1, new Scalar(druse.asDoubleData()));
        }else{
            return null;
        }
    }

    @Override
    public Mat getHexagonalStructuringElement(int size) {
        Mat elem = Mat.zeros(size * 2 - 1, size * 3 - 2, CvType.CV_8UC1);
        int oneCount = size, zeroCount = size - 1;

        for(int row = 0; row < elem.rows(); row++) {
            for (int j = 0; j < elem.cols(); ++j) {
                if(j >= zeroCount && j < zeroCount + oneCount){
                    elem.put(row, j, 255);
                } else {
                    elem.put(row, j, 0);
                }
            }
            if(row < size - 1){
                zeroCount--;
                oneCount+=2;
            } else {
                zeroCount++;
                oneCount-=2;
            }
        }
        return elem;
    }

    @Override
    public Mat morfologicalReconstruction(Mat mask, Mat toReconstruct, Mat structureElem, int type) {
        Mat m0, m1 = toReconstruct.clone();
        Mat ne = new Mat();
        do {
            m0 = m1.clone();
            if(type == 0){
                Imgproc.dilate(m0, m1, structureElem);
                Core.min(m1, mask, m1);
            } else {
                Imgproc.erode(m0, m1, structureElem);
                Core.max(m1, mask, m1);
            }
            Core.min(m1, mask, m1);
            Core.compare(m1, m0, ne, Core.CMP_NE);
        } while(Core.countNonZero(ne) != 0);
        return m1;
    }

    @Override
    public double computeRatio(Size original, Size pattern) {
        if(pattern == null) pattern = new Size(640d, 480d);
        return ((double)(original.height / pattern.height) + (double)(original.width / pattern.width)) / 2.0;
    }

    @Override
    public Mat shadeCorrection(Mat image, Double ratio) {
        Mat structure_elem;

        if(ratio == null) {
            ratio = computeRatio(new Size(image.cols(), image.rows()),null);
        }

        if(image.channels() > 1) {
            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        }

        int maxStructureSize =  15 * ratio.intValue();
        int structureSize = 1;

        while(structureSize < maxStructureSize){
            structure_elem = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(structureSize, structureSize));
            //morphological opening
            Imgproc.morphologyEx(image, image, Imgproc.MORPH_OPEN, structure_elem);
            //morphological closing
            Imgproc.morphologyEx(image, image, Imgproc.MORPH_CLOSE, structure_elem);
            //double structure size
            structureSize *= 2;
        }

        return image;
    }

    @Override
    public Mat localVariation(Mat image, Mat mask, int kernelSize, Point max) {

        // convert input image to grayscale
        if(mask.channels() == 3) {
            Imgproc.cvtColor(mask, mask, Imgproc.COLOR_BGR2GRAY);
        }

        // make borders to input image and image mask, to handle out of image dimension values when processing
        Mat imageBorder = new Mat();
        Core.copyMakeBorder(image, imageBorder, KERNEL_BRANCH_SIZE, KERNEL_BRANCH_SIZE, KERNEL_BRANCH_SIZE, KERNEL_BRANCH_SIZE, Core.BORDER_REFLECT);
        Mat maskBorder = new Mat();
        Core.copyMakeBorder(mask, maskBorder, KERNEL_BRANCH_SIZE, KERNEL_BRANCH_SIZE, KERNEL_BRANCH_SIZE, KERNEL_BRANCH_SIZE, Core.BORDER_CONSTANT);

        // prepare matrix for result of local variation
        Mat localVariationImage;
        localVariationImage = Mat.ones(image.rows(), image.cols(), CvType.CV_8UC1);

        double maxValue = 0;

        // for each point of each row is local variation computed
        for(int row = 0; row < (localVariationImage).rows(); row++) {

            // get lines of matrices
            Mat imagePtr = (localVariationImage).row(row);
            Mat maskPtr = (mask).row(row);

            for (int col = 0; col < (localVariationImage).cols(); col++) {

                // if this point has non-zero value in mask
                if(maskPtr != null && maskPtr.get(0,col)[0] != 0d){

                    // get kernel with actual point in the middle and predefined edge size
                    Mat imageKernel = new Mat(imageBorder, new Rect(col, row, kernelSize, kernelSize));
                    Mat maskKernel = new Mat(maskBorder, new Rect(col, row, kernelSize, kernelSize));

                    // get mean of kernel
                    MatOfDouble kernelMeanScalar = new MatOfDouble(0d);
                    MatOfDouble kernelStdDevScalar = new MatOfDouble(0d);
                    Core.meanStdDev(imageKernel, kernelMeanScalar, kernelStdDevScalar);

                    // convert kernel to one channel image
                    Mat imageKernelOneChannel = new Mat();
                    imageKernel.convertTo(imageKernelOneChannel, CvType.CV_8UC1);

                    // subtract mean value of all elements in kernel matrix
                    Core.subtract(imageKernelOneChannel, new Scalar(kernelMeanScalar.toArray()), imageKernelOneChannel);

                    // exponentiate all values in kernel matrix
                    Core.pow(imageKernelOneChannel, 2, imageKernelOneChannel);

                    // remove/reset all masked values in kernel matrix
                    Mat maskedImageKernel = new Mat();
                    imageKernelOneChannel.copyTo(maskedImageKernel, maskKernel);

                    // compute local variation of pixel
                    double localVariationOfPixel = Math.pow(kernelSize, 2) - 1.0;

                    double finalValue = (Core.sumElems(maskedImageKernel).val[0])/localVariationOfPixel;
                    imagePtr.put(row, col, finalValue);

                    // recompute max value position
                    if(max != null && maxValue < finalValue) {
                        max = new Point(col, row );
                        maxValue = finalValue;
                    }
                }
            }
        }
        return localVariationImage;
    }

    @Override
    public Mat getBackgroundMask(Mat image, int method, int reduction, double ratio) {
        if(method != BG_MASK_BY_CONTOURS && method != BG_MASK_BY_LOCAL_VARIANCE) {
            throw new IllegalArgumentException("Invalid 'method' property value. Enabled only BG_MASK_BY_CONTOURS = 0, BG_MASK_BY_LOCAL_VARIANCE = 1.");
        }

        if(ratio == 0d) {
            ratio = computeRatio(new Size(image.cols(), image.rows()), null);
        }

        if(image.channels() == 3){
            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        }

        Mat computedImage = new Mat();

        if( method == BG_MASK_BY_LOCAL_VARIANCE ) {
            // compute variance of image
            image.convertTo(image, CvType.CV_32F);
            Mat mu = new Mat();
            Imgproc.blur(image, mu, new Size(10, 10));

            Mat mu2 = new Mat();
            Imgproc.blur(image.mul(image), mu2, new Size(10, 10));

            Core.subtract(mu2, mu.mul(mu), image);
            Core.sqrt(image, image);

            Mat thresh = new Mat();
            Imgproc.threshold(image, thresh, 10, 255, Imgproc.THRESH_BINARY);
            thresh.convertTo(computedImage, CvType.CV_8UC1);

        } else if( method == BG_MASK_BY_CONTOURS){
            // apply canny edge detector
            Mat canny = new Mat();
            Imgproc.Canny(image, canny, 20, 60);

            // find the contours
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(canny, contours, null, 0, 1); //CV_RETR_EXTERNAL

            // draw contours to image, thickness depends on image size
            Mat result;

            result = Mat.zeros(image.rows(), image.cols(), CvType.CV_8UC3);
            for( int i = 0; i< contours.size(); i++ ) {
                Imgproc.drawContours( result, contours, i, new Scalar(255,255,255), (int)(3 * ratio));
            }
            Imgproc.cvtColor(result, computedImage, Imgproc.COLOR_BGR2GRAY);
        }

        // find the largest contour of image, largest is object of retina
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(computedImage, contours, null, 0, 1); //CV_RETR_EXTERNAL

        int largestArea = 0;
        int largestContourIndex = 0;
        for( int i = 0; i< contours.size(); i++ ) {
            //Find the area of contour
            double areaIndex = Imgproc.contourArea(contours.get(i), false);
            if(areaIndex > largestArea) {
                largestArea = (int) areaIndex;
                largestContourIndex =i;
            }
        }

        // fill contour and return it as black / white image
        Mat mask = new Mat(image.rows(), image.cols(), CvType.CV_8UC1, new Scalar(0, 0, 0));
        Imgproc.drawContours(mask, contours, largestContourIndex, new Scalar(255, 255, 255), (int) ((MASK_CONTOURS_LINE_THICKNESS + reduction) * ratio));

        Imgproc.floodFill(mask, mask, new Point(5, 5), new Scalar(255d, 255d, 255d) );
        Core.bitwise_not(mask,mask);

        return mask;
    }

    @Override
    public Point getOpticDiscCenter(Mat image, int subImageSize, double ratio) {
        throw new UnsupportedOperationException("Not Implemented yet");
    }

    @Override
    public Collection<Collection<Point>> getOpticDiscContours(Mat image, double ratio) {
        throw new UnsupportedOperationException("Not Implemented yet");
    }

    @Override
    public Mat getOpticDiscMask(Mat image, int reduction, double ratio) {
        throw new UnsupportedOperationException("Not Implemented yet");
    }

    @Override
    public Mat equalizeIntensity(Mat image) {
        return null;
    }

    @Override
    public Mat clahe(Mat image, double clipLimit, Size tileGridSize) {
        return null;
    }

    @Override
    public Mat getGaborKernelImaginary(Size ksize, double sigma, double theta, double lambda, double gamma, double psi, int ktype) {
        return null;
    }

    @Override
    public Collection<Collection<Point>> getExudatesContours(Mat image, Mat opticDiscMask, double ratio) {
        return null;
    }

    @Override
    public Collection<Collection<Point>> getDrusenContours(Mat image, double ratio) {
        return null;
    }

    @Override
    public float computeEntropy(Mat image, int histMax, Mat mask) {
        return 0;
    }

    @Override
    public int inContours(Collection<Collection<Point>> contours, Size dimensions, Point position) {
        return 0;
    }

    @Override
    public void CallBackFunc(int event, int x, int y, Object params) {

    }

    @Override
    public ContourFeatures getContourFeature(Collection<Collection<Point>> contours, Mat image, int index, double ratio) {
        return null;
    }

    @Override
    public void learn(String output, Mat image, int type) {

    }

    @Override
    public double getEccentricity(RotatedRect rectangle) {
        return 0;
    }

    @Override
    public RotatedRect getMaculaEllipse(Mat image, double ratio) {
        return null;
    }

    @Override
    public Mat getMaculaMask(Mat image, int reduction, double ratio) {
        return null;
    }


}
