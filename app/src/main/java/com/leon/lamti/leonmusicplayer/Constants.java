package com.leon.lamti.leonmusicplayer;

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.leon.lamti.leonmusicplayer.action.main";
        public static String PREV_ACTION = "com.leon.lamti.leonmusicplayer.action.prev";
        public static String PLAY_ACTION = "com.leon.lamti.leonmusicplayer.action.play";
        public static String NEXT_ACTION = "com.leon.lamti.leonmusicplayer.action.next";
        public static String STARTFOREGROUND_ACTION = "com.leon.lamti.leonmusicplayer.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.leon.lamti.leonmusicplayer.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
