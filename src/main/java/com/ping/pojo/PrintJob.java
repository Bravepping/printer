package com.ping.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName print_job
 */
@TableName(value ="print_job")
@Data
public class PrintJob {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 文件id
     */
    private Integer filesId;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 完成时间
     */
    private Date endTime;

    /**
     * 页码范围
     */
    private String page;

    /**
     * 份数
     */
    private Integer count;

    /**
     * 是否双面打印
     */
    private Integer isDouble;

    /**
     * 打印机名称
     */
    private String printerName;
    /**
     * 打印状态
     */
    private String status;
    /**
     *是否通过审批，1表示审批，0表示审批不通过
     */
    private Integer isLive;


    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        PrintJob other = (PrintJob) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getFilesId() == null ? other.getFilesId() == null : this.getFilesId().equals(other.getFilesId()))
            && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
            && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
            && (this.getPage() == null ? other.getPage() == null : this.getPage().equals(other.getPage()))
            && (this.getCount() == null ? other.getCount() == null : this.getCount().equals(other.getCount()))
                && (this.getIsDouble() == null ? other.getIsDouble() == null : this.getIsDouble().equals(other.getIsDouble()))
                && (this.getPrinterName() == null ? other.getPrinterName() == null : this.getPrinterName().equals(other.getPrinterName()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getFilesId() == null) ? 0 : getFilesId().hashCode());
        result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
        result = prime * result + ((getPage() == null) ? 0 : getPage().hashCode());
        result = prime * result + ((getCount() == null) ? 0 : getCount().hashCode());
        result = prime * result + ((getIsDouble() == null) ? 0 : getIsDouble().hashCode());
        result = prime * result + ((getPrinterName() == null) ? 0 : getPrinterName().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", filesId=").append(filesId);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", page=").append(page);
        sb.append(", count=").append(count);
        sb.append(", isDouble=").append(isDouble);
        sb.append(", printerName=").append(printerName);
        sb.append(", statue=").append(status);
        sb.append("]");
        return sb.toString();
    }
}