package cz.vutbr.fit.strade.components.diseases.druse;

import cz.vutbr.fit.strade.model.ContourFeatures;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;

import java.util.Collection;

/**
 * Created by Lukas on 5/12/2016.
 */
public interface DruseDetector {

    /** Converts druse struct to matrix of druse values.
     *  @param      druse structure of druse
     *  @return     one line matrix of eleven double values
     */
    Mat druseToMat(ContourFeatures druse);

    /**      Creates structuring element in shape of hexagon where object image points are set to 255 and others are set to 0.
     *  @param     size size is the length of one hexagon edge
     *  @return    matrix of hexagonal element
     */
    Mat getHexagonalStructuringElement(int size);

    /**      Executes reconstruction of image by morfological operation while image before and after operation are not same.
     *  @param      mask of skipped image points, its non-zero elements indicate which matrix elements need to be processed
     *  @param      toReconstruct is matrix which is going to be processed
     *  @param      structureElem is matrix element by which morfological operation is computed
     *  @param      type of morfological operation: 0 = dilatation, other = erosion)
     *  @return     reconstructed image
     */
    Mat morfologicalReconstruction(Mat mask, Mat toReconstruct, Mat structureElem, int type);


    /**       Computes ratio of loaded image and images used to test functions.
     *  @param      original are dimensions of loaded image
     *  @param      pattern are default dimensions of test image
     *  @return     double value of ratio
     */
    double computeRatio(Size original, Size pattern); // default size 640x480

    /**       Compute correction of shades at image.
     *  @param      image to be processed
     *  @param      ratio is optional parameter, if not specified, new is computed by image parameter
     *  @return     matrix of shade corrected image
     */
    Mat shadeCorrection(Mat image, Double ratio); // default ration infinity

    /**       Computes standard local variation of matrix of input image.
     *  @param      image to be processed
     *  @param      mask of skipped image points, its non-zero elements indicate which matrix elements need to be processed
     *  @param      kernelSize define size of structuring element used for computing
     *  @param      max contains coordinates of point with the highest value of local variation
     *  @return     matrix of image local variation
     */
    Mat localVariation(Mat image, Mat mask, int kernelSize, Point max);

    /**      Finds retina image background mask by local variance of image or by contours of image
     *  @param      image to be processed
     *  @param      method - allowed values are BG_MASK_BY_CONTOURS = 0, BG_MASK_BY_LOCAL_VARIANCE = 1
     *  @param      reduction is optional parameter, define reduction of dimensions of foreground image
     *  @param      ratio is optional parameter, if not specified, new is computed by image parameter
     *  @return     matrix with same size as original image, zero element are background elements
     */
    Mat getBackgroundMask(Mat image, int method, int reduction, double ratio); // default ratio: infinity

    /**      Finds optic disc center by standard local variation.
     *  @param      image to be processed
     *  @param      subImageSize define size of sub-image, which is used for improving center position of optic disc by distance tranformation
     *  @param      ratio is optional parameter, if not specified, new is computed by image parameter
     *  @return     point which points to optic disc center
     */
    Point getOpticDiscCenter(Mat image, int subImageSize, double ratio); // default ratio: infinity

    /**      Finds optic disc contours by watershed transformation.
     *  @param      image to be processed
     *  @param      ratio is optional parameter, if not specified, new is computed by image parameter
     *  @return     vector of contours ( vector of points ) of optic disc
     */
    Collection<Collection<Point>> getOpticDiscContours(Mat image, double ratio); // default ratio: infinity

    /**       Creates mask of optic disc.
     * @param       image to be processed
     * @param       reduction is optional parameter, define reduction of dimensions of foreground image
     * @param       ratio is optional parameter, if not specified, new is computed by image parameter
     * @return      matrix with same size as original image, zero element are optic disc elements
     */
    Mat getOpticDiscMask(Mat image, int reduction, double ratio); // default ratio: infinity

    /**      Computes histogram equalization of intensity channel.
     *  @param      image to be processed
     *  @return     image with equalized intensity
     */
    Mat equalizeIntensity(Mat image);

    /**      Computes adaptive histogram equalization of color image.
     *  @param      image to be processed
     *  @param      clipLimit is threshold for contrast limiting
     *  @param      tileGridSize is size of grid for histogram equalization
     */
    Mat clahe(Mat image, double clipLimit, Size tileGridSize);

