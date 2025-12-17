/*
 Navicat Premium Dump SQL

 Source Server         : pc-nh
 Source Server Type    : MySQL
 Source Server Version : 50725 (5.7.25)
 Source Host           : 154.36.154.211:9002
 Source Schema         : webprint

 Target Server Type    : MySQL
 Target Server Version : 50725 (5.7.25)
 File Encoding         : 65001

 Date: 17/12/2025 13:18:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for files
-- ----------------------------
DROP TABLE IF EXISTS `files`;
CREATE TABLE `files`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `file_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目录文件名称',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '源文件名称',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件路径',
  `create_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `file_size` double NULL DEFAULT NULL COMMENT '文件大小',
  `status` int(11) NULL DEFAULT 1 COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fiels_and_user`(`user_id`) USING BTREE,
  CONSTRAINT `fiels_and_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of files
-- ----------------------------
INSERT INTO `files` VALUES (37, 2, '63ab8a0f-e652-4ed7-8e56-64b2e8104b85.docx', '测试 - 副本 (2).docx', 'D:\\upload\\123456\\63ab8a0f-e652-4ed7-8e56-64b2e8104b85.docx', '2025-12-17 13:13:41', 11014, 1);
INSERT INTO `files` VALUES (38, 2, 'ba565983-1660-4e0e-ad42-13bc3904d698.docx', '测试.docx', 'D:\\upload\\123456\\ba565983-1660-4e0e-ad42-13bc3904d698.docx', '2025-12-17 13:13:44', 11014, 1);
INSERT INTO `files` VALUES (39, 2, '6c192c7a-315a-47b0-b736-c42fe798f12b.pdf', '测试-pdf版 - 副本 (2).pdf', 'D:\\upload\\123456\\6c192c7a-315a-47b0-b736-c42fe798f12b.pdf', '2025-12-17 13:13:48', 76506, 1);
INSERT INTO `files` VALUES (40, 2, '0f89ec27-15df-4d95-a584-9090812817cd.pdf', '测试-pdf版.pdf', 'D:\\upload\\123456\\0f89ec27-15df-4d95-a584-9090812817cd.pdf', '2025-12-17 13:13:51', 76506, 1);
INSERT INTO `files` VALUES (41, 2, 'e8bed64a-cad1-4fff-8b41-18d39c3ca3b4.docx', '测试 - 副本 (10).docx', 'D:\\upload\\123456\\e8bed64a-cad1-4fff-8b41-18d39c3ca3b4.docx', '2025-12-17 13:13:55', 11014, 1);
INSERT INTO `files` VALUES (42, 2, '48047231-4ac7-45a6-a8a0-4dfe60588766.pdf', '测试-pdf版 - 副本 (6).pdf', 'D:\\upload\\123456\\48047231-4ac7-45a6-a8a0-4dfe60588766.pdf', '2025-12-17 13:13:58', 76506, 1);
INSERT INTO `files` VALUES (43, 2, 'a2424643-0c49-4d53-a1b4-679d6dce71a8.pdf', '测试-pdf版 - 副本.pdf', 'D:\\upload\\123456\\a2424643-0c49-4d53-a1b4-679d6dce71a8.pdf', '2025-12-17 13:14:03', 76506, 1);
INSERT INTO `files` VALUES (44, 2, 'b8cf7e07-ef19-4956-b58b-fb2b27f36a27.docx', '测试 - 副本 (3).docx', 'D:\\upload\\123456\\b8cf7e07-ef19-4956-b58b-fb2b27f36a27.docx', '2025-12-17 13:14:06', 11014, 1);
INSERT INTO `files` VALUES (45, 2, 'e5033518-987f-47e4-805d-8365ebcf9351.docx', '测试 - 副本 (4).docx', 'D:\\upload\\123456\\e5033518-987f-47e4-805d-8365ebcf9351.docx', '2025-12-17 13:14:09', 11014, 1);
INSERT INTO `files` VALUES (46, 2, '550b608d-c327-4ed2-a925-11cc951e4243.docx', '测试 - 副本 (6).docx', 'D:\\upload\\123456\\550b608d-c327-4ed2-a925-11cc951e4243.docx', '2025-12-17 13:14:12', 11014, 1);
INSERT INTO `files` VALUES (47, 2, '914ef91b-b3a8-4130-a31e-1a3515f9d8f4.docx', '测试 - 副本 (10).docx', 'D:\\upload\\123456\\914ef91b-b3a8-4130-a31e-1a3515f9d8f4.docx', '2025-12-17 13:14:16', 11014, 1);
INSERT INTO `files` VALUES (48, 2, '41e7382a-172b-4896-8f37-0b2e546356af.pdf', '测试-pdf版 - 副本 (8).pdf', 'D:\\upload\\123456\\41e7382a-172b-4896-8f37-0b2e546356af.pdf', '2025-12-17 13:14:21', 76506, 1);
INSERT INTO `files` VALUES (49, 1, 'c4d7433a-ea12-4f15-95fe-299f711b9730.docx', '测试.docx', 'D:\\upload\\123123\\c4d7433a-ea12-4f15-95fe-299f711b9730.docx', '2025-12-17 13:15:08', 11014, 1);
INSERT INTO `files` VALUES (50, 1, '620db130-154a-45c9-8fd4-c111988b0f8c.pdf', '测试-pdf版.pdf', 'D:\\upload\\123123\\620db130-154a-45c9-8fd4-c111988b0f8c.pdf', '2025-12-17 13:15:13', 76506, 1);
INSERT INTO `files` VALUES (51, 1, '49878b1d-67fa-43f3-b91b-5866037135ce.pdf', '测试-pdf版 - 副本 (2).pdf', 'D:\\upload\\123123\\49878b1d-67fa-43f3-b91b-5866037135ce.pdf', '2025-12-17 13:15:16', 76506, 1);
INSERT INTO `files` VALUES (52, 1, '58dfaf10-20cd-4939-8bd1-e4e5af6de1e6.pdf', '测试-pdf版 - 副本 (3).pdf', 'D:\\upload\\123123\\58dfaf10-20cd-4939-8bd1-e4e5af6de1e6.pdf', '2025-12-17 13:15:18', 76506, 1);
INSERT INTO `files` VALUES (53, 1, 'b09653b0-85fb-4a16-90d2-484d8e40c963.pdf', '测试-pdf版 - 副本 (4).pdf', 'D:\\upload\\123123\\b09653b0-85fb-4a16-90d2-484d8e40c963.pdf', '2025-12-17 13:15:22', 76506, 1);
INSERT INTO `files` VALUES (54, 1, 'f0e31a95-7ce9-4eeb-a023-d29c528ae95b.pdf', '测试-pdf版 - 副本 (5).pdf', 'D:\\upload\\123123\\f0e31a95-7ce9-4eeb-a023-d29c528ae95b.pdf', '2025-12-17 13:15:26', 76506, 1);
INSERT INTO `files` VALUES (55, 1, '96669d38-3679-4a24-a6d5-4dc626d11440.pdf', '测试-pdf版 - 副本 (6).pdf', 'D:\\upload\\123123\\96669d38-3679-4a24-a6d5-4dc626d11440.pdf', '2025-12-17 13:15:29', 76506, 1);
INSERT INTO `files` VALUES (56, 1, 'cd2cf4a0-75c8-4f2f-892a-3174b81c1a73.pdf', '测试-pdf版 - 副本 (7).pdf', 'D:\\upload\\123123\\cd2cf4a0-75c8-4f2f-892a-3174b81c1a73.pdf', '2025-12-17 13:15:31', 76506, 1);
INSERT INTO `files` VALUES (57, 1, '5d70ed05-edac-4a26-a721-ba8211fbd68f.pdf', '测试-pdf版 - 副本 (8).pdf', 'D:\\upload\\123123\\5d70ed05-edac-4a26-a721-ba8211fbd68f.pdf', '2025-12-17 13:15:34', 76506, 1);
INSERT INTO `files` VALUES (58, 1, '85fae68b-ba8e-41a9-8c3b-8aca5c08584d.pdf', '测试-pdf版 - 副本 (9).pdf', 'D:\\upload\\123123\\85fae68b-ba8e-41a9-8c3b-8aca5c08584d.pdf', '2025-12-17 13:15:38', 76506, 1);
INSERT INTO `files` VALUES (59, 1, '5701bdbe-d148-4d16-8539-13391b09c39d.pdf', '测试-pdf版 - 副本 (10).pdf', 'D:\\upload\\123123\\5701bdbe-d148-4d16-8539-13391b09c39d.pdf', '2025-12-17 13:15:42', 76506, 1);
INSERT INTO `files` VALUES (60, 3, 'f40d2861-e0b0-4b29-b747-640ec4545d98.pdf', '测试-pdf版.pdf', 'D:\\upload\\admin\\f40d2861-e0b0-4b29-b747-640ec4545d98.pdf', '2025-12-17 13:16:42', 76506, 1);

-- ----------------------------
-- Table structure for print_job
-- ----------------------------
DROP TABLE IF EXISTS `print_job`;
CREATE TABLE `print_job`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `files_id` int(11) NULL DEFAULT NULL COMMENT '文件id',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `page` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '页码范围',
  `is_double` int(11) NULL DEFAULT NULL COMMENT '是否双面打印',
  `count` int(11) NULL DEFAULT NULL COMMENT '份数',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
  `printer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '打印机名称',
  `is_live` int(11) NULL DEFAULT NULL COMMENT '是否打印',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2078711903 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of print_job
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数键值',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES ('allow_file_type', 'pdf,docx', '支持文件类型', '2025-12-15 10:20:23', '2025-12-16 22:29:38');
INSERT INTO `sys_config` VALUES ('default_printer', 'rustdesk printer', '默认打印机名称', '2025-12-15 10:24:36', '2025-12-17 12:27:19');
INSERT INTO `sys_config` VALUES ('is_review', '1', '是否审核(1开启，0关闭)', '2025-12-15 10:25:54', '2025-12-16 10:55:12');
INSERT INTO `sys_config` VALUES ('max_file_size', '10', '最大文件大小(MB)', '2025-12-15 10:19:53', '2025-12-17 13:17:00');
INSERT INTO `sys_config` VALUES ('print_delay', '5', '延迟打印时间', '2025-12-16 21:12:23', '2025-12-17 13:16:59');
INSERT INTO `sys_config` VALUES ('print_timeout', '30', '打印超时时间(s)', '2025-12-15 10:23:14', '2025-12-15 10:23:14');
INSERT INTO `sys_config` VALUES ('system_name', '打印管理系统', '系统名称', '2025-12-15 10:17:00', '2025-12-15 10:17:04');
INSERT INTO `sys_config` VALUES ('system_version', '1.0', '系统版本', '2025-12-15 10:22:16', '2025-12-15 10:22:16');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `role` int(11) NULL DEFAULT 1 COMMENT '权限',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `current_login_time` datetime NULL DEFAULT NULL COMMENT '当前登录时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `status` int(11) NULL DEFAULT 0 COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '123123', '123123', 1, '123123qq.com', '2025-12-17 12:26:28', '2025-12-17 13:15:01', '2025-12-17 12:54:41', 1);
INSERT INTO `user` VALUES (2, '123456', '123456', 2, '123456@qq.com', '2025-12-16 14:08:44', '2025-12-17 13:13:12', '2025-12-17 12:49:35', 1);
INSERT INTO `user` VALUES (3, 'admin', 'admin', 3, 'admin@qq.com', '2025-12-17 12:32:56', '2025-12-17 13:15:59', '2025-12-17 12:17:45', 1);
INSERT INTO `user` VALUES (4, '111111', '111111', 1, '111111@qq.com', '2025-12-17 12:47:35', '2025-12-14 19:30:33', '2025-12-13 22:00:42', 1);
INSERT INTO `user` VALUES (5, '222222', '222222', 1, '222222@qq.com', '2025-12-17 12:47:43', '2025-12-14 21:10:20', NULL, 1);
INSERT INTO `user` VALUES (7, '333333', '333333', 1, '333333qq.com', '2025-12-17 12:47:54', '2025-12-14 21:10:20', NULL, 1);
INSERT INTO `user` VALUES (8, '444444', '444444', 1, '444444qq.com', '2025-12-17 12:48:03', '2025-12-14 21:10:20', NULL, 1);
INSERT INTO `user` VALUES (9, '555555', '555555', 1, '555555@qq.com', '2025-12-17 12:48:12', '2025-12-14 21:10:20', NULL, 0);
INSERT INTO `user` VALUES (10, '666666', '666666', 1, '666666@qq.com', '2025-12-17 12:48:20', '2025-12-14 21:10:20', NULL, 0);
INSERT INTO `user` VALUES (11, '888888', '888888', 1, '666666@qq.com', '2025-12-17 12:48:29', '2025-12-14 21:10:20', NULL, 0);
INSERT INTO `user` VALUES (12, '777777', '777777', 1, '777777@qq.com', '2025-12-17 12:48:36', '2025-12-14 21:10:20', NULL, 0);
INSERT INTO `user` VALUES (14, '999999', '999999', 1, '999999@qq.com', '2025-12-17 12:48:46', '2025-12-16 23:31:06', NULL, 0);

SET FOREIGN_KEY_CHECKS = 1;
