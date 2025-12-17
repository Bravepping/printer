package com.ping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ping.mapper.PrintJobMapper;
import com.ping.model.dto.AddUserDto;
import com.ping.model.dto.AdminAddUserDto;
import com.ping.model.vo.UserInfoListVo;
import com.ping.model.vo.UserInfoVo;
import com.ping.pojo.PrintJob;
import com.ping.pojo.User;
import com.ping.service.PrintJobService;
import com.ping.service.UserService;
import com.ping.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-12-10 17:18:23
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Autowired
    private PrintJobService printJobService;
    @Autowired
    private PrintJobMapper printJobMapper;
    @Override
    public User login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", password);
        //        recordLoginTime(user.getId());
        return this.getOne(queryWrapper);
    }

    // 在登录成功后执行
    public void recordLoginTime(Integer userId) {
        User user = this.getById(userId);

        // 1. 把当前的 current 变为 last
        user.setLastLoginTime(user.getCurrentLoginTime());
        // 2. 把现在的时间设为 current
        user.setCurrentLoginTime(new Date());

        // 3. 更新数据库
        this.updateById(user);
    }

    @Override
    public UserInfoListVo getAllUser(Integer userId, Integer page, Integer size) {
        UserInfoListVo userInfoListVo = new UserInfoListVo();

        // 1. 准备分页对象
        Page<User> pageParam = new Page<>(page, size);

        // 2. 构造查询条件：排除管理员 (role != 3)
        // 推荐使用 LambdaQueryWrapper，防止列名写错
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(User::getRole, 3);
        // 如果需要按创建时间倒序排序，可以加上这个
        queryWrapper.orderByDesc(User::getCreateTime);

        // 3. 执行分页查询 (只查当前页的数据，比如只查10条)
        Page<User> userPage = this.page(pageParam, queryWrapper);

        // 4. 处理记录列表 (User -> UserInfoVo)
        List<UserInfoVo> userInfoVos = new ArrayList<>();

        // 遍历当前页的数据
        if (userPage.getRecords() != null && !userPage.getRecords().isEmpty()) {
            for (User user : userPage.getRecords()) {
                UserInfoVo userInfoVo = new UserInfoVo();
                // 使用 BeanUtils 复制属性，省去一行行 set (前提是属性名一致)
                BeanUtils.copyProperties(user, userInfoVo);

                // 单独处理不一致的或者需要计算的字段
                // 注意：这里仍然存在循环查库问题(N+1问题)，如果并发大建议优化，但分页每页只有10-20条，暂时可以接受
                Integer countByUserId = printJobMapper.getCountByUserId(user.getId());
                userInfoVo.setPrintCount(countByUserId == null ? 0 : countByUserId);

                userInfoVos.add(userInfoVo);
            }
        }

        // 5. 统计顶部的数据 (总人数、各角色人数)
        // 统计 Role=1 (普通用户)
        long role1Count = this.count(new LambdaQueryWrapper<User>().eq(User::getRole, 1));
        // 统计 Role=2 (免审核用户)
        long role2Count = this.count(new LambdaQueryWrapper<User>().eq(User::getRole, 2));
        // 统计当前列表的总人数 (排除管理员后的总数)
        long allUserCount = this.count(new LambdaQueryWrapper<User>().ne(User::getRole, 3));

        // 6. 组装最终结果
        userInfoListVo.setSize(userPage.getSize());
        userInfoListVo.setTotal(userPage.getTotal());
        userInfoListVo.setPages(userPage.getPages());
        userInfoListVo.setCurrent(userPage.getCurrent());
        userInfoListVo.setRecords(userInfoVos);
        userInfoListVo.setRole1Count((int) role1Count);
        userInfoListVo.setRole2Count((int) role2Count);
        userInfoListVo.setUserCount((int) allUserCount); // 这里看需求，是当前页数量还是总数量，通常是总数量

        return userInfoListVo;
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();

        return this.update(updateWrapper.eq("id", id).set("status", status));
    }

    @Override
    public boolean updateRole(Integer id, Integer role) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();

        return this.update(updateWrapper.eq("id", id).set("role", role));
    }

    @Override
    public boolean saveUser(AdminAddUserDto user) {
        User user1 = new User();
        user1.setUsername(user.getUsername());
        user1.setPassword(user.getPassword());
        user1.setRole(user.getRole());
        user1.setEmail(user.getEmail());
        user1.setCreateTime(new Date());
        user1.setStatus(user.getStatus());
        return this.save(user1);
    }

    @Override
    public boolean register(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername()).or().eq("email", user.getEmail());
        User user1 = this.getOne(queryWrapper);
        if (user1 != null){
            throw new RuntimeException("用户名或邮箱已存在");
        }
        return this.save(user);
    }
}




