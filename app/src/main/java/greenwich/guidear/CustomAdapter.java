package greenwich.guidear;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by Marcin on 9/5/2016.
 */
public class CustomAdapter extends ArrayAdapter {
    Model[] modelItems = null;
    Context context;
    public CustomAdapter(Context context, Model[] resource) {
        super(context,R.layout.row,resource);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.modelItems = resource;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row, parent, false);
        TextView name = (TextView) convertView.findViewById(R.id.textView1);
        ToggleButton tb = (ToggleButton) convertView.findViewById(R.id.toggleButton1);
        name.setText(modelItems[position].getName());
        if(modelItems[position].getValue() == 1)
            tb.setChecked(true);
        else
            tb.setChecked(false);
        return convertView;
    }
}
