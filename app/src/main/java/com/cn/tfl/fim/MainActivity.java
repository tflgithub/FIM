package com.cn.tfl.fim;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;

import com.cn.tfl.fim.activity.ActivitySupport;
import com.cn.tfl.fim.activity.LoginActivity;
import com.cn.tfl.fim.activity.im.ContacterMainActivity;
import com.cn.tfl.fim.adapter.MainPageAdapter;
import com.cn.tfl.fim.manager.XmppConnectionManager;
import com.cn.tfl.fim.model.MainPageItem;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;
@ContentView(R.layout.activity_main)
public class MainActivity extends ActivitySupport {
    @ViewInject(R.id.gridview)
    private GridView gridview;
    private List<MainPageItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        initView();
    }

    private void initView() {
        loadMenuList();
        MainPageAdapter adapter = new MainPageAdapter(this);
        adapter.setList(list);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final Intent intent = new Intent();
                switch (position) {
                    case 0:// 我的联系人
                        intent.setClass(context, ContacterMainActivity.class);
                        startActivity(intent);
                        break;
                    case 1:// 我的消息
//                        intent.setClass(context, MyNoticeActivity.class);
//                        startActivity(intent);
                        break;
                    case 2:// 企业通讯录
                        break;
                    case 3:// 个人通讯录
                        break;
                    case 4:// 我的邮件
                        break;
                    case 5:// 网络收藏夹
                        break;
                    case 6:// 个人文件夹
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private  void loadMenuList() {
        list = new ArrayList<>();
        list.add(new MainPageItem("我的联系人", R.drawable.mycontacts));
        list.add(new MainPageItem("我的消息", R.drawable.mynotice));
        list.add(new MainPageItem("企业通讯录", R.drawable.e_contact));
        list.add(new MainPageItem("个人通讯录", R.drawable.p_contact));
        list.add(new MainPageItem("邮件", R.drawable.email));
        list.add(new MainPageItem("单点登录", R.drawable.sso));
        list.add(new MainPageItem("个人文件夹", R.drawable.p_folder));
        list.add(new MainPageItem("我的笔记", R.drawable.mynote));
        list.add(new MainPageItem("我的签到", R.drawable.signin));
        list.add(new MainPageItem("我的工作日报", R.drawable.mydaily));
        list.add(new MainPageItem("我的日程", R.drawable.mymemo));
        list.add(new MainPageItem("设置", R.drawable.set));
    }


    @Override
    public void onBackPressed() {
        isExit();
    }
}
