package org.wikipedia.editing;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.wikipedia.PageTitle;
import org.wikipedia.R;
import org.wikipedia.Utils;
import org.wikipedia.page.Section;

public class EditSectionActivity extends Activity {
    public static final String ACTION_EDIT_SECTION = "org.wikipedia.edit_section";
    public static final String EXTRA_TITLE = "org.wikipedia.edit_section.title";
    public static final String EXTRA_SECTION = "org.wikipedia.edit_section.section";

    private PageTitle title;
    private Section section;

    private String sectionWikitext;

    private EditText sectionText;
    private View sectionProgress;
    private View sectionContainer;
    private View sectionError;
    private Button sectionErrorRetry;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_section);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (!getIntent().getAction().equals(ACTION_EDIT_SECTION)) {
            throw new RuntimeException("Much wrong action. Such exception. Wow");
        }

        title = getIntent().getParcelableExtra(EXTRA_TITLE);
        section = getIntent().getParcelableExtra(EXTRA_SECTION);

        getActionBar().setTitle(getString(R.string.editsection_activity_title, section.getHeading()));

        sectionText = (EditText) findViewById(R.id.edit_section_text);
        sectionProgress = findViewById(R.id.edit_section_load_progress);
        sectionContainer = findViewById(R.id.edit_section_container);
        sectionError = findViewById(R.id.edit_section_error);
        sectionErrorRetry = (Button) findViewById(R.id.edit_section_error_retry);

        if (savedInstanceState != null && savedInstanceState.containsKey("sectionWikitext")) {
            sectionWikitext = savedInstanceState.getString("sectionWikitext");
        }

        sectionErrorRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.crossFade(sectionError, sectionProgress);
                fetchSectionText();
            }
        });

        fetchSectionText();
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_save_section:
                Toast.makeText(this, "This will save things, eventually", Toast.LENGTH_LONG).show();
                return true;
            default:
                throw new RuntimeException("WAT");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_section, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("sectionWikitext", sectionWikitext);
    }

    private void fetchSectionText() {
        if (sectionWikitext == null) {
            new FetchSectionWikitextTask(this, title, section.getId()) {
                @Override
                public void onFinish(String result) {
                    sectionWikitext = result;
                    displaySectionText();
                }

                @Override
                public void onCatch(Throwable caught) {
                    Utils.crossFade(sectionProgress, sectionError);
                    // Not sure why this is required, but without it tapping retry hides langLinksError
                    // FIXME: INVESTIGATE WHY THIS HAPPENS!
                    // Also happens in {@link PageViewFragment}
                    sectionError.setVisibility(View.VISIBLE);
                }
            }.execute();
        } else {
            displaySectionText();
        }
    }

    private void displaySectionText() {
        sectionText.setText(sectionWikitext);
        Utils.crossFade(sectionProgress, sectionContainer);
        invalidateOptionsMenu();
    }

}