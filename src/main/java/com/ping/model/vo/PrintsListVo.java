package com.ping.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class PrintsListVo {
    private Long pages;//总页数
    private Long total;//总记录数
    private Long current;//当前页
    private Long size;//每页记录数
    private Long printCount;//打印总数
    private Long printSuccess;//打印成功总数
    private Long printPending;
    private Long printRejectReview;
    private Long printTimeout;
    private Long printError;//打印失败总数
    private Long printCancel;//打印取消总数
    private Long printWait;//打印等待总数
    private Long printWaitReview;//打印等待审核总数
    private List<PrintsVo> records;//打印任务列表
}
