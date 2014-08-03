package itkach.aard2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import itkach.slob.Slob;

public class LookupFragment extends BaseListFragment {

    private Timer       timer;
    private SearchView  searchView;
    private String      initialQuery = "";
    private String      currentQuery = "";

    @Override
    Icons getEmptyIcon() {
        return Icons.SEARCH;
    }

    @Override
    CharSequence getEmptyText() {
        return "";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            initialQuery = savedInstanceState.getString("lookupQuery", "");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = getListView();
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.i("--", "Item clicked: " + position);
                Intent intent = new Intent(getActivity(),
                        ArticleCollectionActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
        Application app = (Application) getActivity().getApplication();
        getListView().setAdapter(app.lastResult);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.lookup, menu);
        MenuItem miFilter = menu.findItem(R.id.action_lookup);
        View filterActionView = miFilter.getActionView();

        timer = new Timer();

        searchView = (SearchView) filterActionView.findViewById(R.id.fldLookup);
        searchView.setQueryHint(miFilter.getTitle());
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            TimerTask scheduledLookup = null;

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i("SUBMIT", query);
                onQueryTextChange(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i("CHANGE", "New text: " + newText);
                TimerTask doLookup = new TimerTask() {
                    @Override
                    public void run() {
                        final String query = searchView.getQuery().toString();
                        if (currentQuery.equals(query)) {
                            return;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setBusy(true);
                            }
                        });
                        final Application app = (Application) getActivity()
                                .getApplication();
                        final Iterator<Slob.Blob> result;
                        if (query == null || query.equals("")) {
                            result = new ArrayList<Slob.Blob>().iterator();
                        } else {
                            result = app.find(query);
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                app.setLookupResult(query, result);
                                currentQuery = query;
                                setBusy(false);
                            }
                        });
                        scheduledLookup = null;
                    }
                };
                if (scheduledLookup != null) {
                    scheduledLookup.cancel();
                }
                scheduledLookup = doLookup;
                timer.schedule(doLookup, 600);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {
                return true;
            }
        });

        searchView.setSubmitButtonEnabled(false);
        searchView.setQuery(initialQuery, !initialQuery.equals(currentQuery));
        setBusy(false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem miFilter = menu.findItem(R.id.action_lookup);
        View filterActionView = miFilter.getActionView();
        searchView = (SearchView) filterActionView.findViewById(R.id.fldLookup);
        searchView.setQuery(currentQuery, false);
        final Application app = (Application) getActivity()
                .getApplication();
        if (app.lastResult.getCount() > 0) {
            searchView.clearFocus();
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String query = searchView.getQuery().toString();
        outState.putString("lookupQuery", query);
    }

    private void setBusy(boolean busy) {
        setListShown(!busy);
        if (!busy) {
            TextView emptyText = ((TextView)emptyView.findViewById(R.id.empty_text));
            String msg = "";
            if (searchView.getQuery() != null && !searchView.getQuery().toString().equals("")) {
                msg = "Nothing found";
            }
            emptyText.setText(msg);
        }
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

}