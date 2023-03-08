package top.someapp.fime.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.PopupWindow;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import top.someapp.fime.MainActivity;
import top.someapp.fime.R;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.SchemaManager;
import top.someapp.fimesdk.Setting;
import top.someapp.fimesdk.utils.Strings;

/**
 * @author zwz
 * Created on 2023-01-14
 */
class FimePopup extends PopupWindow implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    private FimeContext fimeContext;
    private Button btnReturn;
    private Button btnTheme;
    private Button btnSchema;
    private Button btnClipboard;
    private ListView lvContentList;
    private ArrayAdapter<String> adapter;
    private int active = 0;
    private Setting setting;

    FimePopup() {
        super(FimeContext.getInstance()
                         .getContext());
        this.fimeContext = FimeContext.getInstance();
        init();
    }

    @Override public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btnSetting) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(
                    fimeContext.getContext(), MainActivity.class);
            fimeContext.getContext()
                       .startActivity(intent);
        }
        else if (id == R.id.btnReturn) {
            close();
        }
        else if (active != id) {
            active = id;
            updateAdapter();
        }
    }

    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        fimeContext.showToastDefault(adapter.getItem(position));
    }

    @Override public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item = adapter.getItem(position);
        fimeContext.showToastDefault(item);
        View rootView = fimeContext.getRootView();
        if (active == R.id.btnTheme) {
            if (rootView instanceof InputView) {
                ((InputView) rootView).applyTheme(item.split("[:/]")[0]);
            }
        }
        else if (active == R.id.btnSchema) {
            if (rootView instanceof InputView) {
                ((InputView) rootView).useSchema(item.split("[:/]")[1]);
            }
        }
        else if (active == R.id.btnClipboard) {
            if (rootView instanceof InputView) {
                ((InputView) rootView).commitText(item);
            }
        }
        close();
    }

    void show() {
        showAtLocation(fimeContext.getRootView(), Gravity.BOTTOM, 0, 0);
    }

    void close() {
        dismiss();
    }

    private void init() {
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setBackgroundDrawable(new ColorDrawable(0)); // 去掉背景
        View view = LayoutInflater.from(fimeContext.getContext())
                                  .inflate(R.layout.popup_wrapper, (ViewGroup) null);
        setContentView(view);
        setOutsideTouchable(true);

        btnTheme = view.findViewById(R.id.btnTheme);
        btnSchema = view.findViewById(R.id.btnSchema);
        btnClipboard = view.findViewById(R.id.btnClipboard);
        btnReturn = view.findViewById(R.id.btnReturn);
        lvContentList = view.findViewById(R.id.lvContentList);

        view.findViewById(R.id.btnSetting)
            .setOnClickListener(this);
        btnTheme.setOnClickListener(this);
        btnSchema.setOnClickListener(this);
        btnClipboard.setOnClickListener(this);
        btnReturn.setOnClickListener(this);
        lvContentList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        lvContentList.setOnItemClickListener(this);
        lvContentList.setOnItemSelectedListener(this);
        active = R.id.btnTheme;
        setting = Setting.getInstance();
        updateAdapter();
    }

    private void updateAdapter() {
        switch (active) {
            case R.id.btnTheme:
                adapter = new CheckMarkAdapter(fimeContext.getContext(),
                                               android.R.layout.simple_list_item_single_choice);
                String[] themeOptions = fimeContext.getResources()
                                                   .getStringArray(R.array.theme_options);
                adapter.addAll(themeOptions);
                break;
            case R.id.btnSchema:
                CheckMarkAdapter adapter = new CheckMarkAdapter(fimeContext.getContext(),
                                                                android.R.layout.simple_list_item_single_choice);
                String activeSchema = setting.getString(Setting.kActiveSchema);
                for (SchemaManager.SchemaInfo info : SchemaManager.scan()) {
                    String item = Strings.join('/', info.getName(), info.conf);
                    adapter.add(item);
                    if (activeSchema.equals(info.conf)) {
                        adapter.setCheckedItem(item);
                    }
                }
                this.adapter = adapter;
                break;
            case R.id.btnClipboard:
                this.adapter = new ArrayAdapter<>(fimeContext.getContext(),
                                                  android.R.layout.simple_list_item_1);
                for (int i = 0; i < 10; i++) {
                    this.adapter.add("clipboard-" + i);
                }
                break;
        }
        lvContentList.setAdapter(adapter);
    }

    static class CheckMarkAdapter extends ArrayAdapter<String> {

        private String checkedItem;

        CheckMarkAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }

        @NonNull @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (checkedItem != null) {
                if (checkedItem.equals(getItem(position))) {
                    CheckedTextView checkedTextView = view.findViewById(android.R.id.text1);
                    checkedTextView.setChecked(true);
                    checkedItem = null;
                }
            }
            return view;
        }

        void setCheckedItem(String checked) {
            this.checkedItem = checked;
        }
    }
}
