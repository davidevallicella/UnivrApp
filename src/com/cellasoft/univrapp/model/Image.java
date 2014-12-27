package com.cellasoft.univrapp.model;

import android.net.Uri;
import android.provider.BaseColumns;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.provider.Provider;

import java.util.List;

public class Image implements ActionSupport {
    public static final byte MAX_RETRIES = 3;

    public static final byte IMAGE_STATUS_PENDING = -1;
    public static final byte IMAGE_STATUS_QUEUED = 0;
    public static final byte IMAGE_STATUS_DOWNLOADING = 1;
    public static final byte IMAGE_STATUS_DOWNLOADED = 2;
    public static final byte IMAGE_STATUS_FAILED = 3;
    public static final byte IMAGE_STATUS_DELETED = 4;

    public int id;
    public String url;
    public byte status;
    public byte retries = 0;
    public long updateTime = 0;

    public Image(String url, byte status) {
        this(0, url, status);
    }

    public Image(int id, String url, byte status) {
        this.id = id;
        this.url = url;
        this.status = status;
    }

    public void increaseRetries() {
        this.retries += 1;
    }

    @Override
    public boolean save() {
        return ContentManager.saveImage(this);
    }

    @Override
    public void delete() {
        ContentManager.deleteImage(this);
    }

    @Override
    public boolean exist() {
        return ContentManager.existImage(this);
    }

    public static final class Images implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Provider.AUTHORITY + "/images");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cellasoft.univrapp.provider.images";

        public static final String ID = "ID";
        public static final String URL = "URL";
        public static final String STATUS = "STATUS";
        public static final String RETRIES = "RETRIES";
        public static final String UPDATE_TIME = "UPDATE_TIME";
        public static final String COUNT = "COUNT(*)";

        private Images() {
        }

        public static final Uri limit(int limit) {
            return Uri.parse("content://" + Provider.AUTHORITY + "/images/"
                    + limit);
        }

        public static List<Image> loadAllQueuedImages() {
            return ContentManager.loadAllQueuedImages();
        }
    }
}