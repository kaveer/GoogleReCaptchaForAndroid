package com.kavsoftware.kaveer.googlerecaptcha.ViewModel;

/**
 * Created by kaveer on 11/6/2017.
 */

public class VerifyUserResponseViewModel {
    private boolean success;
    private String challenge_ts;
    private String apk_package_name;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getChallenge_ts() {
        return challenge_ts;
    }

    public void setChallenge_ts(String challenge_ts) {
        this.challenge_ts = challenge_ts;
    }

    public String getApk_package_name() {
        return apk_package_name;
    }

    public void setApk_package_name(String apk_package_name) {
        this.apk_package_name = apk_package_name;
    }
}
