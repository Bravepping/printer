package com.ping.service;

import com.ping.model.dto.PrintDto;
import com.ping.model.vo.AdminPrintsListVo;
import com.ping.model.vo.AdminPrintsVo;
import com.ping.model.vo.PrintsListVo;
import com.ping.model.vo.PrintsVo;
import com.ping.pojo.PrintJob;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【print_job】的数据库操作Service
* @createDate 2025-12-11 22:18:03
*/
public interface PrintJobService extends IService<PrintJob> {

    //创建打印任务
    boolean createPrintJob(PrintDto printDto, Integer userId);

    //获取所有打印机
    List<String> getPrinters();

    PrintsListVo getPrintJobsByUserId(Integer userId, Integer page, Integer size);

    boolean cancelPrintJob(Integer printJobId);

    AdminPrintsListVo getAllPrintJobs(int page, int size, int type);

    boolean approvePrintJob(Integer printJobId,Integer isLive);
}
