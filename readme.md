# 一、游戏基本流程介绍
初始的游戏界面如下所示，一开始只有两个选项，一个是单机模式下游玩，另一个是联网模式的游玩。
单机模式下无需联网即可游玩。
联网模式必须能连外网，连接服务器，登录账号才可进入游戏。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image005.jpg)

## 联网模式
### 登陆阶段
#### 登录
单击联网模式后如下所示（前提手机需要联网，如果未联网会弹出弹框提示连接失败），如果已经注册过用户名和密码，可以直接输入用户名和密码进行登录，服务器返回对应的登录结果。如果登录成功会进入匹配阶段。
选中“记住密码”后，如果登录成功，会记录本次登录的用户名和密码，下次登录的时候用户名和密码会已经被输入。
选中“自动登录”后，如果登录成功，下次登录的时候会自动按照本次登录的用户名和密码进行登录。
点击右上角绿色的“加号”可进行注册。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image006.jpg)

#### 注册
玩家输入用户名，密码和重新输入密码（密码需要大于6位），单击“注册”发送消息给服务器，服务器返回给玩家是否注册成功（没有重复用户名，密码合法）。如果注册成功，会返回到上图的登录界面，并且用户名和密码会已经输入进去。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image007.jpg) 

### 匹配阶段
登录成功后会进入匹配阶段，匹配阶段有“开始匹配”和“个人信息”两个按钮。
点击“个人信息”会出现个人信息界面，如下所示。玩家可修改玩家名，性别，人物图片（图片只有固定的6种），可查看当前积分（不能修改），点击保存会将个人信息上传到服务器。
点击“开始匹配”后，服务器会查询当前大厅的房间，找出当前房间人数未满（小于4），将玩家放入该房间中，进入游戏准备阶段。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image008.jpg)
![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image009.jpg)

### 游戏准备阶段
登录成功后会进入匹配阶段，匹配阶段有“开始匹配”和“个人信息”两个按钮。点击“开始”按钮会准备，点击“机器人”图标会弹出“要添加电脑吗”的提示框，确认后会将该位置的玩家设置成电脑（电脑会自动处于准备阶段）。
当四个人都准备会进入游戏开始阶段。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image010.jpg) 

### 游戏开始（发牌）阶段
游戏开始后，客户端接受服务器端的牌组信息，客户端显示发牌阶段，发牌动画完成进入叫地主阶段。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image011.jpg)
 
### 叫地主阶段
1. 由服务器随机指定一个人开始叫分，玩家可以选择叫“1分”、“2分”、“3分”或者不叫分，所叫的分数必须比上一家高，每位玩家有12秒的叫分时间，如果12秒内未叫分客户端将会自动不叫，如果客户端断开未发消息给服务器，服务器15秒内会认为该玩家不叫，并且认为该玩家断线。叫3分的玩家成为地主；如果没有玩家叫3分，则一轮下来叫分最高的玩家成为地主，进入发地主牌阶段。

2. 界面显示上，如果有人叫了一分、两分，玩家对应的一分、两分按钮就变灰，变成了不可选中的状态。

3. 如果没有任何一个玩家叫分，则服务器洗牌，重新发卡组信息，重新开始叫地主；如果连续三轮没有人叫地主，则服务器随机指定一个人作为地主，底分设置为1，进入发地主牌阶段。

叫完地主后，底牌翻转显示（左上角），给地主发8张牌，当前底分变成所叫的最高分，倍数变为1，显示地主UI，然后进入出牌阶段。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image012.jpg)
 
### 出牌阶段
出牌细则：地主先出牌，按逆时针循环顺序依次出牌，每位玩家有36秒的出牌时间，如果玩家36秒内未出牌，客户端会自动进行托管，由AI帮用户出牌；如果39秒内客户端未发消息给服务器，服务器将其设置为AI出牌，并认为该玩家断线。轮到用户跟牌时，用户可以选择“不出”或出比上一个玩家大的牌。某一玩家出完牌时进入积分结算阶段。

