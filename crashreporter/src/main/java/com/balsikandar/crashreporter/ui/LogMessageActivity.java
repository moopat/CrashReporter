package com.balsikandar.crashreporter.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.balsikandar.crashreporter.R;
import com.balsikandar.crashreporter.utils.AppUtils;
import com.balsikandar.crashreporter.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static androidx.core.content.FileProvider.getUriForFile;

public class LogMessageActivity extends AppCompatActivity {

    private TextView appInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_message);
        appInfo = (TextView) findViewById(R.id.appInfo);

        Intent intent = getIntent();
        if (intent != null) {
            String dirPath = intent.getStringExtra("LogMessage");
            File file = new File(dirPath);
            String crashLog = FileUtils.readFromFile(file);
            TextView textView = (TextView) findViewById(R.id.logMessage);
            textView.setText(crashLog);
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle(getString(R.string.crash_reporter));
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getAppInfo();
    }

    private void getAppInfo() {
        appInfo.setText(AppUtils.getDeviceDetails(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.crash_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = getIntent();
        String filePath = null;
        if (intent != null) {
            filePath = intent.getStringExtra("LogMessage");
        }

        if (item.getItemId() == R.id.delete_log) {
            if (FileUtils.delete(filePath)) {
                finish();
            }
            return true;
        } else if (item.getItemId() == R.id.share_crash_log) {
            shareCrashReport(filePath);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void shareCrashReport(String filePath) {
        final File dir = new File(getFilesDir(), "sharedcrashes");
        dir.mkdirs();

        final File file = new File(dir, "crash.txt");
        if (file.exists()) file.delete();

        try {
            copy(new File(filePath), file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Uri contentUri = getUriForFile(this, "com.balsikandar.crashreporter", file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, appInfo.getText().toString());
        intent.setData(contentUri);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    /*
     * https://stackoverflow.com/questions/9292954/how-to-make-a-copy-of-a-file-in-android
     */
    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
