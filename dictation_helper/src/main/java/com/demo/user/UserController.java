package com.demo.user;

import com.demo.common.model.TblUser;
import com.demo.entity.LoginInfo;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;

public class UserController extends Controller {

    @Inject
    UserService userService;
//    public void index() {
//        redirect("/user/userhome.html");
//    }
    public void registUserByNP() {
        String name = get("name");
        String phone = get("phone");
        String password = get("password");
        renderJson(userService.registByPassword(name, phone, password));
    }
    public void loginUserByPP() {
        String phone = get("phone");
        String password = get("password");
        renderJson(userService.loginByPP(phone, password));
    }

    /**
     * 登录
     */
    public void login(){

        String phone=getPara("json");
        int login_type = getInt("login_type");
        System.out.println(phone);
        System.out.println(":"+login_type);
        if(login_type==1) {
            this.loginByVC(phone);
        }
        else{
            String password=get("password");
            this.loginUserByPwd(phone,password);
        }

    }

    /**
     * 密码登录
     */
    private void loginUserByPwd(String phone,String password) {
        LoginInfo loginInfo=new LoginInfo();
        Boolean info = userService.checkPhone(phone);
        if (info){
            TblUser user= userService.loginByPP(phone,password);
            if (user == null){
                loginInfo.setRegister_type(3);
            }else {
                loginInfo.setUser(user);
                loginInfo.setRegister_type(1);
            }
        }else {
            loginInfo.setRegister_type(2);
        }
        renderJson(loginInfo);
    }

    /**
     * 验证码登录
     */
    public void loginByVC(String phone) {
        LoginInfo loginInfo=new LoginInfo();
        Boolean info = userService.checkPhone(phone);
        if (info){
            TblUser user = userService.findUserByPhone(phone);
            loginInfo.setRegister_type(2);
            loginInfo.setUser(user);
        }
        else {
            userService.saveUser(phone);
            TblUser user = userService.findUserByPhone(phone);
            loginInfo.setRegister_type(1);
            loginInfo.setUser(user);
        }
        renderJson(loginInfo);

    }
    /**
     * 更新user
     * @return
     */
    public void updateuser(){
        //String json=HttpKit.readData(getRequest());
        String json=get("json");
        System.out.println(json);
        TblUser user= JsonKit.parse(json,TblUser.class);
        userService.updateUser(user);
    }

}
