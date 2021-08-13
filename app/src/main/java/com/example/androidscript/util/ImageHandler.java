package com.example.androidscript.util;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;

import java.util.Vector;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

public final class ImageHandler {
    private static Mat grayScale(Bitmap bitmap) {
        Mat tmp = new Mat();
        Utils.bitmapToMat(bitmap, tmp);
        Mat ret = new Mat();
        Imgproc.cvtColor(tmp, ret, Imgproc.COLOR_RGB2GRAY);
        return ret;
    }

    private static Mat toMat(Bitmap bitmap) {
        Mat ret = new Mat();
        Utils.bitmapToMat(bitmap, ret);
        return ret;
    }

    private static Mat featureExtraction(Mat grayBitmap) {//KeyPoint detection and extraction
        ORB orb = ORB.create(100000);
        MatOfKeyPoint keyPoint = new MatOfKeyPoint();
        orb.detect(grayBitmap, keyPoint);
        Mat ret = new Mat();
        orb.compute(grayBitmap, keyPoint, ret);
        return ret;
    }

    private static MatOfDMatch featureMatch(Mat screenDescriptor, Mat targetDescriptor) {
        try {
            MatOfDMatch ret = new MatOfDMatch();
            //BFMatcher matcher = new BFMatcher(BFMatcher.BRUTEFORCE_HAMMING, false);
            //matcher.knnMatch(descInput,descLocal,matchPointsList,2);
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
            matcher.match(screenDescriptor, targetDescriptor, ret);
            return ret;
        } catch (CvException ex) {
            DebugMessage.set("Matching failed " + ex.toString());
            return null;
        }
    }

    public static int matchPicture(Bitmap screenshot, Bitmap target) {
        if (screenshot == null || target == null) {
            return 0;
        }
        Mat sourceMat = grayScale(screenshot);
        Mat targetMat = grayScale(target);
        Mat screenDescriptor = featureExtraction(sourceMat); // Size:(totalFeatures, 32)
        Mat targetDescriptor = featureExtraction(targetMat);

        DMatch[] matchPoints = featureMatch(screenDescriptor, targetDescriptor).toArray();//Length: max(AFeatures,B)

        float minDistance = 0;
        int matchCount = 0;

        for (DMatch match : matchPoints) {
            if (minDistance > match.distance) {
                minDistance = match.distance;
            }
        }

        minDistance = max(2 * minDistance, 30.0f);

        for (int z = 0; z < screenDescriptor.rows(); z++) {
            if (matchPoints[z].distance <= minDistance) {
                matchCount++;
            }
        }

        DebugMessage.set("Match " + matchCount + " points");
        return matchCount;
    }

    public static boolean TestPictureContain(Bitmap screenshot, Bitmap target) {
        if (screenshot == null || target == null) {
            return false;
        }
        Mat sourceMat = grayScale(screenshot);
        Mat targetMat = grayScale(target);
        Mat screenDescriptor = featureExtraction(sourceMat); // Size:(totalFeatures, 32)
        Mat targetDescriptor = featureExtraction(targetMat);

        DMatch[] matchPoints = featureMatch(screenDescriptor, targetDescriptor).toArray();//Length: max(AFeatures,B)

        float minDistance = 0;
        int matchCount = 0;

        for (DMatch match : matchPoints) {
            if (minDistance > match.distance) {
                minDistance = match.distance;
            }
        }

        minDistance = max(2 * minDistance, 30.0f);

        for (int z = 0; z < screenDescriptor.rows(); z++) {
            if (matchPoints[z].distance <= minDistance) {
                matchCount++;
            }
        }

        DebugMessage.set("Match " + matchCount + " points " + screenDescriptor.width() + " " + screenDescriptor.height() + " " + targetDescriptor.width() + " " + targetDescriptor.height());
        return (3040.0 * 1440.0 / ScreenShot.getHeight() / ScreenShot.getWidth() * 3 * matchCount) >= min(screenDescriptor.height(), targetDescriptor.height());
    }

    public static Point findLocation(Bitmap screenshot, Bitmap target) {
        //        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Vector<Point> pos = new Vector<>();
        int[] methods = {Imgproc.TM_CCOEFF_NORMED,Imgproc.TM_CCOEFF,Imgproc.TM_SQDIFF_NORMED,Imgproc.TM_SQDIFF,Imgproc.TM_CCORR_NORMED,Imgproc.TM_CCORR};
        for (int method : methods){
            Mat image = toMat(screenshot);
            Mat template = toMat(target);
            Mat result = new Mat();
            Imgproc.matchTemplate(image, template, result, method);
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
            pos.add(mmr.maxLoc);
            DebugMessage.set("Threshold: " + mmr.maxLoc + " ," + mmr.maxVal);
            DebugMessage.set("Threshold: " + mmr.minLoc + " ," + mmr.minVal);
            DebugMessage.set("Total: " + result.width() + " " + result.height());
        }
//        DebugMessage.set("Target: " + result.get(2400,1140).toString());

        Point ret =  new Point(0,0);
        for(Point p : pos){
            ret.x += p.x;
            ret.y += p.y;
        }
        ret.x /= pos.size();
        ret.y /= pos.size();

        for(Point p : pos){
            if(Dis(p,ret) > max(target.getWidth(),target.getHeight()) / 2.0){
                throw new RuntimeException("Can't find target");
            }
        }
        return ret;
    }
    public static double Dis(Point a, Point b) {
        return sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    };
}
