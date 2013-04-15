package com.cellasoft.univrapp.activity;

import java.math.BigDecimal;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalPayment;

public class AboutScreen extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about);
		init();
	}

	private void init() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				PayPal pp = PayPal.initWithAppID(AboutScreen.this,
						"APP-80W284485P519543T", PayPal.ENV_SANDBOX);

				final CheckoutButton launchSimplePayment = pp.getCheckoutButton(
						AboutScreen.this, PayPal.BUTTON_194x37,
						CheckoutButton.TEXT_DONATE);

				launchSimplePayment.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						PayPalPayment payment = new PayPalPayment();

						payment.setSubtotal(new BigDecimal("1.00"));

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
					}
				});
				
				return null;
			}
		}.execute();
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