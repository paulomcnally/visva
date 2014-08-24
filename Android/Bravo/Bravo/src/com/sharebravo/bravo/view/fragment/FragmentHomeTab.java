package com.sharebravo.bravo.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.sharebravo.bravo.R;
import com.sharebravo.bravo.control.activity.HomeActionListener;
import com.sharebravo.bravo.view.adapter.AdapterRecentPost;
import com.sharebravo.bravo.view.lib.PullAndLoadListView;

public class FragmentHomeTab extends FragmentBasic {
    private PullAndLoadListView listviewRecentPost  = null;
    private AdapterRecentPost   adapterRecentPost   = null;
    private HomeActionListener  mHomeActionListener = null;
    private OnItemClickListener recentPostClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            mHomeActionListener.goToRecentPostDetail();
        }};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = (ViewGroup) inflater.inflate(R.layout.page_home_tab,
                null);

        listviewRecentPost = (PullAndLoadListView) root.findViewById(R.id.listview_recent_post);
        adapterRecentPost = new AdapterRecentPost(getActivity());
        listviewRecentPost.setAdapter(adapterRecentPost);
        listviewRecentPost.setOnItemClickListener(recentPostClickListener);
        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHomeActionListener = (HomeActionListener) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}