package com.xzl.cloud.workflow.process;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class UserSetup {
    String user;
    String manager;
    String confirmor; //维保工人流程：确认人
    String maintenance; //维保工人流程：维保班长
    String property; //维保工人流程：物业审核

    String worker;   //维保工人流程：维保填写人


    public Map toMap() {
        if (user == null && manager == null)
            return null;
        Map map = new HashMap();
        if (user != null)
            map.put("user", user);

        if (manager != null)
            map.put("manager", manager);

        if (confirmor != null)
            map.put("manager", confirmor);

        if (maintenance != null)
            map.put("manager", maintenance);

        if (property != null)
            map.put("manager", property);

        //
        if (worker!=null)
            map.put("worker",worker);

        return map;
    }
}
