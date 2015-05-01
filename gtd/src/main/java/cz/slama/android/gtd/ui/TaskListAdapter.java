package cz.slama.android.gtd.ui;

import android.view.LayoutInflater;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.model.Project;
import cz.slama.android.gtd.model.Task;
import cz.slama.android.gtd.model.util.ContextUtils;
import cz.slama.android.gtd.model.util.StateUtils;

import java.util.List;

public class TaskListAdapter extends AlternatingColorListAdapter<Task> {
    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public TaskListAdapter(final LayoutInflater inflater, final List<Task> items,
                           final boolean selectable) {
        super(R.layout.task_list_item, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public TaskListAdapter(final LayoutInflater inflater, final List<Task> items) {
        super(R.layout.task_list_item, inflater, items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.tv_overview_task_title, R.id.tv_overview_task_description, R.id.tv_overview_task_state};
    }

    @Override
    protected void update(final int position, final Task item) {
        super.update(position, item);

        setText(0, item.getTitle());
        setText(1, item.getDescription());
        setText(2, StateUtils.getStateName(StateUtils.EStateTypes.TASK, item.getState()));
    }
}
