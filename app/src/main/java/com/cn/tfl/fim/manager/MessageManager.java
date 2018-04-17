package com.cn.tfl.fim.manager;

import android.util.Log;

import com.cn.tfl.fim.model.IMMessage;
import com.cn.tfl.fim.utils.StringUtil;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tfl on 2016/5/23.
 */

public class MessageManager {

    private static String TAG = "MessageManager";

    private static MessageManager messageManager = null;

    private DbManager db;

    private MessageManager() {
        initDbManager();
    }

    private void initDbManager() {
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName("fim.db")
                .setDbVersion(2)
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) {
                        // 开启WAL, 对写入加速提升巨大
                        db.getDatabase().enableWriteAheadLogging();
                    }
                })
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                        // TODO: ...
                        // db.addColumn(...);
                        // db.dropTable(...);
                        // ...
                        // or
                        // db.dropDb();
                    }
                });
        db = x.getDb(daoConfig);
    }

    public static MessageManager getInstance() {

        if (messageManager == null) {
            messageManager = new MessageManager();
        }
        return messageManager;
    }


    /**
     * 保存消息到本地
     *
     * @param msg
     * @return
     */
    public void saveMessage(IMMessage msg) {
        try {
            db.save(msg);
        } catch (DbException e) {
            Log.e(TAG, "保存消息失败", e);
        }
    }

    /**
     * 删除同谁的消息记录
     *
     * @param fromUser
     * @return
     */
    public int delChatHisWithSb(String fromUser) {
        if (StringUtil.empty(fromUser)) {
            return 0;
        }
        try {
            return db.delete(IMMessage.class, WhereBuilder.b("fromSubJid", "=", fromUser));
        } catch (DbException e) {
            Log.e(TAG, "删除同" + fromUser + "消息失败", e);
        }
        return 0;
    }

    /**
     * 查询同谁的消息记录
     *
     * @param fromUser
     * @return
     */
    public List<IMMessage> getMessageListByFrom(String fromUser) {
        List<IMMessage> list = new ArrayList<>();
        try {
            list = db.selector(IMMessage.class).where("fromSubJid", "=", fromUser).findAll();
        } catch (DbException e) {
            Log.e(TAG, "查询同" + fromUser + "消息失败", e);
        }
        return list;
    }


    /**
     * 查找与某人的聊天记录总数
     */
    public int getChatCountWithSb(String fromUser) {
        List<IMMessage> list = getMessageListByFrom(fromUser);
        if (list != null) {
            return list.size();
        }
        return 0;
    }
}
