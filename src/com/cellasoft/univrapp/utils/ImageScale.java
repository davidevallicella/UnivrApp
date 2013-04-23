package com.cellasoft.univrapp.utils;

import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Environment;
import android.util.Log;

/**
 * User: mlake Date: 12/9/11 Time: 5:54 PM
 */
public class ImageScale implements ImageTransformation {

	private int mMaxWidth;
	private int mMaxHeight;

	private String mFingerPrint;

	/**
	 * Scale the image proportionally to the bounds of the given max width and
	 * height. This works great when you're trying to use really large images
	 * for thumbnails. Rather than caching the full image, this transformation
	 * will be cached instead, making load times much faster.
	 * 
	 * @param maxWidth
	 * @param maxHeight
	 */

	public ImageScale(int maxWidth, int maxHeight) {
		mMaxWidth = maxWidth;
		mMaxHeight = maxHeight;
		mFingerPrint = ImageScale.class.getSimpleName() + maxWidth + "-"
				+ maxHeight;
	}

	@Override
	public Bitmap transform(Bitmap image) {

		//image = detectFaces(image);
//		int resultWidth = 0;
//		int resultHeight = 0;
//
//		if (image.getWidth() >= image.getHeight()
//				&& image.getWidth() > mMaxWidth) {
//			// we're wider than we are taller
//			resultWidth = mMaxWidth;
//			float ratio = (float) mMaxWidth / (float) image.getWidth();
//			resultHeight = Math.round(ratio * image.getHeight());
//		} else if (image.getHeight() > image.getWidth()
//				&& image.getHeight() > mMaxHeight) {
//			// we're wider than we are taller
//			resultHeight = mMaxHeight;
//			float ratio = (float) mMaxHeight / (float) image.getHeight();
//			resultWidth =  Math.round(ratio * image.getWidth());
//		}

		return Bitmap
				.createScaledBitmap(image, mMaxWidth, mMaxHeight, true);
	}

	@Override
	public String fingerprint() {
		return mFingerPrint;
	}

	private Bitmap detectFaces(Bitmap cameraBitmap) {
		Log.d("FACE_RECOGNITION", "CHECK");
		int width = cameraBitmap.getWidth();
		int height = cameraBitmap.getHeight();

		FaceDetector detector = new FaceDetector(width, height, 1);
		Face[] faces = new Face[1];

		Bitmap bitmap565 = Bitmap.createBitmap(width, height, Config.RGB_565);
		Paint ditherPaint = new Paint();
		Paint drawPaint = new Paint();

		ditherPaint.setDither(true);
		drawPaint.setColor(Color.RED);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeWidth(2);

		Canvas canvas = new Canvas();
		canvas.setBitmap(bitmap565);
		canvas.drawBitmap(cameraBitmap, 0, 0, ditherPaint);

		int facesFound = detector.findFaces(bitmap565, faces);
		PointF midPoint = new PointF();
		float eyeDistance = 0.0f;
		float confidence = 0.0f;

		Log.i("FaceDetector", "Number of faces found: " + facesFound);

		if (facesFound > 0) {
			for (int index = 0; index < facesFound; ++index) {
				faces[index].getMidPoint(midPoint);
				eyeDistance = faces[index].eyesDistance();
				confidence = faces[index].confidence();

				Log.i("FaceDetector", "Confidence: " + confidence
						+ ", Eye distance: " + eyeDistance + ", Mid Point: ("
						+ midPoint.x + ", " + midPoint.y + ")");

				canvas.drawRect((int) midPoint.x - eyeDistance,
						(int) midPoint.y - eyeDistance, (int) midPoint.x
								+ eyeDistance, (int) midPoint.y + eyeDistance,
						drawPaint);
			}
		}

		return bitmap565;

	}
}