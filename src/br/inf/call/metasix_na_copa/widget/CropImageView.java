package br.inf.call.metasix_na_copa.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import br.inf.call.metasix_na_copa.R;
import br.inf.call.metasix_na_copa.activity.BuildBitmapActivity;

public class CropImageView extends ImageView {

	static final int NONE = 0;// No Action
	static final int DRAG = 1; // Action Dragging
	static final int ZOOM = 2; // Action Zooming
	static final int BIGGER = 3; // ZoomIn Image
	static final int SMALLER = 4; // ZoomOutImage
	private int mode = NONE; // Initial ImageView Mode as NoAction

	private float beforeLenght; // Distance of Two Finger points at the Begin
	private float afterLenght; // Distance of Two Finger points at the End
	private float scale = 0.04f; // Scale Factor

	private int screenW;// Area of Moving
	private int screenH;

	private Context context;

	private int start_x;// Start Touch Point x, y
	private int start_y;
	private int stop_x;// End Touch Point x, y
	private int stop_y;

	private Drawable drawable;
	private Matrix mNewMatrix; // NewUpdated ImageView Matrix
	private Bitmap mOriginImage; // Original ImageView Bitmap

	public CropImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CropImageView(Context context) {
		super(context);
	}

	public CropImageView(Context context, int w, int h){
		super(context);
		this.setPadding(0, 0, 0, 0);
		screenW = w;
		screenH = h;
	}

	/**
	 * Calculate distance between two finger points
	 * 
	 * @param event
	 * @return
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {// MotionEvent.ACTION_MASK is multiple touch
		case MotionEvent.ACTION_DOWN:
			mode = DRAG;
			// Get LastTime Stopped point x, y
			stop_x = (int) event.getRawX();
			stop_y = (int) event.getRawY();
			// Corresponding to the ImageView Left/Top point
			start_x = stop_x - this.getLeft();
			start_y = stop_y - this.getTop();

			if (event.getPointerCount() == 2){
				beforeLenght = spacing(event);
			}
			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			if (spacing(event) > 10f) {
				mode = ZOOM;
				beforeLenght = spacing(event);
			}
			break;

		case MotionEvent.ACTION_UP:
			// if current ImageView Height/Width is less the MINIMUN value, need to scale it until reach the MINIMUN
			while (getHeight() < 100 || getWidth() < 100) {
				setScale(scale, BIGGER);
			}
			while (getHeight() > 3000 || getWidth() > 3000) {
				setScale(scale, SMALLER);
			}
			mode = NONE;
			break;

		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;

		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				// Keep updating ImageView Position and Layout frame when Dragging
				this.setPosition(stop_x - start_x, stop_y - start_y, stop_x + this.getWidth() - start_x, stop_y- start_y + this.getHeight());
				stop_x = (int) event.getRawX();
				stop_y = (int) event.getRawY();

			}
			else if (mode == ZOOM) {
				if (spacing(event) > 10f) {
					afterLenght = spacing(event);
					float gapLenght = afterLenght - beforeLenght;
					if (gapLenght == 0) {
						break;
					}
					// If the distance of zoom has been changed 5 pix, and also the width of ImageView is larger than 70
					// Start the ZoomIn/Out
					else if (Math.abs(gapLenght) > 5f && getWidth() > 70) {
						if (gapLenght > 0) {
							this.setScale(scale, BIGGER);
						}
						else {
							this.setScale(scale, SMALLER);
						}
						beforeLenght = afterLenght; // Reassign current distance between two finger points
					}
				}
			}
			break;
		}
		invalidate();
		return true;
	}

	/**
	 * Set Current Rescaled Image Frame
	 * And reset ImageFrame
	 * @param temp
	 * @param flag
	 */
	private void setScale(float temp, int flag) {
		if (flag == BIGGER) {
			// ZoomIn/Enlarge ImageImage
			this.setFrame(this.getLeft() - (int) (temp * this.getWidth()),
					this.getTop() - (int) (temp * this.getHeight()), this.getRight() + (int) (temp * this.getWidth()),
					this.getBottom() + (int) (temp * this.getHeight()));
		}else if (flag == SMALLER) {
			// ZoomOut/Narrow ImageView
			this.setFrame(this.getLeft() + (int) (temp * this.getWidth()),
					this.getTop() + (int) (temp * this.getHeight()), this.getRight() - (int) (temp * this.getWidth()),
					this.getBottom() - (int) (temp * this.getHeight()));
		}
		mNewMatrix = this.getImageMatrix();		// Get New Updated Matrix of ImageView
	}