1.	出牌按钮：判断选中的牌能不能出，能出则打出（按照下面牌型比较的规则），不能出则提示不能出。

2.	不出按钮：显示不出，玩家过，下一玩家出牌（如果当前为玩家出牌阶段，则不出按钮会变成灰色，玩家不能选中）。

3.	提示按钮：根据算法将能出的牌选中。
3.1.	如果当前处于自己出牌阶段，单击“提示”会提示当前能一次性出的牌的数量最多的牌型，再次单击“提示”会出比当前提示大的牌，如果没有，会提示下一种能出的牌的数量最多得牌型，以此循环。
3.2.	如果当前处于上家已出牌阶段，单击“提示”会提示比上家大的牌，再次点击“提示”会提示比当前提示大的牌，依次循环；如果当前没有比上家大的牌，则提示当前没有可出的牌。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image013.jpg)
 
### 积分结算阶段
地主胜：地主得分为3×底分×倍数；其余玩家各得：－底分×倍数
地主败：地主得分为 －3×底分×倍数；其余玩家各得：底分×倍数
以下情况，输赢的基本分翻倍（翻倍条件可重叠）：
每打出一个六张牌和七张牌的炸弹，基本分×2
每打出一个8张牌炸弹和天王炸弹，基本分×3
地主所有牌出完，其他三家一张都未出，基本分×2
其他两家中有一家先出完牌，地主只出过一手牌，基本分×2

结算完四位玩家的积分后，将其保存到数据库信息中（电脑数据不保存），点结算积分榜的差，结算积分，回到游戏准备阶段。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image014.jpg)
 
## 单机模式
单机模式和联网模式的流程相同，单击“单机模式”进入如下界面（单机模式无需联网），其余三个人为电脑，单机模式多了个明牌按钮，下面介绍额外功能会介绍。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image015.jpg)
#  游戏额外功能介绍
## 托管功能
玩家单击托管按钮，显示托管图片，进入托管状态，玩家出牌将由AI进行控制。在托管状态下单击托管按钮可取消托管状态。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image016.jpg)
## 排序功能
本游戏手牌有两种排序方式，一种是按照牌的大小进行排序，另一种是按照牌的数量进行排序。单击排序按钮后会在两种排序方式下切换。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image017.jpg)
## 背景音乐按钮
控制背景音乐播放，可关闭背景音乐和开启背景音乐。
背景音乐播放：从八首背景音乐中循环播放，当一首结束后随机播放下一首。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image018.jpg)
![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image019.jpg)
## 游戏音效按钮
控制游戏音效播放，可关闭游戏音效和开启游戏音效。
游戏音效播放：有开始游戏、发牌、出牌、提醒、倍数翻倍、胜利失败、男女生出牌类型、叫地主、抢地主、打牌、不出、不叫、飞机炸弹音等等各种音效。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image020.jpg)
![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image021.jpg)
## 明牌按钮
在单机模式下，玩家可以点击明牌按钮，电脑的牌将会翻转（显示），图标从“明”字变为“暗”字，点击“暗”字，电脑的将再次翻转（关闭显示）。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image022.jpg)  
![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image023.jpg)

## 连续选牌
选牌的时候可以进行连续选牌，被选中的牌会变成阴影。松开后，变成阴影的牌会相应的上浮或下移，然后扑克牌图片还回原样。

![image](https://raw.githubusercontent.com/WangAnXin/poker/master/Poker/rdImage/image025.jpg)
## 长按屏幕可以使在上方的牌全部放下

## 断线重连功能
在网络模式下提供断线重连的功能，如果玩家在叫分阶段或者是出牌阶段退出游戏，服务器将设置该玩家为不叫分并让AI为其出牌。当该玩家在登录阶段登录时或者在匹配阶段匹配时，如果上局并未结束将会进入断线重连状态，如果当前局处在叫分阶段，则玩家客户端将直接进入发牌阶段（加快发牌动画）；如果当前局处在出牌阶段，将从发牌阶段后直接进入出牌阶段，玩家即可继续本局游戏。
