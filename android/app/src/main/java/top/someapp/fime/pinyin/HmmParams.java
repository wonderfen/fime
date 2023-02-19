package top.someapp.fime.pinyin;

import java.util.List;

/**
 * Hmm 模型参数
 *
 * @author zwz
 * Created on 2023-01-30
 */
public interface HmmParams {

    double MIN_PROB = Math.log(Double.MIN_VALUE);

    /**
     * 某个状态(单字)的初始概率
     *
     * @param state 状态(单个汉字)
     * @param fallback 不存在时使用的默认值
     * @return 单个汉字的初始概率
     */
    double start(String state, double fallback);

    /**
     * 某个状态(单字)到观测值(编码)的发射概率
     *
     * @param state 状态(单个汉字)
     * @param observation 观测值(编码)
     * @param fallback 不存在时使用的默认值
     * @return 发射概率
     */
    double emission(String state, String observation, double fallback);

    /**
     * from -> to 的状态转移概率，任意两个汉字之间的转移概率
     *
     * @param from 起始状态(可能的多个汉字)
     * @param to 结束状态(可能的多个汉字)
     * @return 转移概率
     */
    List<Transition> transition(List<StartAndEmission> from, List<StartAndEmission> to);

    /**
     * 观测值对应的状态和初始概率和发射概率
     *
     * @param observation 观测值
     * @return 初始概率和发射概率列表
     */
    List<StartAndEmission> getStartAndEmission(String observation);

    /**
     * 观测值对应的状态和初始概率和发射概率
     *
     * @param observation 观测值
     * @param prefixMatch 是否仅使用前缀匹配
     * @return 初始概率和发射概率列表
     */
    List<StartAndEmission> getStartAndEmission(String observation, boolean prefixMatch);

    /**
     * @author zwz
     * Create on 2023-01-31
     */
    interface Transition {

        String getFrom();

        void setFrom(String from);

        String getTo();

        void setTo(String to);

        double getTransition();

        void setTransition(double transition);
    }

    /**
     * @author zwz
     * Create on 2023-01-31
     */
    interface StartAndEmission {

        String getText();

        void setText(String text);

        String getCode();

        void setCode(String code);

        double getStart();

        void setStart(double start);

        double getEmission();

        void setEmission(double emission);
    }
}
