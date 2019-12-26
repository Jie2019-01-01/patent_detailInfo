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

/**
 * 下载器：发送请求，获取响应
 */
public class Downloader {

    public Downloader(){System.out.println("* 下载器已经准备就绪."); }

    public PatentEntity execute(String lens_id){
        PatentEntity patentEntity = null;
        // 摘要
        String ab_url = "https://www.lens.org/lens/patent/" + lens_id;
        System.out.println("\t- ab_url ——》" + ab_url);
        String ab = crawl_field(ab_url, "ab");
        if (ab==null){
            System.out.println("----------- ab的结果为null ----------");
            return null;
        }
        System.out.println("摘要：======" + ab);

        // cite_key
        String cite_content = send_Request(ab_url);
        if (StringUtils.isEmpty(cite_content))
            return null;
        String cite_key = PSUtil.getInfo(cite_content, new String[]{"a.breadnum[href^=/lens/patent/]"}).replaceAll(" |/", "_");

        // 被引用
        String cites_by_parents1_url = "https://www.lens.org/lens/service/patent/"+ cite_key +"/incomingCitations?n=50&p=0";
        System.out.println("\t- cites_by_parents1_url ——》" + cites_by_parents1_url);
        String cites_by_parents = crawl_field(cites_by_parents1_url, "cite");
        if(cites_by_parents==null){
            System.out.println("----------- cites_by_parents的结果为null ----------");
            return null;
        }
        System.out.println("cites_by_parents =======" + cites_by_parents);

        // 引用
        String cites_parents1_url = "https://www.lens.org/lens/service/patent/"+ cite_key +"/outgoingCitations?n=50&p=0";
        System.out.println("\t- cites_parents1_url ——》" + cites_parents1_url);
        String cites_parents = crawl_field(cites_parents1_url, "cite");
        if(cites_parents==null){
            System.out.println("----------- cites_parents的结果为null ----------");
            return null;
        }
        System.out.println("cites_parents =======" + cites_parents);

        // 同族
        String family_id_url = "https://www.lens.org/lens/patent/"+ lens_id +"/family";
        String family_content = send_Request(family_id_url);
        if (StringUtils.isEmpty(family_content))
            return null;
        String family_id = PSUtil.getInfo(family_content, new String[]{"lens-patent-family-heading[family-id]", "family-id"});
        String family_url = "https://www.lens.org/lens/api/patents/families/" + family_id;
        System.out.println("\t- family_url ——》" + family_url);
        String family = crawl_field(family_url, "family");
        if(family==null){
            return null;
        }
        System.out.println("family_info =====" + family);
        patentEntity = new PatentEntity(ab, cites_by_parents, cites_parents, family);
        return patentEntity;
    }

    /**
     * 详情信息的字段获取
     * @param url 各个字段的请求地址
     * @param type 字段所属类型
     * @return
     */
    public String crawl_field(String url, String type){
        String field = null;
        String content = send_Request(url);
        if (StringUtils.isNotEmpty(content)) {
            switch (type){
                case "ab":
                    field = PSUtil.getInfo(content, new String[]{"meta[property=og:description]", "content"});
                    break;
                case "cite":
                    field = PSUtil.getCite(content);
                    break;
                case "family":
                    field = PSUtil.getFamily(content);
                    break;
                default:
                    System.out.println("[warning] --- 该领域没有数据可以获取：" + type);
                    break;
            }
        }
        return field;
    }

    /**
     * 发送请求，返回响应
     */
    public String send_Request(String url){
        CloseableHttpClient httpClient = HttpClients.custom().build();
        CloseableHttpResponse response = null;
        String content = null;
        try {
            Thread.sleep(10000);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
            httpGet.addHeader("cookie", "__uzma=6ae79c79-8f8e-9b39-b59b-aafba218bf4b; __uzmb=1577340752; LENS_SESSION_ID=61D5CF34A5260EAD22F9EDA5FFF10632; TZ=Asia%2FShanghai; _pk_ses.1.2a81=1; PREVIOUS_SIDE_TAB_VIEW=st-filters; SIDE_TAB_VIEW=st-filters; _pk_id.1.2a81=d7d3c0d7eb1519ed.1577340760.1.1577340865.1577340760.; __uzmd=1577340865; __uzmc=816714949422; uzdbm_a=60038f5a-9434-1cf4-8ec5-ec4119767e2a; AWSALB=yzNrgXaAutga/eGOkAh760WpHvcH5H2hWABiHgU22t6By7zZdcux5AswSkK6n8BXijnqv6E5KLD4IqL22NvvsFtuEQ0GY1rG4yvkVfvyp5jkY1k80HUGdBmzeeR0");
            RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
            httpGet.setConfig(defaultConfig);
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode()==200) {
                content = EntityUtils.toString(response.getEntity(), "utf-8");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return content;
    }
}
