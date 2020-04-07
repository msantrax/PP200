/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

/**
 *
 * @author opus
 */
public class User {
   
   
        private long uid = System.currentTimeMillis();

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

        private String name ="";

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

        private String password;

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

        private String avatar_path = "";

    public String getAvatar_path() {
        return avatar_path;
    }

    public User setAvatar_path(String avatar_path) {
        this.avatar_path = avatar_path;
        return this;
    }

        private boolean maycalibrate = true;

    public boolean isMaycalibrate() {
        return maycalibrate;
    }

    public User setMaycalibrate(boolean maycalibrate) {
        this.maycalibrate = maycalibrate;
        return this;
    }

        private boolean may_search = true;

    public boolean isMay_search() {
        return may_search;
    }

    public User setMay_search(boolean may_search) {
        this.may_search = may_search;
        return this;
    }

        private boolean may_report = true;

    public boolean isMay_report() {
        return may_report;
    }

    public User setMay_report(boolean may_report) {
        this.may_report = may_report;
        return this;
    }

        private boolean may_changeprofile = true;

    public boolean isMay_changeprofile() {
        return may_changeprofile;
    }

    public User setMay_changeprofile(boolean may_changeprofile) {
        this.may_changeprofile = may_changeprofile;
        return this;
    }
    
    
        private String profile = "Default";

    public String getProfile() {
        return profile;
    }

    public User setProfile(String profile) {
        this.profile = profile;
        return this;
    }


    public User() {
        
    }

    
    
    
    
    
}
