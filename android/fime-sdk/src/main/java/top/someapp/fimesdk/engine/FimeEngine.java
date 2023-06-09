/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.engine;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.SchemaManager;
import top.someapp.fimesdk.Setting;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.Ejector;
import top.someapp.fimesdk.api.Filter;
import top.someapp.fimesdk.api.FimeHandler;
import top.someapp.fimesdk.api.FimeMessage;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.api.Schema;
import top.someapp.fimesdk.api.Translator;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.defaults.DefaultSchema;
import top.someapp.fimesdk.utils.Clipboard;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Nulls;
import top.someapp.fimesdk.view.VirtualKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zwz
 * Create on 2023-01-31
 */
@SuppressWarnings("SpellCheckingInspection")
public class FimeEngine implements ImeEngine, Filter<Candidate> {

    private static final String TAG = "FimeEngine";
    private final Map<String, FimeHandler> handlerMap = new HashMap<>();
    private final HandlerThread workThread;
    private final Handler handler;
    private ImeState state = ImeState.FREEZE;
    private int mode = CN_MODE;
    private InputMethodService ims;
    private FimeContext fimeContext;
    private SchemaManager.SchemaInfo schemaInfo;
    private Schema schema;

    public FimeEngine(InputMethodService ims) {
        this.workThread = new HandlerThread(TAG);
        this.workThread.start();
        this.handler = new Handler(workThread.getLooper());
        useInputMethodService(ims);
    }

    @Override public ImeState getState() {
        return state;
    }

    @Override public void enterState(ImeState newState) {
        ImeState oldState = state;
        switch (state) {
            case QUIT:
            case FREEZE:
                if (newState.ordinal() >= ImeState.READY.ordinal()) {   // FREEZE -> READY+
                    state = newState;
                    String schemaConf = Setting.getInstance()
                                               .getString(Setting.kActiveSchema);
                    if (schemaConf != null) {
                        if (schemaInfo.conf.equals(schemaConf)) {
                            fimeContext.showToastShortCenter("当前输入方案：" + schemaInfo.getName());
                        }
                        else {
                            useSchema(schemaConf);
                        }
                    }
                }
                break;
            case READY:
                if (newState.ordinal() > ImeState.READY.ordinal()) {    // READY -> READY+
                    state = newState;
                }
                else if (newState.ordinal() <= ImeState.FREEZE.ordinal()) {    // READY -> FREEZE-
                    state = newState;
                }
                break;
            case INPUT:
                break;
            default:    // READY+ -> READY+
                if (newState.ordinal() > ImeState.READY.ordinal()) {
                    state = newState;
                }
                break;
        }
        if (newState == ImeState.QUIT) {
            stop();
        }
        if (oldState != newState) resetInputContext();
    }

    @Override public void setMode(int mode) {
        this.mode = mode;
    }

    @Override public ImeEngine useInputMethodService(InputMethodService ims) {
        this.ims = ims;
        if (fimeContext == null) fimeContext = FimeContext.getInstance();
        String conf = Setting.getInstance()
                             .getString(Setting.kActiveSchema);
        useSchema(conf);
        start();
        return this;
    }

    @Override public Context getContext() {
        return ims;
    }

    @Override public Looper getWorkLopper() {
        return workThread.getLooper();
    }

    @Override public Schema getSchema() {
        return schema;
    }

    @Override public ImeEngine useSchema(String conf) {
        Logs.i("use schema by: " + conf);
        schemaInfo = SchemaManager.find(conf);
        try {
            Config config = schemaInfo.loadConfig();
            if (config == null) {
                Logs.e("invalid schema config: " + conf);
                return this;
            }
            schema = new DefaultSchema();
            schema.reconfigure(config);
            schema.setup(this);
            if (!schemaInfo.precompiled) {
                post(() -> SchemaManager.build(schemaInfo, config));    // 这是一个耗时操作!
            }
            resetInputContext();
            // notifyHandlers(FimeMessage.MSG_SCHEMA_ACTIVE);
            fimeContext.showToastLongCenter("切换输入方案为：" + schemaInfo.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
            Logs.w(e.getMessage());
        }
        return this;
    }

    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        if (restarting) enterState(ImeState.READY);
    }

