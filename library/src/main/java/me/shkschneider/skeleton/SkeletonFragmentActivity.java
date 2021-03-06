package me.shkschneider.skeleton;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

public class SkeletonFragmentActivity extends SkeletonActivity {

    protected void setContentFragment(@NonNull final Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sk_fragmentactivity);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

}
