package com.ziwushichen.health.enums;

/**
 * 情绪状态枚举。
 */
public enum EmotionStateEnum {

    JOYFUL("愉悦"),
    CALM("平静"),
    UNPLEASANT("不愉悦");

    private final String inputValue;

    EmotionStateEnum(String inputValue) {
        this.inputValue = inputValue;
    }

    public String getInputValue() {
        return inputValue;
    }

    /**
     * 判断输入值是否合法。
     *
     * @param value 输入值
     * @return 是否合法
     */
    public static boolean isValidInput(String value) {
        for (EmotionStateEnum emotionStateEnum : values()) {
            if (emotionStateEnum.getInputValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
