package com.project.tabletobserverjava.ui.theme;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tabletobserverjava.R;
import com.project.tabletobserverjava.data.model.EventLog;

import java.util.List;

/**
 * Adapter para exibir os logs na RecyclerView.
 */
public class EventLogAdapter extends RecyclerView.Adapter<EventLogAdapter.ViewHolder> {

    private List<EventLog> logs;

    public EventLogAdapter(List<EventLog> logs) {
        this.logs = logs;
    }

    public void updateLogs(List<EventLog> newLogs) {
        this.logs = newLogs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventLog log = logs.get(position);
        holder.typeTextView.setText(log.getEventType());
        holder.descriptionTextView.setText(log.getDescription());
        holder.timestampTextView.setText(String.valueOf(log.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView;
        TextView descriptionTextView;
        TextView timestampTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.text_type);
            descriptionTextView = itemView.findViewById(R.id.text_description);
            timestampTextView = itemView.findViewById(R.id.text_timestamp);
        }
    }
}