    @Override public void onTap(@NonNull VirtualKey virtualKey) {
        Logs.d("onTap");
        final Keycode keycode = Keycode.getByCode(virtualKey.getCode());
        switch (getState()) {
            case FREEZE:
            case QUIT:
                ims.getCurrentInputConnection()
                   .sendKeyEvent(keycode.toNativeKeyEvent(KeyEvent.ACTION_DOWN));
                ims.getCurrentInputConnection()
                   .sendKeyEvent(keycode.toNativeKeyEvent(KeyEvent.ACTION_UP));
                return;
        }

        if (virtualKey.isFunctional()) {
            onFnKeyTap(keycode);
            notifyHandlers(FimeMessage.create(FimeMessage.MSG_INPUT_CHANGE));
            return;
        }

        if (mode == CN_MODE && schema.isOptionActive("cn", 0)) { // 中文输入模式
            cnModeInput(virtualKey, keycode);
        }
        else {
            asciiModeInput(virtualKey, keycode);
        }
        notifyHandlers(FimeMessage.create(FimeMessage.MSG_INPUT_CHANGE));
    }

    @Override public void requestSearch() {
        doSearch();
    }

    @SuppressWarnings("all")
    @Override public void manualEject() {
        getEjector().manualEject(getInputEditor());
        notifyHandlers(FimeMessage.create(FimeMessage.MSG_INPUT_CHANGE));
    }

    @Override public void registerHandler(@NonNull FimeHandler handler) {
        handlerMap.put(handler.getName(), handler);
    }

    @Override public void unregisterHandler(@NonNull String name) {
        handlerMap.remove(name);
    }

    @Override public void notifyHandlers(@NonNull Message message) {
        if (FimeMessage.hasMultipleHandlerFlag(message.what)) {
            for (FimeHandler handler : handlerMap.values()) {
                Logs.d("send message:0x%02x to %s.", message.what, handler.getName());
                handler.handle(message);
            }
        }
        else {
            for (FimeHandler handler : handlerMap.values()) {
                Logs.d("send message:0x%02x to %s.", message.what, handler.getName());
                if (handler.handleOnce(message)) break;
            }
        }
    }

    @Override public void notifyHandlersDelay(@NonNull Message message, int delayMills) {
        handler.postDelayed(() -> notifyHandlers(message), delayMills);
    }

