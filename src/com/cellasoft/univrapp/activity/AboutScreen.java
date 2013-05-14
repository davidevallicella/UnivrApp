package com.cellasoft.univrapp.activity;

import java.math.BigDecimal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cellasoft.univrapp.utils.AsyncTask;
import com.cellasoft.univrapp.utils.FontUtils;
import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalPayment;

public class AboutScreen extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about);
		setProgressBarIndeterminateVisibility(true);
		init();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		FontUtils.setRobotoFont(this, (ViewGroup) getWindow().getDecorView());
		super.onPostCreate(savedInstanceState);
	}

	private void init() {
		AsyncTask<Void, Void, Void> loadPayPalTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				final Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

				findViewById(R.id.contact_email_action).setOnTouchListener(
						new OnTouchListener() {

							@Override
							public boolean onTouch(View v, MotionEvent event) {
								if (MotionEvent.ACTION_DOWN == event
										.getAction()) {
									vib.vibrate(50);
									v.startAnimation(AnimationUtils
											.loadAnimation(AboutScreen.this,
													R.anim.image_click));
									Intent email_intent = new Intent(
											Intent.ACTION_SENDTO,
											Uri.fromParts(
													"mailto",
													"vallicella.davide@gmail.com",
													null));
									startActivity(Intent.createChooser(
											email_intent, "Send email..."));
									return true;
								}
								return false;
							}
						});

				PayPal pp = PayPal.initWithAppID(AboutScreen.this,
						"APP-02V829382W416122M", PayPal.ENV_LIVE);

				final CheckoutButton launchSimplePayment = pp
						.getCheckoutButton(AboutScreen.this,
								PayPal.BUTTON_194x37,
								CheckoutButton.TEXT_DONATE);

				launchSimplePayment.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						PayPalPayment payment = new PayPalPayment();

						payment.setSubtotal(new BigDecimal("0.90"));

						payment.setCurrencyType("EUR");

						payment.setRecipient("vallicella.davide@gmail.com");

						payment.setPaymentType(PayPal.PAYMENT_TYPE_GOODS);

						Intent checkoutIntent = PayPal.getInstance().checkout(
								payment, AboutScreen.this);

						startActivityForResult(checkoutIntent, 1);
					}

				});

				runOnUiThread(new Runnable() {
					public void run() {
						((LinearLayout) findViewById(R.id.paypal_layout))
								.addView(launchSimplePayment);
						findViewById(R.id.paypal_load).setVisibility(View.GONE);
					}
				});

				return null;
			}
		};
		loadPayPalTask.execute((Void[]) null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case Activity.RESULT_OK:
			// Il pagamento è stato effettuato
			String payKey = data.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
			Toast.makeText(this, ";) thanks for the contribution",
					Toast.LENGTH_SHORT).show();
			break;
		case Activity.RESULT_CANCELED:
			// Il pagamento è stato cancellato dall’utente
			Toast.makeText(this, ":'(", Toast.LENGTH_SHORT).show();
			break;
		case PayPalActivity.RESULT_FAILURE:
			// Il pagamento non è stato effettuato a causa di errore
			String errorID = data.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
			String errorMessage = data
					.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
			Toast.makeText(this,
					"Payment error: " + errorMessage + "\n ErrId: " + errorID,
					Toast.LENGTH_LONG).show();
		}
	}
}