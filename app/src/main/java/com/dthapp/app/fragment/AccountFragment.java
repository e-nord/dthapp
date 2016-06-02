package com.dthapp.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dthapp.app.LoginManager;
import com.dthapp.R;
import com.dthapp.app.User;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class AccountFragment extends Fragment {

    public static AccountFragment newInstance(){
        return new AccountFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.account_view, container, false);

        User user = LoginManager.INSTANCE.getUser();

        TextView name = (TextView) view.findViewById(R.id.account_name);
        name.setText(user.getName());

        CircularImageView pic = (CircularImageView) view.findViewById(R.id.account_pic);
        Picasso.with(getActivity()).load(user.getPictureUrl()).into(pic);

        final ImageView background = (ImageView) view.findViewById(R.id.account_background);
        Picasso.with(getActivity()).load(user.getPictureUrl()).noFade().transform(new BlurTransformation(getContext(), 4)).into(background);

        return view;
    }

}