    /**       Generates Gabor filter imaginary coefficients.
     * @param       ksize is size of the filter returned
     * @param       sigma is tandard deviation of the gaussian envelope
     * @param       theta is orientation of the normal to the parallel stripes of a Gabor function
     * @param       lambda is wavelength of the sinusoidal factor
     * @param       gamma is spatial aspect ratio
     * @param       psi is phase offset
     * @param       ktype is type of filter coefficients. It can be CV_32F or CV_64F
     * @return      generated matrix of gabor kernel
     */
    Mat getGaborKernelImaginary(Size ksize, double sigma, double theta, double lambda, double gamma, double psi, int ktype);  //default ktype  CV_64F

    /**       Finds exudates contours in retina image.
     * @param       image to be processed
     * @param       opticDiscMask is mask of pixels where is optic disc, if it is empty ( all elemets of matrix are zeros), new one is computed in image
     * @param       ratio is optional parameter, if not specified, new is computed by image parameter
     * @return      vector of contours ( vector of points ) of exudates
     */
    Collection<Collection<Point>> getExudatesContours(Mat image, Mat opticDiscMask, double ratio);// default ratio: infinity

    /**       Finds contours of drusen in image of retina.
     * @param       image to be processed
     * @param       ratio is optional parameter, if not specified, new is computed by image parameter
     * @return      vector of contours ( vector of points ) of drusen
     */
    Collection<Collection<Point>> getDrusenContours(Mat image, double ratio);// default ratio: infinity

    /**       Computes entropy of image by histogram equalization.
     * @param       image to be processed
     * @param       histMax is maximum value of histogram
     * @param       mask of skipped image points, its non-zero elements indicate which matrix elements need to be processed
     * @return      entropy of image
     */
    float computeEntropy(Mat image, int histMax, Mat mask);

    /**       Finds index of contour in which the point is.
     * @param       contours is pointer to array of elements contours
     * @param       dimensions are width and height of image from which contours are
     * @param       position is point in image, if out of contours
     * @return      index of contour in array of all contoursor -1 if point is not in any of them
     */
    int inContours(Collection<Collection<Point>> contours, Size dimensions, Point position);

    /**       Handles user inputs while learning process is active.
     * @param       event is type of event, i.e. EVENT_LBUTTONDOWN, EVENT_RBUTTONDOWN ...
     * @param       x position in image
     * @param       y position in image
     * @param       data is pointer to structure which contain parameters of function, must be of CallBackParams type
     */
    void CallBackFunc(int event, int x, int y, Object params);

    /**       Computes features of contours, i.e. area, compactness, average boundary intensity ...
     * @param       contours is pointer to array of elements contours
     * @param       image, in which contours were found
     * @param       index of contour in array
     * @return      structure of features
     */
    ContourFeatures getContourFeature(Collection<Collection<Point>> contours, Mat image, int index, double ratio);// default ratio: infinity

    /**       Computes features of selected contours from image and save it to file.
     * @param       output is path to file
     * @param       image, in which objects for selection are found
     * @param       type of object, allowed values are LEARN_DRUSE = 0, LEARN_EXUDATE = 1, LEARN_HEMORRHAGE = 2
     */
    void learn(String output, Mat image, int type);

    /**       Computes first eccentricity of ellipse.
     * @param       rectangle which describes ellipse
     * @return      double value of eccentricity
     */
    double getEccentricity(RotatedRect rectangle);

    /**       Finds ellipse, which describes macula in retina.
     * @param       image to be processed
     * @param       ratio is optional parameter, if not specified, new is computed by image parameter
     * @return      rotated rectangle which describe macula ellipse or zero-sized rectangle if no macula is found
     */
    RotatedRect getMaculaEllipse(Mat image, double ratio);// default ratio: infinity

    /**        Creates mask of macula.
     * @param       image to be processed
     * @param       reduction is optional parameter, define reduction of dimensions of foreground image
     * @param       ratio is optional parameter, if not specified, new is computed by image parameter
     * @return      matrix with same size as original image, zero element are macula elements
     */
    Mat getMaculaMask(Mat image, int reduction, double ratio);// default ratio: infinity
}