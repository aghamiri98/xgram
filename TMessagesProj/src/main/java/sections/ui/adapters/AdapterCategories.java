package sections.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import telegram.messenger.xtelex.R;
import telegram.messenger.xtelex.util.Const;

import java.util.List;

import sections.categories.DatabaseCategories;
import sections.datamodel.Category;


public class AdapterCategories extends ArrayAdapter<Category> {

    Context context;


    public AdapterCategories(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public AdapterCategories(Context context, int resource, List<Category> items) {
        super(context, resource, items);
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_categories, null);
        }

        final Category p = getItem(position);

        if (p != null) {
            TextView name = (TextView) v.findViewById(R.id.name);
            TextView size = (TextView) v.findViewById(R.id.size);


            if (name != null) {
                name.setText(p.getCat_name());
            }

            DatabaseCategories databaseCategories=new DatabaseCategories(context);
           /* catDBAdapter catDBAdapter = new catDBAdapter(context);
            catDBAdapter.open();*/

            Log.i(Const.TAG, "cat_id: " + p.getCat_id());

            size.setText(String.format(context.getResources().getString(R.string.items_few), databaseCategories.getCatSize(p.getCat_id())));

            String size_count=String.format(context.getResources().getString(R.string.items_few), databaseCategories.getCatSize(p.getCat_id()));

            Log.i(Const.TAG, "size: " + size_count);
            //catDBAdapter.close();



        }

        return v;
    }




}
