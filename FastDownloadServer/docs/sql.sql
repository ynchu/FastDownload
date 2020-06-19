/*==============================================================*/
/* create database                                              */
/*==============================================================*/

DROP DATABASE IF EXISTS fastdownload;
CREATE DATABASE fastdownload CHARACTER SET 'utf8mb4';

USE fastdownload;

/*==============================================================*/
/* create table                                                 */
/*==============================================================*/

drop table if exists file_info;

/*==============================================================*/
/* Table: file_info                                             */
/*==============================================================*/
create table file_info
(
   md5                  char(32)                       not null,
   name                 varchar(256)                   not null,
   location             text                           not null,
   count                long                           null,
   url                  text                           null,
   constraint PK_FILE_INFO primary key clustered (md5)
);

drop table if exists user;

/*==============================================================*/
/* Table: user                                                  */
/*==============================================================*/
create table user
(
   id                   char(6)                        not null,
   pwd                  varchar(12)                    not null,
   type                 int                            not null,
   constraint PK_USER primary key clustered (id)
);






/*==============================================================*/
/* insert test data                                             */
/*==============================================================*/

insert  into user values ('123456', '123456', 1),
('123452', '123456', 2);