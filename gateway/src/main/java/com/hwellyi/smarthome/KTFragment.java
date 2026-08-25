package com.hwellyi.smarthome;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KTFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KTFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KTFragment extends Fragment implements View.OnClickListener {
    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ImageView mImgKTPower;
    ImageView mImgKTAuto;
    ImageView mImgKTCold;
    ImageView mImgKTHot;
    ImageView mImgKTChuShi;
    ImageView mImgKTMdFan;
    ImageView mImgKTFanLR;
    ImageView mImgKTFanUD;
    ImageView mImgKTFan;
    TextView mTextKTTempValue;

    int mKTStatus = 0;
    int kKTValue = 16;

    private OnFragmentInteractionListener mListener;

    public KTFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CommunityFragment.
     */
    // Rename and change types and number of parameters
    public static KTFragment newInstance(String param1, String param2) {
        KTFragment fragment = new KTFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View retView =inflater.inflate(R.layout.fragment_kt, container, false);
        mImgKTPower = retView.findViewById(R.id.img_kt_power);
        mImgKTPower.setOnClickListener(this);
        mImgKTAuto = retView.findViewById(R.id.img_kt_auto);
        mImgKTAuto.setOnClickListener(this);
        mImgKTCold = retView.findViewById(R.id.img_kt_cold);
        mImgKTCold.setOnClickListener(this);
        mImgKTHot = retView.findViewById(R.id.img_kt_hot);
        mImgKTHot.setOnClickListener(this);
        mImgKTChuShi = retView.findViewById(R.id.img_kt_chushi);
        mImgKTChuShi.setOnClickListener(this);
        mImgKTMdFan = retView.findViewById(R.id.img_kt_md_fan);
        mImgKTMdFan.setOnClickListener(this);
        mImgKTFanLR = retView.findViewById(R.id.img_kt_fan_lr);
        mImgKTFanLR.setOnClickListener(this);
        mImgKTFanUD = retView.findViewById(R.id.img_kt_fan_ud);
        mImgKTFanUD.setOnClickListener(this);
        mImgKTFan = retView.findViewById(R.id.img_kt_fan);
        mImgKTFan.setOnClickListener(this);
        mTextKTTempValue = retView.findViewById(R.id.text_kt_value);
        mTextKTTempValue.setText(String.valueOf(kKTValue));
        retView.findViewById(R.id.img_kt_temp_left).setOnClickListener(this);
        retView.findViewById(R.id.img_kt_temp_right).setOnClickListener(this);
        return retView;
    }

    // Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.img_kt_power){
            if((mKTStatus & 0x01) > 0)
            {
                mImgKTPower.setBackgroundResource(R.drawable.kt_power_off);
                mKTStatus &= ~0x01;
            }
            else
            {
                mImgKTPower.setBackgroundResource(R.drawable.kt_power_on);
                mKTStatus |= 0x01;
            }
        }else if(v.getId() == R.id.img_kt_temp_left){
            if(kKTValue > 16)
            {
                kKTValue--;
                mTextKTTempValue.setText(String.valueOf(kKTValue));
            }
        }else if(v.getId() == R.id.img_kt_temp_right){
            if(kKTValue < 31)
            {
                kKTValue++;
                mTextKTTempValue.setText(String.valueOf(kKTValue));
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
