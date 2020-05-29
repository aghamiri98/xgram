package sections.messagePrivew;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import telegram.messenger.xtelex.R;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;




/**
 * Created  on 12/6/2017.
 *
 * +
 */


@SuppressLint("ValidFragment")
public class previewDialog extends DialogFragment {

    private int dialogid;
    protected Bundle arguments;
    protected ArrayList<MessageObject> messages = new ArrayList<>();

    private static ArrayList<BaseFragment> mainFragmentsStack = new ArrayList<>();

    @SuppressLint("ValidFragment")
    public previewDialog(Bundle args) {
        arguments = args;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout rootView = new FrameLayout(getActivity());


        dialogid = arguments.getInt("dialogid", 0);

        RelativeLayout layout = new RelativeLayout(getActivity());

        mainFragmentsStack.clear();

        //constant.isPriview = true;

        Bundle args = new Bundle();
        if (dialogid > 0) {
            args.putInt("user_id", (int) dialogid);
        } else {
            args.putInt("chat_id", (int) -dialogid);
        }

        args.putBoolean("privew",true);

        ActionBarLayout actionBarLayout = new ActionBarLayout(getActivity());
        actionBarLayout.init(mainFragmentsStack);
        actionBarLayout.presentFragment(new ChatActivity(args));
        layout.addView(actionBarLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT , LayoutHelper.MATCH_PARENT , Gravity.TOP));
        rootView.addView(layout );

        LinearLayout buttons = new LinearLayout(getActivity());

        buttons.setOrientation(LinearLayout.HORIZONTAL);


        rootView.addView(buttons , LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT , LayoutHelper.WRAP_CONTENT , Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));



        Button cancel = new Button(getActivity());
        cancel.setText(R.string.Close);
        cancel.setTextColor(0x5CFF0000);
        cancel.setBackgroundColor(0xFFFFFFFF);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //constant.isPriview = false;
                dismiss();
            }
        });

        cancel.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        buttons.addView(cancel);

        //int remained = StoreUtils.getPrivew(false);

        /*if (remained != 100) {

            Button shop = new Button(getActivity());
           // shop.setText(LocaleController.formatString("limited", R.string.limited, remained ));
            shop.setTextColor(0x5C11b06c);
            shop.setBackgroundColor(0xFFFFFFFF);
            shop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().startActivity(new Intent( getActivity(), purchase.class));
                    constant.isPriview = false;
                    dismiss();
                }
            });

            shop.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            buttons.addView(shop);

        }*/




        return rootView;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        //constant.isPriview = false;
    }


}
