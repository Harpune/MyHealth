package de.dbis.myhealth.ui.intro;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroCustomLayoutFragment;

import org.jetbrains.annotations.Nullable;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;

public class IntroActivity extends AppIntro {

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mSharedPreferences = getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        setDoneTextAppearance(R.style.TextAppearance_MaterialComponents_Button);
        setSkipTextAppearance(R.style.TextAppearance_MaterialComponents_Button);
        setSkipText(getString(R.string.skip));
        setDoneText(getString(R.string.done));

        int themeColor = getColor(R.color.green_900);
        String currentTheme = this.mSharedPreferences.getString(getString(R.string.theme_key), getString(R.string.green_theme_key));

        if (currentTheme.equalsIgnoreCase(getString(R.string.green_theme_key))) {
            themeColor = getColor(R.color.green_900);
        } else if (currentTheme.equalsIgnoreCase(getString(R.string.blue_theme_key))) {
            themeColor = getColor(R.color.blue_900);
        } else if (currentTheme.equalsIgnoreCase(getString(R.string.red_theme_key))) {
            themeColor = getColor(R.color.red_900);
        }

        setIndicatorColor(themeColor, getColor(android.R.color.darker_gray));


        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.item_intro_slide_1));
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.item_intro_slide_1));
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.item_intro_slide_1));
    }

    @Override
    protected void onSkipPressed(@Nullable Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        this.setViewed();
        finish();
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        this.setViewed();
        finish();
    }

    private void setViewed() {
        this.mSharedPreferences
                .edit()
                .putBoolean(getString(R.string.pref_on_boarding), true)
                .apply();
    }
}