package org.lens.www;

import org.lens.www.crawl.Downloader;
import org.lens.www.pipeline.PatentPipeline;
import org.lens.www.entity.PatentEntity;

import java.util.List;

/**
 * 获取专利数据的abstract、cite works、cite parents、family info
 * ............
 * ............
 * ............
 * ............
 * ............
 *
 */
public class Application {

    public static void main(String[] args) {
        System.out.println("\t\t\t\t\t\t\t---------- 开始启动，patent爬虫 ----------");
        boolean b = true;
        PatentPipeline patentDao = new PatentPipeline();
        while (b){
            List<String> idList = patentDao.findNoAb();
            if (idList==null || idList.size()==0)
                break;
//        List<String> idList = new ArrayList<String>(){{add("000-000-241-677-072");add("063-596-049-675-945");}};
            Downloader downloader = new Downloader();
            // 循环lens_id
            for(String id: idList){
                System.out.println("lens_id =====>" + id);
                PatentEntity patentEntity = downloader.execute(id);
                if(patentEntity == null){
                    b = false;
                    break;
                }
                System.out.println("结果输出：" + patentEntity + "\r\n");
                // 初始化模型，传递管道层
                patentDao.save_data(patentEntity, id);
            }
            System.out.println("********************************************");
        }
    }
}
