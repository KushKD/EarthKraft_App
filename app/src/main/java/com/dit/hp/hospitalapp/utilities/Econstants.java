package com.dit.hp.hospitalapp.utilities;


import com.dit.hp.hospitalapp.Modals.ResponsePojoGet;

public class Econstants {

    // Is Not Empty Check Method
    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static final String internetNotAvailable = "Internet not Available. Please Connect to Internet and try again.";

    // status
    public static final Boolean STATUS_TRUE = true;
    public static final Boolean STATUS_FALSE = false;

    // Search Delay
    public static final Integer SEARCH_DELAY = 700;
    public static final Integer PAGE_SIZE = 30;

    // OTHERS: for appending correct url in HttpManager
    public static final String status = "status=";
    public static final String masterName = "&masterName=";
    public static final String parentId = "&parentId=";
    public static final String secondaryParentId = "&secondaryParentId=";

    // Add master names
    public static final String API_NAME_HRTC = "HRTC";
//    public static final String loginMethod = "/login/Auth?";


    public static final String loginMethod = "/login";
    public static final String labReport = "/labReport";
    public static final String genderMethod = "/genders";
    public static final String registrationModesMethod = "/getRegistrationModes";
    public static final String getReferredByMethod = "/getReferredBy";
    public static final String getTests = "/getTests";

    public static final String savePatient = "/savePatient";



//    public static final String base_url = "http://192.168.29.216:8081"; // Shubham Home Local

//    public static final String base_url = "https://himstaging1.hp.gov.in/hrtc"; // Staging

    public static final String base_url = "http://www.theearthkraftnetwork.com/api"; // PROD


    public static ResponsePojoGet createOfflineObject(String url, String requestParams, String response, String Code, String functionName) {
        ResponsePojoGet pojo = new ResponsePojoGet();
        pojo.setUrl(url);
        pojo.setRequestParams(requestParams);
        pojo.setResponse(response);
        pojo.setFunctionName(functionName);
        pojo.setResponseCode(Code);
        return pojo;
    }


}