	/**
	 * Reset ImageView Position and Frame Layout
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	private void setPosition(int left, int top, int right, int bottom) {
		this.layout(left, top, right, bottom);
	}


	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	/**
	 * Get CropImage according to the frameView
	 * 
	 * @param v
	 * @return
	 */
	public Bitmap getCropImage(View frameView) {
		Bitmap finalBitmap = null;
		float[] cood = CropFrameView.getCropFrameCoordinates();

		mOriginImage = null; // Clear OriginImage
		mOriginImage = ((BitmapDrawable) this.getDrawable()).getBitmap(); // Get Origin Bitmap from ImageView

		// Each ImageView has 4 point-Left,Right,Top,Bottom, which the CropFrame also has 4 points
		// We don't want the ImageView is inside the CropFrame, which means the 4 points of the ImageView must be >=
		// CropFrame's 4 points, otherwise, we don't crop the ImageView
		int cropLeft = (int) cood[0] - this.getLeft();
		int cropTop = (int) cood[1] - this.getTop();
		int cropRight = (int) cood[2] - this.getRight();
		int cropBottom = (int) cood[3] - this.getBottom();

		if (cropLeft < 0 || cropTop < 0 || cropRight > 0 || cropBottom > 0){
			mOriginImage = null;
			mOriginImage = screenShot(getRootView());

			finalBitmap = Bitmap.createBitmap(mOriginImage, (int) cood[0], (int) cood[1]+getStatusBarHeight(), (int) cood[2] - (int) cood[0], (int) cood[3] - (int) cood[1]);

			Canvas canvas = new Canvas(finalBitmap);
			mOriginImage.recycle();
			
			//Nome Jogador
			TextPaint tp = new TextPaint(TextPaint.ANTI_ALIAS_FLAG); 
			tp.setColor(Color.BLACK);
			tp.setTextSize(30);
			StaticLayout sl = new StaticLayout(BuildBitmapActivity.getNomeJogador(), tp, canvas.getWidth()/3, Alignment.ALIGN_CENTER, 1f, 0f, false); 
			canvas.save();
			canvas.translate(canvas.getWidth()/3, canvas.getHeight()-55); //position text on the canvas
			sl.draw(canvas);
			//
			 		
		}else{
			finalBitmap = Bitmap.createBitmap(mOriginImage, 0, 0, mOriginImage.getWidth(), mOriginImage.getHeight(), mNewMatrix, true); // Recreate resized or transmitted Image with Matrix
			finalBitmap = Bitmap.createBitmap(finalBitmap, cropLeft, cropTop, (int) cood[2] - (int) cood[0], (int) cood[3] - (int) cood[1]); // Recreate cropped Bitmap according to the CropFrame and ImageViewPosition
			mOriginImage.recycle();
			
			//Moldura
			Canvas canvas = new Canvas(finalBitmap);
			drawable = getResources().getDrawable(R.drawable.brasil_album_m);
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			Rect src = new Rect(0, 0, finalBitmap.getWidth(), finalBitmap.getHeight());
			canvas.drawBitmap(bitmap, null, src, null);
			drawable.draw(canvas);
			//

			//Nome Jogador
			TextPaint tp = new TextPaint(TextPaint.ANTI_ALIAS_FLAG); 
			tp.setColor(Color.BLACK);
			tp.setTextSize(30);
			StaticLayout sl = new StaticLayout(BuildBitmapActivity.getNomeJogador(), tp, canvas.getWidth()/3, Alignment.ALIGN_CENTER, 1f, 0f, false); 
			canvas.save();
			canvas.translate(canvas.getWidth()/3, canvas.getHeight()-55); //position text on the canvas
			sl.draw(canvas);
			drawable.draw(canvas);
			//
		}
		return finalBitmap;
	}

	public Bitmap screenShot(View view) {
		View v = view.getRootView();
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		v.draw(canvas);
		canvas.drawBitmap(bitmap, 0, 0, null);
		return bitmap;
	}
}
