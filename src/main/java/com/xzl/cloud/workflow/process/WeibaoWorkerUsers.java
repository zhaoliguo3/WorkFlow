package com.xzl.cloud.workflow.process;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class WeibaoWorkerUsers {
    String worker; //维保工人
    String confirmor; //维保工人流程：确认人
    String maintenancer; //维保工人流程：维保班长
    String property; //维保工人流程：物业审核
    String approve;

    //////
    List<String> workers;

    public Map toMap() {
        Map map = new HashMap();

        if (worker != null)
            map.put("worker", worker);

        if (confirmor != null)
            map.put("confirmor", confirmor);
        else
            map.put("confirmor", null);

        if (maintenancer != null)
            map.put("maintenancer", maintenancer);

        if (property != null)
            map.put("property", property);

        if (approve != null)
            map.put("approve", approve);

        if (workers !=null )
            map.put("workers", workers);
        return map;
    }
}
