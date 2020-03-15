package io.branch.search.linktest.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import io.branch.search.linktest.R;

public class BranchTextEntry extends Fragment {
    View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.branch_text_entry, container, false);
        return mView;
    }

    public void setHeader(String header) {
        TextView t = mView.findViewById(R.id.branchTextEntryHeader);
        t.setText(header);
    }

    public void setValues(String content) {
        Boolean isNull = content == null;

        ((CheckBox) mView.findViewById(R.id.branchTextEntryIsNull)).setChecked(isNull);
        ((EditText) mView.findViewById(R.id.branchTextEntryBox)).getText().clear();
        if (!isNull) {
            ((EditText) mView.findViewById(R.id.branchTextEntryBox)).getText().append(content);
        }
    }

    public String getEnteredValue() {
        String content = ((EditText) mView.findViewById(R.id.branchTextEntryBox)).getText().toString();
        Boolean isNull = ((CheckBox) mView.findViewById(R.id.branchTextEntryIsNull)).isChecked();

        if (isNull) {
            return null;
        } else {
            return content;
        }
    }
}
