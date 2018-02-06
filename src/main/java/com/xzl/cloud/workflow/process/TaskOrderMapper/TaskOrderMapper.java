package com.xzl.cloud.workflow.process.TaskOrderMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据task key做流程序号的先后排序，配合驳回操作
 */
public class TaskOrderMapper {
    public static Map MAPPER(){
        Map weibaoWorkerMap = new HashMap();
            weibaoWorkerMap.put("start", 0);
            weibaoWorkerMap.put("worker", 100);
            weibaoWorkerMap.put("confirmor", 200);
            weibaoWorkerMap.put("maintenance", 300);
            weibaoWorkerMap.put("property", 400);
            weibaoWorkerMap.put("end", 10000);
        Map map=new HashMap();
            map.put("weibaoWorker", weibaoWorkerMap);

    return map;

    }




//    public static Map MAPPER = new HashMap(){{
//////////////////////////////////////////////////////////////////////////////////
//        Map weibaoWorkerMap = new HashMap(){{
//            put("start", 0);
//            put("worker", 100);
//            put("confirmor", 200);
//            put("maintenance", 300);
//            put("property", 400);
//            put("end", 10000);
//        }};
//
//        put("weibaoWorker", weibaoWorkerMap);
//    }};


}
