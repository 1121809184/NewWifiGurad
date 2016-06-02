package com.sharedream.wifiguard.task;


import com.sharedream.wifiguard.utils.EncryptionModule;

public class RandomTagCmdHttpTask extends BaseCmdHttpTask {

    @Override
    protected String encryptJson(String json) {
        return EncryptionModule.encryptJsonData(json);
    }

    @Override
    protected String decryptJson(String responseData) {
        return EncryptionModule.decryptJsonData(responseData);
    }

}
