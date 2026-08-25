package com.hwellyi.smarthome;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import JavaType.TypeRoomInfo;
import JavaType.TypeSceneNameInfo;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SceneCtrlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SceneCtrlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SceneCtrlFragment extends Fragment implements View.OnClickListener {
    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private boolean mEditFlag = false;
    ArrayList<TypeRoomInfo> mRoomList = new ArrayList<>();
    ArrayList<TypeSceneNameInfo> mSceneNameList = new ArrayList<>();
    private Boolean isGetData = false;
    TextView textSceneEditText;
    ScrollView sceneCtrlScross;

    private OnFragmentInteractionListener mListener;

    public SceneCtrlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AprtmentFragment.
     */
    // Rename and change types and number of parameters
    public static SceneCtrlFragment newInstance(String param1, String param2) {
        SceneCtrlFragment fragment = new SceneCtrlFragment();
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
        View retView = inflater.inflate(R.layout.fragment_scene, container, false);
        textSceneEditText = retView.findViewById(R.id.sceneEditText);
        textSceneEditText.setOnClickListener(this);
        sceneCtrlScross = retView.findViewById(R.id.sceneCtrlScroll);
        return retView;
    }

    // Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void onUpdateSceneView(boolean editflag, Context context)
    {
        if(editflag)
        {
            textSceneEditText.setText("完成");
        }
        else
        {
            textSceneEditText.setText("编辑");
        }
        String tempRoomListJsonString = PublicUse.mJniFunCB.onGetRoomList();
        //解析出一个房间列表
        ArrayList<TypeRoomInfo> tempRoomList = new ArrayList<>();
        TypeRoomInfo tempRoomInfo;
        JSONObject tempJson = null;
        try {
            tempJson = new JSONObject(tempRoomListJsonString);
            if (!tempJson.isNull("roomlist"))
            {
                JSONArray tempArray = new JSONArray(tempJson.getString("roomlist"));
                if (tempArray.length() > 0)
                {
                    for (int i = 0; i < tempArray.length(); i++)
                    {
                        JSONObject tempDeviceJson = tempArray.getJSONObject(i);
                        tempRoomInfo = new TypeRoomInfo(0, 0, "");
                        if (!tempDeviceJson.isNull("roomid"))
                        {
                            tempRoomInfo.roomID = tempDeviceJson.getInt("roomid");
                        }
                        if (!tempDeviceJson.isNull("iconid"))
                        {
                            tempRoomInfo.iconID = tempDeviceJson.getInt("iconid");
                        }
                        if (!tempDeviceJson.isNull("name"))
                        {
                            tempRoomInfo.name = tempDeviceJson.getString("name");
                        }
                        tempRoomList.add(tempRoomInfo);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String tempSceneListJson = PublicUse.mJniFunCB.onGetSceneList();
        ArrayList<TypeSceneNameInfo> tempSceneNameList = new ArrayList<>();
        TypeSceneNameInfo tempSceneNameInfo;
        try {
            tempJson = new JSONObject(tempSceneListJson);
            if (!tempJson.isNull("scenelist"))
            {
                JSONArray tempArray = new JSONArray(tempJson.getString("scenelist"));
                if (tempArray.length() > 0)
                {
                    for (int i = 0; i < tempArray.length(); i++)
                    {
                        JSONObject tempDeviceJson = tempArray.getJSONObject(i);
                        tempSceneNameInfo = new TypeSceneNameInfo(0, 0, 0, 0, 0, "");
                        if (!tempDeviceJson.isNull("id"))
                        {
                            tempSceneNameInfo.sceneID = tempDeviceJson.getInt("id");
                        }
                        if (!tempDeviceJson.isNull("iconid"))
                        {
                            tempSceneNameInfo.iconID = tempDeviceJson.getInt("iconid");
                        }
                        if (!tempDeviceJson.isNull("roomid"))
                        {
                            tempSceneNameInfo.roomID = tempDeviceJson.getInt("roomid");
                        }
                        if (!tempDeviceJson.isNull("status"))
                        {
                            tempSceneNameInfo.status = tempDeviceJson.getInt("status");
                        }
                        if (!tempDeviceJson.isNull("hidden"))
                        {
                            tempSceneNameInfo.hidden = tempDeviceJson.getInt("hidden");
                        }
                        if (!tempDeviceJson.isNull("name"))
                        {
                            tempSceneNameInfo.name = tempDeviceJson.getString("name");
                        }
                        if((editflag) || (!editflag && ((tempSceneNameInfo.hidden & 0xF0) == 0)))
                        {
                            tempSceneNameList.add(tempSceneNameInfo);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //这里开始刷新UI界面 当然，如果一切信息都没有变化，可以考虑不刷新
        boolean isUpdateFlag = true;
        if((tempSceneNameList.size() == mSceneNameList.size()) && (tempRoomList.size() == mRoomList.size()))
        {
            //然后再一项项判断有没有变化
            for (int i = 0; i < tempSceneNameList.size(); i++)
            {
                if(!tempSceneNameList.get(i).onIsChanged(mSceneNameList.get(i)))
                {
                    isUpdateFlag = false;
                    break;
                }
            }
            if(isUpdateFlag)
            {
                for (int i = 0; i < tempRoomList.size(); i++)
                {
                    if(!tempRoomList.get(i).onIsChanged(mRoomList.get(i)))
                    {
                        isUpdateFlag = false;
                        break;
                    }
                }
            }
        }
        if(editflag || isUpdateFlag)
        {
            //更新所有房间和场景列表
            mRoomList.clear();
            for (int i = 0; i < tempRoomList.size(); i++)
            {
                mRoomList.add(tempRoomList.get(i));
            }
            mSceneNameList.clear();
            for (int i = 0; i < tempSceneNameList.size(); i++)
            {
                mSceneNameList.add(tempSceneNameList.get(i));
            }
            sceneCtrlScross.removeAllViews();
            RelativeLayout relativeLayout = new RelativeLayout(context);
            //刷新UI
            if(mSceneNameList.size() == 0)
            {
                //显示一个提示，添加一个场景
                TextView textRoomName = new TextView(context);
                textRoomName.setText("你目前没有可操作的场景");
                textRoomName.setTextColor(Color.WHITE);
                textRoomName.setTextSize(24);
                textRoomName.setGravity(Gravity.CENTER);
                RelativeLayout.LayoutParams roomNameLayoutParams = new RelativeLayout.LayoutParams(400, 40);
                roomNameLayoutParams.topMargin = 100;
                roomNameLayoutParams.leftMargin = 50;
                relativeLayout.addView(textRoomName, roomNameLayoutParams);
            }
            else
            {
                int tempTopMargin = 20;
                int tempLeftMargin = 30;
                for (int i = 0; i < mSceneNameList.size(); i++)
                {
                    if((i > 0) && ((i % 4) == 0))
                    {
                        tempLeftMargin = 30;
                        tempTopMargin += 115;
                    }
                    //显示图标 房间名称 场景名称
                    ImageView imageIcon = new ImageView(context);
                    imageIcon.setBackgroundResource(onGetDrawableID(mSceneNameList.get(i).iconID));
                    RelativeLayout.LayoutParams iconLayoutParams = new RelativeLayout.LayoutParams(44, 44);
                    iconLayoutParams.topMargin = tempTopMargin;
                    iconLayoutParams.leftMargin = tempLeftMargin + 20;
                    relativeLayout.addView(imageIcon, iconLayoutParams);
                    final int finalSceneID = mSceneNameList.get(i).sceneID;
                    if(editflag)
                    {
                        //添加一个勾选图标上去
                        final ImageView imageCheckView = new ImageView(context);
                        if((mSceneNameList.get(i).hidden & 0xF0) == 0)
                        {
                            imageCheckView.setBackgroundResource(R.drawable.scene_check_on);
                        }
                        else
                        {
                            imageCheckView.setBackgroundResource(R.drawable.scene_check_off);
                        }
                        RelativeLayout.LayoutParams tempCheckBoxParams = new RelativeLayout.LayoutParams(22, 22);
                        tempCheckBoxParams.topMargin = tempTopMargin - 10;
                        tempCheckBoxParams.leftMargin = tempLeftMargin + 75;
                        final int finalSceneIndex = i;
                        relativeLayout.addView(imageCheckView, tempCheckBoxParams);
                        imageCheckView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                if(mSceneNameList.size() > finalSceneIndex)
                                {
                                    if((mSceneNameList.get(finalSceneIndex).hidden & 0xF0) == 0)
                                    {
                                        mSceneNameList.get(finalSceneIndex).hidden |= 0x10;
                                        imageCheckView.setBackgroundResource(R.drawable.scene_check_off);
                                        PublicUse.mJniFunCB.onSetSceneGWHidden(mSceneNameList.get(finalSceneIndex).sceneID, 1);
                                    }
                                    else
                                    {
                                        mSceneNameList.get(finalSceneIndex).hidden &= 0x0f;
                                        imageCheckView.setBackgroundResource(R.drawable.scene_check_on);
                                        PublicUse.mJniFunCB.onSetSceneGWHidden(mSceneNameList.get(finalSceneIndex).sceneID, 0);
                                    }

                                }
                            }
                        });
                    }
                    else
                    {
                        imageIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //执行场景
                                PublicUse.mJniFunCB.onSetSceneStatus(finalSceneID);
                            }
                        });
                    }
                    //添加房间名称
                    TextView textRoomName = new TextView(context);
                    textRoomName.setText(onGetRoomName(mRoomList, mSceneNameList.get(i).roomID));
                    textRoomName.setTextColor(Color.WHITE);
                    textRoomName.setTextSize(16);
                    textRoomName.setGravity(Gravity.CENTER);
                    RelativeLayout.LayoutParams roomNameLayoutParams = new RelativeLayout.LayoutParams(84, 20);
                    roomNameLayoutParams.topMargin = tempTopMargin + 48;
                    roomNameLayoutParams.leftMargin = tempLeftMargin;
                    relativeLayout.addView(textRoomName, roomNameLayoutParams);
                    //显示场景名称
                    TextView textSceneName = new TextView(context);
                    textSceneName.setText(mSceneNameList.get(i).name);
                    textSceneName.setTextColor(Color.argb(153, 255, 255, 255));
                    textSceneName.setTextSize(14);
                    textSceneName.setGravity(Gravity.CENTER);
                    RelativeLayout.LayoutParams sceneNameLayoutParams = new RelativeLayout.LayoutParams(84, 18);
                    sceneNameLayoutParams.topMargin = tempTopMargin + 24 + 48;
                    sceneNameLayoutParams.leftMargin = tempLeftMargin;
                    relativeLayout.addView(textSceneName, sceneNameLayoutParams);

                    tempLeftMargin += (480 - 30) / 4;
                }
            }
            sceneCtrlScross.addView(relativeLayout);
        }
    }

    int onGetDrawableID(int index)
    {
        int retID = 0;
        switch (index)
        {
            case 1:retID = R.drawable.scene_1;break;
            case 2:retID = R.drawable.scene_2;break;
            case 3:retID = R.drawable.scene_3;break;
            case 4:retID = R.drawable.scene_4;break;
            case 5:retID = R.drawable.scene_5;break;
            case 6:retID = R.drawable.scene_6;break;
            case 7:retID = R.drawable.scene_7;break;
            case 8:retID = R.drawable.scene_8;break;
            case 9:retID = R.drawable.scene_9;break;
            case 10:retID = R.drawable.scene_10;break;
            case 11:retID = R.drawable.scene_11;break;
            case 12:retID = R.drawable.scene_12;break;
            case 13:retID = R.drawable.scene_13;break;
            default:retID = R.drawable.scene_0;break;
        }
        return retID;
    }

    String onGetRoomName(ArrayList<TypeRoomInfo> roomlist, int roomid)
    {
        if(roomlist != null)
        {
            for (int i = 0; i < roomlist.size(); i++)
            {
                if(roomlist.get(i).roomID == roomid)
                {
                    return roomlist.get(i).name;
                }
            }
        }
        return "默认房间";
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
        if(v.getId() == R.id.sceneEditText)
        {
            //处理场景编辑
            if(textSceneEditText.getText().equals("编辑"))
            {
                onUpdateSceneView(true, this.getContext());
            }
            else
            {
                mSceneNameList.clear();
                onUpdateSceneView(false, this.getContext());
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
