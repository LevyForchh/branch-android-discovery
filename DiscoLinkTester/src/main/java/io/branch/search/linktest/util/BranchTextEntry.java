package io.branch.search.linktest.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import io.branch.search.linktest.R;

public class BranchTextEntry extends Fragment implements CompoundButton.OnCheckedChangeListener {
    private TextView header;
    private EditText entryForm;
    private CheckBox nullCheckbox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.branch_text_entry, container, false);

        header = mView.findViewById(R.id.branchTextEntryHeader);
        entryForm = mView.findViewById(R.id.branchTextEntryBox);
        nullCheckbox = mView.findViewById(R.id.branchTextEntryIsNull);

        nullCheckbox.setOnCheckedChangeListener(this);

        return mView;
    }

    public void setHeader(String header) {
        this.header.setText(header);
    }

    public void setValues(String content) {
        Boolean isNull = content == null;

        nullCheckbox.setChecked(isNull);
        entryForm.getText().clear();
        if (!isNull) {
            entryForm.getText().append(content);
        }
    }

    public String getEnteredValue() {
        String content = entryForm.getText().toString();
        Boolean isNull = nullCheckbox.isChecked();

        if (isNull) {
            return null;
        } else {
            return content;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            entryForm.setEnabled(false);
        } else {
            entryForm.setEnabled(true);
        }
    }
}
