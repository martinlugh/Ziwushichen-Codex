package com.example.meridian.service;

import com.example.meridian.domain.enums.MeridianTimeSlotEnum;
import com.example.meridian.domain.vo.ScoringResultVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** 文案服务 */
@Service
public class MeridianNarrativeService {
    public String buildMedicalSummary(ScoringResultVo score,String color){
        if("DATA_INSUFFICIENT".equals(color)) return "当前同一时辰窗口数据不足，请继续采集。";
        if("IMBALANCE_ALERT".equals(color)) return "当前出现失衡预警，请降低负荷并持续观察，本结果不用于疾病诊断。";
        if("SUB_HEALTH".equals(color)) return "当前有亚健康趋势，建议优化休息与活动节律。";
        return "当前状态整体稳定，建议保持良好作息。";
    }
    public String buildTcmMeridianSummary(String slotCode){
        MeridianTimeSlotEnum s=MeridianTimeSlotEnum.valueOf(slotCode);
        return "当前处于"+s.getCnName()+"（"+s.getMeridianName()+"）时段，建议顺应时辰节律进行健康管理。";
    }
    public List<String> buildActionAdvice(ScoringResultVo score,String color){
        List<String> list=new ArrayList<>();
        if(score.getLoadScore().doubleValue()<70) list.add("建议降低当前活动负荷并安排短时休息");
        if(score.getOxygenScore().doubleValue()<70) list.add("建议进行平稳呼吸训练并避免剧烈活动");
        if(list.isEmpty()) list.add("建议继续保持当前节律并持续同一时辰跟踪");
        list.add("如持续明显不适，请及时线下就医咨询");
        return list;
    }
}
