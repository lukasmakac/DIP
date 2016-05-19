package cz.vutbr.fit.strade.model;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.Collection;

/**
 * Created by Lukas on 5/13/2016.
 */
public class CallbackParams {
    Collection<Collection<Point>> contours;
    Mat image;
    String windowName;
    Collection<Integer> selectedIndexes;
}
