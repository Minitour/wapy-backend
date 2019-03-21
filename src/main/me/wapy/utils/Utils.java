package me.wapy.utils;


public class Utils {

    public static boolean isNullOrEmpty(String s){
        return s.isEmpty();
    }

    public static boolean isContentEmpty(String title){
        return title == null || title.equals("") || title.trim().equals("") || title.isEmpty();
    }

    public static int max(int... arr) {
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            max = Math.max(arr[i], max);
        }
        return max;
    }

}
