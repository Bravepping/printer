package com.ping.controller;


import com.ping.model.dto.PrintDto;

import com.ping.model.vo.AdminPrintsListVo;
import com.ping.model.vo.AdminPrintsVo;
import com.ping.model.vo.PrintsListVo;

import com.ping.pojo.User;
import com.ping.service.PrintJobService;
import com.ping.utils.ResultT;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(value = "/printer")
public class PrintController {


    private PrintJobService printJobService;


    public PrintController(PrintJobService printJobService) {
        this.printJobService = printJobService;
    }

    @RequestMapping(value = "/print",method = RequestMethod.POST)
    public ResultT<String> print(@RequestBody PrintDto printDto,
                        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResultT.error("请先登录");
        }
        boolean success = printJobService.createPrintJob(printDto, user.getId());
        if (success){
            return ResultT.success("打印任务创建成功");
        }else {
            return ResultT.error("打印任务创建失败");
        }
    }
    //取消打印任务，删除redis中的任务
    @RequestMapping(value = "/cancel",method = RequestMethod.GET)
    public ResultT<String> cancel(HttpSession session,
                                  Integer printJobId) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResultT.error("请先登录");
        }
        if (printJobService.cancelPrintJob(printJobId)){
            return ResultT.success("取消成功");
        }else return ResultT.error("取消失败");
    }

    //获取所有打印机
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public ResultT<List<String>> list(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResultT.error("请先登录");
        }
        return ResultT.success(printJobService.getPrinters());
    }

    //获取当前用户打印任务
    @RequestMapping(value = "/user",method = RequestMethod.GET)
    public ResultT<PrintsListVo> user(HttpSession session,
                                      @RequestParam(value = "page",defaultValue = "1") int page,
                                      @RequestParam(value = "size",defaultValue = "10") int size) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResultT.error("请先登录");
        }
        return ResultT.success(printJobService.getPrintJobsByUserId(user.getId(),page,size));
    }

    //获取所有用户打印任务，不包括管理员
    @RequestMapping(value = "/users",method = RequestMethod.GET)
    public ResultT<AdminPrintsListVo> users(HttpSession session,
                                            @RequestParam(value = "type",defaultValue = "1") int type,
                                            @RequestParam(value = "page",defaultValue = "1") int page,
                                            @RequestParam(value = "size",defaultValue = "10") int size) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResultT.error("请先登录");
        }
        if (user.getRole()!=3){
            return ResultT.error("权限不足");
        }
        return ResultT.success(printJobService.getAllPrintJobs(page,size,type));
    }

    //审批打印任务
    @RequestMapping(value = "/approve",method = RequestMethod.POST)
    public ResultT<String> approve(HttpSession session,
                                   @RequestParam("id") Integer printJobId,
                                   @RequestParam("isLive") Integer isLive){
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResultT.error("请先登录");
        }
        if (user.getRole()!=3){
            return ResultT.error("权限不足");
        }
        if (printJobService.approvePrintJob(printJobId,isLive)){
            return ResultT.success("审批成功");
        }else return ResultT.error("审批失败");
    }
}
