package com.cn.tfl.fim.utils;

import android.widget.TextView;

/**
 * Created by Administrator on 2016/5/19.
 */
public class ValidateUtil {

    public static boolean isEmpty(TextView w, String displayStr) {
        if (StringUtil.empty(w.getText().toString().trim())) {
            w.setError(displayStr + "不能为空！");
            w.setFocusable(true);
            w.requestFocus();
            return true;
        }
        return false;
    }
}
