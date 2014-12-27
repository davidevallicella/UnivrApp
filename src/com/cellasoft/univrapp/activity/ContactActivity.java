package com.cellasoft.univrapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.AsyncTask;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.UIUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ContactActivity extends SherlockActivity {

    public static final String LECTURER_ID_PARAM = "LecturerId";
    public static final String LECTURER_NAME_PARAM = "LecturerName";
    public static final String LECTURER_OFFICE_PARAM = "LecturerOffice";
    public static final String LECTURER_THUMB_PARAM = "LecturerThumb";
    protected volatile boolean running;
    private Lecturer lecturer;
    private String tellOffice;
    private String tellLab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact);

        if (getIntent().hasExtra(LECTURER_ID_PARAM)) {
            Intent intent = getIntent();
            int lecturerId = intent.getIntExtra(LECTURER_ID_PARAM, 0);
            String lecturerName = intent.getStringExtra(LECTURER_NAME_PARAM);
            String lecturerOffice = intent
                    .getStringExtra(LECTURER_OFFICE_PARAM);
            String lecturerThumb = intent.getStringExtra(LECTURER_THUMB_PARAM);
            loadContactInfo(lecturerId, lecturerName, lecturerOffice,
                    lecturerThumb);
        }

        getSupportActionBar().setTitle(
                getResources().getString(R.string.contact_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        running = true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        FontUtils.setRobotoFont(this, getWindow().getDecorView());
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        getSupportMenuInflater().inflate(R.menu.contact_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(
            com.actionbarsherlock.view.MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                // go back
                finish();
                return true;
            case R.id.menu_web_page:
                showWebPage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadContactInfo(final int lecturerId,
                                 final String lecturerName, final String lecturerOffice,
                                 final String imageUrl) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                ImageLoader.getInstance().displayImage(imageUrl,
                        (ImageView) findViewById(R.id.contact_image));
                ((TextView) findViewById(R.id.contact_title))
                        .setText(lecturerName);
                ((TextView) findViewById(R.id.contact_office))
                        .setText(lecturerOffice);
            }

            @Override
            protected Void doInBackground(Void... params) {

                // final Bitmap b =
                // ImageFetcher.getInstance(ContactActivity.this).get(imageUrl);

                lecturer = Lecturer.findById(lecturerId);

                final Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /*
                         * if (b != null) { image.setImageBitmap(b); }
                         */

                        if (lecturer.telephone != null
                                && lecturer.telephone.length() > 0) {

                            tellOffice = lecturer.telephone;

                            if (tellOffice.contains("/")) {
                                String[] tells = tellOffice.split("/");
                                tellOffice = tells[0].trim();
                                tellLab = tells[1].trim();
                            }

                            ((TextView) findViewById(R.id.contact_phone))
                                    .setText(tellOffice.replace(" - ", "\n")
                                            .trim());
                            findViewById(R.id.contact_phone_action)
                                    .setOnTouchListener(new OnTouchListener() {

                                        @Override
                                        public boolean onTouch(View v,
                                                               MotionEvent event) {
                                            if (MotionEvent.ACTION_DOWN == event
                                                    .getAction()) {
                                                vib.vibrate(50);
                                                v.startAnimation(AnimationUtils
                                                        .loadAnimation(
                                                                ContactActivity.this,
                                                                R.anim.image_click));

                                                callNumber(tellOffice);
                                                return true;
                                            }
                                            return false;
                                        }
                                    });
                            findViewById(R.id.contact_phone_row_view)
                                    .setVisibility(View.VISIBLE);

                            if (tellLab != null && tellLab.length() > 0) {
                                ((TextView) findViewById(R.id.contact_phone_lab))
                                        .setText(tellLab.replace(" - ", "\n")
                                                .trim());
                                findViewById(R.id.contact_phone_lab_action)
                                        .setOnTouchListener(
                                                new OnTouchListener() {

                                                    @Override
                                                    public boolean onTouch(
                                                            View v,
                                                            MotionEvent event) {
                                                        if (MotionEvent.ACTION_DOWN == event
                                                                .getAction()) {
                                                            vib.vibrate(50);
                                                            v.startAnimation(AnimationUtils
                                                                    .loadAnimation(
                                                                            ContactActivity.this,
                                                                            R.anim.image_click));

                                                            callNumber(tellLab);
                                                            return true;
                                                        }
                                                        return false;
                                                    }
                                                });
                                findViewById(R.id.contact_phone_lab_row_view)
                                        .setVisibility(View.VISIBLE);
                            }
                        } else {
                            findViewById(R.id.contact_phone_row_view)
                                    .setVisibility(View.GONE);
                            findViewById(R.id.contact_phone_lab_row_view)
                                    .setVisibility(View.GONE);
                        }

                        if (lecturer.email != null
                                && lecturer.email.length() > 0) {
                            ((TextView) findViewById(R.id.contact_email))
                                    .setText(lecturer.email);
                            findViewById(R.id.contact_email_action)
                                    .setOnTouchListener(new OnTouchListener() {

                                        @Override
                                        public boolean onTouch(View v,
                                                               MotionEvent event) {
                                            if (MotionEvent.ACTION_DOWN == event
                                                    .getAction()) {
                                                vib.vibrate(50);
                                                v.startAnimation(AnimationUtils
                                                        .loadAnimation(
                                                                ContactActivity.this,
                                                                R.anim.image_click));
                                                String mailTo = lecturer.email;
                                                Intent emailIntent = new Intent(
                                                        Intent.ACTION_SENDTO,
                                                        Uri.fromParts("mailto",
                                                                mailTo, null));
                                                startActivity(Intent
                                                        .createChooser(
                                                                emailIntent,
                                                                "Send email..."));
                                                return true;
                                            }
                                            return false;
                                        }
                                    });
                            findViewById(R.id.contact_mail_row_view)
                                    .setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.contact_mail_row_view)
                                    .setVisibility(View.GONE);
                        }

                        if (lecturer.department != null
                                && lecturer.department.length() > 0) {
                            ((TextView) findViewById(R.id.contact_department))
                                    .setText(lecturer.department);
                            findViewById(R.id.contact_department_row_view)
                                    .setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.contact_department_row_view)
                                    .setVisibility(View.GONE);
                        }
                    }
                });

                return null;
            }

        }.execute((Void[]) null);
    }

    private void chooseNumberDialog(final String[] telephones) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            ContactActivity.this).setTitle(getResources()
                            .getString(R.string.contact_dialog_title));
                    builder.setItems(telephones, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            call(telephones[which].trim());
                        }

                    });
                    builder.show();
                }
            }
        });
    }

    private void call(String telephone) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + telephone));
        startActivity(callIntent);
    }

    private void showWebPage() {
        Uri uri = Uri.parse(Settings.getUniversity().domain).buildUpon()
                .path("/fol/main").appendQueryParameter("ent", "persona")
                .appendQueryParameter("id", String.valueOf(lecturer.key))
                .build();

        UIUtils.safeOpenLink(this, new Intent(Intent.ACTION_VIEW, uri));
    }

    private void callNumber(String telephone) {
        if (telephone.contains("-")) {
            String[] telephones = telephone.split(" - ");
            chooseNumberDialog(telephones);
        } else {
            call(telephone);
        }
    }
}