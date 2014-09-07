package com.sharebravo.bravo.view.fragment.home;

import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sharebravo.bravo.R;
import com.sharebravo.bravo.control.activity.HomeActionListener;
import com.sharebravo.bravo.model.SessionLogin;
import com.sharebravo.bravo.model.response.ObBravo;
import com.sharebravo.bravo.model.response.ObGetAllBravoRecentPosts;
import com.sharebravo.bravo.sdk.log.AIOLog;
import com.sharebravo.bravo.sdk.util.network.AsyncHttpGet;
import com.sharebravo.bravo.sdk.util.network.AsyncHttpResponseProcess;
import com.sharebravo.bravo.sdk.util.network.ParameterFactory;
import com.sharebravo.bravo.utils.BravoConstant;
import com.sharebravo.bravo.utils.BravoSharePrefs;
import com.sharebravo.bravo.utils.BravoUtils;
import com.sharebravo.bravo.utils.BravoWebServiceConfig;
import com.sharebravo.bravo.utils.StringUtility;
import com.sharebravo.bravo.view.adapter.AdapterRecentPost;
import com.sharebravo.bravo.view.fragment.FragmentBasic;
import com.sharebravo.bravo.view.lib.PullAndLoadListView;
import com.sharebravo.bravo.view.lib.PullAndLoadListView.IOnLoadMoreListener;
import com.sharebravo.bravo.view.lib.PullToRefreshListView.IOnRefreshListener;

public class FragmentHomeTab extends FragmentBasic {
    private PullAndLoadListView       mListviewRecentPost       = null;
    private AdapterRecentPost         mAdapterRecentPost        = null;
    private HomeActionListener        mHomeActionListener       = null;
    private ObGetAllBravoRecentPosts  mObGetAllBravoRecentPosts = null;

