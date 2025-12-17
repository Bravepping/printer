package com.ping.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ping.model.vo.AdminPrintsVo;
import com.ping.model.vo.PrintsVo;
import com.ping.pojo.PrintJob;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author Administrator
* @description 针对表【print_job】的数据库操作Mapper
* @createDate 2025-12-11 22:18:03
* @Entity com.ping.pojo.PrintJob
*/
public interface PrintJobMapper extends BaseMapper<PrintJob> {

    //获取用户打印任务列表
    IPage<PrintsVo> getPrintJobsByUserIdMapper(IPage<PrintsVo> page, @Param("userId") Integer userId);

    // 分页查询所有打印任务
    IPage<AdminPrintsVo> getAllPrintJobsIdMapper(Page<AdminPrintsVo> page, @Param("type") int type);

    // 统计各状态数量(排除管理员)
    List<Map<String, Object>> getPrintJobStatusStatistics();

    // 统计待审核数量(排除管理员)
    Integer getAllWaitReviewCount();

    Integer getCountByUserId(Integer id);

    Integer getWaitReviewCount(Integer userId);

}




