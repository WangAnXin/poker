using Newtonsoft.Json;
using pokerServer.Landlord.entity;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.Helper {
    public class JsonHelper {

        //发送卡组信息
        public static string sendCards(string propertyName, List<Card> cards) {
            //因为卡最多108张，一个byte8位0~255，足够放了
            byte[] buffer = new byte[cards.Count];
            for (int i = 0; i < cards.Count; i++) {
                buffer[i] = (byte)cards[i].cardIndex;
            }
            string cardsMsg = Encoding.UTF8.GetString(buffer, 0, buffer.Length);

            //创建发送的消息
            StringWriter sw = new StringWriter();
            JsonWriter jsonWriter = new JsonTextWriter(sw);
            //写入卡组信息
            jsonWriter.WriteStartObject();
            jsonWriter.WritePropertyName(propertyName);
            jsonWriter.WriteValue(cardsMsg);
            jsonWriter.WriteEndObject();

            //清空流
            jsonWriter.Flush();

            //发送卡组信息
            return sw.GetStringBuilder().ToString();
        }

        //将卡组信息转换为Card数组
        public static List<Card> jsonToCards(string cardsMsg) {
            List<Card> resCards = new List<Card>();

            //先将string转为byte
            byte[] cardByte = Encoding.UTF8.GetBytes(cardsMsg);
            for (int i = 0; i < cardByte.Length; i++) {
                resCards.Add(Landlord_GameMode.ConvertCard(cardByte[i]));
            }
            //更新卡组卡的数量
            PeopleOperator.updateCardNum(resCards);

            return resCards;
        }

        //发送普通的int信息
        public static string jsonObjectInt(string propertyName, int msg) {
            //创建发送的消息
            StringWriter sw = new StringWriter();
            JsonWriter jsonWriter = new JsonTextWriter(sw);
            //写入卡组信息
            jsonWriter.WriteStartObject();
            jsonWriter.WritePropertyName(propertyName);
            jsonWriter.WriteValue(msg);
            jsonWriter.WriteEndObject();

            //清空流
            jsonWriter.Flush();

            //发送卡组信息
            return sw.GetStringBuilder().ToString();
        }

        //发送普通的bool信息
        public static string jsonObjectBool(string propertyName, bool msg) {
            //创建发送的消息
            StringWriter sw = new StringWriter();
            JsonWriter jsonWriter = new JsonTextWriter(sw);
            //写入卡组信息
            jsonWriter.WriteStartObject();
            jsonWriter.WritePropertyName(propertyName);
            jsonWriter.WriteValue(msg);
            jsonWriter.WriteEndObject();

            //清空流
            jsonWriter.Flush();

            //发送卡组信息
            return sw.GetStringBuilder().ToString();
        }
    }
}
