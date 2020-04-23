package com.dictation.user.controller;

import com.dictation.user.entity.LoginInfo;
import com.dictation.user.entity.User;
import com.dictation.user.service.UserService;
import com.dictation.util.FileUtil;
import com.dictation.util.QiniuUtil;
import com.dictation.util.RedisUtil;
import com.dictation.util.TimeUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @ClassName: UserController
 * @Description: TODO
 * @Author: szy
 * @Date 2020/4/14
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    RedisUtil redisUtil;

    @RequestMapping("/login")
    public LoginInfo login(@RequestParam(value = "login_type",required = true)int login_type,
                        @RequestParam(value = "phone",required = true,defaultValue = "")String phone,
                        @RequestParam(value = "password",required = false)String password){
        LoginInfo loginInfo = null;
        switch (login_type) {
            case 1:
                loginInfo = this.loginByVC(phone);   // 使用验证码登陆
                break;
            case 2:

                loginInfo = this.loginUserByPwd(phone, password);    // 使用密码登录
                break;
        }
        return loginInfo;
    }
    /**
    * @Description: 使用密码登陆
    *
    * @return: com.dictation.user.entity.User
    * @Author: szy
    */
    private LoginInfo loginUserByPwd(String phone,String password) {
        LoginInfo loginInfo=new LoginInfo();
        Boolean info = userService.checkUser(phone);
        if (info){
            User user= this.userService.loginByPP(phone,password);
            if (user == null){
                loginInfo.setRegister_type(3);
            }else {
                loginInfo.setUser(user);
                loginInfo.setRegister_type(1);
            }
        }else {
            loginInfo.setRegister_type(2);
        }
        return loginInfo;
    }

    /**
     * 验证码登录
     */
    public LoginInfo loginByVC(String phone) {
        LoginInfo loginInfo=new LoginInfo();
        Boolean info = userService.checkUser(phone);
        if (info){
            User user = userService.findUserByPhone(phone);
            loginInfo.setRegister_type(2);
            loginInfo.setUser(user);
        }
        else {
            userService.saveUser(phone);
            User user = userService.findUserByPhone(phone);
            loginInfo.setRegister_type(1);
            loginInfo.setUser(user);
        }
        return loginInfo;
    }

    /**
     * 更新 user 信息
     * @return
     */
    @RequestMapping("/updateuser")
    public User updateUser(@RequestBody User user){
        System.out.println(user.toString());
        return this.userService.updateUser(user);
    }

    /**
     * 修改头像
     */
    @RequestMapping("/uploadhead")
    public User uploadhead(@RequestParam(value = "fileUrl",required = false) String fileUrl,
                           @RequestParam(value = "uid",required = false) int uid){
        // 1. 修改头像地址
        this.userService.updateUserImage(uid,fileUrl);
        // 2. 返回user对象
        User user = this.userService.findUserByUid(uid);
        return user;
    }

    /**
    * @Description: 修改密码
    * @Param:  * @param type 0：为拥有密码时设置密码，1：通过旧密码修改密码，2：通过手机验证修改密码
     * @param uid   userid
     * @param passwordOld  旧密码
     * @param passwordNew  新密码
     * @param password      设置的密码
    * @return: com.dictation.user.entity.LoginInfo
    * @Author: szy
    * @date: 2020/4/15
    */
    @RequestMapping("/updatepwd")
    public LoginInfo updatepwd(@RequestParam(value = "type" , required = true,defaultValue = "")int type,
                               @RequestParam(value = "uid",required = true,defaultValue = "")int uid,
                               @RequestParam(value = "upasswordOld",required = false)String passwordOld,
                               @RequestParam(value = "upasswordNew",required = false)String passwordNew,
                               @RequestParam(value = "upassword",required = false)String password) {
        LoginInfo loginInfo = new LoginInfo();
        if (type == 1) {    // 此时为根据旧密码修改旧密码
            System.out.println("旧"+passwordOld+",新"+passwordNew);
            String phone = this.userService.findPhoneByUid(uid);
            User user = userService.loginByPP(phone, passwordOld);
            if (user != null) {
                user = userService.updatePwd(user, passwordNew);
                loginInfo.setRegister_type(1);
                loginInfo.setUser(user);
            } else {
                loginInfo.setRegister_type(0);
            }
        } else if(type == 0 || type == 2){    // 此时为设置密码
            System.out.println(password);
            User user = userService.findUserByUid(uid);
            user = userService.updatePwd(user, password);
            loginInfo.setRegister_type(1);
            loginInfo.setUser(user);
        }
        return loginInfo;
    }

    /**
     * 删除数据库用户
     */
    @RequestMapping("/deleteUser")
    public String deleteUser(@RequestBody User user){
        this.userService.deleteUser(user);
        return "success";
    }



    @RequestMapping("/check")
    public List<Boolean> getSignInList(@RequestParam("id") int id){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return redisUtil.getBitList("signin:" + id + ":" + simpleDateFormat.format(new Date()));
    }


    @RequestMapping("/signIn")
    public List<Boolean> signIn(@RequestParam("id") int id){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String key = "signin:" + id + ":" + simpleDateFormat.format(new Date());

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if(dayOfWeek != 1){
            dayOfWeek -= 1;
        }else{
            dayOfWeek = 7;
        }
        long time = 0;
        if(!redisUtil.hasKey(key)){
            time = TimeUtil.getSecondsToNextMonday4pm();
        }
        if(!redisUtil.getBit(key,dayOfWeek-1)){
            redisUtil.setBit(key,dayOfWeek-1,true, time);
            userService.updateUserCreditAndInsertRecord(id,"每日登录",5);
        }
        return redisUtil.getBitList(key);
    }










}
