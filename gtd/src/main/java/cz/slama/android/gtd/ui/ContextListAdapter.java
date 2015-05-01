package cz.slama.android.gtd.ui;

import android.view.LayoutInflater;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.model.ContextGtd;

import java.util.List;

public class ContextListAdapter extends AlternatingColorListAdapter<ContextGtd> {
    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public ContextListAdapter(final LayoutInflater inflater, final List<ContextGtd> items,
                              final boolean selectable) {
        super(R.layout.context_list_item, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public ContextListAdapter(final LayoutInflater inflater, final List<ContextGtd> items) {
        super(R.layout.context_list_item, inflater, items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.tv_overview_context_title};
    }

    @Override
    protected void update(final int position, final ContextGtd item) {
        super.update(position, item);

        setText(0, item.getTitle());
    }
}
