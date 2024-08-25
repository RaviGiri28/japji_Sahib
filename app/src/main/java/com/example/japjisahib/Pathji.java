package com.example.japjisahib;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.MenuInflater;

public class Pathji extends AppCompatActivity {

    private ImageButton playPauseButton;
    private SeekBar seekBar;
    private TextView playbackTime;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private Handler handler = new Handler();
    private WebView webView;
    private boolean isLightMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pathji);

        // Initialize MediaPlayer controls
        playPauseButton = findViewById(R.id.playPauseButton);
        seekBar = findViewById(R.id.seekBar);
        playbackTime = findViewById(R.id.playbackTime);

        // Initialize the MediaPlayer with the audio resource
        mediaPlayer = MediaPlayer.create(this, R.raw.japjisahibji);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                isPlaying = false;
                playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
                seekBar.setProgress(0);
                playbackTime.setText("00:00");
            }
        });

        // Update the SeekBar max value
        seekBar.setMax(mediaPlayer.getDuration());

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlayPauseClicked(view);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    updatePlaybackTime();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Initialize WebView
        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(false);
        webSettings.setLoadsImagesAutomatically(true);
        webView.setInitialScale(175);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webView.setVerticalScrollBarEnabled(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Set WebViewClient to handle page load and apply centering
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Center align the content horizontally using JavaScript
                view.loadUrl("javascript:(function() { " +
                        "var style = document.createElement('style'); " +
                        "style.innerHTML = 'body { margin: 0 auto !important; color: " + (isLightMode ? "black" : "white") + "; }'; " +
                        "document.head.appendChild(style); })();");

                // Optionally scroll to top to ensure content is visible from the start
                view.scrollTo(0, 0);
            }
        });

        // Load your HTML content
        String htmlContent = readHtmlFile(R.raw.japjisahibnew);
        if (htmlContent != null) {
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_toggle_theme) {
            toggleTheme(item);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("This is an About dialog.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void toggleTheme(MenuItem item) {
        isLightMode = !isLightMode;
        if (isLightMode) {
            item.setIcon(R.drawable.baseline_wb_sunny_24);
            item.setTitle("Light Mode");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            item.setIcon(R.drawable.baseline_nightlight_round_24);
            item.setTitle("Dark Mode");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        // Reload the WebView to apply the new theme
        webView.reload();
    }

    public void onPlayPauseClicked(View view) {
        if (isPlaying) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
        } else {
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24);
            updateSeekBar();
        }
        isPlaying = !isPlaying;
    }

    private void updateSeekBar() {
        if (mediaPlayer.isPlaying()) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            updatePlaybackTime();
            handler.postDelayed(updateRunnable, 1000);
        }
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
        }
    };

    private void updatePlaybackTime() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        int minutes = (currentPosition / 1000) / 60;
        int seconds = (currentPosition / 1000) % 60;
        playbackTime.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateRunnable);
    }

    private String readHtmlFile(int resourceId) {
        InputStream inputStream = getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }
}