    private Button                    mBtnHomeNotification      = null;
    private IShowPageHomeNotification mListener                 = null;
    private SessionLogin              mSessionLogin             = null;
    private int                       mLoginBravoViaType        = BravoConstant.NO_LOGIN_SNS;
    private boolean                   isNoFirstTime = false;
    private OnItemClickListener       iRecentPostClickListener  = new OnItemClickListener() {

                                                                    @Override
                                                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                                        AIOLog.d("position:" + position);
                                                                        mHomeActionListener.goToRecentPostDetail(mObGetAllBravoRecentPosts.data
                                                                                .get(position - 1));
                                                                    }
                                                                };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = (ViewGroup) inflater.inflate(R.layout.page_home_tab, container);

        intializeView(root);

        /* request news */
        mLoginBravoViaType = BravoSharePrefs.getInstance(getActivity()).getIntValue(BravoConstant.PREF_KEY_SESSION_LOGIN_BRAVO_VIA_TYPE);
        mSessionLogin = BravoUtils.getSession(getActivity(), mLoginBravoViaType);
        requestNewsItemsOnBravoServer(mSessionLogin);
        isNoFirstTime = BravoSharePrefs.getInstance(getActivity()).getBooleanValue(BravoConstant.PREF_KEY_BRAVO_FISRT_TIME);
        if (!isNoFirstTime) {
            showDialogWelcome();
            BravoSharePrefs.getInstance(getActivity()).putBooleanValue(BravoConstant.PREF_KEY_BRAVO_FISRT_TIME,true);
        }
        return root;

    }

    private void requestNewsItemsOnBravoServer(SessionLogin sessionLogin) {
        String userId = sessionLogin.userID;
        String accessToken = sessionLogin.accessToken;
        AIOLog.d("mUserId:" + sessionLogin.userID + ", mAccessToken:" + sessionLogin.accessToken);
        if (StringUtility.isEmpty(sessionLogin.userID) || StringUtility.isEmpty(sessionLogin.accessToken)) {
            userId = "";
            accessToken = "";
        }
        String url = BravoWebServiceConfig.URL_GET_ALL_BRAVO;
        List<NameValuePair> params = ParameterFactory.createSubParamsGetAllBravoItems(userId, accessToken);
        AsyncHttpGet getLoginRequest = new AsyncHttpGet(getActivity(), new AsyncHttpResponseProcess(getActivity()) {
            @Override
            public void processIfResponseSuccess(String response) {
                AIOLog.d("requestBravoNews:" + response);
                Gson gson = new GsonBuilder().serializeNulls().create();
                mObGetAllBravoRecentPosts = gson.fromJson(response.toString(), ObGetAllBravoRecentPosts.class);
                AIOLog.d("obGetAllBravoRecentPosts:" + mObGetAllBravoRecentPosts);
                if (mObGetAllBravoRecentPosts == null)
                    return;
                else {
                    AIOLog.d("size of recent post list: " + mObGetAllBravoRecentPosts.data.size());
                    mAdapterRecentPost.updateRecentPostList(mObGetAllBravoRecentPosts);
                    if (mListviewRecentPost.getVisibility() == View.GONE)
                        mListviewRecentPost.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void processIfResponseFail() {
                AIOLog.d("response error");
            }
        }, params, true);

        getLoginRequest.execute(url);

    }

    private void intializeView(View root) {
        mBtnHomeNotification = (Button) root.findViewById(R.id.btn_home_notification);
        mBtnHomeNotification.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // show home notification tab
                mListener.showPageHomeNotification();
            }
        });
        mListviewRecentPost = (PullAndLoadListView) root.findViewById(R.id.listview_recent_post);
        mAdapterRecentPost = new AdapterRecentPost(getActivity(), mObGetAllBravoRecentPosts);
        mListviewRecentPost.setAdapter(mAdapterRecentPost);
        mListviewRecentPost.setOnItemClickListener(iRecentPostClickListener);
        mListviewRecentPost.setVisibility(View.GONE);
        /* load more old items */
        mListviewRecentPost.setOnLoadMoreListener(new IOnLoadMoreListener() {

            @Override
            public void onLoadMore() {
                int size = mObGetAllBravoRecentPosts.data.size();
                if (size > 0)
                    onPullDownToRefreshBravoItems(mObGetAllBravoRecentPosts.data.get(size - 1), false);
                AIOLog.d("IOnLoadMoreListener");
            }
        });

        /* on refresh new items */
        /* load more old items */
        mListviewRecentPost.setOnRefreshListener(new IOnRefreshListener() {

            @Override
            public void onRefresh() {
                AIOLog.d("IOnRefreshListener");
                int size = mObGetAllBravoRecentPosts.data.size();
                if (size > 0)
                    onPullDownToRefreshBravoItems(mObGetAllBravoRecentPosts.data.get(0), true);
            }
        });
    }


    public void showDialogWelcome() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
        View dialog_view = inflater.inflate(R.layout.dialog_welcome, null);
        Button btnStart = (Button) dialog_view.findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        dialog.setContentView(dialog_view);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        // This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        dialog.show();
    }

    private void onPullDownToRefreshBravoItems(ObBravo obBravo, final boolean isPulDownToRefresh) {
        AIOLog.d("obBravo.bravoId:" + obBravo.Bravo_ID);
        HashMap<String, String> subParams = new HashMap<String, String>();
        if (isPulDownToRefresh)
            subParams.put("Min_Bravo_ID", obBravo.Bravo_ID);
        else
            subParams.put("Max_Bravo_ID", obBravo.Bravo_ID);
        subParams.put("View_Deleted_Users", "0");
        subParams.put("Global", "TRUE");
        JSONObject subParamsJson = new JSONObject(subParams);
        String subParamsJsonStr = subParamsJson.toString();
        String userId = mSessionLogin.userID;
        String accessToken = mSessionLogin.accessToken;
        AIOLog.d("mUserId:" + mSessionLogin.userID + ", mAccessToken:" + mSessionLogin.accessToken);
        if (StringUtility.isEmpty(mSessionLogin.userID) || StringUtility.isEmpty(mSessionLogin.accessToken)) {
            userId = "";
            accessToken = "";
        }
        String url = BravoWebServiceConfig.URL_GET_BRAVO_SEARCH;
        List<NameValuePair> params = ParameterFactory.createSubParamsGetNewsBravoItems(userId, accessToken, subParamsJsonStr);
        AsyncHttpGet getPullDown_LoadMoreRequest = new AsyncHttpGet(getActivity(), new AsyncHttpResponseProcess(getActivity()) {
            @Override
            public void processIfResponseSuccess(String response) {
                AIOLog.d("onLoadMoreBravoItems:" + response);
                if (isPulDownToRefresh) {
                    mListviewRecentPost.onRefreshComplete();
                } else {
                    mListviewRecentPost.onLoadMoreComplete();
                }

                Gson gson = new GsonBuilder().serializeNulls().create();
                mObGetAllBravoRecentPosts = gson.fromJson(response.toString(), ObGetAllBravoRecentPosts.class);
                AIOLog.d("obGetAllBravoRecentPosts:" + mObGetAllBravoRecentPosts);
                if (mObGetAllBravoRecentPosts == null)
                    return;
                else {
                    AIOLog.d("size of recent post list: " + mObGetAllBravoRecentPosts.data.size());
                    int reponseSize = mObGetAllBravoRecentPosts.data.size();
                    if (reponseSize <= 0)
                        return;
                    mAdapterRecentPost.updatePullDownLoadMorePostList(mObGetAllBravoRecentPosts, isPulDownToRefresh);
                    if (mListviewRecentPost.getVisibility() == View.GONE)
                        mListviewRecentPost.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void processIfResponseFail() {
                AIOLog.e("response error");
                if (isPulDownToRefresh) {
                    mListviewRecentPost.onRefreshComplete();
                } else {
                    mListviewRecentPost.onLoadMoreComplete();
                }
            }
        }, params, true);
        AIOLog.e("url: " + url);
        getPullDown_LoadMoreRequest.execute(url);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHomeActionListener = (HomeActionListener) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public interface IShowPageHomeNotification {
        public void showPageHomeNotification();
    }

    public void setListener(IShowPageHomeNotification iShowPageHomeNotification) {
        this.mListener = iShowPageHomeNotification;
    }
}
