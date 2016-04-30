package com.example.grimones.testopencv;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    //static{ System.loadLibrary("opencv_java3"); }

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat Findimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        try {
            Findimage = Utils.loadResource(getApplicationContext(), R.drawable.earth_spirit_full);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Findimage.convertTo(Findimage, CvType.CV_8U);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initDebug();
        mOpenCvCameraView.enableView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat input = inputFrame.rgba();
        Mat gray = new Mat();
        Imgproc.cvtColor(input, gray, Imgproc.COLOR_RGBA2GRAY);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(15,15));
        //Imgproc.threshold(gray,gray,0, 255, Imgproc.THRESH_BINARY);

        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);
        Imgproc.morphologyEx(gray, gray, Imgproc.MORPH_CLOSE, kernel);
        Core.normalize(gray, gray, 0, 255, Core.NORM_MINMAX);
        Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 15, 15);

        //Imgproc.threshold(gray,gray,50,255,Imgproc.THRESH_BINARY);
        //Imgproc.Canny(gray, gray, 50, 50);
        Mat result = gray;
        Imgproc.resize(result, result, new Size(1088, 1088));
        return result;
    }


    public Mat warp(Mat inputMat,Mat startM) {
        int resultWidth = 1000;
        int resultHeight = 1000;

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);



        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(0, resultHeight);
        Point ocvPOut3 = new Point(resultWidth, resultHeight);
        Point ocvPOut4 = new Point(resultWidth, 0);
        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat,
                outputMat,
                perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_CUBIC);

        return outputMat;
    }

    /*
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat input = inputFrame.rgba();
        Log.d("SHIT", "Conevert input");
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGBA2RGB);
        //input.convertTo(input, CvType.CV_8UC3);


        Log.d("SHIT", "Create detectors");
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.AKAZE);
        DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        ///////////////

        Log.d("SHIT", "Descriptor 1");
        Mat descriptors1 = new Mat();
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();

        Log.d("SHIT", "Detect points input");
        detector.detect(input, keypoints1);
        descriptor.compute(input, keypoints1, descriptors1);

        ////////////////
        Log.d("SHIT", "Descriptor 2");
        Mat descriptors2 = new Mat();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();

        Log.d("SHIT", "Detect points findimage");
        detector.detect(Findimage, keypoints2);
        descriptor.compute(Findimage, keypoints2, descriptors2);

        ////////////////
        Log.d("SHIT", "Start of mathching");
        MatOfDMatch matches = new MatOfDMatch();
        if (descriptors1.type() == descriptors2.type() &&
                descriptors1.cols() == descriptors2.cols()) {
            matcher.match(descriptors1, descriptors2, matches);
            Log.d("SHIT", "MATCHING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            int DIST_LIMIT = 80;
            List<DMatch> matchesList = matches.toList();
            List<DMatch> matches_final = new ArrayList<DMatch>();
            for (int i = 0; i < matchesList.size(); i++) {
                if (matchesList.get(i).distance <= DIST_LIMIT) {
                    matches_final.add(matches.toList().get(i));
                }
            }

            MatOfDMatch matches_final_mat = new MatOfDMatch();
            matches_final_mat.fromList(matches_final);

//            Scalar kpColor = new Scalar(255, 159, 10);
//            Features2d.drawKeypoints(input, keypoints1, outputImg, kpColor, 0);

            Scalar RED = new Scalar(255,0,0);
            Scalar GREEN = new Scalar(0,255,0);
            MatOfByte drawnMatches = new MatOfByte();
            Mat outputImg = input.clone();
            Features2d.drawMatches(input, keypoints1, Findimage, keypoints2, matches_final_mat, outputImg, GREEN, RED,  drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);

            Imgproc.resize(outputImg, outputImg, new Size(1088, 1088));
            return outputImg;
        } else

        {
            Log.d("SHIT", "NOT MATCHING");
        }

        ////////////////

        /////////////////

        /////////////////


        return null;
    }
    */
}
