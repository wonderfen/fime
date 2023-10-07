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
import com.osfans.trime.core.Rime;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.Setting;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.FimeHandler;
import top.someapp.fimesdk.api.FimeMessage;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.api.Schema;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.rime.RimeSchema;
import top.someapp.fimesdk.utils.Clipboard;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Nulls;
import top.someapp.fimesdk.utils.Strings;
import top.someapp.fimesdk.view.VirtualKey;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zwz
 * Created on 2023-07-13
 */
public class RimeEngine implements ImeEngine {

    private static final String TAG = "RimeEngine";
    private final Map<String, FimeHandler> handlerMap = new HashMap<>();
    private final HandlerThread workThread;
    private final Handler handler;
    private InputMethodService ims;
    private FimeContext fimeContext;
    private ImeState state = ImeState.FREEZE;
    private int mode = CN_MODE;
    private Rime rime;
    private Schema schema;

    public RimeEngine(InputMethodService ims) {
        this.workThread = new HandlerThread(TAG);
        this.workThread.start();
        this.handler = new Handler(workThread.getLooper());
        useInputMethodService(ims);
        start();
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
                    if (schema == null || schema.getName() == null) {
                        String schemaConf = Setting.getInstance()
                                                   .getString(Setting.kActiveSchema);
                        useSchema(schemaConf);
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
        if (rime == null) rime = Rime.get(fimeContext.getContext());
        if (Rime.select_schema(conf)) {
            schema.setName(conf);
            schema.setup(this);
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

        if (mode == CN_MODE) { // 中文输入模式
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

    @Override public void manualEject() {
        InputEditor inputEditor = getInputEditor();
        Candidate candidate = inputEditor.getActiveCandidate();
        if (candidate != null) {
            if (!Strings.isNullOrEmpty(candidate.text)) commitText(candidate.text);
        }
    }

    @Override public void commitText(String text) {
        if (ims != null) {
            ims.getCurrentInputConnection()
               .commitText(text, 1);
        }
        resetInputContext();
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

    private void start() {
        Logs.i("start.");
        schema = new RimeSchema();
        Clipboard.listen(fimeContext.getContext(), fimeContext.getCacheDir());
        post(() -> {
            rime = Rime.get(fimeContext.getContext());
            resetInputContext();
        });
    }

    private void stop() {
    }

    private InputEditor getInputEditor() {
        return getSchema().getInputEditor();
    }

    private void resetInputContext() {
        try {
            Rime.clearComposition();
        }
        catch (Exception e) {
            Logs.e(e.getMessage());
        }
        InputEditor inputEditor = getInputEditor();
        inputEditor.clearInput();
        inputEditor.clearCandidates();
        inputEditor.setActiveIndex(0);
        notifyHandlers(FimeMessage.create(FimeMessage.MSG_CANDIDATE_CHANGE));
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

    private void onFnKeyTap(Keycode keycode) {
        InputEditor inputEditor = getInputEditor();
        if (keycode.code == Keycode.VK_FN_BACKSPACE) {
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

    private void doSearch() {
        final InputEditor inputEditor = getInputEditor();
        inputEditor.clearCandidates();
        inputEditor.setActiveIndex(0);
        if (inputEditor.hasInput()) {
            StringBuilder searchCode = new StringBuilder();
            for (String s : inputEditor.getSearchCodes()) {
                searchCode.append(s);
            }
            Logs.i("search `%s` start.", searchCode);
            handler.post(() -> {
                String prevInput = Rime.get_input();
                if (searchCode.length() > prevInput.length() && searchCode.indexOf(
                        prevInput) == 0) {
                    for (int i = prevInput.length(); i < searchCode.length(); i++) {
                        Rime.onKey(new int[] { searchCode.charAt(i), 0 });
                    }
                }
                else {
                    Rime.clearComposition();
                    Rime.onText(searchCode);
                }
                Rime.RimeCandidate[] candidates = Rime.getCandidatesWithoutSwitch();
                final int resultSize = candidates == null ? 0 : candidates.length;
                inputEditor.clearCandidates();
                inputEditor.setActiveIndex(0);
                for (int i = 0; i < resultSize; i++) {
                    inputEditor.appendCandidate(
                            new Candidate(searchCode.toString(), candidates[i].text));
                }
                Logs.i("search `%s` end, result.size=%d", searchCode, resultSize);
                notifyHandlers(FimeMessage.create(FimeMessage.MSG_CANDIDATE_CHANGE));
                if (resultSize > 0) {
                    getSchema().getEjector()
                               .ejectOnCandidateChange(inputEditor);
                }
            });
        }
    }
}