    @Override public void post(Runnable work) {
        try {
            handler.post(work);
        }
        catch (Exception e) {
            e.printStackTrace();
            Logs.w(e.getMessage());
        }
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        Logs.d("onKeyDown");
        if (state == ImeState.FREEZE || state == ImeState.QUIT) return false;

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ims.requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS);
            state = ImeState.FREEZE;
            return true;
        }
        Keycode keycode = Keycode.convertNativeKey(keyCode, event);
        if (keycode != null) {
            onTap(new VirtualKey(keycode.code, keycode.label));
            // notifyHandlers(FimeMessage.MSG_REPAINT);
            return true;
        }
        return false;
    }

    @Override public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Logs.d("onKeyLongPress");
        return false;
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        Logs.d("onKeyUp");
        return state != ImeState.FREEZE && state != ImeState.QUIT;
    }

    @Override public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        Logs.d("onKeyMultiple");
        return false;
    }

    @Override public void commitText(String text) {
        if (ims != null) {
            ims.getCurrentInputConnection()
               .commitText(text, 1);    // <=0: 提交的文字在光标前，> 0: 在光标后
        }
        resetInputContext();
    }

    @Override public void filter(Collection<Candidate> items, Schema schema) {
        if (items == null || items.size() < 2) return;
        Set<Candidate> candidateSet = new LinkedHashSet<>(items.size());
        candidateSet.addAll(items);
        items.clear();
        items.addAll(candidateSet);
        candidateSet.clear();
    }

    private void asciiModeInput(VirtualKey virtualKey, Keycode keycode) {
        final int code = virtualKey.getCode();
        if (Keycode.isSpaceCode(code)) {
            commitText(" ");
        }
        else if (Keycode.isLetterCode(code)) {
            commitText(virtualKey.getLabel());
        }
        else if (Keycode.isEnterCode(code)) {
            ims.getCurrentInputConnection()
               .sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            ims.getCurrentInputConnection()
               .sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
        }
        else if (Keycode.isAnyKeyCode(code)) {
            commitText(Nulls.firstNonNull(virtualKey.getText(), virtualKey.getLabel()));
        }
        else {
            commitText(Nulls.firstNonNull(virtualKey.getText(), virtualKey.getLabel()));
        }
    }

    private void cnModeInput(VirtualKey virtualKey, Keycode keycode) {
        InputEditor inputEditor = getInputEditor();
        final int code = virtualKey.getCode();
        assert inputEditor != null;
        if (inputEditor.accept(keycode)) {
            doSearch();
        }
        else if (Keycode.isSpaceCode(code)) {
            if (inputEditor.hasInput()) {
                manualEject();
            }
            else {
                commitText(" ");
            }
        }
        else if (Keycode.isEnterCode(code)) {
            if (inputEditor.hasInput()) {
                commitText(inputEditor.getRawInput());
            }
            else {
                ims.getCurrentInputConnection()
                   .sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                ims.getCurrentInputConnection()
                   .sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
            }
        }
        else if (Keycode.isAnyKeyCode(code)) {
            commitText(Nulls.firstNonNull(virtualKey.getText(), virtualKey.getLabel()));
        }
        else {
            commitText(Nulls.firstNonNull(virtualKey.getText(), virtualKey.getLabel()));
        }
    }

    private void start() {
        Logs.i("start.");
        Clipboard.listen(fimeContext.getContext(), fimeContext.getCacheDir());
        if (schema == null) {
            Logs.e("invalid schema!");
        }
        else {
            resetInputContext();
        }
    }

    private void stop() {
        Logs.i("stop.");
        if (schema != null) {
            // TODO: 2023/2/23 do something clean up.
        }
    }

    private InputEditor getInputEditor() {
        return getSchema() == null ? null : getSchema().getInputEditor();
    }

    private Translator getTranslator() {
        return getSchema() == null ? null : getSchema().getTranslator();
    }

    private Ejector getEjector() {
        return getSchema() == null ? null : getSchema().getEjector();
    }

    private void resetInputContext() {
        InputEditor inputEditor = getInputEditor();
        if (inputEditor == null) return;

        inputEditor.clearInput()
                   .clearCandidates()
                   .setActiveIndex(0);
    }

    private void onFnKeyTap(Keycode keycode) {
        InputEditor inputEditor = getInputEditor();
        if (keycode.code == Keycode.VK_FN_BACKSPACE) {
            assert inputEditor != null;
            if (inputEditor.hasInput()) {
                inputEditor.backspace();
                doSearch();
            }
            else {
                ims.getCurrentInputConnection()
                   .sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                ims.getCurrentInputConnection()
                   .sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            notifyHandlers(FimeMessage.create(FimeMessage.MSG_INPUT_CHANGE));
        }
        else if (keycode.code == Keycode.VK_FN_CLEAR) {
            resetInputContext();
            notifyHandlers(FimeMessage.create(FimeMessage.MSG_INPUT_CHANGE));
        }
        else if (keycode.code == Keycode.VK_FN_ENTER) {
            assert inputEditor != null;
            if (inputEditor.hasInput()) {
                commitText(inputEditor.getRawInput());
                resetInputContext();
                notifyHandlers(FimeMessage.create(FimeMessage.MSG_INPUT_CHANGE));
            }
            else {
                ims.getCurrentInputConnection()
                   .sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                ims.getCurrentInputConnection()
                   .sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
            }
        }
    }

    @SuppressWarnings("all")
    private void doSearch() {
        InputEditor inputEditor = getInputEditor();
        Translator translator = getTranslator();
        assert inputEditor != null;
        inputEditor.clearCandidates();
        inputEditor.setActiveIndex(0);
        final List<String> searchCodes = inputEditor.getSearchCodes();
        if (searchCodes == null || searchCodes.isEmpty()) return;

        Logs.i("search(" + searchCodes + ") start.");
        handler.post(() -> {
            List<Candidate> candidates;
            assert translator != null;
            try {
                if (inputEditor.getSelected() == null) {
                    candidates = translator.translate(searchCodes);
                }
                else {
                    candidates = translator.translate(inputEditor.getSelected().text, searchCodes);
                }
                filter(candidates, getSchema());
                Logs.i("search(" + searchCodes + ") end, result.size=" + candidates.size());
                inputEditor.clearCandidates();
                inputEditor.setActiveIndex(0);
                for (Candidate candidate : candidates) {
                    inputEditor.appendCandidate(candidate);
                }
                notifyHandlers(FimeMessage.create(FimeMessage.MSG_CANDIDATE_CHANGE));
                if (!candidates.isEmpty()) {
                    getEjector().ejectOnCandidateChange(getInputEditor());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Logs.e(e.getMessage());
            }
        });
    }
}
