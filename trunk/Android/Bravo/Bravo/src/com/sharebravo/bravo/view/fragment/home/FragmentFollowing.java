package com.sharebravo.bravo.view.fragment.home;

import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sharebravo.bravo.R;
import com.sharebravo.bravo.control.activity.HomeActionListener;
import com.sharebravo.bravo.control.activity.HomeActivity;
import com.sharebravo.bravo.model.SessionLogin;
import com.sharebravo.bravo.model.response.ObGetUserFollowing;
import com.sharebravo.bravo.sdk.log.AIOLog;
import com.sharebravo.bravo.sdk.util.network.AsyncHttpGet;
import com.sharebravo.bravo.sdk.util.network.AsyncHttpResponseProcess;
import com.sharebravo.bravo.sdk.util.network.ParameterFactory;
import com.sharebravo.bravo.utils.BravoConstant;
import com.sharebravo.bravo.utils.BravoSharePrefs;
import com.sharebravo.bravo.utils.BravoUtils;
import com.sharebravo.bravo.utils.BravoWebServiceConfig;
import com.sharebravo.bravo.utils.StringUtility;
import com.sharebravo.bravo.view.adapter.AdapterPostList.IClickUserAvatar;
import com.sharebravo.bravo.view.adapter.AdapterUserList;
import com.sharebravo.bravo.view.fragment.FragmentBasic;
import com.sharebravo.bravo.view.lib.PullAndLoadListView;

public class FragmentFollowing extends FragmentBasic implements IClickUserAvatar {
    private PullAndLoadListView mListviewFollowing  = null;

    private AdapterUserList     mAdapterUserList    = null;

    private HomeActionListener  mHomeActionListener = null;
    private ObGetUserFollowing  mObGetUserFollowing = null;

    private SessionLogin        mSessionLogin       = null;
    private String              foreignID           = "";
    private int                 mLoginBravoViaType  = BravoConstant.NO_LOGIN_SNS;
    Button                      btnBack             = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = (ViewGroup) inflater.inflate(R.layout.page_following, container);

        intializeView(root);
        mHomeActionListener = (HomeActivity) getActivity();
        btnBack = (Button) root.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mHomeActionListener.goToBack();
            }
        });
        /* request news */
        mLoginBravoViaType = BravoSharePrefs.getInstance(getActivity()).getIntValue(BravoConstant.PREF_KEY_SESSION_LOGIN_BRAVO_VIA_TYPE);
        mSessionLogin = BravoUtils.getSession(getActivity(), mLoginBravoViaType);

        return root;

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            requestGetUserFollowing(mSessionLogin);
        }
        mListviewFollowing.setVisibility(View.GONE);
    }

    private void requestGetUserFollowing(SessionLogin sessionLogin) {
        String userId = mSessionLogin.userID;
        String accessToken = mSessionLogin.accessToken;
        AIOLog.d("mUserId:" + sessionLogin.userID + ", mAccessToken:" + sessionLogin.accessToken);
        if (StringUtility.isEmpty(sessionLogin.userID) || StringUtility.isEmpty(sessionLogin.accessToken)) {
            userId = "";
            accessToken = "";
        }

        HashMap<String, String> subParams = new HashMap<String, String>();
        subParams.put("Start", "0");
        JSONObject subParamsJson = new JSONObject(subParams);
        String subParamsJsonStr = subParamsJson.toString();
        String url = BravoWebServiceConfig.URL_GET_USER_FOLLOWING.replace("{User_ID}", foreignID);
        List<NameValuePair> params = ParameterFactory.createSubParamsGetTimeLine(userId, accessToken, subParamsJsonStr);
        AsyncHttpGet getUserFollowing = new AsyncHttpGet(getActivity(), new AsyncHttpResponseProcess(getActivity(), this) {
            @Override
            public void processIfResponseSuccess(String response) {
                // AIOLog.d("requestBravoNews:" + response);
                Gson gson = new GsonBuilder().serializeNulls().create();

                mObGetUserFollowing = gson.fromJson(response.toString(), ObGetUserFollowing.class);
                AIOLog.d("mObGetUserFollowing:" + mObGetUserFollowing);
                if (mObGetUserFollowing == null || mObGetUserFollowing.data.size() == 0) {

                    return;
                }
                else {
                    mAdapterUserList.updateUserList(mObGetUserFollowing.data);
                }
                mListviewFollowing.setVisibility(View.VISIBLE);
            }

            @Override
            public void processIfResponseFail() {
                AIOLog.d("response error");
            }
        }, params, true);
        getUserFollowing.execute(url);
    }

    private void intializeView(View root) {
        mAdapterUserList = new AdapterUserList(getActivity());
        mAdapterUserList.setListener(this);
        mListviewFollowing = (PullAndLoadListView) root.findViewById(R.id.listview_following);
        mListviewFollowing.setAdapter(mAdapterUserList);
        mListviewFollowing.onRefreshComplete();
    }

    @Override
    public void onClickUserAvatar(String userId) {
        mHomeActionListener.goToUserData(userId);
    }

    public String getForeignID() {
        return foreignID;
    }

    public void setForeignID(String foreignID) {
        this.foreignID = foreignID;
    }
}