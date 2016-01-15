package com.subinkrishna.curah.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.subinkrishna.curah.R;
import com.subinkrishna.curah.util.ObjectUtil;

/**
 * Welcome fragment that shows the terms & conditions. User need to agree to the T&C to
 * proceed from this screen.
 *
 * @author Subinkrishna Gopi
 */
public class WelcomeNoteFragment
        extends Fragment
        implements View.OnClickListener {

    /** Log Tag */
    private static final String TAG = WelcomeNoteFragment.class.getSimpleName();

    public static interface OnAgreementChangeListener {
        public void onAgreementChange(boolean status);
    }

    private OnAgreementChangeListener mAgreementChangeListener;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_note, container, false);
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        final Activity parent = getActivity();
        final Button btnAgree = (Button) view.findViewById(R.id.button_agree);
        final Button btnDisagree = (Button) view.findViewById(R.id.button_disagree);

        // Maps parent activity to OnAgreementChangeListener
        mAgreementChangeListener = ObjectUtil.mapTo(parent,
                OnAgreementChangeListener.class,
                true);

        /*
        // Set blurred background
        final Bitmap bgImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_hrc_xs);
        final Bitmap blurredBitmap = ImageUtil.blur(parent, bgImageBitmap);
        //ViewUtil.setBitmapBackground(parent, view, blurredBgImage);

        // Set blurred bitmap & apply color filter
        final ImageView bgImageView = (ImageView) view.findViewById(R.id.background);
        bgImageView.setImageBitmap(blurredBitmap);
        bgImageView.setAlpha(.5f);
        */

        // Set the click listeners
        btnAgree.setOnClickListener(this);
        btnDisagree.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_agree:
                mAgreementChangeListener.onAgreementChange(true);
                break;
            case R.id.button_disagree:
                mAgreementChangeListener.onAgreementChange(false);
                break;
        }
    }
}
