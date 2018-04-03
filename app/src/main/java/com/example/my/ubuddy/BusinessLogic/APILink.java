package com.example.my.ubuddy.BusinessLogic;

public class APILink {
    private static String serverAPI="http://27.147.246.149/www/laravels/UBuddyServer/public/";
    public static String loginAPI=serverAPI+"api/user/login";
    public static String RegistrationAPI=serverAPI+"api/user/register";
    public static String UniversityAPI=serverAPI+"api/json/universities?orderBy=name";
    public static String FacultyAPI=serverAPI+"api/json/faculties/";
    public static String DepartmentAPI=serverAPI+"api/json/departments/";
    public static String SessionAPI=serverAPI+"api/session/index/";
    public static String SaveSessionAPI=serverAPI+"api/user/sessionInfo/create?api_token=";
    public static String GroupNewsFeedAPI=serverAPI+"api/session/mine?api_token=";
    public static String CreatePostAPI=serverAPI+"api/posts?api_token=";
    public static String LikeUnlikeAPI=serverAPI+"api/posts/";
    public static String CreateEventAPI=serverAPI+"api/events/create?api_token=";
    public static String AllEventAPI=serverAPI+"api/events/all?api_token=";
    public static String SingleEventAPI=serverAPI+"/api/events/single/";
}

