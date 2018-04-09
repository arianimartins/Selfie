
package br.inf.call.metasix_na_copa.activity;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.util.LruCache;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import br.inf.call.metasix_na_copa.R;
import br.inf.call.metasix_na_copa.utils.ImageUtils;
import br.inf.call.metasix_na_copa.widget.CropFrameView;
import br.inf.call.metasix_na_copa.widget.CustomSimpleDialog;
import br.inf.call.metasix_na_copa.widget.ViewAreaFrameLayout;

public class BuildBitmapActivity extends Activity implements View.OnClickListener{

	public static Button btnVoltar, btnTirarNovamente, btnCriarFigurinha, btnAddNome;
	public static EditText nome;
	private static String nomeJogador;

	private LinearLayout mViewAreaLinerarLayout;
	private LinearLayout.LayoutParams mViewAreaLinearLayoutParams;
	private ViewAreaFrameLayout mViewArea;

	Button mTakePhotoButton;
	private String TAG = "-PreviewActivity-";
	private static final int imageResize = 1080;

	private String path;
	private Context context;

	Set<SoftReference<Bitmap>> mReusableBitmaps;
	private LruCache<String, BitmapDrawable> mMemoryCache;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_build_bitmap);

		configComponentes();
		configButtons();
		showImage();
	}


	private void configComponentes() {
		nome = (EditText) findViewById(R.id.txt_nome);
		btnAddNome = (Button) findViewById(R.id.btn_add_nome);
		btnAddNome.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto_Condensed.ttf"));
		btnAddNome.setOnClickListener(this);
	}

	private void configButtons() {
		btnTirarNovamente = (Button) findViewById(R.id.btn_voltar_camera);
		btnTirarNovamente.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto_Condensed.ttf"));
		btnTirarNovamente.setOnClickListener(this);

		btnCriarFigurinha = (Button) findViewById(R.id.btn_continuar);
		btnCriarFigurinha.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto_Condensed.ttf"));
		btnCriarFigurinha.setOnClickListener(this);
	}

	private void showImage(){
		Bitmap myBitmap = null;
		if(getIntent().getBooleanExtra("isGallery", false)){
			myBitmap = this.imageFromGallery(getIntent().getStringExtra("filePath"));
			int orientation = getIntent().getIntExtra("orientation", 0);
			myBitmap = ImageUtils.RotateImage(myBitmap, orientation);
		}else{
			Bundle bundle = getIntent().getBundleExtra("data");
			byte[] pictureData = bundle.getByteArray("pictureData");
			if (pictureData != null) {
				BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
				bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
				myBitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length, bitmapFatoryOptions);
			}
			else {
				Log.v(TAG, "picture data == null");
			}
		}

		if(myBitmap != null){
			mViewAreaLinerarLayout = (LinearLayout) findViewById(R.id.ll_viewArea);
			mViewAreaLinearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
			mViewArea = new ViewAreaFrameLayout(BuildBitmapActivity.this, myBitmap);
			mViewAreaLinerarLayout.addView(mViewArea,mViewAreaLinearLayoutParams);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "onConfigurationChanged");
	}

	/**
	 * Image result from gallery
	 * @param resultCode
	 * @param data
	 */
	private Bitmap imageFromGallery(String filePath) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		options.inSampleSize = calculateInSampleSize(options, imageResize, imageResize);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			}else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	/**
	 * Remove Photo
	 * @param contentUri
	 */
	public void removePhoto(Uri contentUri) {
		getContentResolver().delete(contentUri, null, null);
	}


	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("path", path);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			path = savedInstanceState.getString("path");
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch(v.getId()){
		case R.id.btn_add_nome:
			Toast.makeText(this, "Nome alterado", Toast.LENGTH_SHORT).show();
			setNomeJogador(nome.getText().toString());
			nome.setHint(nome.getText().toString());
			nome.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
			InputMethodManager imm = (InputMethodManager)getSystemService(context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(nome.getWindowToken(), 0);
			nome.clearFocus();
			Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
			vib.vibrate(100);
			break;
		
		case R.id.btn_voltar_camera:
			intent = new Intent(BuildBitmapActivity.this, CameraActivity.class);
			startActivity(intent);
			break;

		case R.id.btn_continuar:
			intent = new Intent(BuildBitmapActivity.this, PreviewActivity.class);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Bitmap img_retorno = mViewArea.getCroppedImage();
			img_retorno.compress(Bitmap.CompressFormat.JPEG, 90, stream);
			img_retorno.recycle();
			byte[] byteArray = stream.toByteArray();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("figurinha", byteArray);
			startActivity(intent);
			break;
		}
	}


	public static String getNomeJogador() {
		if(nomeJogador == null){
			setNomeJogador(" ");
		}
		return nomeJogador;
	}

	public static void setNomeJogador(String nomeJogador) {
		BuildBitmapActivity.nomeJogador = nomeJogador;
	}
}
