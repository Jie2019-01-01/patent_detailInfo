package org.lens.www.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 数据解析工具
 */
public class PSUtil {

    public PSUtil(){System.out.println("* 数据解析工具PS已经就绪");}
    /**
     * 解析数据
     */
    public static String getInfo(String content, String[] policy){
        System.out.println("\t- 解析策略==》" + policy[0]);
        Element element = null;
        String field = "";
        try {
            Document docs = Jsoup.parse(content);
            if(policy.length==2){
                element = docs.select(policy[0]).get(0);
                field = element.attr(policy[1]);
            }else{
                element = docs.select(policy[0]).get(0);
                field = element.text();
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return field;
    }

    /**
     * 获取Cite专利
     */
    public static String getCite(String content){
        String field = "";
        try {
            JSONObject jsonObject = JSONObject.parseObject(content);
            JSONArray jsonArray = jsonObject.getJSONArray("hits");
            if (jsonArray.size()>0){
                for(int i=0; i<jsonArray.size()-1; i++){
                    field += jsonArray.getJSONObject(i).getString("publicationKey") + ",";
                }
                field += jsonArray.getJSONObject(jsonArray.size()-1).getString("publicationKey");
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return field;
    }

    /**
     * 获取同族专利
     */
    public static String getFamily(String content){
        String field = "";
        try{
            JSONObject jsonObject = JSONObject.parseObject(content).getJSONObject("result");
            JSONArray jsonArray = jsonObject.getJSONArray("hits");
            for(int i=0; i<jsonArray.size()-1; i++){
                field += jsonArray.getJSONObject(i).getString("displayKey") + ",";
            }
            field += jsonArray.getJSONObject(jsonArray.size()-1).getString("displayKey");
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return field;
    }
}
