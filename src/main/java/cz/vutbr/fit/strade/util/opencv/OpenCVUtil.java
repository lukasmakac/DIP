package cz.vutbr.fit.strade.util.opencv;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static org.opencv.imgproc.Imgproc.*;

/**
 * Created by Lukas on 12/3/2015.
 */
public class OpenCVUtil {

    public final static int IMG_TYPE_GRAY = 0;
    public final static int IMG_TYPE_RGB = 1;

    public final static int CHANNEL_RED = 2;
    public final static int CHANNEL_GREEN = 1;
    public final static int CHANNEL_BLUE = 0;

    public static Mat getChannel(Mat sourceImage, int channel){

        int rows = sourceImage.rows();
        int cols = sourceImage.cols();
        int type = sourceImage.type();

        if(sourceImage.channels() <= 1) return sourceImage;

        ArrayList<Mat> srcList = new ArrayList<>();
        srcList.add(sourceImage);

        ArrayList<Mat> destList = new ArrayList<>();
        destList.add(Mat.zeros(rows, cols, type));

        //BGRA - copy only green channel
        Core.mixChannels(srcList, destList, new MatOfInt(channel,channel));

        return destList.get(0);
    }

    /**
     *
     * @param input
     * @return
     */
    public static Mat gaussianFilter(Mat input){
        Mat output = input.clone();

        medianBlur(input, output, 9); //src, dst, size of matrix (9x9)

        return output;
    }


    /**
     *
     * @param input Gray level image
     * @return
     */
    public static Mat close(Mat input){
        Mat output = input.clone();
        Mat kernel = Mat.zeros(5, 5, input.type());

        morphologyEx(input, output, MORPH_CLOSE, kernel);

        return output;
    }

    public static Mat convertToGray(Mat input){
        Mat output = input.clone();

        cvtColor(input, output, Imgproc.COLOR_RGB2GRAY);

        return output;
    }

    /**
     *
     * @param input 32F image ( int )
     * @return
     */
    public static Point getBrightestPoint(Mat input) {
        // accept only char type matrices
        //CV_Assert(input.depth() == sizeof(float));

        final int channels = input.channels();
        Point p = new Point();	    //brightest point
        float max = 0;              //maximal value(brightest)

        //ROI : TODO - make more resistent against errors
        int y_start = (int)((input.size().height/2) - 150);
        int y_end = (int)((input.size().height/2) + 150);
        int x_start = (int)((input.size().width/2) - 150);
        int x_end = (int)((input.size().width/2) + 150);

        // assu
        float float_arr[] = new float[(int) (input.total() * input.channels())];
        input.get(0, 0, float_arr);

        switch(channels){
            case 1:
                for(int i = y_start; i < y_end; i++){
                    for(int j = x_start; j < x_end; j++){
                        if (max < input.get(i, j)[0]){ //found maximum value
                            p.y = i;
                            p.x = j;
                            max = (float) input.get(i, j)[0];
                        }
                    }
                }
                break;
            case 3:
                //we have 3-channels - so convert it to gray and search among this
                Mat dest = new Mat(input.rows(), input.cols(),input.type());
                cvtColor(input,dest, Imgproc.COLOR_BGR2GRAY); // convert

                for(int i = 0; i < dest.size().height; i++){
                    for(int j = 0; j < dest.size().width; j++){
                        if (max < dest.get(i, j)[0]){ //found maximum value
                            p.y = i;
                            p.x = j;
                            max = (float) dest.get(i, j)[0];
                        }
                    }
                }
                break;
        }

        return p;
    }



}
