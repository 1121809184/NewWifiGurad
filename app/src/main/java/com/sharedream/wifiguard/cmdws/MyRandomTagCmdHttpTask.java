package com.sharedream.wifiguard.cmdws;


public class MyRandomTagCmdHttpTask extends MyCmdHttpTask {

    @Override
    protected String encryptJson(String json) {
        return EncryptionModuleWithCBC.encryptJsonData(json);
    }

    @Override
    protected String decryptJson(String responseData) {
        return EncryptionModuleWithCBC.decryptJsonData(responseData);
    }

}
