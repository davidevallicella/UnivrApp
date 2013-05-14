package test;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ImageLoaderHandler extends Handler {
  
    private Bitmap image;

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == ImageLoader.BITMAP_DOWNLOADED_SUCCESS) {
            Bundle data = msg.getData();
            this.image = data.getParcelable(ImageLoader.BITMAP_EXTRA);            
        }
    }

    public Bitmap getImage() {
        return image;
    }
}