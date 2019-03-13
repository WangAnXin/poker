USE master;
GO
CREATE DATABASE LandlordDB
ON 
( NAME = LandlordDB,
    FILENAME = 'D:\\Study\\Android\\poker_server\\pokerServer\\LandlordDB.mdf',
    SIZE = 5MB,
    MAXSIZE = 50MB,
    FILEGROWTH = 1MB )
LOG ON
( NAME = RSGLXT_log,
    FILENAME = 'D:\\Study\\Android\\poker_server\\pokerServer\\LandlordDB.ldf',
    SIZE = 3MB,
    MAXSIZE = 25MB,
    FILEGROWTH = 1MB )
		COLLATE Chinese_PRC_CI_AS;		--不区分大小写
GO

USE LandlordDB
CREATE TABLE USER_Table					  --用户表
(
username nvarchar(15) not null primary key,   --玩家用户名
password nvarchar(20) null,						--玩家密码

name nvarchar(10) null,		--玩家名
sex bit null default 1,				   --玩家性别
score int null default 0,				--玩家的积分
image int null default 0,				--玩家的图片
)

CREATE UNIQUE INDEX USERNAME_INDEX ON USER_Table(USERNAME);	--按用户名来建立索引