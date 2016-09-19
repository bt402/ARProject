package greenwich.guidear;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter {
    Model[] modelItems = null;
    Context context;
    public ArrayList<ToggleButton> toggleButtonArrayList = new ArrayList<>();
    public CustomAdapter(Context context, Model[] resource) {
        super(context,R.layout.row,resource);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.modelItems = resource;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row, parent, false);
        TextView name = (TextView) convertView.findViewById(R.id.textView1);
        name.setText(modelItems[position].getName());

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OptionDialog optionDialog = new OptionDialog();
                optionDialog.showOption(context, (modelItems[position].getName()));
            }
        });


        ToggleButton tb = (ToggleButton) convertView.findViewById(R.id.toggleButton1);
        toggleButtonArrayList.add(tb);
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    modelItems[position].setValue(1);
                }
                else if (!b){
                    modelItems[position].setValue(0);
                }
            }
        });
        if(modelItems[position].getValue() == 1)
            tb.setChecked(true);
        else
            tb.setChecked(false);
        return convertView;
    }

}
