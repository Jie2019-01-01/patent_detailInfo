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
        String ab = crawl_ab(ab_url);
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
        String cites_by_parents = crawl_Cite(cites_by_parents1_url, "incoming");
        if(cites_by_parents==null){
            System.out.println("----------- cites_by_parents的结果为null ----------");
            return null;
        }
        System.out.println("cites_by_parents =======" + cites_by_parents);

        // 引用
        String cites_parents1_url = "https://www.lens.org/lens/service/patent/"+ cite_key +"/outgoingCitations?n=50&p=0";
        System.out.println("\t- cites_parents1_url ——》" + cites_parents1_url);
        String cites_parents = crawl_Cite(cites_parents1_url, "outgoing");
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
        String family = crawl_Family(family_url);
        if(family==null){
            return null;
        }
        System.out.println("family_info =====" + family);
        patentEntity = new PatentEntity(ab, cites_by_parents, cites_parents, family);
        return patentEntity;
    }

    /**
     * 获取摘要
     */
    public String crawl_ab(String url){
        String ab = null;
        String content = send_Request(url);
        if (StringUtils.isNotEmpty(content)) {
            ab = PSUtil.getInfo(content, new String[]{"meta[property=og:description]", "content"});
        }
        return ab;
    }

    /**
     * 获取cite和cite_by
     */
    public String crawl_Cite(String url, String type){
        String cite_field = null;
        String cites_content = send_Request(url);
        if (StringUtils.isNotEmpty(cites_content)){
            cite_field = PSUtil.getCite(cites_content);
        }
        return cite_field;
    }

    /**
     * 同族
     */
    public String crawl_Family(String url){
        String family_field = null;
        String content = send_Request(url);
        if (StringUtils.isNotEmpty(content)) {
            family_field = PSUtil.getFamily(content);
        }
        return family_field;
    }

    /**
     * 发送请求，返回响应
     */
    public String send_Request(String url){
        CloseableHttpClient httpClient = HttpClients.custom().build();
        CloseableHttpResponse response = null;
        String content = null;
        try {
//            Thread.sleep(10000);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
            httpGet.addHeader("cookie", "__uzma=3bde9398-8ca0-90b9-87ab-a629837bb2db; __uzmb=1577233773; LENS_SESSION_ID=7EEEE71679F6A0C75642147F0E289267; TZ=Asia%2FShanghai; _pk_ses.1.2a81=1; PREVIOUS_SIDE_TAB_VIEW=st-filters; SIDE_TAB_VIEW=st-filters; _pk_id.1.2a81=5512c32c46872b85.1577233784.1.1577236430.1577233784.; __uzmd=1577236430; __uzmc=496339776779; uzdbm_a=da93cf9d-9434-ec7b-9cb7-e3b9402df13e; AWSALB=uF5SrnoeuZhZTa1u/A4PNXi1M7H4oJyGS5I5TlrQ0ZvX1GD9RiLbfI569ZGfnEFWowhc4R/dysI8/29t17t8QebnImGNSgExdvzIObD5aH4AWi80/6+OOjUxh5Fg");
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
