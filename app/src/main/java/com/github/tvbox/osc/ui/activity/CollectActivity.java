package com.github.tvbox.osc.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import android.os.Environment;
import android.widget.Toast;

import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.cache.VodCollect;
import com.github.tvbox.osc.data.AppDataManager;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.adapter.CollectAdapter;
import com.github.tvbox.osc.ui.dialog.ConfirmClearDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.FileUtils;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.LOG;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CollectActivity extends BaseActivity {
    private ImageView tvDelete;
    private ImageView tvClear;
    private TextView tvExport;
    private TextView tvImport;
    private TextView tvDelTip;
    private TvRecyclerView mGridView;
    public static CollectAdapter collectAdapter;
    private boolean delMode = false;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_collect;
    }

    @Override
    protected void init() {
        initView();
        initData();
    }

    private void toggleDelMode() {
    	HawkConfig.hotVodDelete = !HawkConfig.hotVodDelete;
        collectAdapter.notifyDataSetChanged();
        delMode = !delMode;
        tvDelTip.setVisibility(delMode ? View.VISIBLE : View.GONE);
    }

    private void initView() {
        EventBus.getDefault().register(this);
        tvDelete = findViewById(R.id.tvDelete);
        tvClear = findViewById(R.id.tvClear);
        tvExport = findViewById(R.id.tvExport);
        tvImport = findViewById(R.id.tvImport);
        tvDelTip = findViewById(R.id.tvDelTip);
        mGridView = findViewById(R.id.mGridView);
        mGridView.setHasFixedSize(true);
        mGridView.setLayoutManager(new V7GridLayoutManager(this.mContext, isBaseOnWidth() ? 5 : 6));
        collectAdapter = new CollectAdapter();
        mGridView.setAdapter(collectAdapter);
        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDelMode();
            }
        });
        tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmClearDialog dialog = new ConfirmClearDialog(mContext, "Collect");
                dialog.show();
            }
        });
        tvExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportFavorites();
            }
        });
        tvImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importFavorites();
            }
        });
        mGridView.setOnInBorderKeyEventListener(new TvRecyclerView.OnInBorderKeyEventListener() {
            @Override
            public boolean onInBorderKeyEvent(int direction, View focused) {
                if (direction == View.FOCUS_UP) {
                    tvDelete.setFocusable(true);
                    tvClear.setFocusable(true);
                    tvDelete.requestFocus();
                }
                return false;
            }
        });
        mGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator()).start();
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator()).start();
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        collectAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                VodCollect vodInfo = collectAdapter.getData().get(position);
                if (vodInfo != null) {
                    if (delMode) {
                        collectAdapter.remove(position);
                        RoomDataManger.deleteVodCollect(vodInfo.getId());
                    } else {
                        if (ApiConfig.get().getSource(vodInfo.sourceKey) != null) {
                            Bundle bundle = new Bundle();
                            bundle.putString("id", vodInfo.vodId);
                            bundle.putString("sourceKey", vodInfo.sourceKey);
                            bundle.putString("picture", vodInfo.pic);
                            jumpActivity(DetailActivity.class, bundle);
                        } else {
                            Intent newIntent = new Intent(mContext, SearchActivity.class);
                            newIntent.putExtra("title", vodInfo.name);
                            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(newIntent);
                        }
                    }
                }
            }
        });
        collectAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
//                FastClickCheckUtil.check(view);
//                VodCollect vodInfo = collectAdapter.getData().get(position);
//                collectAdapter.remove(position);
//                RoomDataManger.deleteVodCollect(vodInfo.getId());
                tvDelete.setFocusable(true);
                toggleDelMode();
                return true;
            }
        });
    }

    private void initData() {
        List<VodCollect> allVodRecord = RoomDataManger.getAllVodCollect();
        List<VodCollect> vodInfoList = new ArrayList<>();
        for (VodCollect vodInfo : allVodRecord) {
            vodInfoList.add(vodInfo);
        }
        collectAdapter.setNewData(vodInfoList);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent event) {
        if (event.type == RefreshEvent.TYPE_HISTORY_REFRESH) {
            initData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void exportFavorites() {
        try {
            List<VodCollect> list = RoomDataManger.getAllVodCollect();
            JSONArray arr = new JSONArray();
            for (VodCollect v : list) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("vodId", v.vodId);
                obj.put("sourceKey", v.sourceKey);
                obj.put("name", v.name);
                obj.put("pic", v.pic);
                obj.put("updateTime", v.updateTime);
                arr.put(obj);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            File dir = new File(getExternalFilesDir(null), "tvbox_backup");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "favorites_" + sdf.format(new Date()) + ".json");
            FileWriter fw = new FileWriter(file);
            fw.write(arr.toString(2));
            fw.close();
            Toast.makeText(this, "收藏已导出: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importFavorites() {
        try {
            File dir = new File(getExternalFilesDir(null), "tvbox_backup");
            if (!dir.exists() || !dir.isDirectory()) {
                Toast.makeText(this, "未找到导出文件", Toast.LENGTH_SHORT).show();
                return;
            }
            File[] files = dir.listFiles((d, name) -> name.startsWith("favorites_") && name.endsWith(".json"));
            if (files == null || files.length == 0) {
                Toast.makeText(this, "未找到收藏导出文件", Toast.LENGTH_SHORT).show();
                return;
            }
            // 取最新的
            File latest = files[0];
            for (File f : files) {
                if (f.lastModified() > latest.lastModified()) latest = f;
            }
            BufferedReader br = new BufferedReader(new FileReader(latest));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            JSONArray arr = new JSONArray(sb.toString());
            int count = 0;
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                VodCollect v = new VodCollect();
                v.vodId = obj.optString("vodId", "");
                v.sourceKey = obj.optString("sourceKey", "");
                v.name = obj.optString("name", "");
                v.pic = obj.optString("pic", "");
                v.updateTime = obj.optLong("updateTime", System.currentTimeMillis());
                com.github.tvbox.osc.cache.VodCollect existing = AppDataManager.get().getVodCollectDao().getVodCollect(v.sourceKey, v.vodId);
                if (existing == null) {
                    AppDataManager.get().getVodCollectDao().insert(v);
                    count++;
                }
            }
            initData();
            Toast.makeText(this, "已导入 " + count + " 条收藏", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "导入失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (delMode) {
            toggleDelMode();
            return;
        }
        super.onBackPressed();
    }
}