package com.ping.mapper;

import com.ping.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Administrator
* @description 针对表【user】的数据库操作Mapper
* @createDate 2025-12-10 17:18:23
* @Entity com.ping.pojo.User
*/
public interface UserMapper extends BaseMapper<User> {

    Integer getRoleByUserId(Integer userId);
}




