package com.example.meridian.domain.enums;

import lombok.Getter;
import java.time.LocalTime;
/** 固定12时辰 */
@Getter
public enum MeridianTimeSlotEnum {
ZI("ZI","子时",LocalTime.of(23,0),LocalTime.of(0,59),"胆经"), CHOU("CHOU","丑时",LocalTime.of(1,0),LocalTime.of(2,59),"肝经"),
YIN("YIN","寅时",LocalTime.of(3,0),LocalTime.of(4,59),"肺经"), MAO("MAO","卯时",LocalTime.of(5,0),LocalTime.of(6,59),"大肠经"),
CHEN("CHEN","辰时",LocalTime.of(7,0),LocalTime.of(8,59),"胃经"), SI("SI","巳时",LocalTime.of(9,0),LocalTime.of(10,59),"脾经"),
WU("WU","午时",LocalTime.of(11,0),LocalTime.of(12,59),"心经"), WEI("WEI","未时",LocalTime.of(13,0),LocalTime.of(14,59),"小肠经"),
SHEN("SHEN","申时",LocalTime.of(15,0),LocalTime.of(16,59),"膀胱经"), YOU("YOU","酉时",LocalTime.of(17,0),LocalTime.of(18,59),"肾经"),
XU("XU","戌时",LocalTime.of(19,0),LocalTime.of(20,59),"心包经"), HAI("HAI","亥时",LocalTime.of(21,0),LocalTime.of(22,59),"三焦经");
private final String code; private final String cnName; private final LocalTime start; private final LocalTime end; private final String meridianName;
MeridianTimeSlotEnum(String c,String n,LocalTime s,LocalTime e,String m){code=c;cnName=n;start=s;end=e;meridianName=m;}
}
