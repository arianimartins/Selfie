package br.inf.call.metasix_na_copa.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.view.View;
import br.inf.call.metasix_na_copa.R;
import br.inf.call.metasix_na_copa.activity.BuildBitmapActivity;


@SuppressLint("DrawAllocation")
public class CropFrameView extends View{

	public static int STROKE_WIDTH = 3;
	private static float centerX, centerY;
	private static float FRAME_HALF_WIDTH, FRAME_HALF_HEIGHT;

	private Drawable drawable;
	private Bitmap bitmap;
	private int densityDpi;
	private int dpX = 320;
	private int dpY = 400;
	private int tamanhoX = 720;
	private int tamanhoY = 1184;

	public CropFrameView(Context context) {
		super(context);
		centerX = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth() / 2 ;
		centerY = ((Activity) context).getWindowManager().getDefaultDisplay().getHeight() / 2;

		densityDpi = context.getResources().getDisplayMetrics().densityDpi;
		FRAME_HALF_WIDTH = (dpX * (centerX * 2)) / tamanhoX; 
		FRAME_HALF_HEIGHT = (dpY * (centerY * 2)) / tamanhoY;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		drawable = getResources().getDrawable(R.drawable.brasil_album_m);
		bitmap = ((BitmapDrawable) drawable).getBitmap();
		Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		RectF dst = new RectF(centerX - FRAME_HALF_WIDTH, centerY - FRAME_HALF_HEIGHT, centerX + FRAME_HALF_WIDTH, centerY + FRAME_HALF_HEIGHT);
		canvas.drawBitmap(bitmap, src, dst, null);

		/*Paint myPaint = new Paint();
		canvas.drawBitmap(bitmap, null, null);
		myPaint.setColor(Color.RED);
		myPaint.setStyle(Paint.Style.STROKE);
		myPaint.setStrokeWidth(STROKE_WIDTH);
		canvas.drawRect(centerX - FRAME_HALF_WIDTH, centerY - FRAME_HALF_HEIGHT, centerX + FRAME_HALF_WIDTH, centerY + FRAME_HALF_HEIGHT, myPaint);*/
	}

	public static float[] getCropFrameCoordinates(){
		float[] coord = new float[]{centerX - FRAME_HALF_WIDTH, centerY - FRAME_HALF_HEIGHT, centerX + FRAME_HALF_WIDTH, centerY + FRAME_HALF_HEIGHT};
		return coord;
	}
}