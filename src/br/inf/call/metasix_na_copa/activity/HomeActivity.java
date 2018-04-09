package br.inf.call.metasix_na_copa.activity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import br.inf.call.metasix_na_copa.R;
import br.inf.call.metasix_na_copa.utils.CameraConstants;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class HomeActivity extends Activity {

	private static final int IMAGE_CAPTURE = 20;
	private static final int IMAGE_PICK = 1;
	private String filePath;
	private int orientation;
	
	private AdView mAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		configAdMob();
		configActionBar();
		configButtons();

	}
	

	private void configAdMob() {
		mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());
	}

	private void configButtons() {
		Button btn_camera = (Button) findViewById(R.id.ircamera);
		btn_camera.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto_Condensed.ttf"));
		btn_camera.setOnClickListener(abrirCamera());

		Button btn_galeria = (Button) findViewById(R.id.irgaleria);
		btn_galeria.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto_Condensed.ttf"));
		btn_galeria.setOnClickListener(abrirGaleria());
	}

	private void configActionBar() {
		Typeface type = Typeface.createFromAsset(HomeActivity.this.getAssets(),"lunabar.TTF");
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		LayoutInflater inflator = LayoutInflater.from(this);
		View v = inflator.inflate(R.layout.titleview, null);
		TextView titulo = (TextView) v.findViewById(R.id.titleActionBar);
		titulo.setText("SelfieCup");
		titulo.setTypeface(type);
		actionBar.setCustomView(v);
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	
	private OnClickListener abrirGaleria() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				openGallery();
			}
		};
	}
	private void openGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		startActivityForResult(intent, IMAGE_PICK);
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case IMAGE_PICK:
					retreiveFilePathFromGallery(data);
					Intent intent = new Intent(HomeActivity.this, BuildBitmapActivity.class);
					intent.putExtra("isGallery", true);
					intent.putExtra("filePath", filePath);
					intent.putExtra("orientation", orientation);
					startActivity(intent);
					break;
			}
		}
	}
	
	
	private void retreiveFilePathFromGallery(Intent data){
		Uri selectedImage = data.getData();
    	String [] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION};
    	
    	Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
    	cursor.moveToFirst();
    	
    	int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
    	filePath = cursor.getString(columnIndex);
		int columnIndex_orient = cursor.getColumnIndexOrThrow(filePathColumn[1]);
		orientation = cursor.getInt(columnIndex_orient);
    	cursor.close();
	}


	private OnClickListener abrirCamera() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				openCamera();
			}
		};
	}
	private void openCamera() {
		Intent intent = new Intent();
		intent.setClass(this, CameraActivity.class);
		intent.putExtra(CameraConstants.CATEGORY_TYPE, 1);
		startActivityForResult(intent, IMAGE_CAPTURE);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case android.R.id.home:
			return true;
			
		case R.id.action_info:
			Intent intent = new Intent(HomeActivity.this, SobreActivity.class);
			startActivity(intent);
			return true;
			
		default:
			return super.onContextItemSelected((android.view.MenuItem) item);
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
}