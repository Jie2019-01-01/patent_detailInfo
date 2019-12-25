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
            httpGet.addHeader("cookie", " __uzma=3bde9398-8ca0-90b9-87ab-a629837bb2db; __uzmb=1577233773; LENS_SESSION_ID=012A89CDA0CBE8FBB60FF14C4EE66AA1; _pk_ref.1.2a81=%5B%22%22%2C%22%22%2C1577253957%2C%22http%3A%2F%2Fvalidate.perfdrive.com%2Flens%2Fcaptcha%3Fssa%3D5b809f18-b79c-878b-83b3-8b2ab279bad8%26ssb%3Da51pzz6ac3460be4dm013h51h%26ssc%3Dwww.lens.org%2Flens%2Fpatent%2F186-113-970-743-881%26ssd%3D066599659848267%26sse%3Djaaifhbkedfdd%40p%26ssf%3D4458ef26ad3c95ebefac9a65c067f52f8d45e327%26ssg%3Df731d8ac-20be-7fa3-c6ee-ae003a372a80%26ssh%3Db41c9030-0c76-b130-08d4-0f79764215df%26ssi%3D9525a0ef-9434-69cb-34a0-3debdf5e867f%26ssj%3D359f1d0e-5900-a65b-2cfa-4d4df18a2760%26ssk%3Dunblock%40lens.org%26ssl%3D709656256195%26ssm%3D425354662240973101068213781787186%26ssn%3D6a2df54c03e898782cf2116075a92ead1fe93bde9398-8ca0-90b9-8c2591%26sso%3D83c9a7ab-a629837bb2dbf6d300c1f80265aaa4699cbbd027066617b6%26ssp%3D21919804411577257905157721591807526%26ssq%3D40849693765474722311133773587560516947483%26ssr%3DMTE5LjE2My4zNS4xMDg%3D%26sss%3DMozilla%2F4.0%20(Windows%20NT%205.1)%20AppleWebKit%2F535.7%20(KHTML%2Clike%20zeco)%20Chrome%2F33.0.1750.154%20Safari%2F536.7%26sst%3DMozilla%2F5.0%20(Windows%20NT%2010.0%3B%20Win64%3B%20x64)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Chrome%2F79.0.3945.88%20Safari%2F537.36%26ssu%3DMozilla%2F5.0%20(compatible%3B%20Yahoo!%20Slurp%3B%20http%3A%2F%2Fhelp.yahoo.com%2Fhelp%2Fus%2Fys%22%5D; _pk_ses.1.2a81=1; TZ=Asia%2FShanghai; _pk_id.1.2a81=5512c32c46872b85.1577233784.4.1577254653.1577253957.; __uzmc=6134223513528; uzdbm_a=3fe573df-9434-0dff-1dc2-2b82f971deff; __uzmd=1577254864; AWSALB=gpIXQ8U1sXTuS/mCXwmCgjzYF41Szl6/IsX5h2MPM6FA2FoD7XV/JHaK5GqFT8TuWLutcIY4wkjREuQUXxb2gjGtgl3dvu0W9beN8H26zo523hcvz3mqJH1d/UZR");
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
