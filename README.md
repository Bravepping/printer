# 🖨️ Shared Print Management System | 多用户共享打印管理系统

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-green) ![Vue](https://img.shields.io/badge/Vue.js-3.x-4FC08D) ![JDK](https://img.shields.io/badge/JDK-21-orange) ![Redis](https://img.shields.io/badge/Redis-Cache%20%26%20Queue-red)

> 一个基于 Spring Boot 3 + Vue 3 的多用户共享打印解决方案。支持远程文件上传、打印队列管理、管理员审批、以及高性能的 Redis+DB 双重保障机制。

## 📖 项目简介

本项目旨在解决办公室或实验室环境下的“共享打印”难题。用户无需连接打印机即可通过 Web 端上传文件并申请打印，管理员可在后台审核任务。系统后端采用**生产级消费者模型**，通过 Redis 队列实现削峰填谷，并配合数据库兜底策略，确保打印任务不丢失、不卡死。

## ✨ 核心功能

* **多格式支持**：支持 PDF、Word (.doc, .docx) 文件的解析与打印。
* **智能队列**：
    * **Redis 优先**：高并发下优先读取 Redis List 队列，毫秒级响应。
    * **数据库兜底**：若 Redis 异常或任务积压，自动扫描数据库防止漏单。
* **可靠性设计**：
    * **异步打印**：物理打印任务在独立线程池中执行。
    * **超时控制**：打印任务若超过 30秒（可配置）无响应，自动标记超时，防止阻塞队列。
* **审批流**：支持配置 `is_review` 开关。开启后，普通用户的任务需管理员审核通过方可打印。
* **个性化设置**：支持双面打印、份数设置、自定义页码范围（支持 `0-0` 自动识别为全文档）。
* **系统配置热更新**：基于内存缓存配置项，修改配置无需重启服务。

## 🛠️ 技术栈

### 后端 (Backend)
* **核心框架**：Spring Boot 3.4.1
* **ORM**：MyBatis-Plus
* **数据库**：MySQL 8.0
* **缓存/消息队列**：Redis (StringRedisTemplate)
* **文档处理**：
    * `Apache PDFBox` (PDF 处理)
    * `Spire.Doc` (Word 处理)
    * `Java AWT PrinterJob` (调用物理打印机驱动)
* **工具库**：Lombok, FastJson/Jackson

### 前端 (Frontend)
* **框架**：Vue 3 + Vite
* **UI 组件**：Element Plus
* **网络请求**：Axios

## 🚀 快速开始

### 1. 环境准备
* JDK 21+
* Node.js 18+
* MySQL 8.0+
* Redis 5.0+
* **重要**：部署服务器（或本机）必须安装有物理打印机驱动，或为了测试安装虚拟打印机（如 Microsoft Print to PDF）。

### 2. 数据库配置
创建数据库 `print_system` 并导入 `sql/init.sql`（需自行导出结构）。

### 3. 后端启动
1. 修改 `application.yml` 中的数据库和 Redis 连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/print_system?useUnicode=true&characterEncoding=utf-8
       username: root
       password: your_password
     data:
       redis:
         host: localhost
         port: 6379
   
   file:
     upload-path: D:/print_files/  # 文件存储路径
## 4   演示账号
* 管理员账号：admin/admin
* 免审核账号：123456/123456
* 普通账号：123123/123123