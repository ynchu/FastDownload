/*==============================================================*/
/* create database                                              */
/*==============================================================*/

DROP DATABASE IF EXISTS `fastdownload`;
CREATE DATABASE `fastdownload`;

USE `fastdownload`;

/*==============================================================*/
/* create table                                                 */
/*==============================================================*/

drop table if exists `file_info`;

/*==============================================================*/
/* Table: file_info                                             */
/*==============================================================*/
create table `file_info`
(
    `md5`      char(32)     not null comment '文件MD5码',
    `name`     varchar(256) not null comment '文件名',
    `location` varchar(256) not null comment '文件存储在服务器的位置',
    `count`    long comment '文件下载次数',
    `url`      varchar(128) unique comment '文件链接',
    constraint PK_USER primary key clustered (`md5`)
) comment '文件信息表';

drop table if exists user;

/*==============================================================*/
/* Table: user                                                  */
/*==============================================================*/
create table user
(
    `id`   char(6)     not null comment '用户ID',
    `pwd`  varchar(12) not null comment '用户密码',
    `type` int         not null default 0 comment '用户类型',
    constraint PK_USER primary key clustered (`id`)
) comment '用户表';