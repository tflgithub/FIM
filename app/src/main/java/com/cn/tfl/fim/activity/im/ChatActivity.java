package com.cn.tfl.fim.activity.im;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cn.tfl.fim.R;
import com.cn.tfl.fim.adapter.EmoViewPagerAdapter;
import com.cn.tfl.fim.adapter.EmoteAdapter;
import com.cn.tfl.fim.manager.ContacterManager;
import com.cn.tfl.fim.manager.MessageManager;
import com.cn.tfl.fim.manager.XmppConnectionManager;
import com.cn.tfl.fim.model.FaceText;
import com.cn.tfl.fim.model.IMMessage;
import com.cn.tfl.fim.model.User;
import com.cn.tfl.fim.utils.BitmapCommon;
import com.cn.tfl.fim.utils.CommonUtils;
import com.cn.tfl.fim.utils.FaceTextUtils;
import com.cn.tfl.fim.utils.StringUtil;
import com.cn.tfl.fim.view.EmoticonsEditText;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.activity_chat)
public class ChatActivity extends AChatActivity implements View.OnClickListener {

    private static String LOG_TAG = "ChatActivity";
    @ViewInject(R.id.btn_chat_add)
    private Button btn_chat_add;
    @ViewInject(R.id.btn_chat_emo)
    private Button btn_chat_emo;
    @ViewInject(R.id.btn_chat_send)
    private Button btn_chat_send;
    @ViewInject(R.id.btn_chat_keyboard)
    private Button btn_chat_keyboard;
    @ViewInject(R.id.btn_speak)
    private Button btn_speak;
    @ViewInject(R.id.btn_chat_voice)
    private Button btn_chat_voice;
    @ViewInject(R.id.layout_add)
    private LinearLayout layout_add;
    @ViewInject(R.id.layout_emo)
    private LinearLayout layout_emo;
    @ViewInject(R.id.layout_more)
    private LinearLayout layout_more;
    @ViewInject(R.id.edit_user_comment)
    private EmoticonsEditText messageInput;
    @ViewInject(R.id.pager_emo)
    private ViewPager pager_emo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        initView();
    }

    private void initView() {
        btn_chat_emo.setOnClickListener(this);
        btn_chat_add.setOnClickListener(this);
        btn_chat_send.setOnClickListener(this);
        btn_chat_voice.setOnClickListener(this);
        btn_chat_keyboard.setOnClickListener(this);
        initBottomView();
        initVoiceView();
        init();
    }

    private void initBottomView() {
        messageInput.setOnClickListener(this);
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub
                if (!TextUtils.isEmpty(s)) {
                    btn_chat_send.setVisibility(View.VISIBLE);
                    btn_chat_keyboard.setVisibility(View.GONE);
                    btn_chat_voice.setVisibility(View.GONE);
                } else {
                    if (btn_chat_voice.getVisibility() != View.VISIBLE) {
                        btn_chat_voice.setVisibility(View.VISIBLE);
                        btn_chat_send.setVisibility(View.GONE);
                        btn_chat_keyboard.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });
        initEmoView();
    }


    @ViewInject(R.id.to_chat_name)
    private TextView tvChatTitle;
    @ViewInject(R.id.chat_list)
    private ListView listView;
    private String to_name;
    private MessageListAdapter adapter;
    private View listHead;

    private void init() {
        findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 与谁聊天
        user = ContacterManager.getByUserJid(to, XmppConnectionManager
                .getInstance().getConnection());
        if (null == user) {
            to_name = StringUtil.getUserNameByJid(to);
        } else {
            to_name = user.getName() == null ? user.getJID() : user.getName();

        }
        tvChatTitle.setText(to_name);
        listView = (ListView) findViewById(R.id.chat_list);
        listView.setCacheColorHint(0);
        adapter = new MessageListAdapter(ChatActivity.this, getMessages(),
                listView);
        LayoutInflater mynflater = LayoutInflater.from(context);
        listHead = mynflater.inflate(R.layout.chatlistheader, null);
        listHead.findViewById(R.id.buttonChatHistory).setOnClickListener(chatHistoryCk);
        listView.addHeaderView(listHead);
        listView.setAdapter(adapter);
        findViewById(R.id.btn_chat_send).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString();
                if ("".equals(message)) {
                    showToast("不能为空");
                } else {

                    try {
                        sendMessage(message);
                        messageInput.setText("");
                    } catch (Exception e) {
                        showToast("信息发送失败");
                        messageInput.setText(message);
                    }
                    closeInput();
                }
            }
        });
    }


    /**
     * 点击进入聊天记录
     */
    private View.OnClickListener chatHistoryCk = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
