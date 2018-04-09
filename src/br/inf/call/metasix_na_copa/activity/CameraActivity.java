
package br.inf.call.metasix_na_copa.activity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import br.inf.call.metasix_na_copa.R;
import br.inf.call.metasix_na_copa.camera.CameraController;
import br.inf.call.metasix_na_copa.utils.CameraConstants;

public class CameraActivity extends Activity implements SurfaceHolder.Callback, Camera.PictureCallback, OnClickListener {

	private CameraController cameraController;
	private int categoryType;
	private SurfaceHolder holder;
	private SurfaceView previewView;
	private RelativeLayout shootPhotoBar;
	private ImageButton mTakeBtn, mSwitchBtn;
	private ImageView imgSilhueta;
	private static float centerX, centerY;
	private static float FRAME_HALF_WIDTH, FRAME_HALF_HEIGHT;
	private boolean takingPhoto = false;
	Uri imageFileUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_preview);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		categoryType = getIntent().getIntExtra(CameraConstants.CATEGORY_TYPE, 0);
		try {
			initCamera(categoryType);
		}
		catch (IOException e) {
			Log.e("onCreate()", "error in initCamera()");
		}
		
		calcMedidas();
		configOverlay();
	}

	private void calcMedidas() {
		centerX = this.getWindowManager().getDefaultDisplay().getWidth() / 2 ;
		centerY = this.getWindowManager().getDefaultDisplay().getHeight() / 2;
		float density = this.getResources().getDisplayMetrics().density;
		FRAME_HALF_WIDTH = this.getResources().getDimension(R.dimen.crop_frame_width_half) * density;
		FRAME_HALF_HEIGHT = this.getResources().getDimension(R.dimen.crop_frame_height_half) * density;
	}

	private void configOverlay() {
		imgSilhueta = (ImageView) findViewById(R.id.camera_silhueta);
		imgSilhueta.setMaxWidth((int) (centerX + FRAME_HALF_WIDTH)/2);
		imgSilhueta.setMaxHeight((int) (centerY + FRAME_HALF_HEIGHT)/2);
	}

	private void initCamera(int categoryType) throws IOException {
		CameraController.init(CameraActivity.this, categoryType);
		cameraController = CameraController.getController();

		mTakeBtn = (ImageButton) findViewById(R.id.shootPhoto);
		mTakeBtn.setOnClickListener(this);

		mSwitchBtn = (ImageButton) findViewById(R.id.switch_button);
		mSwitchBtn.setOnClickListener(this);
		
		previewView = (SurfaceView) findViewById(R.id.surfaceViewPreview);
		holder = previewView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		shootPhotoBar = (RelativeLayout) findViewById(R.id.shootPhotoBar);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.e("surfaceChanged:", "camera surfaceChanged");
		try {
			cameraController.cameraOpen(holder);
			cameraController.startPreview(previewView, shootPhotoBar);
		}
		catch (Exception e) {
			Log.e("Error in surfaceCreated:", "camera controller problem");
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		cameraController.stopPreview();
		cameraController.releaseCamera();
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		imageFileUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());
		try {
//			Toast.makeText(this, "imageFileUri:" + imageFileUri.toString(), Toast.LENGTH_LONG).show();
			OutputStream imageFileOS = getContentResolver().openOutputStream(imageFileUri);
			imageFileOS.write(data);
			imageFileOS.flush();
			imageFileOS.close();
		}catch (FileNotFoundException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}catch (IOException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.shootPhoto:
			if (!takingPhoto) {
				((ImageButton) v).setEnabled(false);
				takingPhoto = true;
				CameraController.isTakingPhoto = true;
				cameraController.autoFocus();
			}
			MediaPlayer mp = MediaPlayer.create(CameraActivity.this, R.raw.camera_sound);  
			mp.start();
			break;
			
		case R.id.switch_button:
			try {
				cameraController.restartPreview();
				CameraController.getController().cameraOpen(holder);
				CameraController.getController().startPreview(previewView, shootPhotoBar);
			}
			catch (Exception e) {
				Log.e("Error in surfaceCreated:", "camera controller problem");
			}
			break;
		}
	}
}