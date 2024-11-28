package com.project.tabletobserverjava.ui.theme;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tabletobserverjava.R;
import com.project.tabletobserverjava.data.model.EventLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para exibir os logs na RecyclerView.
 */
public class EventLogAdapter extends RecyclerView.Adapter<EventLogAdapter.ViewHolder> {

    private List<EventLog> logs;

    public EventLogAdapter(List<EventLog> logs) {
        this.logs = logs;
    }

    public void updateLogs(List<EventLog> newLogs) {
        Log.d("EventLogAdapter", "Atualizando logs. Total de logs recebidos: " + newLogs.size());
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

        // Formata o timestamp para exibir data e hora
        String formattedDate = formatTimestamp(log.getTimestamp());

        //Comentando para formatar a exibição apenas da descrição do evento
        // holder.typeTextView.setText(log.getEventType());
        holder.descriptionTextView.setText(log.getDescription());
        holder.timestampTextView.setText(formattedDate); // Exibe a data e hora formatadas
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    /**
     * Converte um timestamp (em milissegundos) para uma string de data e hora formatada.
     *
     * @param timestamp O valor do timestamp em milissegundos.
     * @return String representando a data e hora no formato "dd/MM/yyyy HH:mm:ss".
     */
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
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