//            Intent in = new Intent(context, ChatHistoryActivity.class);
//            in.putExtra("to", to);
//            startActivity(in);
        }
    };

    private void initEmoView() {
        pager_emo = (ViewPager) findViewById(R.id.pager_emo);
        emos = FaceTextUtils.faceTexts;
        List<View> views = new ArrayList<View>();
        for (int i = 0; i < 2; ++i) {
            views.add(getGridView(i));
        }
        pager_emo.setAdapter(new EmoViewPagerAdapter(views));
    }

    List<FaceText> emos;

    private View getGridView(final int i) {
        View view = View.inflate(this, R.layout.include_emo_gridview, null);
        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        List<FaceText> list = new ArrayList<>();
        if (i == 0) {
            list.addAll(emos.subList(0, 21));
        } else if (i == 1) {
            list.addAll(emos.subList(21, emos.size()));
        }
        final EmoteAdapter gridAdapter = new EmoteAdapter(ChatActivity.this,
                list);
        gridview.setAdapter(gridAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                FaceText name = (FaceText) gridAdapter.getItem(position);
                String key = name.text.toString();
                try {
                    if (messageInput != null && !TextUtils.isEmpty(key)) {
                        int start = messageInput.getSelectionStart();
                        CharSequence content = messageInput.getText().insert(start, key);
                        messageInput.setText(content);
                        CharSequence info = messageInput.getText();
                        if (info instanceof Spannable) {
                            Spannable spanText = (Spannable) info;
                            Selection.setSelection(spanText,
                                    start + key.length());
                        }
                    }
                } catch (Exception e) {

                }

            }
        });
        return view;
    }


    @ViewInject(R.id.layout_record)
    private RelativeLayout layout_record;
    @ViewInject(R.id.tv_voice_tips)
    private TextView tv_voice_tips;
    @ViewInject(R.id.iv_record)
    private ImageView iv_record;
    private Handler mHandler = new Handler();
    private static final int POLL_INTERVAL = 300;
    /**
     * 语音文件保存路径
     */
    private String mFileName = null;
    /**
     * 按住说话按钮
     */
    private Button mBtnVoice;
    /**
     * 用于语音播放
     */
    private MediaPlayer mPlayer = null;
    /**
     * 用于完成录音
     */
    private MediaRecorder mRecorder = null;
    private static final String PATH = "/sdcard/MyVoiceForder/Record/";
    private static final String PATHIMG = "/sdcard/MyImageForder/Record/";

    /**
     * 开始录音
     */
    private String startVoice() {
        String dir = String.valueOf(System.currentTimeMillis());
        // 设置录音保存路径
        mFileName = PATH + dir + ".amr";
        File directory = new File(mFileName).getParentFile();
        showToast("开始录音");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mRecorder.start();
        return dir;
    }

    /**
     * 停止录音
     */
    private void stopVoice() {
        //(BUG)如果录音不足1s ，就停止会报错。
        mRecorder.setOnErrorListener(null);
        mRecorder.setPreviewDisplay(null);
        try {
            mRecorder.stop();
            mRecorder.release();
        } catch (Exception e) {
        }
        mRecorder = null;
    }


    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = mRecorder.getMaxAmplitude() / 2700.0;
            updateDisplay(amp);
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };


    private void updateDisplay(double signalEMA) {

        switch ((int) signalEMA) {
            case 0:
            case 1:
            case 2:
                iv_record.setImageResource(R.drawable.chat_icon_voice1);
                break;
            case 3:
            case 4:
            case 5:
                iv_record.setImageResource(R.drawable.chat_icon_voice2);
                break;
            case 6:
            case 7:
            case 8:
                iv_record.setImageResource(R.drawable.chat_icon_voice3);
                break;
            case 9:
            case 10:
            case 11:
                iv_record.setImageResource(R.drawable.chat_icon_voice4);
                break;
            case 12:
            case 13:
            case 14:
                iv_record.setImageResource(R.drawable.chat_icon_voice5);
                break;
            default:
                iv_record.setImageResource(R.drawable.chat_icon_voice6);
                break;
        }
    }

    private Runnable mSleepTask = new Runnable() {
        public void run() {
        }
    };

    private void initVoiceView() {
        btn_speak.setOnTouchListener(new View.OnTouchListener() {
            long beforeTime;
            long afterTime;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String dir = null;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        beforeTime = System.currentTimeMillis();
                        try {
                            v.setPressed(true);
                            tv_voice_tips
                                    .setText(getString(R.string.voice_cancel_tips));
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    layout_record.setVisibility(View.VISIBLE);
                                }
                            }, 300);
                        } catch (Exception e) {
                        }
                        startVoice();
                        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
                        break;
                    case MotionEvent.ACTION_UP:
                        afterTime = System.currentTimeMillis();
                        v.setPressed(false);
                        layout_record.setVisibility(View.INVISIBLE);
                        mHandler.removeCallbacks(mSleepTask);
                        mHandler.removeCallbacks(mPollTask);
                        stopVoice();
                        String voiceFile = CommonUtils.VOICE_SIGN
                                + CommonUtils.GetImageStr(mFileName) + "@" + dir
                                + CommonUtils.VOICE_SIGN;
                        if ("".equals(mFileName)) {
                            showToast("不能为空");
                        } else {
                            // (afterTime-beforeTime)/1000
                            try {
                                if ((afterTime - beforeTime) < 500) {
                                    showToast("录音时间太短！");
                                    File file = new File(mFileName);
                                    file.delete();
                                } else {
                                    sendMessage(voiceFile + "&"
                                            + (afterTime - beforeTime) / 1000);
                                }
                            } catch (Exception e) {
                                showToast("信息发送失败");
                            }
                            closeInput();
                        }
                        iv_record.setImageResource(R.drawable.chat_icon_voice1);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private Bitmap bp;
    private User user;// 聊天人

    private class MessageListAdapter extends BaseAdapter {
        private List<IMMessage> items;
        private Context context;
        private ListView adapterList;
        private LayoutInflater inflater;

        public MessageListAdapter(Context context, List<IMMessage> items,
                                  ListView adapterList) {
            this.context = context;
            this.items = items;
            this.adapterList = adapterList;
        }

        public void refreshList(List<IMMessage> items) {
            this.items = items;
            this.notifyDataSetChanged();
            adapterList.setSelection(items.size() - 1);
        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            IMMessage message = items.get(position);
            String[] strarray;
            String voiceFile;
            String voiceTime;
            String path;
            final Intent intent = new Intent();
            boolean result = false;
            List<String> list = CommonUtils.getImagePathFromSD();
            int msgViewTime;
            if (message.getMsgType() == 0) {
                if (message.getContent().contains(CommonUtils.PIC_SIGN)) {
                    convertView = this.inflater.inflate(
                            R.layout.chat_rce_picture, null);
                } else {
                    convertView = this.inflater.inflate(
                            R.layout.formclient_chat_in, null);
                }
            } else {
                if (message.getContent().contains(CommonUtils.PIC_SIGN)) {
                    convertView = this.inflater.inflate(
                            R.layout.chat_send_picture, null);
                } else {
                    convertView = this.inflater.inflate(
                            R.layout.formclient_chat_out, null);
                }
            }
            ImageView imageMsg = (ImageView) convertView
                    .findViewById(R.id.send_picture);
            TextView useridView = (TextView) convertView
                    .findViewById(R.id.formclient_row_userid);
            TextView dateView = (TextView) convertView
                    .findViewById(R.id.formclient_row_date);
            TextView msgView = (TextView) convertView
                    .findViewById(R.id.formclient_row_msg);
            TextView voiceTimeView = (TextView) convertView
                    .findViewById(R.id.voice_time);
            dateView.setText(message.getTime());
            if (message.getMsgType() == 0) {

                if (message.getContent().contains(CommonUtils.PIC_SIGN)) {
                    String[] arr = message.getContent().split(
                            CommonUtils.PIC_SIGN);
                    String[] brr = arr[1].split("@");
                    String imgFilePath = PATH + brr[1] + ".jpg";
                    result = CommonUtils.judge(list, imgFilePath);
                    if (!result) {
                        imgFilePath = CommonUtils.GenerateImage(brr[0], brr[1]);
                    }
                    try {
                        // 实例化Bitmap
                        bp = BitmapCommon.setBitmapSize(imgFilePath);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    }
                    imageMsg.setImageBitmap(bp);
                    final String dir = imgFilePath;
                    imageMsg.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            intent.putExtra("dir", dir);
                            intent.setClass(ChatActivity.this, ImageShower.class);
                            startActivity(intent);
                        }
                    });
                } else if (message.getContent()
                        .contains(CommonUtils.VOICE_SIGN)) {
                    path = CommonUtils
                            .getMediaPath(items, message.getContent());
                    strarray = message.getContent().split("&");
                    voiceFile = strarray[0];
                    voiceTime = strarray[1];
                    msgViewTime = Integer.parseInt(voiceTime);
                    String msgViewLength = "";
                    voiceTimeView.setVisibility(View.VISIBLE);
                    voiceTimeView.setText(voiceTime + "\"");
                    for (int i = 0; i < msgViewTime; i++) {
                        msgViewLength += "  ";
                    }
                    String voiceArr[] = voiceFile.split(CommonUtils.VOICE_SIGN);
                    String voiceBrr[] = voiceArr[1].split("@");
                    String imgFilePath = PATH + voiceBrr[1] + ".amr";
                    result = CommonUtils.judge(list, imgFilePath);
                    final String voiceFileDir;
                    if (result) {
                        voiceFileDir = imgFilePath;
                    } else {
                        voiceFileDir = CommonUtils.GenerateVoic(voiceBrr[0],
                                voiceBrr[1]);

                    }
                    msgView.setText(msgViewLength);
                    msgView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                            R.drawable.chatto_voice_playing, 0);
                    msgView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            System.out.println(voiceFileDir);
                            if (voiceFileDir.contains(".amr")) {
                                CommonUtils.playMusic(voiceFileDir);
                            }
                        }
                    });
                } else {
                    SpannableString spannableString = FaceTextUtils
                            .toSpannableString(context, message.getContent());
                    msgView.setText(spannableString);
                }
                if (null == user) {
                    useridView.setText(StringUtil.getUserNameByJid(to));
                } else {
                    useridView.setText(user.getName());
                }

            } else {
                if (message.getContent().contains(CommonUtils.PIC_SIGN)) {
                    String[] arr = message.getContent().split(
                            CommonUtils.PIC_SIGN);
                    String[] brr = arr[1].split("@");
                    String imgFilePath = PATH + brr[1] + ".jpg";
                    result = CommonUtils.judge(list, imgFilePath);
                    if (!result) {
                        imgFilePath = CommonUtils.GenerateImage(brr[0], brr[1]);
                    }
                    try {
                        // 实例化Bitmap
                        bp = BitmapCommon.setBitmapSize(imgFilePath);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    }
                    imageMsg.setImageBitmap(bp);
                    final String dir = imgFilePath;
                    imageMsg.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            intent.putExtra("dir", dir);
                            intent.setClass(ChatActivity.this, ImageShower.class);
                            startActivity(intent);
                        }
                    });
                } else if (message.getContent().contains(CommonUtils.VOICE_SIGN)) {
                    path = CommonUtils
                            .getMediaPath(items, message.getContent());
                    strarray = message.getContent().split("&");
                    voiceFile = strarray[0];
                    voiceTime = strarray[1];
                    msgViewTime = Integer.parseInt(voiceTime);
                    String msgViewLength = "";
                    voiceTimeView.setVisibility(View.VISIBLE);
                    voiceTimeView.setText(voiceTime + "\"");
                    for (int i = 0; i < msgViewTime; i++) {
                        msgViewLength += "  ";
                    }
                    String voiceArr[] = voiceFile.split(CommonUtils.VOICE_SIGN);
                    String voiceBrr[] = voiceArr[1].split("@");
                    String imgFilePath = PATH + voiceBrr[1] + ".amr";
                    result = CommonUtils.judge(list, imgFilePath);
                    final String voiceFileDir;
                    if (result) {
                        voiceFileDir = imgFilePath;
                    } else {
                        voiceFileDir = CommonUtils.GenerateVoic(voiceBrr[0],
                                voiceBrr[1]);

                    }
                    msgView.setText(msgViewLength);
                    msgView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                            R.drawable.chatto_voice_playing, 0);
                    msgView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            System.out.println(voiceFileDir);
                            if (voiceFileDir.contains(".amr")) {
                                CommonUtils.playMusic(voiceFileDir);
                            }
                        }
                    });
                } else {
                    SpannableString spannableString = FaceTextUtils
                            .toSpannableString(context, message.getContent());
                    msgView.setText(spannableString);
                }
                useridView.setText("我");
            }
            return convertView;
        }

    }

    @Override
    protected void receiveNewMessage(IMMessage message) {

    }

    @Override
    protected void refreshMessage(List<IMMessage> messages) {
        adapter.refreshList(messages);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_user_comment:
                if (layout_more.getVisibility() == View.VISIBLE) {
                    layout_add.setVisibility(View.GONE);
                    layout_emo.setVisibility(View.GONE);
                    layout_more.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_chat_emo:
                if (layout_more.getVisibility() == View.GONE) {
                    showEditState(true);
                } else {
                    if (layout_add.getVisibility() == View.VISIBLE) {
                        layout_add.setVisibility(View.GONE);
                        layout_emo.setVisibility(View.VISIBLE);
                    } else {
                        layout_more.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.btn_chat_send:
                String message = messageInput.getText().toString();
                if ("".equals(message)) {
                    showToast("不能为空");
                } else {
                    try {
                        sendMessage(message);
                        messageInput.setText("");
                    } catch (Exception e) {
                        showToast("信息发送失败");
                        messageInput.setText(message);
                    }
                    closeInput();
                }
                break;
            case R.id.btn_chat_add:
                if (layout_more.getVisibility() == View.GONE) {
                    layout_more.setVisibility(View.VISIBLE);
                    layout_add.setVisibility(View.VISIBLE);
                    layout_emo.setVisibility(View.GONE);
                    hideSoftInputView();
                } else {
                    if (layout_emo.getVisibility() == View.VISIBLE) {
                        layout_emo.setVisibility(View.GONE);
                        layout_add.setVisibility(View.VISIBLE);
                    } else {
                        layout_more.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.btn_chat_voice:
                messageInput.setVisibility(View.GONE);
                layout_more.setVisibility(View.GONE);
                btn_chat_voice.setVisibility(View.GONE);
                btn_chat_keyboard.setVisibility(View.VISIBLE);
                btn_speak.setVisibility(View.VISIBLE);
                hideSoftInputView();
                break;
            case R.id.btn_chat_keyboard:
                showEditState(false);
                break;
            case R.id.tv_camera:
                //selectImageFromCamera();
                break;
            case R.id.tv_picture:
                //selectImageFromLocal();
                break;
            case R.id.tv_location:
                // selectLocationFromMap();
                break;
            default:
                break;
        }
    }


    private void showEditState(boolean isEmo) {
        messageInput.setVisibility(View.VISIBLE);
        btn_chat_keyboard.setVisibility(View.GONE);
        btn_chat_voice.setVisibility(View.VISIBLE);
        btn_speak.setVisibility(View.GONE);
        messageInput.requestFocus();
        if (isEmo) {
            layout_more.setVisibility(View.VISIBLE);
            layout_more.setVisibility(View.VISIBLE);
            layout_emo.setVisibility(View.VISIBLE);
            layout_add.setVisibility(View.GONE);
            hideSoftInputView();
        } else {
            layout_more.setVisibility(View.GONE);
            showSoftInputView();
        }
    }

    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) this
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showSoftInputView() {
        if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .showSoftInput(messageInput, 0);
        }
    }

    int recordCount;

    @Override
    protected void onResume() {
        super.onResume();
        recordCount = MessageManager.getInstance()
                .getChatCountWithSb(to);
        if (recordCount <= 0) {
            listHead.setVisibility(View.GONE);
        } else {
            listHead.setVisibility(View.VISIBLE);
        }
        adapter.refreshList(getMessages());
    }
}
