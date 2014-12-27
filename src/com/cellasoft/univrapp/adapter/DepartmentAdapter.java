package com.cellasoft.univrapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.model.Department;

import java.util.List;

public class DepartmentAdapter extends BaseListAdapter<Department> {

    class Holder extends ViewHolder {
        TextView name;
    }

    public DepartmentAdapter(Context context, int resource) {
        super(context, resource);
        this.items = Department.getAllUniversity();
    }

    @Override
    protected void populateDataForRow(ViewHolder viewHolder, Department department, int position) {
        if (viewHolder instanceof Holder) {
            Holder holder = (Holder) viewHolder;
            holder.thumbnail.setImageResource(department.logo_from_resource);
            holder.name.setText(department.name);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        Department department = items.get(position);

        if (convertView == null) {
            convertView = mInflater.inflate(resource, parent, false);
            holder = new Holder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.univr_logo);
            holder.name = (TextView) convertView.findViewById(R.id.univr_name);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        populateDataForRow(holder, department, position);
        convertView.setBackgroundResource(R.color.aliceBlue);

        return convertView;
    }

    public void setUniversites(List<Department> universites) {
        super.setItems(universites);
    }
}
