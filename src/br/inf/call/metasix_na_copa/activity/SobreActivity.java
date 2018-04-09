package br.inf.call.metasix_na_copa.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.android.gms.internal.ac;

public class SobreActivity extends Activity {

	private AdView mAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sobre);
		
		configAdMob();
		configActionBar();

	}

	private void configAdMob() {
		mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());
	}

	private void configActionBar() {
		Typeface type = Typeface.createFromAsset(SobreActivity.this.getAssets(),"lunabar.TTF");
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		LayoutInflater inflator = LayoutInflater.from(this);
		View v = inflator.inflate(R.layout.titleview, null);
		TextView titulo = (TextView) v.findViewById(R.id.titleActionBar);
		titulo.setText("SelfieCup");
		titulo.setTypeface(type);
		actionBar.setCustomView(v);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		
		case android.R.id.home:
			finish();
			return true;

		default:
			return super.onContextItemSelected((android.view.MenuItem) item);
		}
	}


}