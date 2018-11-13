package cnpm31.nhom10.caroplay.GameBoard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import cnpm31.nhom10.caroplay.R;

public class GameGridViewAdapter extends BaseAdapter {

    public GameGridViewAdapter(Context context, int layout, List<ItemStatus> board) {
        this.context = context;
        this.layout = layout;
        this.board = board;
    }

    private Context context;
    private int layout;
    private List<ItemStatus> board;

    @Override
    public int getCount() {
        return board.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        ImageView picture;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
            holder.picture = convertView.findViewById(R.id.statusPicture);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ItemStatus status = board.get(position);
        holder.picture.setImageResource(status.getImage());

        return convertView;
    }
}
