package com.didichuxing.doraemonkit.kit.custom;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.didichuxing.doraemonkit.R;
import com.didichuxing.doraemonkit.kit.common.PerformanceDataManager;
import com.didichuxing.doraemonkit.ui.base.BaseFragment;
import com.didichuxing.doraemonkit.ui.fileexplorer.FileInfo;
import com.didichuxing.doraemonkit.ui.widget.recyclerview.DividerItemDecoration;
import com.didichuxing.doraemonkit.ui.widget.titlebar.HomeTitleBar;
import com.didichuxing.doraemonkit.ui.widget.titlebar.TitleBar;
import com.didichuxing.doraemonkit.util.JsonUtil;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PageDataFragment extends BaseFragment {
    private RecyclerView mRvList;
    private PageDataItemAdapter mAdapter;

    @Override
    protected int onRequestLayout() {
        return R.layout.dk_fragment_monitor_pagedata;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();

        initData();
    }

    private void initView() {
        TitleBar titleBar = findViewById(R.id.title_bar);
        titleBar.setOnTitleBarClickListener(new TitleBar.OnTitleBarClickListener() {
            @Override
            public void onLeftClick() {
                getActivity().onBackPressed();
            }

            @Override
            public void onRightClick() {

            }
        });

        mRvList = findViewById(R.id.info_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRvList.setLayoutManager(layoutManager);
        mAdapter = new PageDataItemAdapter(getContext());
        mRvList.setAdapter(mAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(DividerItemDecoration.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.dk_divider_gray));
        mRvList.addItemDecoration(decoration);
        mRvList.setAdapter(mAdapter);
    }

    private void initData() {
        new LoadDataTask().execute(PerformanceDataManager.getInstance().getCustomFilePath());
    }

    private class LoadDataTask extends AsyncTask<String, Integer, List<PageDataItem>> {
        @Override
        protected void onPostExecute(List<PageDataItem> items) {
            mAdapter.setData(items);
        }

        @Override
        protected List<PageDataItem> doInBackground(String... strings) {
            File file = new File(strings[0]);

            String fileString = getFileString(file);
            fileString = '['+fileString+']';
            fileString = fileString.replaceAll("\\}\\{","},{");

            List<UploadMonitorInfoBean> infoBeans = JsonUtil.jsonToList(fileString, UploadMonitorInfoBean.class);

            return convert2ItemData(infoBeans);
        }
    }

    private List<PageDataItem> convert2ItemData(List<UploadMonitorInfoBean> infoBeans) {
        List<PageDataItem> dataItems = new ArrayList<>();
        if(null == infoBeans || 0 >= infoBeans.size()){
            return dataItems;
        }

        for (UploadMonitorInfoBean infoBean : infoBeans) {
            if(null == infoBean || null == infoBean.performanceArray || 0 >= infoBean.performanceArray.size()){
                continue;
            }

            PageDataItem item = new PageDataItem();

            List<PerformanceInfo> performanceInfos = infoBean.performanceArray;
            item.pageName = infoBean.appName;

            item.upNetWork  =new PageDataItemChild<>(R.string.dk_frameinfo_upstream);
            item.downNetWork = new PageDataItemChild<>(R.string.dk_frameinfo_downstream);
            item.memory = new PageDataItemChild<>(R.string.dk_frameinfo_ram);
            item.cpu = new PageDataItemChild<>(R.string.dk_frameinfo_cpu);
            item.fps = new PageDataItemChild<>(R.string.dk_frameinfo_fps);

            for (PerformanceInfo info : performanceInfos) {
                setValue(item.memory,info.memory);
                setValue(item.cpu,info.cpu);
                setValue(item.fps,info.fps);
            }

            item.memory.avg /= performanceInfos.size();
            item.cpu.avg /= performanceInfos.size();
            item.fps.avg /= performanceInfos.size();

            dataItems.add(item);
        }

        return dataItems;
    }
    private void setValue(PageDataItemChild<Float> child, float newValue) {
        child.min = Math.min(null == child.min ? 0:child.min, newValue);
        child.max = Math.max(null == child.max ? 0:child.max, newValue);
        child.avg = (null == child.avg ? 0:child.avg)+newValue;
    }

    private void setValue(PageDataItemChild<Integer> child, int newValue) {
        child.min = Math.min(null == child.min ? 0:child.min, newValue);
        child.max = Math.max(null == child.max ? 0:child.max, newValue);
        child.avg = (null == child.avg ? 0:child.avg)+newValue;
    }

    private void setValue(PageDataItemChild<Double> child, double newValue) {
        child.min = Math.min(null == child.min ? 0:child.min, newValue);
        child.max = Math.max(null == child.max ? 0:child.max, newValue);
        child.avg = (null == child.avg ? 0:child.avg)+newValue;
    }

    private String getFileString(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        if (file.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String tempString = null;
                while ((tempString = reader.readLine()) != null) {
                    stringBuilder.append(tempString);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        return stringBuilder.toString();
    }
}
