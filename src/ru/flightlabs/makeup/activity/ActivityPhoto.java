package ru.flightlabs.makeup.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import ru.flightlabs.commonlib.Settings;
import ru.flightlabs.makeup.R;
import ru.flightlabs.makeup.adapter.PhotoPagerAdapter;


/**
 * Created by sov on 13.02.2017.
 */

public class ActivityPhoto extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_photo_makeup);

        // TODO add pager
        Bundle extras = getIntent().getExtras();
        final String fileName = extras.getString(Settings.PHOTO);

        final ViewPager pager = (ViewPager)findViewById(R.id.pager);
        // TODO список
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File newFile = new File(file, Settings.DIRECTORY_SELFIE);
        if(!newFile.exists()){
            newFile.mkdirs();
        }
        File[] files = newFile.listFiles();

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return -Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });

        final List<String> photos = new ArrayList<String>();
        for (File f : files) {
            photos.add(f.getPath());
        }
        final PhotoPagerAdapter adapter = new PhotoPagerAdapter(this, photos);
        pager.setAdapter(adapter);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.thrash_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new File(adapter.photos.get(pager.getCurrentItem())).delete();
                photos.remove(adapter.photos.get(pager.getCurrentItem()));
                adapter.notifyDataSetChanged();
            }
        });
        findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(adapter.photos.get(pager.getCurrentItem()))));
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, getString(R.string.send_photo_with)));
            }
        });

    }
}