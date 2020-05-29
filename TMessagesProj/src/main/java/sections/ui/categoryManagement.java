package sections.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import telegram.messenger.xtelex.ApplicationLoader;
import telegram.messenger.xtelex.R;
import telegram.messenger.xtelex.util.Const;

import java.util.ArrayList;
import java.util.List;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.DialogsActivity;
import sections.categories.DatabaseCategories;
import sections.datamodel.Category;
import sections.ui.adapters.AdapterCategories;

import static org.telegram.ui.DialogsActivity.catCode;

public class categoryManagement extends BaseFragment {

    private final int MENU_SETTINGS = 1;
    private final int ADD_MENU = 2;
    private ListView listView;

    List<Category> categories = new ArrayList<>();


    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }


    @Override
    public View createView(Context context) {

        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setItemsBackgroundColor(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR, false);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("CategoryManagement", R.string.CategoryManagement));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == MENU_SETTINGS) {


                    final CharSequence[] items = {context.getResources().getString(R.string.categorySettings)};

                    final ArrayList seletedItems = new ArrayList();

                    final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    final boolean scr = preferences.getBoolean("categoryMenu", true);
                    final boolean[] checked = {scr};

                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.categorySettingsTitle))
                            .setMultiChoiceItems(items, new boolean[]{!scr}, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                    if (isChecked) {
                                        checked[0] = false;

                                    } else {

                                        checked[0] = true;
                                    }
                                }
                            }).setPositiveButton(context.getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    preferences.edit().putBoolean(Const.CATEGORY_MENU, checked[0]).apply();
//                                            DialogsActivity.refreshToolbarItems();

                                }
                            }).setNegativeButton(context.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    //  Your code when user clicked on Cancel
                                }
                            }).create();
                    dialog.show();


                } else if (id == ADD_MENU) {
                    final EditText input = new EditText(context);
                    input.setPadding(15, 4, 15, 4);

                    // add list item
                    new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.newCategory))
                            .setMessage(context.getResources().getString(R.string.insertCategoryName))
                            .setView(input)
                            .setPositiveButton(context.getResources().getString(R.string.insertCategory), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    DatabaseCategories databaseCategories = new DatabaseCategories(context);
                                    Category category = new Category();
                                    category.setCat_name(input.getText().toString());
                                     int currentAccount = UserConfig.selectedAccount;
                                    category.setCurrent_user(currentAccount);
                                    databaseCategories.insert(category);
                                    refreshDisplay(context);
                                    /*categoryDBAdapter catDBAdapter = new categoryDBAdapter(context);
                                    catDBAdapter.open();
                                    category category = new category();
                                    category.setName(input.getText().toString());
                                    catDBAdapter.insert(category);
                                    catDBAdapter.close();
                                    refreshDisplay(context);*/

                                }
                            })
                            .setNegativeButton(context.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(R.drawable.ic_menu_category)
                            .show();


                }
            }
        });


        ActionBarMenu menu = actionBar.createMenu();
        menu.addItemWithWidth(ADD_MENU, R.drawable.add, AndroidUtilities.dp(56));
        menu.addItemWithWidth(MENU_SETTINGS, R.drawable.menu_settings, AndroidUtilities.dp(56));

        fragmentView = new FrameLayout(context);
        fragmentView.setLayoutParams(new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new ListView(context);

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        listView.setBackgroundColor(preferences.getInt("prefBGColor", 0xffffffff));
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);

        frameLayout.addView(listView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.TOP;
        listView.setLayoutParams(layoutParams);
        refreshDisplay(context);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setItems(new CharSequence[]{context.getResources().getString(R.string.Open), context.getResources().getString(R.string.Delete)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) {

                            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                            sharedPreferences.edit().putInt("selectedCat", categories.get(position).getCat_id()).apply();

                            catCode = categories.get(position).getCat_id();
                            Const.setCats(catCode);
                            Bundle args = new Bundle();
                            args.putBoolean("hiddens", true);
                            args.putInt("hiddenCode", 0220);
                            args.putInt("dialogsType", 12);
                            args.putString("catName", categories.get(position).getCat_name());
                            presentFragment(new HiddenChats(args));


                        } else if (which == 1) {

                            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                            if (categories.get(position).getCat_id() == sharedPreferences.getInt("selectedCat", -1)) {

                                sharedPreferences.edit().putInt("selectedCat", -1).commit();
                                DialogsActivity.needRefreshCategory = true;
                                finishFragment();

                            }

                            DatabaseCategories databaseCategories = new DatabaseCategories(context);
                            databaseCategories.delete(categories.get(position).getCat_id());
                            databaseCategories.close();


                            databaseCategories.deleteAll(categories.get(position).getCat_id());
                            databaseCategories.close();


                            refreshDisplay(context);

                        }
                    }
                });
                builder.show();
            }
        });


        return fragmentView;
    }

    private void refreshDisplay(Context context) {
        DatabaseCategories databaseCategories = new DatabaseCategories(context);

        // catDBAdapter.open();

        categories = databaseCategories.getAllItms();
        AdapterCategories listAdapter = new AdapterCategories(context, R.layout.list_item_categories, categories);
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

}
