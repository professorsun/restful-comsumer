package com.uocloud.consumer.controller;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uocloud.consumer.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class UserController {

    @Autowired
    private RestTemplate restTemplate;
    /*
     * 注册功能
     * */
    @RequestMapping("/register")
    public String reg(){
        return "register";
    }


    @RequestMapping("/adduser")
    public String register(HttpServletRequest request, HttpServletResponse response,Map<String,Object> map){
        String username = request.getParameter("username");
        String password = request.getParameter("password");
//        String password2 = request.getParameter("password2");
//        String userPhone = request.getParameter("userPhone");
        User user=new User();
        user.setUsername(username);
        user.setPassword(password);
        HttpSession session = request.getSession();
        String s = restTemplate.postForObject("http://localhost:9000/user/adduser?username="+username+"&password="+password, null, String.class);
        System.out.println(s);
//        ModelAndView mv = new ModelAndView("register");
        map.put("msg",s);
        if("the user is registered".equals(s)){
            return "register";
        }else {

            return "login";
        }
    }


    @RequestMapping("/getalluser")
    public String getusers(Model model){

        List<User> list =  restTemplate.getForObject("http://localhost:9000/user/getalluser",List.class);
//        System.out.println(list);
//        for (int i = 0; i < list.size(); i++) {
//            Object o = list.get(i);
//            System.out.println(o.toString());
//
//
//            }
        for (Object o : list) {
            JSON json = (JSON) JSON.toJSON(o);
            User user = JSON.toJavaObject(json,User.class);;
            System.out.println(user.getPassword());
          }
//       Iterator<Object> iter = list.iterator();
//        while (iter.hasNext()) {
//            Object s = (Object) iter.next();

//            System.out.println(s);
//        }
        model.addAttribute(list);
        return "register";

    }
    @RequestMapping("/getuser")
    public String getuser(HttpServletRequest request, Map<String,Object> map) {
        String username = request.getParameter("username");
//        String password = request.getParameter("password");
//        String password2 = request.getParameter("password2");
//        String userPhone = request.getParameter("userPhone");
        User user = restTemplate.postForObject("http://localhost:9000/user/getuser?username=" + username, null, User.class);

        if (user != null) {
            System.out.println(user);
            map.put("msg", user + "success find");

            return "index";
        } else {

            map.put("msg", "find fault");

            return "register";
        }
    }
    @GetMapping("/login")

    @RequestMapping("/login")
    public String login(HttpServletRequest request,
                              HttpServletResponse response,
                              Map<String,Object> map){
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        User dbUser = restTemplate.postForObject(
                "http://localhost:9000/user/getuser?username=" + username + "&password=" + password,
                null,User.class);
        HttpSession session = request.getSession();
        if (dbUser != null){  //user不为空，说明用户合法，进行更新操作
            String toc= UUID.randomUUID().toString();
            dbUser.setTocken(toc);
            System.out.println(toc);
//            restTemplate.postForObject("http://localhost:9000/user/updateuser?username="+username+"&tocken="+toc,null, Integer.class);
            Cookie cookie=new Cookie("tocken",toc);
            cookie.setMaxAge(60*60*24*7);       //设置cookie保存时间为一周
            response.addCookie(cookie);
            session.setAttribute("user", dbUser);
            map.put("msg", "登陆成功");

            return "index";
        }else{
            map.put("msg","密码或用户名错误！");

            return "login";
        }

    }


    @RequestMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response){
        request.getSession().removeAttribute("user");   //移除session
        Cookie cookie = new Cookie("token", null);  //要删除cookie需要新建一个同名的cookie，并将value设置为null
        cookie.setMaxAge(0);    //立即删除型
        response.addCookie(cookie);//删除cookie
        return "redirect:/";

    }
}
