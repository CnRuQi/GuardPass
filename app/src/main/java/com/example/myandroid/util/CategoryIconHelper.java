package com.example.myandroid.util;

import com.example.myandroid.R;

public class CategoryIconHelper {

    public static int getIconResource(String icon) {
        if (icon == null) return R.drawable.ic_category;

        switch (icon) {
            case "ic_social":
                return R.drawable.ic_cat_social;
            case "ic_bank":
                return R.drawable.ic_cat_bank;
            case "ic_email":
                return R.drawable.ic_cat_email;
            case "ic_work":
                return R.drawable.ic_cat_work;
            case "ic_api":
                return R.drawable.ic_cat_api;
            case "ic_other":
                return R.drawable.ic_cat_other;
            default:
                return R.drawable.ic_category;
        }
    }

    public static int getIconByName(String name) {
        if (name == null) return R.drawable.ic_category;

        switch (name) {
            case "社交":
                return R.drawable.ic_cat_social;
            case "银行":
                return R.drawable.ic_cat_bank;
            case "邮箱":
                return R.drawable.ic_cat_email;
            case "工作":
                return R.drawable.ic_cat_work;
            case "API Key":
                return R.drawable.ic_cat_api;
            case "其他":
                return R.drawable.ic_cat_other;
            default:
                return R.drawable.ic_category;
        }
    }
}
