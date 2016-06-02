package com.sharedream.wifiguard.cmdws;


import com.sharedream.wifiguard.sd.SD;

public class CmdCreateToken {

    public static Params createParams(String accessToken) {
        CmdCreateToken.Params params = new CmdCreateToken.Params();
        params.type = 2;
        params.accessToken = accessToken;
        return params;
    }

    public static class Params implements SD{
        public int type;
        public String accessToken;
    }

    public static class Results implements SD{
        public int code;
        public String msg;
        public Data data;
    }

    public static class Data implements SD{
        public String uptoken;
        public String name;
        public String domain;
    }
}
