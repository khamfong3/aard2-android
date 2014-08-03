package itkach.aard2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import itkach.fdrawable.IconicFontDrawable;
import itkach.slob.Slob;

abstract class BlobDescriptorListFragment extends BaseListFragment {

    private Drawable icFilter;
    private Drawable icClock;
    private Drawable icList;
    private Drawable icArrowUp;
    private Drawable icArrowDown;

    abstract BlobDescriptorList getDescriptorList();

    abstract String getItemClickAction();

    private BlobDescriptorListAdapter listAdapter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listAdapter = new BlobDescriptorListAdapter(getDescriptorList());

        int iconColor = getResources().getColor(android.R.color.secondary_text_dark);
        icFilter = Icons.FILTER.forActionBar();
        icClock = Icons.CLOCK.forActionBar();
        icList = Icons.LIST.forActionBar();
        icArrowUp = Icons.ARROW_UP.forActionBar();
        icArrowDown = Icons.ARROW_DOWN.forActionBar();

        final ListView listView = getListView();

        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.blob_descriptor_selection, menu);
                listAdapter.setSelectionMode(true);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                ListView listView = getListView();
                switch (item.getItemId()) {
                    case R.id.blob_descriptor_delete:
                        SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                        new AlertDialog.Builder(getActivity())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("")
                                .setMessage(String.format("Are you sure you want to delete %d items?", checkedItems.size()))
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteSelectedItems();
                                        mode.finish();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                        return true;
                    case R.id.blob_descriptor_select_all:
                        int itemCount = listView.getCount();
                        for (int i = itemCount - 1; i > -1; --i) {
                            listView.setItemChecked(i, true);
                        }
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                listAdapter.setSelectionMode(false);
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
            }

        });

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(getActivity(),
                        ArticleCollectionActivity.class);
                intent.setAction(getItemClickAction());
                intent.putExtra("position", position);
                startActivity(intent);
//                BlobDescriptor item = getDescriptorList().get(position);
//                Slob.Blob blob = getDescriptorList().resolve(item);
//                if (blob != null) {
//                    intent.putExtra("position", position);
//                    startActivity(intent);
//                }
            }
        });

        setListAdapter(listAdapter);
    }

    protected void deleteSelectedItems() {
        SparseBooleanArray checkedItems = getListView().getCheckedItemPositions();
        for (int i = checkedItems.size() - 1; i > -1; --i) {
            int position = checkedItems.keyAt(i);
            getDescriptorList().remove(position);
         }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.blob_descriptor_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {

        BlobDescriptorList list = getDescriptorList();

        MenuItem miFilter = menu.findItem(R.id.action_filter);
        miFilter.setIcon(icFilter);

        View filterActionView = miFilter.getActionView();
        SearchView searchView = (SearchView) filterActionView
                .findViewById(R.id.fldFilter);
        searchView.setQueryHint(miFilter.getTitle());
        searchView.setQuery(list.getFilter(), true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                BlobDescriptorList list = getDescriptorList();
                if (!newText.equals(list.getFilter())) {
                    getDescriptorList().setFilter(newText);
                }
                return true;
            }
        });
        setSortOrder(menu.findItem(R.id.action_sort_order), list.getSortOrder());
        setAscending(menu.findItem(R.id.action_sort_asc), list.isAscending());

        super.onPrepareOptionsMenu(menu);
    }

    private void setSortOrder(MenuItem mi, BlobDescriptorList.SortOrder order) {
        Drawable icon;
        int textRes;
        if (order == BlobDescriptorList.SortOrder.TIME) {
            icon = icClock;
            textRes = R.string.action_sort_by_time;
        } else {
            icon = icList;
            textRes = R.string.action_sort_by_title;
        }
        mi.setIcon(icon);
        mi.setTitle(textRes);
    }

    private void setAscending(MenuItem mi, boolean ascending) {
        Drawable icon;
        int textRes;
        if (ascending) {
            icon = icArrowUp;
            textRes = R.string.action_ascending;
        } else {
            icon = icArrowDown;
            textRes = R.string.action_descending;
        }
        mi.setIcon(icon);
        mi.setTitle(textRes);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        BlobDescriptorList list = getDescriptorList();
        int itemId = mi.getItemId();
        if (itemId == R.id.action_sort_asc) {
            list.setSort(!list.isAscending());
            setAscending(mi, list.isAscending());
            return true;
        }
        if (itemId == R.id.action_sort_order) {
            if (list.getSortOrder() == BlobDescriptorList.SortOrder.TIME) {
                list.setSort(BlobDescriptorList.SortOrder.NAME);
            } else {
                list.setSort(BlobDescriptorList.SortOrder.TIME);
            }
            setSortOrder(mi, list.getSortOrder());
            return true;
        }
        return super.onOptionsItemSelected(mi);
    }
}