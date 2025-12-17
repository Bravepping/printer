package com.ping.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ping.model.dto.AddUserDto;
import com.ping.model.dto.LoginUserDto;
import com.ping.model.vo.UserInfoVo;
import com.ping.pojo.PrintJob;
import com.ping.pojo.User;
import com.ping.service.PrintJobService;
import com.ping.service.UserService;

import com.ping.utils.ResultT;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/user")
@Slf4j
public class UserController {

    private UserService userService;

    @Autowired
    private PrintJobService printJobService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 注册
     * @param addUserDto
     * @return
     */
    @PostMapping(value = "/register")
    public ResultT< String> addUser(@RequestBody AddUserDto addUserDto){
        User user = new User();
        user.setUsername(addUserDto.getUsername());
        user.setPassword(addUserDto.getPassword());
        user.setEmail(addUserDto.getEmail());
        user.setCreateTime(new Date());

        boolean save = userService.register(user);
        if (save){
            return ResultT.success("添加成功");
        }
        return ResultT.error("添加失败");
    }

    /**
     * 登录
     * @param loginUserDto
     * @return
     */
    @PostMapping(value = "/login")
    public ResultT<String> login(@RequestBody LoginUserDto loginUserDto,
                                 HttpSession  session
    ){
        User user = userService.login(loginUserDto.getUsername(), loginUserDto.getPassword());
        if (user != null){
            //更新登录时间
            userService.recordLoginTime(user.getId());
            // 1. 将用户信息存入 Session（服务端内存）
            session.setAttribute("user", user);
            return ResultT.success(session.getId());
        }
        return ResultT.error("用户名或密码错误");
    }
    /**
     * 注销
     * @param session
     * @return
     */
    @GetMapping(value = "/logout")
    public ResultT<String> logout(HttpSession session){
        // 1. 移除 Session 中的用户信息，标记用户未登录
        session.removeAttribute("user");
        return ResultT.success("注销成功");
    }
    /**
     * 修改密码
     * @param session
     * @param newPassword
     * @return
     */
    @PostMapping(value = "/change-password")
    public ResultT<String> updatePassword(HttpSession session,String newPassword){
        User user = (User) session.getAttribute("user");
        if (user == null){
            return ResultT.error("用户未登录");
        }
        user.setPassword(newPassword);
        if (userService.updateById(user)){
            session.removeAttribute("user");
            return ResultT.success("修改成功");
        }
        return ResultT.error("修改失败");
    }
    /**
     * 获取用户信息
     * @param session
     * @return
     */
    @RequestMapping("/info")
    public ResultT<UserInfoVo> info(HttpSession session){
        UserInfoVo userInfoVo = new UserInfoVo();
        User user = (User) session.getAttribute("user");
        if (user == null){
            return ResultT.error("用户未登录");
        }
//        log.info("用户信息：{}", user);
        //获取打印次数
        QueryWrapper<PrintJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        long count = printJobService.count(queryWrapper);
        userInfoVo.setUsername(user.getUsername());
        userInfoVo.setCreateTime(user.getCreateTime());
        userInfoVo.setLastLoginTime(user.getLastLoginTime());
        userInfoVo.setPrintCount((int) count);
        userInfoVo.setEmail(user.getEmail());
        userInfoVo.setStatus(user.getStatus());
        userInfoVo.setRole(user.getRole());
        return ResultT.success(userInfoVo);
    }

}
