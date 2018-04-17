package com.cn.tfl.fim.activity.im;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.cn.tfl.fim.R;
import com.cn.tfl.fim.adapter.ContacterExpandAdapter;
import com.cn.tfl.fim.comm.Constant;
import com.cn.tfl.fim.manager.ContacterManager;
import com.cn.tfl.fim.manager.XmppConnectionManager;
import com.cn.tfl.fim.model.User;
import com.cn.tfl.fim.utils.StringUtil;
import com.cn.tfl.fim.view.ScrollLayout;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/20.
 */
@ContentView(R.layout.contacter_main)
public class ContacterMainActivity extends AContacterActivity implements ScrollLayout.LayoutChangeListener {
    private List<String> groupNames;
    private List<ContacterManager.MRosterGroup> rGroups;
    private LayoutInflater inflater;
    private ExpandableListView contacterList;
    private ScrollLayout layout;
    private ImageView tab1, tab2, tab3, imageView;
    private List<String> newNames = new ArrayList<>();
    private ContacterExpandAdapter expandAdapter = null;
    private User clickUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        init();
    }

    private void init() {
        try {
            ContacterManager.getInstance().init(XmppConnectionManager.getInstance().getConnection());
            Roster roster = Roster.getInstanceFor(XmppConnectionManager.getInstance().getConnection());
            groupNames = ContacterManager.getInstance().getGroupNames(roster);
            rGroups = ContacterManager.getInstance().getGroups(roster);
        } catch (Exception e) {
            groupNames = new ArrayList<>();
            rGroups = new ArrayList<>();
        }
        inflater = LayoutInflater.from(context);
        layout = (ScrollLayout) findViewById(R.id.scrolllayout);
        layout.addChangeListener(this);
        tab1 = (ImageView) findViewById(R.id.tab1);
        tab2 = (ImageView) findViewById(R.id.tab2);
        tab3 = (ImageView) findViewById(R.id.tab3);
        imageView = (ImageView) findViewById(R.id.top_bar_select);
        findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.ib_add_contacter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSubscriber();
            }
        });
        View contacterTab1 = inflater.inflate(R.layout.contacter_tab1, null);
        View contacterTab2 = inflater.inflate(R.layout.contacter_tab2, null);
        View contacterTab3 = inflater.inflate(R.layout.contacter_tab3, null);
        layout.addView(contacterTab1);
        layout.addView(contacterTab2);
        layout.addView(contacterTab3);
        layout.setToScreen(1);
        contacterList = (ExpandableListView) findViewById(R.id.main_expand_list);
        // 联系人
        expandAdapter = new ContacterExpandAdapter(context, rGroups);
        contacterList.setAdapter(expandAdapter);
        contacterList
                .setOnCreateContextMenuListener(onCreateContextMenuListener);
        contacterList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                createChat((User) v.findViewById(R.id.username).getTag());
                return false;
            }
        });
    }


    @Event({R.id.tab1, R.id.tab2, R.id.tab3})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tab1:
                layout.snapToScreen(0);
                break;
            case R.id.tab2:
                layout.snapToScreen(1);
                break;
            case R.id.tab3:
                layout.snapToScreen(2);
                break;
        }
    }

    View.OnCreateContextMenuListener onCreateContextMenuListener = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {

            ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

            // 类型，0代表是group类，1代表是child类
            int type = ExpandableListView
                    .getPackedPositionType(info.packedPosition);

            if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                int gId = ExpandableListView
                        .getPackedPositionGroup(info.packedPosition);
                String[] longClickItems = null;
                final String groupName = rGroups.get(gId).getName();
                if (StringUtil.notEmpty(groupName)
                        && !Constant.ALL_FRIEND.equals(groupName)
                        && !Constant.NO_GROUP_FRIEND.equals(groupName)) {
                    longClickItems = new String[]{"添加分组", "更改组名"};
                } else {
                    longClickItems = new String[]{"添加分组"};
                }
                new AlertDialog.Builder(context)
                        .setItems(longClickItems,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        switch (which) {
                                            case 0:// 添加分组
                                                addNewGroup();
                                                break;
                                            case 1:// 更改组名
                                                updateGroupNameA(groupName);
                                                break;
                                        }
                                    }
                                }).setTitle("选项").show();
            } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

                String[] longClickItems = null;

                View vx = info.targetView;
                clickUser = (User) vx.findViewById(R.id.username).getTag();
                showToast(clickUser.getJID() + "---");

                if (StringUtil.notEmpty(clickUser.getGroupName())
                        && !Constant.ALL_FRIEND
                        .equals(clickUser.getGroupName())
                        && !Constant.NO_GROUP_FRIEND.equals(clickUser
                        .getGroupName())) {
                    longClickItems = new String[]{"设置昵称", "添加好友", "删除好友",
                            "移动到分组", "退出该组"};
                } else {
                    longClickItems = new String[]{"设置昵称", "添加好友", "删除好友",
                            "移动到分组"};
                }
                new AlertDialog.Builder(context)
                        .setItems(longClickItems,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        switch (which) {
                                            case 0:// 设置昵称
                                                setNickname(clickUser);
                                                break;
                                            case 1:// 添加好友
                                                addSubscriber();
                                                break;
                                            case 2:// 删除好友
                                                showDeleteDialog(clickUser);
                                                break;
                                            case 3:// 移动到分组 （1.先移除本组，2移入某组）
                                                /**
                                                 * ui移除old组
                                                 */
                                                removeUserFromGroupUI(clickUser);
                                                removeUserFromGroup(clickUser,
                                                        clickUser.getGroupName());
                                                addToGroup(clickUser);
                                                break;

                                            case 4:// 移出组
                                                /**
                                                 * ui移除old组
                                                 */
                                                removeUserFromGroupUI(clickUser);
                                                /**
                                                 * api级出某组
                                                 */
                                                removeUserFromGroup(clickUser,
                                                        clickUser.getGroupName());
                                                break;
                                        }
                                    }

                                }).setTitle("选项").show();
            }
        }

    };

    /**
     * 添加好友
     */
    private void addSubscriber() {
        final EditText name_input = new EditText(context);
        name_input.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        name_input.setHint(getResources().getString(R.string.input_username));
        final EditText nickname = new EditText(context);
        nickname.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        nickname.setHint(getResources().getString(R.string.set_nickname));
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(name_input);
        layout.addView(nickname);
        new AlertDialog.Builder(context).setTitle("添加好友").setView(layout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userName = name_input.getText().toString();
                        String nickname_in = nickname.getText().toString();
                        String name= userName+ "@"+ XmppConnectionManager.getInstance().getConnection().getServiceName();
                        if (StringUtil.empty(userName)) {
                            showToast(getResources().getString(
                                    R.string.username_not_null));
                            return;
                        }
                        userName = StringUtil.doEmpty(userName);
                        if (StringUtil.empty(nickname_in)) {
                            nickname_in = null;
                        }

                        if (isExitJid(StringUtil.getJidByName(userName),
                                rGroups)) {
                            showToast(getResources().getString(
                                    R.string.username_exist));
                            return;
                        }
                        try {
                            createSubscriber(name,
                                    nickname_in, null);
                            Presence subscription = new Presence(Presence.Type.subscribe);
                            subscription.setTo(name);
                        } catch (XMPPException e) {
                        } catch (SmackException.NotLoggedInException e) {
                            e.printStackTrace();
                        } catch (SmackException.NoResponseException e) {
                            e.printStackTrace();
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("取消", null).show();
    }




    /**
     * 删除好友
     *
     * @param clickUser
     */
    private void showDeleteDialog(final User clickUser) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                getResources().getString(R.string.delete_user_confim))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // ui删除
                                deleteUserUI(clickUser);
                                // api删除
                                try {
                                    removeSubscriber(clickUser.getJID());
                                } catch (XMPPException e) {
                                } catch (SmackException.NotLoggedInException e) {
                                    e.printStackTrace();
                                } catch (SmackException.NoResponseException e) {
                                    e.printStackTrace();
                                } catch (SmackException.NotConnectedException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    /**
     * UI级删除好友
     */
    private void deleteUserUI(User user) {
        for (ContacterManager.MRosterGroup g : rGroups) {
            List<User> us = g.getUsers();
            if (us != null && us.size() > 0) {
                if (us.contains(user)) {
                    us.remove(user);
                    g.setUsers(us);
                }
            }
        }
        expandAdapter.setContacter(rGroups);
        expandAdapter.notifyDataSetChanged();

    }

    /**
     * 添加新组
     */
    private void addNewGroup() {
        final EditText name_input = new EditText(context);
        name_input.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        name_input.setHint("输入组名");
        new AlertDialog.Builder(context).setTitle("加入组").setView(name_input)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String groupName = name_input.getText().toString();
                        if (StringUtil.empty(groupName)) {
                            showToast("组名不能为空");
                            return;
                        }
                        // ui上增加数据
                        if (groupNames.contains(groupName)) {
                            showToast("组名已经存在");
                            return;
                        }
                        addGroupNamesUi(groupName);
                        addGroup(groupName);
                    }
                }).setNegativeButton("取消", null).show();
    }


    /**
     * 修改组名
     */
    private void updateGroupNameA(final String groupName) {
        final EditText name_input = new EditText(context);
        name_input.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        name_input.setHint("输入组名");
        new AlertDialog.Builder(context).setTitle("修改组名").setView(name_input)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String gNewName = name_input.getText().toString();
                        if (newNames.contains(gNewName)
                                || groupNames.contains(gNewName)) {
                            showToast("组名已存在");
                            return;
                        }
                        // UI级修改操作
                        updateGroupNameUI(groupName, gNewName);
                        // UIAPI
                        try {
                            updateGroupName(groupName, gNewName);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        } catch (XMPPException.XMPPErrorException e) {
                            e.printStackTrace();
                        } catch (SmackException.NoResponseException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("取消", null).show();
    }


    /**
     * UI更改组名
     */
    private void updateGroupNameUI(String old, String newGroupName) {

        if (StringUtil.empty(old) || Constant.ALL_FRIEND.equals(old)
                || Constant.NO_GROUP_FRIEND.equals(old)) {
            return;
        }
        // 虽然没必要，但是如果输入忘记限制
        if (StringUtil.empty(newGroupName)
                || Constant.ALL_FRIEND.equals(newGroupName)
                || Constant.NO_GROUP_FRIEND.equals(newGroupName)) {
            return;
        }

        // 要修改的组名是新添加的但是没有添加到服务器端的，只是ui级添加的，如下操作
        if (newNames.contains(old)) {
            newNames.remove(old);
            newNames.add(newGroupName);
            return;
        }
        // 列表修改;
        for (ContacterManager.MRosterGroup g : rGroups) {
            if (g.getName().equals(old)) {
                g.setName(newGroupName);
            }
        }
        expandAdapter.notifyDataSetChanged();
    }


    /**
     * UI级移动用户，把用户移除某组
     */

    private void removeUserFromGroupUI(User user) {

        for (ContacterManager.MRosterGroup g : rGroups) {
            if (g.getUsers().contains(user)) {
                if (StringUtil.notEmpty(g.getName())
                        && !Constant.ALL_FRIEND.equals(g.getName())) {
                    List<User> users = g.getUsers();
                    users.remove(user);
                    g.setUsers(users);

                }
            }
        }
        expandAdapter.setContacter(rGroups);
        expandAdapter.notifyDataSetChanged();
    }

    public void addGroupNamesUi(String newGroupName) {
        groupNames.add(newGroupName);
        newNames.add(newGroupName);
        ContacterManager.MRosterGroup mg = new ContacterManager.MRosterGroup(newGroupName, new ArrayList<User>());
        rGroups.add(rGroups.size() - 1, mg);
        expandAdapter.setContacter(rGroups);
        expandAdapter.notifyDataSetChanged();
    }


    /**
     * 加入组
     *
     * @param user
     */
    private void addToGroup(final User user) {
        LayoutInflater inflaterx = LayoutInflater.from(context);
        View dialogView = inflaterx.inflate(R.layout.yd_group_dialog, null);
        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.list);
        ArrayAdapter<String> ada = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, groupNames);
        spinner.setAdapter(ada);
        new AlertDialog.Builder(context).setTitle("移动" + "至分组")
                .setView(dialogView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String groupName = (spinner.getSelectedItem())
                                .toString();
                        if (StringUtil.notEmpty(groupName)) {
                            groupName = StringUtil.doEmpty(groupName);
                            if (newNames.contains(groupName)) {
                                newNames.remove(groupName);
                            }
                            // UI级把用户移到某组
                            addUserGroupUI(user, groupName);

                            // api移入组
                            addUserToGroup(user, groupName);
                        }
                    }
                }).setNegativeButton("取消", null).show();
    }


    /**
     * UI级移动用户，把用户加入某组
     */

    private void addUserGroupUI(User user, String groupName) {
        for (ContacterManager.MRosterGroup g : rGroups) {
            if (groupName.equals(g.getName())) {
                List<User> users = g.getUsers();
                users.add(user);
                g.setUsers(users);
            }
        }
        expandAdapter.setContacter(rGroups);
        expandAdapter.notifyDataSetChanged();
    }

    /**
     * 设置昵称
     *
     * @param user
     */
    private void setNickname(final User user) {
        final EditText name_input = new EditText(context);
        name_input.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        name_input.setHint("输入昵称");
        new AlertDialog.Builder(context).setTitle("修改昵称").setView(name_input)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = name_input.getText().toString();
                        if (!"".equals(name))
                            try {
                                setNickname(user, name);
                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            } catch (XMPPException.XMPPErrorException e) {
                                e.printStackTrace();
                            } catch (SmackException.NoResponseException e) {
                                e.printStackTrace();
                            }
                    }
                }).setNegativeButton("取消", null).show();
    }

    @Override
    protected void addUserReceive(User user) {

    }

    @Override
    protected void deleteUserReceive(User user) {

    }

    @Override
    protected void changePresenceReceive(User user) {

    }

    @Override
    protected void updateUserReceive(User user) {

    }

    @Override
    protected void subscripUserReceive(String subFrom) {

    }

    @Override
    protected void handReConnect(boolean isSuccess) {

    }

    @Override
    public void doChange(int lastIndex, int currentIndex) {
        if (lastIndex != currentIndex) {
            TranslateAnimation animation = null;
            LinearLayout layout = null;
            switch (currentIndex) {
                case 0:
                    if (lastIndex == 1) {
                        layout = (LinearLayout) tab1.getParent();
                        animation = new TranslateAnimation(0, -layout.getWidth(),
                                0, 0);
                    } else if (lastIndex == 2) {
                        layout = (LinearLayout) tab2.getParent();
                        animation = new TranslateAnimation(layout.getLeft(),
                                -((LinearLayout) tab1.getParent()).getWidth(), 0, 0);
                    }
                    break;
                case 1:
                    if (lastIndex < 1) {
                        // 左到中
                        layout = (LinearLayout) tab1.getParent();
                        animation = new TranslateAnimation(-layout.getWidth(), 0,
                                0, 0);
                    } else if (lastIndex > 1) {
                        // 右到中
                        layout = (LinearLayout) tab2.getParent();
                        animation = new TranslateAnimation(layout.getLeft(), 0, 0,
                                0);
                    }
                    break;
                case 2:
                    if (lastIndex == 1) {
                        layout = (LinearLayout) tab2.getParent();
                        animation = new TranslateAnimation(0, layout.getLeft(), 0,
                                0);
                    } else if (lastIndex == 0) {
                        layout = (LinearLayout) tab2.getParent();
                        animation = new TranslateAnimation(
                                -((LinearLayout) tab1.getParent()).getWidth(),
                                layout.getLeft(), 0, 0);
                    }
                    break;
            }
            animation.setDuration(300);
            animation.setFillAfter(true);
            imageView.startAnimation(animation);
        }
    }
}
