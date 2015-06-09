package com.example.krishna.gmailintegration.gmail;

import org.json.JSONException;
import org.json.JSONObject;

public class AccDetails {
    private String id;
    private String name;
    private String givenName;
    private String familyName;
    private String link;
    private String picture;
    private String gender;
    private String locale;
    private String birthday;

    public AccDetails(String response) {
        try {
            JSONObject profileData = new JSONObject(response);

            if (profileData.has("id")) {
                id = profileData.getString("id");
            }
            if (profileData.has("picture")) {
                picture = profileData.getString("picture");
            }
            if (profileData.has("name")) {
                name = profileData.getString("name");
            }
            if (profileData.has("gender")) {
                gender = profileData.getString("gender");
            }
            if (profileData.has("birthday")) {
                birthday = profileData.getString("birthday");
            }
            if (profileData.has("given_name")) {
                givenName = profileData.getString("given_name");
            }
            if (profileData.has("family_name")) {
                familyName = profileData.getString("family_name");
            }
            if (profileData.has("locale")) {
                locale = profileData.getString("locale");
            }
            if (profileData.has("link")) {
                link = profileData.getString("link");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getGender() {
        return gender;
    }

    public String getLocale() {
        return locale;
    }

    public String getPicture() {
        return picture;
    }

    public String getLink() {
        return link;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getBirthday() {
        return birthday;
    }

    @Override
    public String toString() {
        return "AccDetails{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", link='" + link + '\'' +
                ", picture='" + picture + '\'' +
                ", gender='" + gender + '\'' +
                ", locale='" + locale + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}