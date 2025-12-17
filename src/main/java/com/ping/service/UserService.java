package com.ping.service;

import com.ping.model.dto.AdminAddUserDto;
import com.ping.model.vo.UserInfoListVo;
import com.ping.model.vo.UserInfoVo;
import com.ping.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【user】的数据库操作Service
* @createDate 2025-12-10 17:18:23
*/
public interface UserService extends IService<User> {
    //实现登录
    User login(String username, String password);

    void recordLoginTime(Integer userId);

    UserInfoListVo getAllUser(Integer userId, Integer page, Integer size);

    boolean updateStatus(Integer id, Integer status);

    boolean updateRole(Integer id, Integer role);

    boolean saveUser(AdminAddUserDto user);

    boolean register(User user);
}
