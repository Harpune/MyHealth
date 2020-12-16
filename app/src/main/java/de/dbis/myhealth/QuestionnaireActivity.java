package de.dbis.myhealth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import de.dbis.myhealth.ui.questionnaires.QuestionnaireFragment;

public class QuestionnaireActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, QuestionnaireFragment.newInstance())
                    .commitNow();
        }
    }
}