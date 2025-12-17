package com.ping.controller;


import com.ping.model.dto.AdminAddUserDto;
import com.ping.model.dto.EditUserDto;
import com.ping.model.vo.UserInfoListVo;
import com.ping.model.vo.UserInfoVo;
import com.ping.pojo.User;
import com.ping.service.SysConfigService;
import com.ping.service.UserService;
import com.ping.utils.ResultT;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RequestMapping("/admin")
@RestController
@Slf4j
public class AdminController {
    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private UserService userService;

    //获取所有用户信息
    @RequestMapping(value = "/user/list",method = RequestMethod.GET)
    public ResultT<UserInfoListVo> getAllUser(HttpSession session,
                                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                                              @RequestParam(value = "size",defaultValue = "10") Integer size){
        User user =(User) session.getAttribute("user");
        if (user == null){
            return ResultT.error("请先登录");
        }
        if (!user.getRole().equals(3)){
            return ResultT.error("无权限");
        }
        return ResultT.success(userService.getAllUser(user.getId(),page,size));
    }
    //添加一个用户
    @RequestMapping(value = "/user/add",method = RequestMethod.POST)
    public ResultT<String> addUser(@RequestBody AdminAddUserDto user,HttpSession session){
        User user2 = (User) session.getAttribute("user");
        if (user2 == null){
            return ResultT.error("未登录");
        }
        if (!user2.getRole().equals(3)){
            return ResultT.error("无权限");
        }
        return userService.saveUser(user) ? ResultT.success("添加成功") : ResultT.error("添加失败");
    }
    //编辑一个用户
    @RequestMapping(value = "/user/edit",method = RequestMethod.POST)
    public ResultT<String> editUser(@RequestBody EditUserDto user,HttpSession session){
        User user2 = (User) session.getAttribute("user");
        if (user2 == null){
            return ResultT.error("未登录");
        }
        if (!user2.getRole().equals(3)){
            return ResultT.error("无权限");
        }
        User userById = userService.getById(user.getId());
        userById.setEmail(user.getEmail());
        userById.setStatus(user.getStatus());
        userById.setRole(user.getRole());
        return userService.updateById(userById) ? ResultT.success("更新成功") : ResultT.error("更新失败");
    }
    //更新用户状态 0禁用 1正常 -1伪删除
    @RequestMapping(value = "/user/updateStatus",method = RequestMethod.POST)
    public ResultT<String> updateStatus(Integer id,Integer status){

        return userService.updateStatus(id,status) ? ResultT.success("更新成功") : ResultT.error("更新失败");
    }
    //更新用户权限 1普通用户2免审核用户
    @RequestMapping(value = "/user/updateRole",method = RequestMethod.POST)
    public ResultT<String> updateRole(Integer id,Integer role){

        return userService.updateRole(id,role) ? ResultT.success("更新成功") : ResultT.error("更新失败");
    }

    /**
     * 获取全部系统配置
     * @return Map<Key, Value>
     */
    @RequestMapping(value = "/sys/config/list", method = RequestMethod.GET)
    public ResultT<Map<String, String>> getSysConfig(HttpSession session) {
        // 1. 权限校验 (视业务需要，有些配置可能需要登录才能看，有些可能公开)
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResultT.error("未登录");
        }

        // 2. 调用 Service
        Map<String, String> configs = sysConfigService.getAllConfigs();
        return ResultT.success(configs);
    }
    /**
     * 更新系统配置
     * URL: /sys/config/update
     * Method: POST
     * Body: JSON {"system_name": "新的名称", "max_file_size": "50", ...}
     */
    @RequestMapping(value = "/sys/config/update", method = RequestMethod.POST)
    public ResultT<String> updateSysConfig(@RequestBody Map<String, String> configs, HttpSession session) {
        // 1. 权限校验
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResultT.error("未登录");
        }
        // 假设 3 是超级管理员权限
        if (!Integer.valueOf(3).equals(user.getRole())) {
            return ResultT.error("无权限，仅管理员可修改配置");
        }

        // 2. 参数校验
        if (configs == null || configs.isEmpty()) {
            return ResultT.error("提交的配置参数不能为空");
        }

        // 3. 调用 Service 执行更新
        // 注意：这里需要注入 SysConfigService，而不是 UserService，职责要分离
        try {
            sysConfigService.batchUpdateConfigs(configs);
            return ResultT.success("配置更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultT.error("配置更新失败：" + e.getMessage());
        }
    }
}
