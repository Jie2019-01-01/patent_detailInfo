package org.lens.www.crawl;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.lens.www.entity.PatentEntity;
import org.lens.www.utils.PSUtil;
import java.io.IOException;

/**
 * 下载器：发送请求，获取响应
 */
public class Downloader {

    public Downloader(){System.out.println("* 下载器已经准备就绪."); }

    public PatentEntity execute(String lens_id){
        System.out.println("\t• 执行下载器，当前lens_id ——》" + lens_id);
        PatentEntity patentEntity = null;
        // 摘要
        String ab_url = "https://www.lens.org/lens/patent/" + lens_id;
        System.out.println("\t- ab_url ——》" + ab_url);
        String ab = crawl_ab(ab_url);

        // cite_key
        String cite_content = send_Request(ab_url);
        if (StringUtils.isEmpty(cite_content))
            return null;
        String cite_key = PSUtil.getInfo(cite_content, new String[]{"a.breadnum[href^=/lens/patent/]"}).replaceAll(" |/", "_");

        // 被引用
        String cites_by_parents1_url = "https://www.lens.org/lens/service/patent/"+ cite_key +"/incomingCitations?n=50&p=0";
        System.out.println("\t- cites_by_parents1_url ——》" + cites_by_parents1_url);
        String cites_by_parents = crawl_Cite(cites_by_parents1_url, "incoming");

        // 引用
        String cites_parents1_url = "https://www.lens.org/lens/service/patent/"+ cite_key +"/outgoingCitations?n=50&p=0";
        System.out.println("\t- cites_parents1_url ——》" + cites_parents1_url);
        String cites_parents = crawl_Cite(cites_parents1_url, "outgoing");

        // 同族
        String family_id_url = "https://www.lens.org/lens/patent/"+ lens_id +"/family";
        String family_content = send_Request(family_id_url);
        if (StringUtils.isEmpty(family_content))
            return null;
        String family_id = PSUtil.getInfo(family_content, new String[]{"lens-patent-family-heading[family-id]", "family-id"});
        String family_url = "https://www.lens.org/lens/api/patents/families/" + family_id;
        System.out.println("\t- family_url ——》" + family_url);
        String family = crawl_Family(family_url);

        if (ab==null || cites_by_parents==null || cites_parents==null || family==null) {
            if (ab==null){
                System.out.println("----------- ab的结果为null ----------");
            }else if(cites_by_parents==null){
                System.out.println("----------- cites_by_parents的结果为null ----------");
            }else if(cites_parents==null){
                System.out.println("----------- cites_parents的结果为null ----------");
            }else if(family==null){
                System.out.println("----------- family的结果为null ----------");
            }
            return null;
        }else {
            patentEntity = new PatentEntity(ab, cites_by_parents, cites_parents, family);
        }
        return patentEntity;
    }

    /**
     * 获取摘要
     */
    public String crawl_ab(String url){
        String content = send_Request(url);
        if (StringUtils.isEmpty(content))
            return null;
        String ab = PSUtil.getInfo(content, new String[]{"meta[property=og:description]", "content"});
        return ab;
    }

    /**
     * 获取cite和cite_by
     */
    public String crawl_Cite(String url, String type){
        String cite_field = "";
        switch (type){
            case "incoming":
                // 被引用
                String cites_by_parents_content = send_Request(url);
                if (StringUtils.isEmpty(cites_by_parents_content))
                    return "";
                cite_field = PSUtil.getCite(cites_by_parents_content, type);
                System.out.println("被引用字段==>" + cite_field);
                break;
            case "outgoing":
                // 引用
                String cites_parents_content = send_Request(url);
                if (StringUtils.isEmpty(cites_parents_content))
                    return "";
                cite_field = PSUtil.getCite(cites_parents_content, type);
                System.out.println("引用字段==>" + cite_field);
                break;
            default:
                System.out.println("检查一下，可能出错了......");
                break;
        }
        return cite_field;
    }

    /**
     * 发送请求，返回响应
     */
    public String send_Request(String url){
        CloseableHttpClient httpClient = HttpClients.custom().build();
        CloseableHttpResponse response = null;
        String content = "";
        try {
            Thread.sleep(5000);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
            httpGet.addHeader("cookie", "__uzma=6a03b738-9bbb-b34b-af83-8a998eb9a58e; __uzmb=1577153980; LENS_SESSION_ID=994EC9FFA4C17B4007ADE627CA2E15C5; TZ=Asia%2FShanghai; PREVIOUS_SIDE_TAB_VIEW=st-filters; SIDE_TAB_VIEW=st-filters; _pk_ses.1.2a81=1; _pk_id.1.2a81=38fea63b8625a787.1577153984.2.1577156282.1577156190.; __uzmc=783369151249; uzdbm_a=3aeb692d-9434-39f7-9d94-a7c5f86e3d5f; __uzmd=1577156499; AWSALB=HVBoqxNMbV1j0yUZtIva4nNVuCCDZo5P16WjFtgnMPbmuznopMCutilbCWQg/PNkOT/ffx0x+QaFlXgZ0t2at1BmLWH3lvPsDSCLNRSTvzs935bX5v44m0JM9qYD");
            RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
            httpGet.setConfig(defaultConfig);
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode()==200) {
                content = EntityUtils.toString(response.getEntity(), "utf-8");
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return content;
    }

    /**
     * 同族
     */
    public String crawl_Family(String url){
        String content = send_Request(url);
        if (StringUtils.isEmpty(content))
            return null;
        String family_field = PSUtil.getFamily(content);
        return family_field;
    }
}
