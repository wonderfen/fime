package top.someapp.fimesdk.engine;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.Fime;
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
public class FimeEngine implements ImeEngine, Filter<Candidate> {

    private static final String TAG = Fime.makeTag("FimeEngine");
    private ImeState state = ImeState.FREEZE;
    private int mode = CN_MODE;
    private InputMethodService ims;
    private FimeContext fimeContext;
    private SchemaManager.SchemaInfo schemaInfo;
    private Schema schema;
    private Map<String, FimeHandler> handlerMap = new HashMap<>();
    private HandlerThread workThread;
    private Handler handler;

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
        resetInputContext();
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

    @Override public Schema getSchema() {
        return schema;
    }

    @Override public ImeEngine useSchema(String conf) {
        Log.i(TAG, "use schema by: " + conf);
        schemaInfo = SchemaManager.find(conf);
        try {
            Config config = schemaInfo.loadConfig();
            if (config == null) {
                Log.e(TAG, "invalid schema config: " + conf);
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
        }
        return this;
    }

    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        if (restarting) enterState(ImeState.READY);
    }

    @Override public void onTap(@NonNull VirtualKey virtualKey) {
        Log.d(TAG, "onTap");
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
            return;
        }

        if (mode == CN_MODE && schema.isOptionActive("cn", 0)) { // 中文输入模式
            cnModeInput(virtualKey, keycode);
        }
        else {
            asciiModeInput(virtualKey, keycode);
        }
    }

    @Override public void requestSearch() {
        doSearch();
    }

    @Override public void eject() {
        getEjector().eject(getInputEditor());
    }

    @Override public void registerHandler(@NonNull FimeHandler handler) {
        handlerMap.put(handler.getName(), handler);
    }

    @Override public void unregisterHandler(@NonNull String name) {
        handlerMap.remove(name);
    }

    @Override public void notifyHandlers(@NonNull Message message) {
        for (FimeHandler handler : handlerMap.values()) {
            handler.handleMessage(message);
        }
    }

    @Override public void post(Runnable work) {
        try {
            handler.post(work);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown");
        if (state == ImeState.FREEZE || state == ImeState.QUIT) return false;

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ims.requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS);
            state = ImeState.FREEZE;
            return true;
        }
        Keycode keycode = Keycode.convertNativeKey(keyCode, event);
        if (keycode != null) {
            onTap(new VirtualKey(keycode.code, keycode.label));
            notifyHandlers(FimeMessage.MSG_REPAINT);
            return true;
        }
        return false;
    }

    @Override public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyLongPress");
        return false;
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp");
        return state != ImeState.FREEZE && state != ImeState.QUIT;
    }

    @Override public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        Log.d(TAG, "onKeyMultiple");
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

    void notifyHandlers(int what) {
        for (FimeHandler handler : handlerMap.values()) {
            if (handler.sendEmptyMessage(what)) break;
        }
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
            getEjector().eject(inputEditor);
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
        Log.i(TAG, "start.");
        if (schema == null) {
            Log.e(TAG, "invalid schema!");
        }
        else {
            resetInputContext();
        }
    }

    private void stop() {
        Log.i(TAG, "stop.");
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
        }
        else if (keycode.code == Keycode.VK_FN_CLEAR) {
            resetInputContext();
        }
        else if (keycode.code == Keycode.VK_FN_ENTER) {
            assert inputEditor != null;
            if (inputEditor.hasInput()) {
                commitText(inputEditor.getRawInput());
                resetInputContext();
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
        InputEditor inputEditor = getInputEditor();
        Translator translator = getTranslator();
        assert inputEditor != null;
        inputEditor.clearCandidates();
        inputEditor.setActiveIndex(0);
        final List<String> searchCodes = inputEditor.getSearchCodes();
        if (searchCodes == null || searchCodes.isEmpty()) return;

        Log.i(TAG, "search(" + searchCodes + ") start.");
        handler.post(() -> {
            List<Candidate> candidates;
            assert translator != null;
            if (inputEditor.getSelected() == null) {
                candidates = translator.translate(searchCodes, 512);
            }
            else {
                candidates = translator.translate(inputEditor.getSelected().text, searchCodes, 512);
            }
            filter(candidates, getSchema());
            Log.i(TAG, "search(" + searchCodes + ") end, result.size=" + candidates.size());
            inputEditor.clearCandidates();
            inputEditor.setActiveIndex(0);
            for (Candidate candidate : candidates) {
                inputEditor.appendCandidate(candidate);
            }
            notifyHandlers(FimeMessage.MSG_CANDIDATE_CHANGE);
        });
    }
}
