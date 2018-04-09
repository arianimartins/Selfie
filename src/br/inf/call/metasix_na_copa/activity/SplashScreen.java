package br.inf.call.metasix_na_copa.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import br.inf.call.metasix_na_copa.R;

public class SplashScreen extends Activity{

	private final int SPLASH_DISPLAY_LENGHT = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				finish();
				Intent i = new Intent(SplashScreen.this, HomeActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				overridePendingTransition(R.anim.rightin, R.anim.leftout);
			}
		}, SPLASH_DISPLAY_LENGHT);
	}

}
