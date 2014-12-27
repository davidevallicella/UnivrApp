package com.cellasoft.univrapp.activity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.cellasoft.univrapp.*;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.model.Department;
import com.cellasoft.univrapp.utils.AsyncTask;
import com.cellasoft.univrapp.utils.Lists;
import com.cellasoft.univrapp.utils.UIUtils;
import com.cellasoft.univrapp.widget.ContactItemInterface;
import com.cellasoft.univrapp.widget.LecturerListView;
import com.cellasoft.univrapp.widget.LecturerView;
import com.cellasoft.univrapp.widget.OnLecturerViewListener;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

public class ContactListActivity extends BaseListActivity implements
        OnQueryTextListener {

    private final static String TAG = makeLogTag(ContactListActivity.class);
    OnLecturerViewListener lecturerListener = new OnLecturerViewListener() {
        @Override
        public void onSelected(LecturerView view, boolean selected) {
            final int position = listView.getPositionForView(view);
            ContactItemInterface item = listView.getItemAtPosition(position);
            if (item instanceof Lecturer) {
                ((Lecturer) item).isSelected = selected;
            }
        }
    };
    private LecturerListView listView;
    private List<ContactItemInterface> lecturers = null;
    private List<ContactItemInterface> filterList = null;
    private SearchListTask curSearchTask = null;
    private Department department;
    private PostData postData;
    private Object searchLock = new Object();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_list);

        Application.parents.push(getClass());
        init();
    }

    private void init() {
        department = Settings.getUniversity();
        lecturers = Lists.newArrayList();
        filterList = Lists.newArrayList();
        postData = new PostData();

        initActionBar();
        initListView();
        initAnimation();
    }

    @Override
    protected void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.rss);
        getSupportActionBar().setTitle(
                getResources().getString(R.string.subscribe_title));
        getSupportActionBar().setSubtitle(department.name);
    }

    @Override
    protected void initListView() {
        listView = (LecturerListView) getListView();

        // Set a listener to be invoked when the list should be refreshed.
        listView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Do work to refresh the list here.
                refresh();
            }
        });

        listView.setLecturerViewlistener(lecturerListener);
        loadData();
    }

    private void initSearchView(Menu menu) {
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
                .getActionView();
        if (searchView != null) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
            searchView.setOnQueryTextListener(this);
        }
    }

    private void initAnimation() {
        LayoutAnimationController controller = AnimationUtils
                .loadLayoutAnimation(this, R.anim.list_layout_controller);
        controller.getAnimation().reset();

        listView.setLayoutAnimation(controller);
    }

    @Override
    protected void onPause() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onPause()");
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onResume()");
        }
        super.onResume();

        listView.refresh();
    }

    @Override
    protected void onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDestroy()");
        }
        super.onDestroy();

        if (curSearchTask != null) {
            curSearchTask.cancel();
            curSearchTask = null;
        }

        if (listView != null) {
            listView.clean();
        }

        if (filterList != null) {
            filterList.clear();
        }

        if (lecturers != null) {
            lecturers.clear();
        }

    }

    @Override
    protected void loadData() {
        if (refresh)
            return;

        refresh = true;

        BetterAsyncTask<Void, Void, Void> task = new BetterAsyncTask<Void, Void, Void>(
                this) {

            @Override
            protected void before(Context context) {
                listView.clean();
            }

            @Override
            protected void after(Context arg0, Void items) {
                if (running) {
                    listView.setItems(lecturers);
                    listView.startLayoutAnimation();
                }
                refresh = false;
            }

            @Override
            protected void handleError(Context context, Exception e) {
                if (running) {
                    String message = getResources().getString(
                            R.string.not_load_lecturer_notification);
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
                refresh = false;
            }
        };

        task.setCallable(new BetterAsyncTaskCallable<Void, Void, Void>() {
            @Override
            public Void call(BetterAsyncTask<Void, Void, Void> task)
                    throws Exception {
                lecturers = Lecturer.loadFullLecturers();
                return null;
            }
        });
        task.disableDialog();
        UIUtils.execute(task, (Void[]) null);
    }

    private void refresh() {
        if (refresh)
            return;

        refresh = true;

        BetterAsyncTask<Void, Void, List<ContactItemInterface>> task = new BetterAsyncTask<Void, Void, List<ContactItemInterface>>(
                this) {

            @Override
            protected void after(Context context,
                                 final List<ContactItemInterface> newItems) {
                if (running) {
                    String size = "0";
                    if (newItems != null && newItems.size() > 0) {
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    listView.addItems(newItems);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.run();

                        size = String.valueOf(newItems.size());
                    }

                    listView.onRefreshComplete();
                    String message = getResources().getString(
                            R.string.new_lecturers_notification).replace(
                            "{total}", size);
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
                refresh = false;
            }

            @Override
            protected void handleError(Context context, Exception e) {
                if (running) {
                    listView.onRefreshComplete();
                    String message = getResources().getString(
                            R.string.not_load_lecturer_notification);
                    Toast.makeText(context, message + "\n" + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
                refresh = false;
            }
        };
        task.setCallable(new BetterAsyncTaskCallable<Void, Void, List<ContactItemInterface>>() {

            @Override
            public List<ContactItemInterface> call(
                    BetterAsyncTask<Void, Void, List<ContactItemInterface>> arg0)
                    throws Exception {
                if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {
                    return department.update();
                } else
                    throw new UnivrReaderException(getResources().getString(
                            R.string.univrapp_connection_exception));
            }
        });
        task.disableDialog();
        UIUtils.execute(task, (Void[]) null);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ContactItemInterface item = listView.getItemAtPosition(position);
        if (item instanceof Lecturer) {
            showContact((Lecturer) item, v);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.lecturer_menu, menu);
        initSearchView(menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(
            com.actionbarsherlock.view.MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_subscribe:
                confirmBeforeSavingSubscriptions();
                return true;
            case android.R.id.home:
                // NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            case R.id.clear_cache:
                ImageLoader.getInstance().clearDiscCache();
                ImageLoader.getInstance().clearMemoryCache();
                Toast.makeText(this, R.string.clear_cache_complete_toast,
                        Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirmBeforeSavingSubscriptions() {
        Resources res = getResources();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(res.getString(R.string.sub_channel_dialog_title))
                .setMessage(res.getString(R.string.sub_channel_dialog))
                .setPositiveButton(res.getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                saveSubscriptions();
                            }
                        })
                .setNegativeButton(res.getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).create();
        dialog.show();
    }

    private void saveSubscriptions() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(
                R.string.sub_channel_dialog2));
        BetterAsyncTask<Void, Void, Void> task = new BetterAsyncTask<Void, Void, Void>(
                this) {

            @Override
            protected void after(Context context, Void arg1) {
                if (running) {
                    listView.refresh();
                    progressDialog.dismiss();
                    String message = getResources().getString(R.string.success);
                    Toast.makeText(ContactListActivity.this, message,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected void handleError(Context context, Exception e) {
                progressDialog.dismiss();
                if (running) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }
        };

        task.setCallable(new BetterAsyncTaskCallable<Void, Void, Void>() {
            @Override
            public Void call(BetterAsyncTask<Void, Void, Void> arg0)
                    throws Exception {
                for (ContactItemInterface item : lecturers) {
                    Lecturer lecturer = (Lecturer) item;
                    if (lecturer.isSelected) {
                        postData.setPersoneMittente(lecturer.key);
                        String description = lecturer.email;
                        String url = postData.getUrl().toString();
                        if (new Channel(lecturer.id, lecturer.name, url,
                                lecturer.thumbnail, description).subscribe()) {
                            lecturer.isSelected = false;
                        }
                    }
                }
                return null;
            }
        });
        task.disableDialog();
        progressDialog.show();

        UIUtils.execute(task, (Void[]) null);
    }

    @SuppressLint("NewApi")
    private void showContact(Lecturer lecturer, View v) {
        Intent intent = new Intent(this, ContactActivity.class);
        intent.putExtra(ContactActivity.LECTURER_ID_PARAM, lecturer.id);
        intent.putExtra(ContactActivity.LECTURER_NAME_PARAM, lecturer.name);
        intent.putExtra(ContactActivity.LECTURER_OFFICE_PARAM, lecturer.office);
        intent.putExtra(ContactActivity.LECTURER_THUMB_PARAM,
                lecturer.thumbnail);
        if (UIUtils.hasJellyBean()) {
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v,
                    0, 0, v.getWidth(), v.getHeight());
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (curSearchTask != null) {
            curSearchTask.cancel();
        }

        curSearchTask = new SearchListTask();
        curSearchTask.execute(newText.toUpperCase(Locale.getDefault()));
        return true;
    }

    final class PostData {
        public static final int ALL = -1;
        public static final int NO_ONE = 0;

        private int gi;
        private int mi;
        private int ai;
        private int gf;
        private int mf;
        private int af;
        private int personeMittente;
        private int struttureMittente = NO_ONE;
        private int biblioCRMittente = NO_ONE;
        private int csMittente = NO_ONE;
        private int oiMittente = NO_ONE;

        public PostData() {
            Calendar c = Calendar.getInstance();
            gi = c.get(Calendar.DAY_OF_MONTH);
            mi = c.get(Calendar.MONTH) + 1;
            ai = c.get(Calendar.YEAR);
            gf = gi;
            mf = mi;
            af = ai + 7;
        }

        public void setPersoneMittente(int personeMittente) {
            this.personeMittente = personeMittente;
        }

        public Uri getUrl() {
            return Uri
                    .parse(department.domain)
                    .buildUpon()
                    .path("/")
                    .appendQueryParameter("ent", "avviso")
                    .appendQueryParameter("rss", "1")
                    .appendQueryParameter("dest",
                            String.valueOf(department.dest))
                    .appendQueryParameter("gi", String.valueOf(gi))
                    .appendQueryParameter("mi", String.valueOf(mi))
                    .appendQueryParameter("ai", String.valueOf(ai))
                    .appendQueryParameter("gf", String.valueOf(gf))
                    .appendQueryParameter("mf", String.valueOf(mf))
                    .appendQueryParameter("af", String.valueOf(af))
                    .appendQueryParameter("personeMittente",
                            String.valueOf(personeMittente))
                    .appendQueryParameter("struttureMittente",
                            String.valueOf(struttureMittente))
                    .appendQueryParameter("biblioCRMittente",
                            String.valueOf(biblioCRMittente))
                    .appendQueryParameter("csMittente",
                            String.valueOf(csMittente))
                    .appendQueryParameter("oiMittente",
                            String.valueOf(oiMittente)).build();
        }
    }

    private class SearchListTask extends AsyncTask<String, Void, String> {

        boolean inSearchMode = false;

        @Override
        protected String doInBackground(String... params) {
            filterList.clear();

            String keyword = params[0];

            inSearchMode = (keyword.length() > 0);

            if (inSearchMode) {
                // get all the items matching this
                for (ContactItemInterface item : lecturers) {
                    Lecturer contact = (Lecturer) item;

                    if ((contact.getItemForIndex()
                            .toUpperCase(Locale.getDefault()).indexOf(keyword) > -1)) {
                        filterList.add(item);
                    }

                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (running) {
                synchronized (searchLock) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (inSearchMode) {
                                listView.setInSearchMode(true);
                                listView.setItems(filterList);
                            } else {
                                listView.setInSearchMode(false);
                                listView.setItems(lecturers);
                            }
                        }
                    });
                }
            }
        }

        public void cancel() {
            if (getStatus() != AsyncTask.Status.FINISHED) {
                try {
                    cancel(true);
                } catch (Exception e) {
                    Log.i(TAG, "Fail to cancel running search task");
                }
            }
        }
    }

}