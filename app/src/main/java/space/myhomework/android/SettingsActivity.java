package space.myhomework.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void deleteAccount(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://app.myhomework.space/#!settings:requestAccountDeletion"));
        startActivity(browserIntent);
    }
}
