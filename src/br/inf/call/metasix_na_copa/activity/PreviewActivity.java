package br.inf.call.metasix_na_copa.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import br.inf.call.metasix_na_copa.R;
import br.inf.call.metasix_na_copa.widget.CustomSimpleDialog;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.FacebookDialog;



public class PreviewActivity extends Activity implements OnClickListener {

	private Button btnCompartilhar, btnSalvar;
	private ImageView imgPreview;
	private Bitmap bmp;
	private Context context;
	private byte[] byteArray;

	//FACEBOOK
	private static final String PERMISSION = "publish_actions";
	private final String PENDING_ACTION_BUNDLE_KEY = "br.inf.call.metasix_na_copa.activity:PendingAction";
	private UiLifecycleHelper uiHelper;
	private PendingAction pendingAction = PendingAction.NONE;
	private boolean canPresentShareDialog;
	private boolean canPresentShareDialogWithPhotos;
	//

	private enum PendingAction {
		NONE,
		POST_PHOTO,
		POST_STATUS_UPDATE
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);

		configActionBar();
		configurarFacebook(savedInstanceState);
		configComponentes();
		recuperarExtras();
	}

	private void configurarFacebook(Bundle savedInstanceState) {
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		// Can we present the share dialog for regular links?
		canPresentShareDialog = FacebookDialog.canPresentShareDialog(this, FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
		// Can we present the share dialog for photos?
		canPresentShareDialogWithPhotos = FacebookDialog.canPresentShareDialog(this, FacebookDialog.ShareDialogFeature.PHOTOS);
	}


	private void configComponentes() {
		btnSalvar = (Button) findViewById(R.id.btn_salvar);
		btnSalvar.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto_Condensed.ttf"));
		btnSalvar.setOnClickListener(this);

		btnCompartilhar = (Button) findViewById(R.id.btn_compartilhar);	
		btnCompartilhar.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto_Condensed.ttf"));
		btnCompartilhar.setOnClickListener(this);

		imgPreview = (ImageView) findViewById(R.id.img_preview);
	}

	private void recuperarExtras() {
		Bundle extras = getIntent().getExtras();
		byteArray = extras.getByteArray("figurinha");
		bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		imgPreview.setImageBitmap(bmp);
	}

	private void configActionBar() {
		Typeface type = Typeface.createFromAsset(PreviewActivity.this.getAssets(),"lunabar.TTF");
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

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
		@Override
		public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
			Log.d("Facebook", String.format("Error: %s", error.toString()));
		}

		@Override
		public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
			Log.d("Facebook", "Success!");
		}
	};

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		final String TAG_LOG = PreviewActivity.class.getSimpleName();
		if (state.isOpened()) {
			Log.i(TAG_LOG, "Logged in...");
		} else if (state.isClosed()) {
			Log.i(TAG_LOG, "Logged out...");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
			@Override
			public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
				Log.e("Activity", String.format("Error: %s", error.toString()));
			}

			@Override
			public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
				Log.i("Activity", "Success!");
			}
		});
	}

	private FacebookDialog.PhotoShareDialogBuilder createShareDialogBuilderForPhoto(Bitmap... photos) {
		return new FacebookDialog.PhotoShareDialogBuilder(this).addPhotos(Arrays.asList(photos));
	}

	private void compartilhar(){
		Intent intent = new Intent(PreviewActivity.this, facebookUpload.class);
		intent.putExtra("bitmapPath", byteArray);
		startActivity(intent);
//		performPublish(PendingAction.POST_PHOTO, canPresentShareDialogWithPhotos);
//		new CustomSimpleDialog(PreviewActivity.this, R.string.txt_share_titulo, R.string.txt_share_msg, true).show();
	}

	private void postPhoto() {
		Bitmap image = bmp;
		if (canPresentShareDialogWithPhotos) {
			FacebookDialog shareDialog = createShareDialogBuilderForPhoto(image).build();
			uiHelper.trackPendingDialogCall(shareDialog.present());
		} else if (hasPublishPermission()) {
			Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback() {
				@Override
				public void onCompleted(Response response) {
					showPublishResult(getString(R.string.photo_post), response.getGraphObject(), response.getError());
				}
			});
			request.executeAsync();
		} else {
			pendingAction = PendingAction.POST_PHOTO;
		}
	}

	private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
		String title = null;
		String alertMessage = null;
		if (error == null) {
			title = getString(R.string.success);
			String id = result.cast(GraphObjectWithId.class).getId();
			alertMessage = getString(R.string.successfully_posted_post, message, id);
		} else {
			title = getString(R.string.error);
			alertMessage = error.getErrorMessage();
		}

		new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(alertMessage)
		.setPositiveButton(R.string.txt_ok, null)
		.show();
	}


	private interface GraphObjectWithId extends GraphObject {
		String getId();
	}


	private void performPublish(PendingAction action, boolean allowNoSession) {
		Session session = Session.getActiveSession();
		if (session != null) {
			pendingAction = action;
			if (hasPublishPermission()) {
				// We can do the action right away.
				handlePendingAction();
				return;
			} else if (session.isOpened()) {
				// We need to get new permissions, then complete the action when we get called back.
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSION));
				return;
			}
		}

		if (allowNoSession) {
			pendingAction = action;
			handlePendingAction();
		}
	}

	@SuppressWarnings("incomplete-switch")
	private void handlePendingAction() {
		PendingAction previouslyPendingAction = pendingAction;
		// These actions may re-set pendingAction if they are still pending, but we assume they will succeed.
		pendingAction = PendingAction.NONE;

		switch (previouslyPendingAction) {
		case POST_PHOTO:
			postPhoto();
			break;
		}
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null && session.getPermissions().contains("publish_actions");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_salvar:
			salvar();
			break;

		case R.id.btn_compartilhar:
			compartilhar();
			bmp.recycle();
			break;
		}
	}


	private void salvar() {
		File storagePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Copa 2014/");
		storagePath.mkdirs();
		File myImage = new File(storagePath, Long.toString(System.currentTimeMillis())+".jpg");
		try {
			FileOutputStream out = new FileOutputStream(myImage);
			bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
			new CustomSimpleDialog(this, R.string.txt_ok, false, R.string.txt_imagem_salva, R.string.txt_path_img, null).show();

		} catch (FileNotFoundException e) {
			Log.d("ERRO: In saving file", e+"");
		}catch (IOException e){
			Log.d("ERRO: In saving file", e+"");
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
		AppEventsLogger.activateApp(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {

		case R.id.action_info:
			Intent intent = new Intent(PreviewActivity.this, SobreActivity.class);
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
