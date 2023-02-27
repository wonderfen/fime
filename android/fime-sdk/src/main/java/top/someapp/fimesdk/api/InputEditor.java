package top.someapp.fimesdk.api;

import top.someapp.fimesdk.config.Keycode;

import java.util.List;

/**
 * 在输入过程中的编辑器
 *
 * @author zwz
 * Create on 2022-12-27
 */
public interface InputEditor extends ImeEngineAware, Configurable {

    /**
     * 是否接受按键值 keycode
     *
     * @param keycode 按键值
     * @return true: 接受；false：不接受
     */
    boolean accept(Keycode keycode);

    /**
     * 获取原始的输入码
     *
     * @return 原始的输入码
     */
    String getRawInput();

    /**
     * 获取最后一个编码段，如拼音方案输入码为 xi'an 时，最后一个编码断为 an
     *
     * @return
     */
    default String getLastSegment() {
        return getRawInput();
    }

    List<String> getSearchCodes();

    void setSearchCodes(List<String> codes);

    /**
     * 获取输入码提示，如：输入码是双拼，提示为全拼
     *
     * @return 输入码提示
     */
    String getPrompt();

    /**
     * 清空当前输入码
     */
    InputEditor clearInput();

    /**
     * 清空候选
     *
     * @return InputEditor.this
     */
    InputEditor clearCandidates();

    /**
     * 获取当前光标位置
     *
     * @return 当前光标位置
     */
    int getCursor();

    /**
     * 将 code 追加到输入内容后面
     *
     * @param code 要追加的内容
     */
    InputEditor append(String code);

    /**
     * 将 code 插入到输入内容的 index 位置
     *
     * @param code 要插入的内容
     * @param index 要插入的位置
     */
    InputEditor insert(String code, int index);

    /**
     * 往前删除一个字符或清楚已选择的候选
     */
    InputEditor backspace();

    /**
     * 删除 index 之后的一个字符
     *
     * @param index 待删除字符的位置
     */
    InputEditor delete(int index);

    /**
     * 获取候选列表
     *
     * @return 候选列表
     */
    List<Candidate> getCandidateList();

    /**
     * 追加一个候选到候选列表中
     *
     * @param candidate 待追加的候选
     */
    void appendCandidate(Candidate candidate);

    /**
     * 获取索引为 index 的候选
     *
     * @param index 索引
     * @return 索引为 index 的候选，不存在返回 null
     */
    Candidate getCandidateAt(int index);

    /**
     * 获取选中的候选项
     *
     * @return 选中的候选项，不存在返回 null
     */
    Candidate getActiveCandidate();

    /**
     * 是否存在输入码
     *
     * @return true：存在；false：不存在
     */
    boolean hasInput();

    /**
     * 候选列表是否有候选项
     *
     * @return true：有候选项；false：没有
     */
    boolean hasCandidate();

    /**
     * 获取选中候选项的索引
     *
     * @return 选中候选项的索引
     */
    int getActiveIndex();

    /**
     * 设置选中候选项的索引
     *
     * @param activeIndex 选中候选项的索引
     * @return InputEditor.this
     */
    InputEditor setActiveIndex(int activeIndex);

    /**
     * 接受 index 对应的候选
     *
     * @param index 候选项的索引
     */
    void select(int index);

    /**
     * 获取已接受的候选项
     *
     * @return 已接收的候选项
     */
    Candidate getSelected();
}
